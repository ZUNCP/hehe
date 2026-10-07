package com.vegeshop.repository;

import com.vegeshop.model.PurchaseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceipt, Integer> {
    List<PurchaseReceipt> findAllByOrderByReceiptIdDesc();
}
