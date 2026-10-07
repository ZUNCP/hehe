package com.vegeshop.service.event;

import com.vegeshop.model.enums.OrderStatus;
import com.vegeshop.repository.OrderItemRepository;
import com.vegeshop.repository.ProductRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StockRestoreListener {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public StockRestoreListener(OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getNewStatus() != OrderStatus.DaHuy || event.getOldStatus() == OrderStatus.DaHuy) {
            return;
        }
        orderItemRepository.findByOrder_OrderId(event.getOrder().getOrderId()).forEach(item ->
                productRepository.restoreStock(item.getProduct().getProductId(), item.getQuantity())
        );
    }
}
