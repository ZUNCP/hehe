package com.vegeshop.repository;

import com.vegeshop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findByProductNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findByCategory_CategoryIdAndProductNameContainingIgnoreCase(Integer categoryId, String keyword, Pageable pageable);

    Page<Product> findByCategory_CategoryId(Integer categoryId, Pageable pageable);

    boolean existsByCategory_CategoryId(Integer categoryId);

    boolean existsBySupplier_SupplierId(Integer supplierId);

    long countByStatusTrue();

    @Modifying
    @Query(value = "UPDATE SanPham SET SoLuongTon = SoLuongTon + :qty WHERE MaSP = :id", nativeQuery = true)
    void restoreStock(@Param("id") Integer productId, @Param("qty") BigDecimal qty);
}
