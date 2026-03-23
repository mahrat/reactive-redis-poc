package com.poc.reactiveredis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Demonstrates Reactive Redis Pub/Sub publishing.
 *
 * Use RedisMessageListenerContainer or ReactiveRedisMessageListenerContainer
 * on the subscriber side in a real application.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisherService {

    private static final String PRODUCT_CHANNEL = "product-events";

    private final ReactiveStringRedisTemplate stringRedisTemplate;

    public Mono<Long> publishProductEvent(String event, String productId) {
        String message = String.format("{\"event\":\"%s\",\"productId\":\"%s\"}", event, productId);
        log.debug("Publishing to channel [{}]: {}", PRODUCT_CHANNEL, message);

        return stringRedisTemplate.convertAndSend(PRODUCT_CHANNEL, message)
                .doOnSuccess(count ->
                        log.info("Published '{}' event for product {} to {} subscriber(s)",
                                event, productId, count));
    }
}
