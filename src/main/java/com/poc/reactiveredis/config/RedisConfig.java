package com.poc.reactiveredis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.poc.reactiveredis.model.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * ObjectMapper configured with JavaTimeModule for proper
     * Java 8 date/time serialization support.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Reactive Redis Template for Product entities.
     * Uses JSON serialization for values and String for keys.
     */
    @Bean
    public ReactiveRedisTemplate<String, Product> reactiveProductRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            ObjectMapper objectMapper) {

        Jackson2JsonRedisSerializer<Product> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Product.class);

        RedisSerializationContext<String, Product> context =
                RedisSerializationContext.<String, Product>newSerializationContext(
                        new StringRedisSerializer())
                        .value(serializer)
                        .hashKey(new StringRedisSerializer())
                        .hashValue(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    /**
     * Generic Reactive String Redis Template for simple key-value ops.
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory);
    }

    /**
     * ReactiveValueOperations bean for Product - convenient for direct injection.
     */
    @Bean
    public ReactiveValueOperations<String, Product> productValueOps(
            ReactiveRedisTemplate<String, Product> template) {
        return template.opsForValue();
    }

    /**
     * ReactiveHashOperations bean for Product - useful for partial updates.
     */
    @Bean
    public ReactiveHashOperations<String, String, Product> productHashOps(
            ReactiveRedisTemplate<String, Product> template) {
        return template.opsForHash();
    }
}
