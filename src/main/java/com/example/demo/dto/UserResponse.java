package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User response payload")
public record UserResponse(
        @Schema(description = "User ID", example = "1") Long id,
        @Schema(description = "Full name", example = "John Doe") String name,
        @Schema(description = "Email address", example = "john@example.com") String email
) {}
