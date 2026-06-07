package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Product response payload")
public record ProductResponse(
        @Schema(description = "Product ID", example = "1") Long id,
        @Schema(description = "Product name", example = "Laptop") String name,
        @Schema(description = "Product description", example = "High performance laptop") String description,
        @Schema(description = "Product price", example = "999.99") BigDecimal price,
        @Schema(description = "Stock quantity", example = "50") Integer quantity,
        @Schema(description = "Product category", example = "Electronics") String category,
        @Schema(description = "Created timestamp") LocalDateTime createdAt,
        @Schema(description = "Last updated timestamp") LocalDateTime updatedAt
) {}
