package com.vegeshop.repository;

import com.vegeshop.model.OrderItem;
import com.vegeshop.model.id.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
    List<OrderItem> findByOrder_OrderId(Integer orderId);
}
