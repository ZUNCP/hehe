package com.vegeshop.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceiptItemId implements Serializable {
    @Column(name = "MaPN")
    private Integer receiptId;
    @Column(name = "MaSP")
    private Integer productId;
}
