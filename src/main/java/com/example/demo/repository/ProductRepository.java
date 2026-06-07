package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ── Derived Query Methods ─────────────────────────────────────────────────

    List<Product> findByCategory(String category);

    // Products whose price falls between min and max (inclusive)
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Products with stock at or above the given quantity
    List<Product> findByQuantityGreaterThanEqual(Integer quantity);

    // ── JPQL ─────────────────────────────────────────────────────────────────

    // Case-insensitive name search using LIKE
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);

    // Products in a category sorted by price ascending
    @Query("SELECT p FROM Product p WHERE p.category = :category ORDER BY p.price ASC")
    List<Product> findByCategorySortedByPrice(@Param("category") String category);

    // ── Native Queries ────────────────────────────────────────────────────────

    // Products with price strictly below the given threshold
    @Query(value = "SELECT * FROM products WHERE price < :maxPrice", nativeQuery = true)
    List<Product> findProductsPricedBelow(@Param("maxPrice") BigDecimal maxPrice);

    // Top N most expensive products
    @Query(value = "SELECT * FROM products ORDER BY price DESC LIMIT :limit", nativeQuery = true)
    List<Product> findTopExpensiveProducts(@Param("limit") int limit);
}
