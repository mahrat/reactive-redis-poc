package com.poc.reactiveredis.config;

import com.poc.reactiveredis.model.Product;
import com.poc.reactiveredis.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

/**
 * Seeds Redis with sample data when the application starts.
 * Useful for quick POC demos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ProductService productService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Seeding Redis with sample products...");

        Flux.just(
                Product.builder()
                        .name("MacBook Pro M3")
                        .description("Apple Silicon laptop, 16GB RAM")
                        .price(new BigDecimal("2499.99"))
                        .category("Electronics")
                        .stock(50)
                        .build(),
                Product.builder()
                        .name("Sony WH-1000XM5")
                        .description("Noise cancelling wireless headphones")
                        .price(new BigDecimal("349.99"))
                        .category("Audio")
                        .stock(200)
                        .build(),
                Product.builder()
                        .name("Standing Desk Pro")
                        .description("Electric height-adjustable desk, oak finish")
                        .price(new BigDecimal("799.00"))
                        .category("Furniture")
                        .stock(30)
                        .build()
        )
        .flatMap(productService::createProduct)
        .subscribe(
                p -> log.info("Seeded product: {} [{}]", p.getName(), p.getId()),
                e -> log.error("Seed error: {}", e.getMessage()),
                () -> log.info("Data seeding complete.")
        );
    }
}
