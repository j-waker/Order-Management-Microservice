package com.jwaker.ordermanagems.repository;

import com.jwaker.ordermanagems.model.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Standard CRUD operations are inherited from JpaRepository
}