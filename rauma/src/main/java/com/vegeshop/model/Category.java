package com.vegeshop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LoaiSanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLoai")
    private Integer categoryId;

    @Column(name = "TenLoai", nullable = false, unique = true, length = 100)
    private String categoryName;

    @Column(name = "MoTa", length = 255)
    private String description;
}
