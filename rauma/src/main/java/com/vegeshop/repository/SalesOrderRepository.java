package com.vegeshop.repository;

import com.vegeshop.model.SalesOrder;
import com.vegeshop.model.enums.OrderStatus;
import com.vegeshop.model.enums.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Integer> {
    List<SalesOrder> findByCustomer_CustomerIdOrderByOrderIdDesc(Integer customerId);

    List<SalesOrder> findByOrderTypeOrderByOrderIdDesc(OrderType orderType);

    @Query("""
            SELECT o FROM SalesOrder o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:type IS NULL OR o.orderType = :type)
              AND (:from IS NULL OR o.orderDate >= :from)
              AND (:to IS NULL OR o.orderDate <= :to)
            ORDER BY o.orderId DESC
            """)
    List<SalesOrder> search(
            @Param("status") OrderStatus status,
            @Param("type") OrderType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
