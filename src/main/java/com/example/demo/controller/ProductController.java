package com.example.demo.controller;

import com.example.demo.dto.PatchProductRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "CRUD and search operations for products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all products", description = "Optionally filter by category")
    @ApiResponse(responseCode = "200", description = "List of products returned")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category) {
        List<ProductResponse> products = (category != null && !category.isBlank())
                ? productService.getProductsByCategory(category)
                : productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    @ApiResponse(responseCode = "204", description = "Product deleted")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ── PATCH ─────────────────────────────────────────────────────────────────

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a product", description = "Only supplied fields are updated; omitted fields are left unchanged")
    @ApiResponse(responseCode = "200", description = "Product patched")
    @ApiResponse(responseCode = "400", description = "Invalid field value")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductResponse> patchProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Valid @RequestBody PatchProductRequest request) {
        return ResponseEntity.ok(productService.patchProduct(id, request));
    }

    // ── HEAD ──────────────────────────────────────────────────────────────────

    @RequestMapping(method = RequestMethod.HEAD)
    @Operation(summary = "Check product collection", description = "Returns metadata headers for the collection without a response body")
    @ApiResponse(responseCode = "200", description = "Collection is accessible")
    public ResponseEntity<Void> headProducts() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    @Operation(summary = "Check product existence", description = "Returns 200 with headers if the product exists, 404 otherwise — no response body")
    @ApiResponse(responseCode = "200", description = "Product exists")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<Void> headProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        productService.getProductById(id); // throws ProductNotFoundException → 404 if absent
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    // ── OPTIONS ───────────────────────────────────────────────────────────────

    @RequestMapping(method = RequestMethod.OPTIONS)
    @Operation(summary = "Supported methods for /api/products", description = "Returns the Allow header listing all HTTP methods supported on the collection endpoint")
    @ApiResponse(responseCode = "200", description = "Allow header returned")
    public ResponseEntity<Void> optionsProducts() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAllow(Set.of(HttpMethod.GET, HttpMethod.POST,
                HttpMethod.HEAD, HttpMethod.OPTIONS));
        return ResponseEntity.ok().headers(headers).build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    @Operation(summary = "Supported methods for /api/products/{id}", description = "Returns the Allow header listing all HTTP methods supported on the item endpoint")
    @ApiResponse(responseCode = "200", description = "Allow header returned")
    public ResponseEntity<Void> optionsProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAllow(Set.of(HttpMethod.GET, HttpMethod.PUT, HttpMethod.PATCH,
                HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS));
        return ResponseEntity.ok().headers(headers).build();
    }

    // ── Derived Query ─────────────────────────────────────────────────────────

    @GetMapping("/search/price-range")
    @Operation(summary = "[Derived] Products within a price range",
               description = "Uses Spring Data derived query: findByPriceBetween")
    @ApiResponse(responseCode = "200", description = "Products returned")
    public ResponseEntity<List<ProductResponse>> getByPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.getProductsByPriceRange(minPrice, maxPrice));
    }

    @GetMapping("/search/in-stock")
    @Operation(summary = "[Derived] Products with minimum stock",
               description = "Uses Spring Data derived query: findByQuantityGreaterThanEqual")
    @ApiResponse(responseCode = "200", description = "Products returned")
    public ResponseEntity<List<ProductResponse>> getInStock(
            @Parameter(description = "Minimum quantity in stock") @RequestParam(defaultValue = "1") Integer minQuantity) {
        return ResponseEntity.ok(productService.getInStockProducts(minQuantity));
    }

    // ── JPQL ──────────────────────────────────────────────────────────────────

    @GetMapping("/search/by-name")
    @Operation(summary = "[JPQL] Search products by name keyword",
               description = "Uses JPQL with LIKE — case-insensitive name search")
    @ApiResponse(responseCode = "200", description = "Products returned")
    public ResponseEntity<List<ProductResponse>> searchByName(
            @Parameter(description = "Keyword to search in product name") @RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProductsByName(keyword));
    }

    @GetMapping("/search/by-category-sorted")
    @Operation(summary = "[JPQL] Products by category sorted by price",
               description = "Uses JPQL ORDER BY — returns products in category sorted by price ASC")
    @ApiResponse(responseCode = "200", description = "Products returned")
    public ResponseEntity<List<ProductResponse>> getByCategorySorted(
            @Parameter(description = "Category name") @RequestParam String category) {
        return ResponseEntity.ok(productService.getProductsByCategorySortedByPrice(category));
    }

    // ── Native Query ──────────────────────────────────────────────────────────

    @GetMapping("/search/price-below")
    @Operation(summary = "[Native] Products priced below a threshold",
               description = "Uses native SQL: SELECT * FROM products WHERE price < :maxPrice")
    @ApiResponse(responseCode = "200", description = "Products returned")
    public ResponseEntity<List<ProductResponse>> getPricedBelow(
            @Parameter(description = "Maximum price (exclusive)") @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.getProductsPricedBelow(maxPrice));
    }

    @GetMapping("/search/most-expensive")
    @Operation(summary = "[Native] Top N most expensive products",
               description = "Uses native SQL: SELECT * FROM products ORDER BY price DESC LIMIT :limit")
    @ApiResponse(responseCode = "200", description = "Products returned")
    public ResponseEntity<List<ProductResponse>> getMostExpensive(
            @Parameter(description = "Number of top products to return") @RequestParam(defaultValue = "5") @Min(1) int limit) {
        return ResponseEntity.ok(productService.getTopExpensiveProducts(limit));
    }
}
