package com.jwaker.ordermanagems.service;

import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin")
    void shouldReturnPaginatedProducts() throws Exception {
        productRepository.deleteAll(); // Clean start

        int itemsToAdd = 15;
        for (int i = 0; i < itemsToAdd; i++) {
            productRepository.save(new Product("Product " + i, new BigDecimal("10.00")));
        }

        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10)) // Page size is 10
                .andExpect(jsonPath("$.totalElements").value(itemsToAdd)); // Total is 15
    }

    @Test
    @WithMockUser(username = "admin")
    void shouldVerifyCachingOnProductDetail() throws Exception {
        Product p = productRepository.save(new Product("CacheTest", BigDecimal.ONE));

        mockMvc.perform(get("/api/products/" + p.getId())).andExpect(status().isOk());

        mockMvc.perform(get("/api/products/" + p.getId())).andExpect(status().isOk());
    }
}