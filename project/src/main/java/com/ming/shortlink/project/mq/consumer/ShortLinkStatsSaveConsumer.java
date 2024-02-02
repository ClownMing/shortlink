package com.ming.shortlink.project.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ming.shortlink.project.dao.entity.*;
import com.ming.shortlink.project.dao.mapper.*;
import com.ming.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.ming.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import com.ming.shortlink.project.toolkit.ClientUtil;
import com.ming.shortlink.project.toolkit.SnowUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ming.shortlink.project.common.constant.RedisKeyConstant.LOCK_GID_UPDATE_KEY;
import static com.ming.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * @author clownMing
 * 短链接监控状态保存消息队列消费者
 */
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger LOG = LoggerFactory.getLogger(ShortLinkStatsSaveConsumer.class);

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

    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAMapKey;


    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String stream = message.getStream();
        RecordId id = message.getId();
        Map<String, String> producerMap = message.getValue();
        String fullShortUrl = producerMap.get("fullShortUrl");
        if(StrUtil.isNotBlank(fullShortUrl)) {
            String gid = producerMap.get("gid");
            ShortLinkStatsRecordDTO statsRecord = JSON.parseObject(producerMap.get("statsRecord"), ShortLinkStatsRecordDTO.class);
            actualSaveShortLinkStats(fullShortUrl, gid, statsRecord);
        }
        stringRedisTemplate.opsForStream().delete(Objects.requireNonNull(stream), id.getValue());
    }

    public void actualSaveShortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
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
                shortLinkMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);
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
}
