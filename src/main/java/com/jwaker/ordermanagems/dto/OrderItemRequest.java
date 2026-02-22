package com.jwaker.ordermanagems.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderItemRequest(
        @Schema(example = "1", description = "The ID of the product")
        Long id,

        @Schema(example = "2", description = "Quantity of the product to order")
        Integer quantity
) {}