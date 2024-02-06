package com.ming.shortlink.project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @author clownMing
 * 短链接统计消息队列配置类
 */
@Configuration
@RequiredArgsConstructor
public class ShortLinkStatsMQConfig{

    /**
     * exchange name
     */
    public static final String MQ_SHORT_LINK_STATS_EXCHANGE = "short-link_stats-exchange";

    /**
     * delay exchange name
     */
    public static final String MQ_SHORT_LINK_STATS_EXCHANGE_DELAY = "short-link_stats-delay-exchange";

    /**
     * dead exchange name
     */
    public static final String MQ_SHORT_LINK_STATS_EXCHANGE_DEAD = "short-link_stats-dead-exchange";


    /**
     * queue name
     */
    public static final String MQ_SHORT_LINK_STATS_QUEUE= "short-link_stats-queue";

    /**
     * delay queue name
     */
    public static final String MQ_SHORT_LINK_STATS_QUEUE_DELAY= "short-link_stats-delay-queue";

    /**
     * dead queue name
     */
    public static final String MQ_SHORT_LINK_STATS_QUEUE_DEAD= "short-link_stats-dead-queue";


    /**
     * routingKey
     */
    public static final String MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY = "short-link_stats_key";

    /**
     * delay routing key
     */
    public static final String MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY_DELAY = "short-link_stats_delay_key";

    /**
     * dead routing key
     */
    public static final String MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY_DEAD = "short-link_stats_dead_key";

    /**
     *  exchange
     */
    @Bean("statsExchange")
    public DirectExchange statsExchange() {
        return ExchangeBuilder.directExchange(MQ_SHORT_LINK_STATS_EXCHANGE).durable(true).build();
    }

    /**
     * delay exchange
     */
    @Bean("delayStatsExchange")
    public DirectExchange delayStatsExchange() {
        return ExchangeBuilder.directExchange(MQ_SHORT_LINK_STATS_EXCHANGE_DELAY).durable(true).build();
    }

    /**
     * dead exchange
     */
    @Bean("deadStatsExchange")
    public DirectExchange deadStatsExchange() {
        return ExchangeBuilder.directExchange(MQ_SHORT_LINK_STATS_EXCHANGE_DEAD).durable(true).build();
    }

    /**
     * queue
     */
    @Bean("statsQueue")
    public Queue statsQueue() {
        return QueueBuilder.durable(MQ_SHORT_LINK_STATS_QUEUE).build();
    }

    /**
     * delay queue
     */
    @Bean("delayStatsQueue")
    public Queue delayStatsQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        // 设置死信交换机
        arguments.put("x-dead-letter-exchange", MQ_SHORT_LINK_STATS_EXCHANGE_DEAD);
        // 设置死信 routing key
        arguments.put("x-dead-letter-routing-key", MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY_DEAD);
        return QueueBuilder.durable(MQ_SHORT_LINK_STATS_QUEUE_DELAY).withArguments(arguments).build();
    }

    /**
     * dead queue
     */
    @Bean("deadStatsQueue")
    public Queue deadStatsQueue() {
        return QueueBuilder.durable(MQ_SHORT_LINK_STATS_QUEUE_DEAD).build();
    }

    /**
     * queue binding exchange
     */
    @Bean
    public Binding queueBindingExchange(@Qualifier("statsExchange") DirectExchange statsExchange,
                                        @Qualifier("statsQueue") Queue statsQueue) {
        return BindingBuilder.bind(statsQueue).to(statsExchange).with(MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY);
    }

    /**
     * delay queue binding exchange
     */
    @Bean
    public Binding delayQueueBindingExchange(@Qualifier("delayStatsExchange") DirectExchange delayStatsExchange,
                                             @Qualifier("delayStatsQueue") Queue delayStatsQueue) {
        return BindingBuilder.bind(delayStatsQueue).to(delayStatsExchange).with(MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY_DELAY);
    }

    /**
     * dead queue binding exchange
     */
    @Bean
    public Binding deadQueueBindingExchange(@Qualifier("deadStatsExchange") DirectExchange deadStatsExchange,
                                             @Qualifier("deadStatsQueue") Queue deadStatsQueue) {
        return BindingBuilder.bind(deadStatsQueue).to(deadStatsExchange).with(MQ_SHORT_LINK_STATS_EXCHANGE_QUEUE_KEY_DEAD);
    }
}
