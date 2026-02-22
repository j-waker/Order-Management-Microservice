package com.jwaker.ordermanagems.repository;

import com.jwaker.ordermanagems.model.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Standard CRUD operations are inherited from JpaRepository

    @Modifying
    @Query("UPDATE Product p SET p.price = p.price + :adjustment WHERE p.id = :id")
    void incrementPrice(@Param("id") Long id, @Param("adjustment") BigDecimal adjustment);
}