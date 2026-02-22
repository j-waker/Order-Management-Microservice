package com.jwaker.ordermanagems.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@NoArgsConstructor
@JsonPropertyOrder({ "productId", "productName", "productPrice", "quantity"})
public class OrderItem {
    @Id
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Ensure product is loaded
    @JoinColumn(name = "product_id")
    private Product product;

    private Long quantity;

    @ManyToOne
    @JsonIgnore
    private Order order;

    public OrderItem(Product product, Long quantity, Order order) {
        this.product = product;
        this.quantity = quantity;
        this.order = order;
    }

    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    public String getProductName() {
        return product != null ? product.getName() : null;
    }

    public BigDecimal getProductPrice() {
        return product != null ? product.getPrice() : null;
    }

    public Long getQuantity() {
        return quantity;
    }
}