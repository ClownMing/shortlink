package com.ming.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.ming.shortlink.project.dao.entity.*;
import com.ming.shortlink.project.dao.mapper.*;
import com.ming.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkPageRespDTO;
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

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAMapKey;

    @Transactional
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + shortLinkSuffix;
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setFullShortUrl(fullShortUrl);
        shortLinkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));
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
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        ShortLinkPageReqDTO resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(item -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(item, ShortLinkPageRespDTO.class);
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
    public Void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
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
            LambdaQueryWrapper<ShortLinkDO> deleteWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.delete(deleteWrapper);
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
            baseMapper.insert(shortLinkDO);
        }
        return null;
    }

    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            try {
                shortLinkStats(fullShortUrl, null, request, response);
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
                    shortLinkStats(fullShortUrl, null, request, response);
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
                shortLinkStats(shortLinkDO.getFullShortUrl(), shortLinkDO.getGid(), request, response);
                response.sendRedirect(shortLinkDO.getOriginUrl());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    private void shortLinkStats(String fullShortUrl, String gid, HttpServletRequest request, HttpServletResponse response) {
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
        try {
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
            String ipAddress = ClientUtil.getIpAddress(request);
            Long uipAdd = stringRedisTemplate.opsForSet().add("short-link:stats:ip:" + fullShortUrl, ipAddress);
            boolean uipFirstFlag = uipAdd != null && uipAdd > 0L;
            if (StrUtil.isEmpty(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO linkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = linkGotoDO.getGid();
            }
            Date date = new Date();
            int weekValue = DateUtil.dayOfWeekEnum(date).getIso8601Value();
            int hour = DateUtil.hour(date, true);
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .id(SnowUtil.getSnowflakeNextId())
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(date)
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            HashMap<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocaleAMapKey);
            localeParamMap.put("ip", ipAddress);
            String mapResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject jsonObject = JSON.parseObject(mapResultStr);
            // 状态码
            String infocode = jsonObject.getString("infocode");
            LinkLocaleStatsDO linkLocaleStatsDO;
            if (StrUtil.isNotBlank(infocode) && Objects.equals(infocode, "10000")) {
                String province = jsonObject.getString("province");
                boolean unknownFlag = StrUtil.isBlank(province);
                linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .province(unknownFlag ? "未知" : province)
                        .city(unknownFlag ? "未知" : jsonObject.getString("city"))
                        .adcode(unknownFlag ? "未知" : jsonObject.getString("adcode"))
                        .country("中国")
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkLocaleStatsMapper.shortLinkLocalStats(linkLocaleStatsDO);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .os(ClientUtil.getClientOS(request))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .browser(ClientUtil.getBrowser(request))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);
                LinkAccessLogDO linkAccessLogDO = LinkAccessLogDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .user(uv.get())
                        .browser(ClientUtil.getBrowser(request))
                        .os(ClientUtil.getClientOS(request))
                        .ip(ipAddress)
                        .build();
                linkAccessLogMapper.insert(linkAccessLogDO);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .id(SnowUtil.getSnowflakeNextId())
                        .device(ClientUtil.getDeviceType(request))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(date)
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceStats(linkDeviceStatsDO);
            }


        } catch (Exception ex) {
            LOG.error("短链接访问信息统计错误：{}", ex.getMessage());
        }
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
            if (!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
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
}


