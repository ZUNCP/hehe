package com.vegeshop.service.event;

import com.vegeshop.config.AppConfigManager;
import com.vegeshop.repository.OrderItemRepository;
import com.vegeshop.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LowStockWarningListener {

    private static final Logger log = LoggerFactory.getLogger(LowStockWarningListener.class);

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AppConfigManager appConfigManager;

    public LowStockWarningListener(OrderItemRepository orderItemRepository,
                                   ProductRepository productRepository,
                                   AppConfigManager appConfigManager) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.appConfigManager = appConfigManager;
    }

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        BigDecimal threshold = appConfigManager.getLowStockThresholdDecimal();
        orderItemRepository.findByOrder_OrderId(event.getOrder().getOrderId()).forEach(item ->
                productRepository.findById(item.getProduct().getProductId()).ifPresent(product -> {
                    if (product.getStockQuantity() != null && product.getStockQuantity().compareTo(threshold) < 0) {
                        log.warn("Cảnh báo tồn kho thấp: {} còn {} {}",
                                product.getProductName(), product.getStockQuantity(), product.getUnit());
                    }
                })
        );
    }
}
