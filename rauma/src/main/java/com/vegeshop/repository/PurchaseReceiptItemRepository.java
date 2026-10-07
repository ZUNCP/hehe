package com.vegeshop.repository;

import com.vegeshop.model.PurchaseReceiptItem;
import com.vegeshop.model.id.PurchaseReceiptItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseReceiptItemRepository extends JpaRepository<PurchaseReceiptItem, PurchaseReceiptItemId> {
    List<PurchaseReceiptItem> findByReceipt_ReceiptId(Integer receiptId);
}
