package com.jwaker.ordermanagems.service;

import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.repository.ProductRepository;
import com.jwaker.ordermanagems.exception.ResourceNotFoundException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<Product> getAllProducts(Pageable pageable) {
        log.info("Fetching paginated products: Page {}, Size {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable);
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        log.info("Fetching product id {} from DB", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: id " + id));
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product saveProduct(Product product) {
        log.info("Adding new product: {}", product.getName());
        return productRepository.save(product);
    }
}