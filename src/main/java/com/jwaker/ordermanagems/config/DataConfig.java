package com.jwaker.ordermanagems.config;

import com.jwaker.ordermanagems.model.Product;
import com.jwaker.ordermanagems.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("!test")
public class DataConfig {

    @Bean
    CommandLineRunner initDatabase(ProductRepository repository) {
        return args -> {
            repository.save(new Product("Laptop", new BigDecimal("1200.00")));
            repository.save(new Product("Mouse", new BigDecimal("25.50")));
            repository.save(new Product("Keyboard", new BigDecimal("75.00")));
        };
    }
}