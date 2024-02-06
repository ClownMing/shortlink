package com.ming.shortlink.project.mq.producer;

import com.alibaba.fastjson.JSON;
import com.ming.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.ming.shortlink.project.config.ShortLinkStatsMQConfig.MQ_SHORT_LINK_STATS_EXCHANGE_DELAY;
import static com.ming.shortlink.project.config.ShortLinkStatsMQConfig.MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY_DELAY;

/**
 * @author clownMing
 * 延迟消费短链接统计发送者
 */
@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsMQProducer {

    private final RabbitTemplate rabbitTemplate;

    private final static String DELAY_TIME = "5000";

    /**
     * 发送延迟消费短链接统计
     */
    public void send(ShortLinkStatsRecordDTO statsRecord) {
        String jsonString = JSON.toJSONString(statsRecord);
        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setMessageId(java.util.UUID.randomUUID().toString());
            message.getMessageProperties().setExpiration(DELAY_TIME);
            return message;
        };
        rabbitTemplate.convertAndSend(
                MQ_SHORT_LINK_STATS_EXCHANGE_DELAY,
                MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY_DELAY,
                jsonString,
                messagePostProcessor);
    }
}
