package com.vegeshop.model;

import com.vegeshop.model.enums.OrderStatus;
import com.vegeshop.model.enums.OrderType;
import com.vegeshop.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DonHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDH")
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "MaKH")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "MaNV")
    private Employee employee;

    @Column(name = "NgayDat", nullable = false, insertable = false, updatable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "HinhThuc", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "PhuongThucTT", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "DiaChiGiao", length = 255)
    private String shippingAddress;

    @Column(name = "GhiChu", length = 255)
    private String note;

    @Column(name = "TongTien", nullable = false, insertable = false, updatable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
