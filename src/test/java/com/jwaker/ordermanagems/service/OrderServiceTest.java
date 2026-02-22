package com.jwaker.ordermanagems.service;

import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.model.Order;
import com.jwaker.ordermanagems.repository.OrderRepository;
import com.jwaker.ordermanagems.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testCreateOrder_Success() {
        Long productId = 1L;
        List<Long> inputIds = List.of(productId, productId); // Testing duplicate support
        Product mockProduct = new Product("Laptop", new BigDecimal("1000.00"));
        mockProduct.setId(productId);

        when(productRepository.findAllById(any())).thenReturn(List.of(mockProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(UUID.randomUUID());
            }
            return order;
        });

        Order result = orderService.createOrder(inputIds);

        assertNotNull(result, "Service returned null; check if an exception was thrown internally");
        assertNotNull(result.getId());
        assertEquals(new BigDecimal("2000.00"), result.getTotalPrice());
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().getFirst().getQuantity());
    }

    @Test
    void testGetOrderById() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(orderId);

        assertEquals(orderId, result.getId());
    }

    @Test
    void testListAllOrders_Paginated() {
        Order order1 = new Order();
        order1.setId(UUID.randomUUID());
        Order order2 = new Order();
        order2.setId(UUID.randomUUID());

        List<Order> orderList = List.of(order1, order2);
        Page<Order> orderPage = new PageImpl<>(orderList);
        Pageable pageable = PageRequest.of(0, 10);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        Page<Order> result = orderService.getAllOrders(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(order1.getId(), result.getContent().get(0).getId());
        verify(orderRepository, times(1)).findAll(pageable);
    }
}