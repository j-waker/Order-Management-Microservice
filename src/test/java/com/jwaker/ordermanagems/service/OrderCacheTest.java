package com.jwaker.ordermanagems.service;

import com.jwaker.ordermanagems.model.Order;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class OrderCacheTest {

    @TestConfiguration
    static class CacheTestConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("orders_list", "order_details", "products");
        }
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private OrderService orderService;

    @Test
    void testOrderCaching() {
        UUID orderId = UUID.randomUUID();

        //force cache an order
        cacheManager.getCache("order_details").put(orderId, new Order());

        //check order without touching DB
        Order cachedOrder = orderService.getOrderById(orderId);
        assertNotNull(cachedOrder);

        //check cache
        assertNotNull(cacheManager.getCache("order_details").get(orderId));
    }
}