package com.jwaker.ordermanagems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class OrderManageMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderManageMsApplication.class, args);
	}
}
