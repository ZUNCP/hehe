package com.vegeshop.model;

import com.vegeshop.model.id.PurchaseReceiptItemId;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietPhieuNhap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReceiptItem {
    @EmbeddedId
    private PurchaseReceiptItemId id;

    @ManyToOne(optional = false)
    @MapsId("receiptId")
    @JoinColumn(name = "MaPN")
    private PurchaseReceipt receipt;

    @ManyToOne(optional = false)
    @MapsId("productId")
    @JoinColumn(name = "MaSP")
    private Product product;

    @Column(name = "SoLuong", nullable = false)
    private BigDecimal quantity;

    @Column(name = "GiaNhap", nullable = false)
    private BigDecimal importPrice;
}
