package com.jwaker.ordermanagems.controller;

import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products")
    @Parameters({
            @Parameter(name = "page", description = "Page index", example = "0"),
            @Parameter(name = "size", description = "Page size", example = "10"),
            @Parameter(name = "sort", description = "Sort criteria", example = "id,asc")
    })
    public Page<Product> getAll(@Parameter(hidden = true) Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Add a new product to the catalog. Ensure the price is a positive decimal.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Default Product",
                            value = "{\"name\": \"Mechanical Keyboard\", \"price\": 120.50}",
                            summary = "Standard product example"
                    )
            )
    )
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return new ResponseEntity<>(productService.saveProduct(product), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product", description = "Updates name and price. This will invalidate the product cache.")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {
        return ResponseEntity.ok(productService.updateProduct(id, productDetails));
    }

    @PutMapping("adjustPrice/{id}")
    @Operation(summary = "Update an existing product's price", description = "Threadsafe update product price. This will invalidate the product cache.")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody BigDecimal priceAdjustment) {
        return ResponseEntity.ok(productService.adjustProductPrice(id, priceAdjustment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Removes a product from the catalog and clears the cache. Fails if the product is part of an existing order.")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}