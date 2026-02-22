package com.jwaker.ordermanagems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwaker.ordermanagems.dto.OrderItemRequest;
import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.repository.OrderRepository;
import com.jwaker.ordermanagems.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    private ObjectMapper objectMapper;

    private Long product1Id;
    private Long product2Id;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        orderRepository.deleteAll();
        productRepository.deleteAll();

        Product p1 = productRepository.save(new Product("Item 1", new BigDecimal("10.00")));
        Product p2 = productRepository.save(new Product("Item 2", new BigDecimal("20.00")));

        this.product1Id = p1.getId();
        this.product2Id = p2.getId();
    }

    @Test
    @WithMockUser(username = "admin")
    void shouldCreateOrderAndCalculateTotal() throws Exception {
        OrderItemRequest item1 = new OrderItemRequest(product1Id, 1);
        OrderItemRequest item2 = new OrderItemRequest(product2Id, 3);
        List<OrderItemRequest> payload = List.of(item1, item2);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items").exists());
    }

    @Test
    @WithMockUser(username = "admin")
    void shouldReturn404WhenProductDoesNotExist() throws Exception {
        OrderItemRequest nonExistentItem = new OrderItemRequest(999L, 1);
        List<OrderItemRequest> payload = List.of(nonExistentItem);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1]"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin")
    void shouldReturnPaginatedOrders() throws Exception {
        Product p1 = productRepository.save(new Product("Item 1", new BigDecimal("10")));
        Product p2 = productRepository.save(new Product("Item 2", new BigDecimal("20")));

        orderService.createOrder(List.of(new OrderItemRequest(p1.getId(), 1)));
        orderService.createOrder(List.of(new OrderItemRequest(p2.getId(), 1)));

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "admin")
    void shouldCreateOrderWithNewDtoFormat() throws Exception {
        OrderItemRequest item1 = new OrderItemRequest(product1Id, 2);
        OrderItemRequest item2 = new OrderItemRequest(product2Id, 5);
        List<OrderItemRequest> payload = List.of(item1, item2);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").exists())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }
}