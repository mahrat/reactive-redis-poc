package com.poc.reactiveredis.service;

import com.poc.reactiveredis.model.Product;
import com.poc.reactiveredis.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Mono<Product> createProduct(Product product) {
        return Mono.just(product)
                .map(p -> {
                    p.setId(UUID.randomUUID().toString());
                    p.setCreatedAt(LocalDateTime.now());
                    p.setUpdatedAt(LocalDateTime.now());
                    return p;
                })
                .flatMap(productRepository::save)
                .doOnSuccess(p -> log.info("Created product: {}", p.getId()));
    }

    public Mono<Product> getProduct(String id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Product not found with id: " + id)));
    }

    public Flux<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Mono<Product> updateProduct(String id, Product updatedProduct) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Product not found with id: " + id)))
                .map(existing -> {
                    updatedProduct.setId(existing.getId());
                    updatedProduct.setCreatedAt(existing.getCreatedAt());
                    updatedProduct.setUpdatedAt(LocalDateTime.now());
                    return updatedProduct;
                })
                .flatMap(productRepository::save)
                .doOnSuccess(p -> log.info("Updated product: {}", p.getId()));
    }

    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id)
                .flatMap(deleted -> {
                    if (!deleted) {
                        return Mono.error(
                                new RuntimeException("Product not found with id: " + id));
                    }
                    log.info("Deleted product: {}", id);
                    return Mono.empty();
                });
    }

    public Mono<Boolean> setProductTtl(String id, Duration ttl) {
        return productRepository.setTtl(id, ttl);
    }

    public Mono<Duration> getProductTtl(String id) {
        return productRepository.getTtl(id);
    }

    /**
     * Save multiple products reactively using merge for concurrent execution.
     */
    public Flux<Product> saveAll(Flux<Product> products) {
        return products
                .flatMap(this::createProduct)
                .doOnComplete(() -> log.info("Bulk save completed"));
    }
}
