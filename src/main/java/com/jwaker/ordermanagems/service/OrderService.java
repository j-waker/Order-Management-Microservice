package com.jwaker.ordermanagems.service;

import com.jwaker.ordermanagems.exception.ResourceNotFoundException;
import com.jwaker.ordermanagems.model.Order;
import com.jwaker.ordermanagems.model.OrderItem;
import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.repository.OrderRepository;
import com.jwaker.ordermanagems.repository.ProductRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    @CacheEvict(value = "orders_list", allEntries = true)
    public Order createOrder(List<Long> productIds) {
        Map<Long, Long> quantityMap = productIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<Product> products = productRepository.findAllById(quantityMap.keySet());

        if (products.size() != quantityMap.keySet().size()) {
            throw new ResourceNotFoundException("One or more product IDs are invalid.");
        }

        Order order = new Order();
        order.setId(UUID.randomUUID());

        BigDecimal total = BigDecimal.ZERO;
        for (Product product : products) {
            Long quantity = quantityMap.get(product.getId());

            OrderItem item = new OrderItem(product, quantity, order);
            order.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalPrice(total);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "orders_list", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Order> getAllOrders(Pageable pageable) {
        log.info("Fetching paginated orders: Page {}, Size {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return orderRepository.findAll(pageable);
    }

    @Cacheable(value = "order_details", key = "#id")
    public Order getOrderById(UUID id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Order not found"));
    }
}