package com.ming.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ming.shortlink.project.common.convention.exception.ClientException;
import com.ming.shortlink.project.common.convention.exception.ServiceException;
import com.ming.shortlink.project.common.enums.UserAgentEnum;
import com.ming.shortlink.project.common.enums.ValidDateTypeEnum;
import com.ming.shortlink.project.config.GotoDomainWhiteListConfiguration;
import com.ming.shortlink.project.dao.entity.*;
import com.ming.shortlink.project.dao.mapper.*;
import com.ming.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.ming.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ming.shortlink.project.dto.resp.*;
import com.ming.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import com.ming.shortlink.project.service.LinkStatsTodayService;
import com.ming.shortlink.project.service.ShortLinkService;
import com.ming.shortlink.project.toolkit.ClientUtil;
import com.ming.shortlink.project.toolkit.HashUtil;
import com.ming.shortlink.project.toolkit.LinkUtil;
import com.ming.shortlink.project.toolkit.SnowUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.ming.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.ming.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * @author clownMing
 * 短链接接口实现层
 */
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(ShortLinkServiceImpl.class);

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    private final ShortLinkMapper shortLinkMapper;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkAccessLogMapper linkAccessLogMapper;

    private final LinkDeviceStatsMapper linkDeviceStatsMapper;

    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final LinkStatsTodayMapper linkStatsTodayMapper;

    private final LinkStatsTodayService linkStatsTodayService;

    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;

    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAMapKey;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Transactional
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = createShortLinkDefaultDomain + "/" + shortLinkSuffix;
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setId(SnowUtil.getSnowflakeNextId());
        shortLinkDO.setDomain(createShortLinkDefaultDomain);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setFullShortUrl(fullShortUrl);
        shortLinkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));
        shortLinkDO.setTotalPv(0);
        shortLinkDO.setTotalUv(0);
        shortLinkDO.setTotalUip(0);
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .gid(requestParam.getGid())
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException e) {
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLink = baseMapper.selectOne(queryWrapper);
            if (hasShortLink != null) {
                LOG.warn("短链接：{} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        // 缓存预热
        stringRedisTemplate.opsForValue().set(String.format(
                        GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        int size = originUrls.size();
        if (size < 1) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable e) {
                LOG.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });

    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        return shortLinkMapper.listGroupShortLinkCount(requestParam);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO dbShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (dbShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        if (Objects.equals(dbShortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(dbShortLinkDO.getDomain())
                    .shortUri(dbShortLinkDO.getShortUri())
                    .favicon(dbShortLinkDO.getFavicon())
                    .createdType(dbShortLinkDO.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            if(!rLock.tryLock()) {
                throw new ServiceException("短链接正在被访问，请稍后再试");
            }
            try {
                LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, dbShortLinkDO.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 0);
                ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(dbShortLinkDO.getDomain())
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .shortUri(dbShortLinkDO.getShortUri())
                        .favicon(dbShortLinkDO.getFavicon())
                        .createdType(dbShortLinkDO.getCreatedType())
                        .originUrl(requestParam.getOriginUrl())
                        .describe(requestParam.getDescribe())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .enableStatus(dbShortLinkDO.getEnableStatus())
                        .totalPv(dbShortLinkDO.getTotalPv())
                        .totalUv(dbShortLinkDO.getTotalUv())
                        .totalUip(dbShortLinkDO.getTotalUip())
                        .fullShortUrl(dbShortLinkDO.getFullShortUrl())
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);
                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, dbShortLinkDO.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, dbShortLinkDO.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);
                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, dbShortLinkDO.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, dbShortLinkDO.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, dbShortLinkDO.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, dbShortLinkDO.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, dbShortLinkDO.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, dbShortLinkDO.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogDO.class)
                        .eq(LinkAccessLogDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogDO::getGid, dbShortLinkDO.getGid())
                        .eq(LinkAccessLogDO::getDelFlag, 0);
                LinkAccessLogDO linkAccessLogsDO = LinkAccessLogDO.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessLogMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            }finally {
                rLock.unlock();
            }
        }
        if(!Objects.equals(dbShortLinkDO.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(dbShortLinkDO.getValidDate(), requestParam.getValidDate())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            Date date = new Date();
            if(dbShortLinkDO.getValidDate() != null && dbShortLinkDO.getValidDate().before(date)) {
                if(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType())
                        || requestParam.getValidDate().after(date)) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));

                }
            }
        }
    }

    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each).orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            try {
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecord);
                response.sendRedirect(originalLink);
                return;
            } catch (IOException e) {
                throw new ServiceException("重定向错误");
            }
        }
        // 缓存穿透 --  如果缓存中没有，查询布隆过滤器 如果布隆过滤器有，则再进行校验，防止误判
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            try {
                response.sendRedirect("/page/notfound");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        // 缓存穿透 -- 空对象判断 如果有，直接返回，否则查询数据库
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            try {
                response.sendRedirect("/page/notfound");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        // 通过分布式锁，查询数据库
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // dcl 双重检查锁
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                try {
                    ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                    shortLinkStats(fullShortUrl, null, statsRecord);
                    response.sendRedirect(originalLink);
                    return;
                } catch (IOException e) {
                    throw new ServiceException("重定向错误");
                }
            }
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoDOLambdaQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO linkGotoDO = shortLinkGotoMapper.selectOne(linkGotoDOLambdaQueryWrapper);
            if (linkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                try {
                    response.sendRedirect("/page/notfound");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, linkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))) {
                // 说明已经过期了
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                try {
                    response.sendRedirect("/page/notfound");
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            stringRedisTemplate.opsForValue().set(String.format(
                            GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()),
                    TimeUnit.MILLISECONDS);
            shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
            try {
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, shortLinkDO.getGid(), statsRecord);
                response.sendRedirect(shortLinkDO.getOriginUrl());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        if(!rLock.tryLock()) {
            delayShortLinkStatsProducer.send(statsRecord);
            return ;
        }
        try {
            if(StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            Date date = new Date();
            int weekValue = DateUtil.dayOfWeekEnum(date).getIso8601Value();
            int hour = DateUtil.hour(date, true);
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .id(SnowUtil.getSnowflakeNextId())
                    .pv(1)
                    .uv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .uip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            HashMap<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocaleAMapKey);
            String ip = statsRecord.getRemoteAddr();
            localeParamMap.put("ip", ip);
            String mapResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject jsonObject = JSON.parseObject(mapResultStr);
            String actualProvince;
            String actualCity;
            // 状态码
            String infocode = jsonObject.getString("infocode");
            LinkLocaleStatsDO linkLocaleStatsDO;
            if (StrUtil.isNotBlank(infocode) && Objects.equals(infocode, "10000")) {
                String province = jsonObject.getString("province");
                boolean unknownFlag = StrUtil.isBlank(province);
                linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .city(actualCity = unknownFlag ? "未知" : jsonObject.getString("city"))
                        .adcode(unknownFlag ? "未知" : jsonObject.getString("adcode"))
                        .country("中国")
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkLocaleStatsMapper.shortLinkLocalStats(linkLocaleStatsDO);
                String os = statsRecord.getOs();
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .os(os)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);
                String browser = statsRecord.getBrowser();
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .browser(browser)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);
                String device = statsRecord.getDevice();
                LinkAccessLogDO linkAccessLogDO = LinkAccessLogDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .network(ClientUtil.getNetworkInterfaces())
                        .device(device)
                        .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                        .user(statsRecord.getUv())
                        .browser(browser)
                        .os(os)
                        .ip(ip)
                        .build();
                linkAccessLogMapper.insert(linkAccessLogDO);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .device(device)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceStats(linkDeviceStatsDO);

                String network = statsRecord.getNetwork();
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .network(network)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkNetworkStatsMapper.shortLinkNetwork(linkNetworkStatsDO);
                baseMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);
                AtomicBoolean uvTodayFirstFlag = new AtomicBoolean();
                Long uvTodayAdd = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + DateUtil.formatDate(date) + ":" + fullShortUrl, statsRecord.getUv());
                uvTodayFirstFlag.set(uvTodayAdd != null && uvTodayAdd > 0L);
                Long uipTodayAdd = stringRedisTemplate.opsForSet().add("short-link:stats:ip:" + DateUtil.formatDate(date) + ":" + fullShortUrl, ip);
                boolean uipTodayFirstFlag = uipTodayAdd != null && uipTodayAdd > 0L;
                LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(date)
                        .todayPv(1)
                        .todayUv(uvTodayFirstFlag.get() ? 1 : 0)
                        .todayUip(uipTodayFirstFlag ? 1 : 0)
                        .build();
                linkStatsTodayMapper.shortLinkTodayStats(linkStatsTodayDO);
            }
        }catch (Throwable ex) {
            LOG.error("短链接访问信息统计错误：{}", ex.getMessage());
        }
    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, HttpServletRequest request, HttpServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = request.getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 15);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            response.addCookie(uvCookie);
            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
            uvFirstFlag.set(Boolean.TRUE);
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals("uv", each.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdd = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                        uvFirstFlag.set(uvAdd != null && uvAdd > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = ClientUtil.getIpAddress(request);
        String os = ClientUtil.getClientOS(request);
        String browser = ClientUtil.getBrowser(request);
        String device = ClientUtil.getDeviceType(request);
        String network = ClientUtil.getNetworkInterfaces();
        Long uipAdd = stringRedisTemplate.opsForSet().add("short-link:stats:ip:" + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdd != null && uipAdd > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();

    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl();
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortUriCreateCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    private static String getFavicon(String url) {
        Document doc;
        try {
            doc = Jsoup.connect(url).
                    userAgent(UserAgentEnum.EDGE_USER_AGENT.getAgent())
                    .timeout(5 * 1000)
                    .get();
        } catch (IOException e) {
            throw new ServiceException("获取网站图标出错");
        }
        Element link = doc.select("link[href~=.*\\.(ico|png)]").first();
        try {
            assert link != null;
        } catch (Exception e) {
            throw new ServiceException("获取网站图标出错");
        }
        return link.absUrl("href").isEmpty() ? null : link.absUrl("href");
    }

    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if(enable == null || !enable) {
            return ;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if(StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if(!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成一下网站跳转链接: " + gotoDomainWhiteListConfiguration.getNames());
        }
    }
}


