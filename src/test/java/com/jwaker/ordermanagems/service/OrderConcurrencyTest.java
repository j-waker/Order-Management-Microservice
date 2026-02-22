package com.jwaker.ordermanagems.service;

import com.jwaker.ordermanagems.dto.OrderItemRequest;
import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.repository.OrderRepository;
import com.jwaker.ordermanagems.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
public class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private List<Long> productIds;

    private Long product1Id;
    private Long product2Id;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();

        Product p1 = productRepository.save(new Product("Item 1", new BigDecimal("10.00")));
        Product p2 = productRepository.save(new Product("Item 2", new BigDecimal("20.00")));

        this.product1Id = p1.getId();
        this.product2Id = p2.getId();
    }

    @TestConfiguration
    static class CacheTestConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("orders_list", "order_details", "products");
        }
    }

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testConcurrentOrderCreationWithDto() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // Define the items once to be used by all threads
        List<OrderItemRequest> itemRequests = List.of(
                new OrderItemRequest(product1Id, 1),
                new OrderItemRequest(product2Id, 1)
        );

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // Keep your random jitter for accuracy
                    Thread.sleep(ThreadLocalRandom.current().nextLong(0, 500));

                    orderService.createOrder(itemRequests);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Thread failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(threadCount, successCount.get(), "All concurrent orders should be processed");
        assertEquals(threadCount, orderRepository.count(), "Database should contain all processed orders");
    }

    @Test
    void testConcurrentProductUpdate() throws InterruptedException {
        Product p = productRepository.findById(product1Id).orElseThrow();
        p.setPrice(BigDecimal.ZERO);
        productRepository.saveAndFlush(p);
        cacheManager.getCache("products").clear();

        int threadCount = 100;
        BigDecimal adjustment = new BigDecimal("10.00");
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextLong(0, 1001));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    productService.adjustProductPrice(product1Id, adjustment);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);

        BigDecimal finalPrice = productRepository.findById(product1Id).get().getPrice();
        assertEquals(new BigDecimal("1000.00"), finalPrice);
    }
}