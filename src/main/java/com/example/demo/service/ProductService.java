package com.example.demo.service;

import com.example.demo.dto.PatchProductRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // ── CRUD ──────────────────────────────────────────────────────────────────
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getProductsByCategory(@NonNull String category);
    ProductResponse getProductById(@NonNull Long id);
    ProductResponse createProduct(@NonNull ProductRequest request);
    ProductResponse updateProduct(@NonNull Long id, @NonNull ProductRequest request);
    ProductResponse patchProduct(@NonNull Long id, @NonNull PatchProductRequest request);
    void deleteProduct(@NonNull Long id);

    // ── Derived Query ─────────────────────────────────────────────────────────
    List<ProductResponse> getProductsByPriceRange(@NonNull BigDecimal minPrice, @NonNull BigDecimal maxPrice);
    List<ProductResponse> getInStockProducts(@NonNull Integer minQuantity);

    // ── JPQL ──────────────────────────────────────────────────────────────────
    List<ProductResponse> searchProductsByName(@NonNull String keyword);
    List<ProductResponse> getProductsByCategorySortedByPrice(@NonNull String category);

    // ── Native Query ──────────────────────────────────────────────────────────
    List<ProductResponse> getProductsPricedBelow(@NonNull BigDecimal maxPrice);
    List<ProductResponse> getTopExpensiveProducts(int limit);
}
