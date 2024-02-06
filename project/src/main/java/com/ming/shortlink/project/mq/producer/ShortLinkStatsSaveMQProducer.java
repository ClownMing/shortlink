package com.ming.shortlink.project.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.ming.shortlink.project.mq.dto.MQDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.ming.shortlink.project.config.ShortLinkStatsMQConfig.MQ_SHORT_LINK_STATS_EXCHANGE;
import static com.ming.shortlink.project.config.ShortLinkStatsMQConfig.MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY;

/**
 * @author clownMing
 * 短链接监控状态保存消息队列生产者
 */
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveMQProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ShortLinkStatsSaveMQProducer.class);

    private final RabbitTemplate rabbitTemplate;
    /**
     * 发送延迟消费短链接统计
     */
    public void send(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecordDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        String mapperResult = null;
        try {
            mapperResult = objectMapper.writeValueAsString(MQDTO.builder()
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .shortLinkStatsRecordDTO(statsRecordDTO)
                    .build());
        } catch (JsonProcessingException e) {
            LOG.error("出错了");
        }
        rabbitTemplate.convertAndSend(
                MQ_SHORT_LINK_STATS_EXCHANGE,
                MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY,
                mapperResult,
                message -> {
                    message.getMessageProperties().setMessageId(java.util.UUID.randomUUID().toString());
                    return message;
                });
    }
}
