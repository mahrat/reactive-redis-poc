package com.poc.reactiveredis.repository;

import com.poc.reactiveredis.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Reactive Redis repository for Product CRUD operations.
 *
 * Key patterns:
 *  - Single product : "product:{id}"
 *  - All product IDs: "product:ids" (Redis Set)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private static final String KEY_PREFIX = "product:";
    private static final String IDS_SET_KEY = "product:ids";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    private final ReactiveRedisTemplate<String, Product> redisTemplate;

    // ─── Save ────────────────────────────────────────────────────────────────

    public Mono<Product> save(Product product) {
        String key = buildKey(product.getId());
        log.debug("Saving product with key: {}", key);

        return redisTemplate.opsForValue()
                .set(key, product, DEFAULT_TTL)
                .then(redisTemplate.opsForSet().add(IDS_SET_KEY, product))
                .thenReturn(product)
                .doOnSuccess(p -> log.debug("Saved product: {}", p.getId()))
                .doOnError(e -> log.error("Failed to save product: {}", product.getId(), e));
    }

    // ─── Find by ID ──────────────────────────────────────────────────────────

    public Mono<Product> findById(String id) {
        String key = buildKey(id);
        log.debug("Fetching product with key: {}", key);

        return redisTemplate.opsForValue()
                .get(key)
                .doOnNext(p -> log.debug("Found product: {}", p.getId()))
                .doOnEmpty(() -> log.debug("Product not found for id: {}", id));
    }

    // ─── Find All ────────────────────────────────────────────────────────────

    /**
     * Retrieves all products stored in the IDs set.
     * Uses flatMap for concurrent Redis lookups.
     */
    public Flux<Product> findAll() {
        log.debug("Fetching all products");

        return redisTemplate.opsForSet()
                .members(IDS_SET_KEY)
                .flatMap(product -> findById(product.getId()))
                .doOnComplete(() -> log.debug("Completed fetching all products"));
    }

    // ─── Delete by ID ────────────────────────────────────────────────────────

    public Mono<Boolean> deleteById(String id) {
        String key = buildKey(id);
        log.debug("Deleting product with key: {}", key);

        return findById(id)
                .flatMap(product ->
                        redisTemplate.opsForValue().delete(key)
                                .then(redisTemplate.opsForSet().remove(IDS_SET_KEY, product))
                                .map(count -> count > 0)
                )
                .defaultIfEmpty(false)
                .doOnSuccess(deleted -> log.debug("Delete result for {}: {}", id, deleted));
    }

    // ─── Exists ──────────────────────────────────────────────────────────────

    public Mono<Boolean> existsById(String id) {
        return redisTemplate.hasKey(buildKey(id));
    }

    // ─── TTL Management ──────────────────────────────────────────────────────

    public Mono<Boolean> setTtl(String id, Duration ttl) {
        return redisTemplate.expire(buildKey(id), ttl);
    }

    public Mono<Duration> getTtl(String id) {
        return redisTemplate.getExpire(buildKey(id));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String buildKey(String id) {
        return KEY_PREFIX + id;
    }
}
