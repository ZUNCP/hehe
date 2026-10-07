package com.vegeshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSP")
    private Integer productId;

    @Column(name = "TenSP", nullable = false, length = 150)
    private String productName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MaLoai", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "MaNCC")
    private Supplier supplier;

    @Column(name = "DonViTinh", nullable = false, length = 20)
    private String unit;

    @Column(name = "GiaBan", nullable = false)
    private BigDecimal price;

    @Column(name = "SoLuongTon", nullable = false, insertable = false, updatable = false)
    private BigDecimal stockQuantity;

    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String description;

    @Column(name = "HinhAnh")
    private String image;

    @Column(name = "TrangThai", nullable = false)
    private Boolean status;
}
