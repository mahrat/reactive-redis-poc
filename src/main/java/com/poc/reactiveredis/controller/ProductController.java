package com.poc.reactiveredis.controller;

import com.poc.reactiveredis.model.Product;
import com.poc.reactiveredis.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> create(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @GetMapping("/{id}")
    public Mono<Product> getById(@PathVariable String id) {
        return productService.getProduct(id);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Product> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> streamAll() {
        return productService.getAllProducts()
                .delayElements(Duration.ofMillis(200)); // simulate streaming delay
    }

    @PutMapping("/{id}")
    public Mono<Product> update(@PathVariable String id,
                                @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return productService.deleteProduct(id);
    }

    @PutMapping("/{id}/ttl")
    public Mono<Map<String, Object>> setTtl(@PathVariable String id,
                                            @RequestParam(defaultValue = "3600") long seconds) {
        Duration ttl = Duration.ofSeconds(seconds);
        return productService.setProductTtl(id, ttl)
                .map(result -> Map.of(
                        "productId", id,
                        "ttlSet", result,
                        "ttlSeconds", seconds
                ));
    }

    @GetMapping("/{id}/ttl")
    public Mono<Map<String, Object>> getTtl(@PathVariable String id) {
        return productService.getProductTtl(id)
                .map(duration -> Map.of(
                        "productId", id,
                        "remainingTtlSeconds", duration.getSeconds()
                ));
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<Product> bulkCreate(@RequestBody Flux<Product> products) {
        return productService.saveAll(products);
    }
}
