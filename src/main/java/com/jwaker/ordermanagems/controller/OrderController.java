package com.jwaker.ordermanagems.controller;

import com.jwaker.ordermanagems.dto.OrderItemRequest;
import com.jwaker.ordermanagems.model.Order;
import com.jwaker.ordermanagems.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "List all orders", description = "Retrieves a paginated list of all orders with items.")
    public ResponseEntity<Page<Order>> getAllOrders(
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable UUID id) {
        return orderService.getOrderById(id);
    }

    @PostMapping
    @Operation(summary = "Create an order", description = "Submit a list of product IDs and quantities.")
    public ResponseEntity<Order> createOrder(@RequestBody List<OrderItemRequest> itemRequests) {
        return new ResponseEntity<>(orderService.createOrder(itemRequests), HttpStatus.CREATED);
    }
}