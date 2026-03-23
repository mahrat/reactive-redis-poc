package com.poc.reactiveredis;

import com.poc.reactiveredis.model.Product;
import com.poc.reactiveredis.repository.ProductRepository;
import com.poc.reactiveredis.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id("test-id-123")
                .name("Test Product")
                .description("A test product")
                .price(new BigDecimal("99.99"))
                .category("Test")
                .stock(10)
                .build();
    }

    @Test
    @DisplayName("Should create a product with generated ID and timestamps")
    void shouldCreateProduct() {
        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.just(sampleProduct));

        Product input = Product.builder()
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .build();

        StepVerifier.create(productService.createProduct(input))
                .expectNextMatches(p ->
                        p.getName().equals("Test Product") &&
                        p.getId() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return product by ID")
    void shouldGetProductById() {
        when(productRepository.findById("test-id-123"))
                .thenReturn(Mono.just(sampleProduct));

        StepVerifier.create(productService.getProduct("test-id-123"))
                .expectNextMatches(p -> p.getId().equals("test-id-123"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should emit error when product not found")
    void shouldErrorWhenProductNotFound() {
        when(productRepository.findById("missing-id"))
                .thenReturn(Mono.empty());

        StepVerifier.create(productService.getProduct("missing-id"))
                .expectErrorMatches(e ->
                        e instanceof RuntimeException &&
                        e.getMessage().contains("missing-id"))
                .verify();
    }

    @Test
    @DisplayName("Should return all products as Flux")
    void shouldGetAllProducts() {
        Product p2 = Product.builder().id("id-2").name("Product 2").build();

        when(productRepository.findAll())
                .thenReturn(Flux.just(sampleProduct, p2));

        StepVerifier.create(productService.getAllProducts())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProduct() {
        when(productRepository.deleteById("test-id-123"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(productService.deleteProduct("test-id-123"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should error when deleting non-existent product")
    void shouldErrorWhenDeletingNonExistentProduct() {
        when(productRepository.deleteById("ghost-id"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(productService.deleteProduct("ghost-id"))
                .expectErrorMatches(e ->
                        e instanceof RuntimeException &&
                        e.getMessage().contains("ghost-id"))
                .verify();
    }
}
