package com.vegeshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PhieuNhap")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPN")
    private Integer receiptId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MaNCC", nullable = false)
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "MaNV")
    private Employee employee;

    @Column(name = "NgayNhap", nullable = false, insertable = false, updatable = false)
    private LocalDateTime receiptDate;

    @Column(name = "TongTien", nullable = false, insertable = false, updatable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "receipt")
    @Builder.Default
    private List<PurchaseReceiptItem> items = new ArrayList<>();
}
