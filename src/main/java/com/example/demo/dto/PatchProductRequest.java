package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Partial product update payload — all fields are optional")
public class PatchProductRequest {

    @Size(min = 1, max = 100, message = "Name must not be blank and must not exceed 100 characters")
    @Schema(description = "Product name", example = "Laptop Pro")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Product description", example = "Updated description")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    @Schema(description = "Product price", example = "799.99")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity must be 0 or more")
    @Schema(description = "Stock quantity", example = "30")
    private Integer quantity;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Schema(description = "Product category", example = "Electronics")
    private String category;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
