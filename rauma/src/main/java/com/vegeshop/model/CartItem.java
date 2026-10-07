package com.vegeshop.model;

import com.vegeshop.model.id.CartItemId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietGioHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    @EmbeddedId
    private CartItemId id;

    @ManyToOne(optional = false)
    @MapsId("cartId")
    @JoinColumn(name = "MaGH")
    private Cart cart;

    @ManyToOne(optional = false)
    @MapsId("productId")
    @JoinColumn(name = "MaSP")
    private Product product;

    @Column(name = "SoLuong", nullable = false)
    private BigDecimal quantity;
}
