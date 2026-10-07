package com.vegeshop.model;

import com.vegeshop.model.id.OrderItemId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietDonHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @EmbeddedId
    private OrderItemId id;

    @ManyToOne(optional = false)
    @MapsId("orderId")
    @JoinColumn(name = "MaDH")
    private SalesOrder order;

    @ManyToOne(optional = false)
    @MapsId("productId")
    @JoinColumn(name = "MaSP")
    private Product product;

    @Column(name = "SoLuong", nullable = false)
    private BigDecimal quantity;

    @Column(name = "DonGia", nullable = false)
    private BigDecimal unitPrice;
}
