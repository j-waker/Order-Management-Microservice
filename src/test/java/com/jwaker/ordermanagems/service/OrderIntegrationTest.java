package com.jwaker.ordermanagems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.repository.OrderRepository;
import com.jwaker.ordermanagems.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        productRepository.deleteAll();

        Product p1 = new Product("Laptop", new BigDecimal("1000.00"));
        Product p2 = new Product("Mouse", new BigDecimal("50.00"));
        productRepository.saveAll(List.of(p1, p2));
    }

    @Test
    @WithMockUser(username = "admin")
    void shouldCreateOrderAndCalculateTotal() throws Exception {
        List<Long> ids = productRepository.findAll().stream()
                .map(Product::getId).toList();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(ids)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.totalPrice").value(1050.00));

        assertEquals(1, orderRepository.count());
    }

    @Test
    @WithMockUser(username = "admin")
    void shouldReturn404WhenProductDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[999, 1000]")) // Non-existent IDs
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
        orderRepository.deleteAll();
        productRepository.deleteAll();

        Product p1 = productRepository.save(new Product("Laptop", new BigDecimal("1000.00")));
        Long p1Id = p1.getId();

        orderService.createOrder(List.of(p1Id));
        orderService.createOrder(List.of(p1Id, p1Id)); // Testing duplicate support too

        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}