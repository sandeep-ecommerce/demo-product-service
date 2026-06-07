package com.example.demo.service.impl;

import com.example.demo.dto.PatchProductRequest;
import com.example.demo.dto.ProductRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(productMapper::toResponse).toList();
    }

    @Override
    public List<ProductResponse> getProductsByCategory(@NonNull String category) {
        return productRepository.findByCategory(category).stream().map(productMapper::toResponse).toList();
    }

    @Override
    public ProductResponse getProductById(@NonNull Long id) {
        return productMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(@NonNull ProductRequest request) {
        return productMapper.toResponse(productRepository.save(productMapper.toEntity(request)));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(@NonNull Long id, @NonNull ProductRequest request) {
        Product product = findOrThrow(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(request.getCategory());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse patchProduct(@NonNull Long id, @NonNull PatchProductRequest request) {
        Product product = findOrThrow(id);
        if (request.getName() != null)        product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null)       product.setPrice(request.getPrice());
        if (request.getQuantity() != null)    product.setQuantity(request.getQuantity());
        if (request.getCategory() != null)    product.setCategory(request.getCategory());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(@NonNull Long id) {
        if (!productRepository.existsById(id)) throw new ProductNotFoundException(id);
        productRepository.deleteById(id);
    }

    // ── Derived Query ─────────────────────────────────────────────────────────

    @Override
    public List<ProductResponse> getProductsByPriceRange(@NonNull BigDecimal minPrice, @NonNull BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice)
                .stream().map(productMapper::toResponse).toList();
    }

    @Override
    public List<ProductResponse> getInStockProducts(@NonNull Integer minQuantity) {
        return productRepository.findByQuantityGreaterThanEqual(minQuantity)
                .stream().map(productMapper::toResponse).toList();
    }

    // ── JPQL ──────────────────────────────────────────────────────────────────

    @Override
    public List<ProductResponse> searchProductsByName(@NonNull String keyword) {
        return productRepository.searchByName(keyword)
                .stream().map(productMapper::toResponse).toList();
    }

    @Override
    public List<ProductResponse> getProductsByCategorySortedByPrice(@NonNull String category) {
        return productRepository.findByCategorySortedByPrice(category)
                .stream().map(productMapper::toResponse).toList();
    }

    // ── Native Query ──────────────────────────────────────────────────────────

    @Override
    public List<ProductResponse> getProductsPricedBelow(@NonNull BigDecimal maxPrice) {
        return productRepository.findProductsPricedBelow(maxPrice)
                .stream().map(productMapper::toResponse).toList();
    }

    @Override
    public List<ProductResponse> getTopExpensiveProducts(int limit) {
        return productRepository.findTopExpensiveProducts(limit)
                .stream().map(productMapper::toResponse).toList();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    @NonNull
    private Product findOrThrow(Long id) {
        return Objects.requireNonNull(
                productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id)));
    }
}
