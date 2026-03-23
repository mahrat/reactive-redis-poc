package com.poc.reactiveredis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;

/**
 * Configures a reactive Pub/Sub message listener.
 * Subscribes to the "product-events" channel and logs incoming messages.
 */
@Slf4j
@Configuration
public class RedisSubscriberConfig {

    private static final String PRODUCT_CHANNEL = "product-events";

    @Bean
    public ReactiveRedisMessageListenerContainer messageListenerContainer(
            ReactiveRedisConnectionFactory factory) {

        ReactiveRedisMessageListenerContainer container =
                new ReactiveRedisMessageListenerContainer(factory);

        // Subscribe reactively — messages are processed on the Lettuce event loop
        container.receive(ChannelTopic.of(PRODUCT_CHANNEL))
                .subscribe(
                        msg -> log.info("[PubSub] Received on [{}]: {}",
                                msg.getChannel(), msg.getMessage()),
                        err -> log.error("[PubSub] Listener error: {}", err.getMessage())
                );

        return container;
    }
}
