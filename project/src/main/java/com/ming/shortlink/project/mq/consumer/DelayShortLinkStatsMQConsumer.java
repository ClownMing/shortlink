package com.ming.shortlink.project.mq.consumer;

import cn.hutool.json.JSONUtil;
import com.ming.shortlink.project.common.convention.exception.ServiceException;
import com.ming.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.ming.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.ming.shortlink.project.service.ShortLinkService;
import com.rabbitmq.client.Channel;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.ming.shortlink.project.config.ShortLinkStatsMQConfig.MQ_SHORT_LINK_STATS_QUEUE_DELAY;

/**
 * 延迟消费短链接统计消费者
 */

@Component
@RequiredArgsConstructor
public class DelayShortLinkStatsMQConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(DelayShortLinkStatsMQConsumer.class);

    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;


    @SneakyThrows
    @RabbitListener(queues = MQ_SHORT_LINK_STATS_QUEUE_DELAY, ackMode = "MANUAL")
    public void receive(Message message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String content = new String(message.getBody());
        if (StringUtil.isBlank(content)) {
            // 手动进行nack
            try {
                channel.basicNack(deliveryTag, false, false);
                throw new ServiceException("消息为空");
            } catch (IOException e) {
                LOG.error("消息消费者进行nack失败");
            }
        }
        String messageId = message.getMessageProperties().getMessageId();
        if (!messageQueueIdempotentHandler.isMessageProcessed(messageId)) {
            // 判断当前的这个消息流程是否执行完成
            if (messageQueueIdempotentHandler.isAccomplish(messageId)) {
                try {
                    channel.basicAck(deliveryTag, false);
                } catch (IOException e) {
                    LOG.error("消息消费者进行nack失败");
                }
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列重试");
        }
        ShortLinkStatsRecordDTO statsRecord = JSONUtil.toBean(content, ShortLinkStatsRecordDTO.class);

        shortLinkService.shortLinkStats(null, null, statsRecord);
        messageQueueIdempotentHandler.setAccomplish(statsRecord.getKeys());
        channel.basicAck(deliveryTag, false);

    }
}
