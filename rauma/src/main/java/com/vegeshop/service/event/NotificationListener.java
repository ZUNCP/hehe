package com.vegeshop.service.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        String customer = event.getOrder().getCustomer() != null
                ? event.getOrder().getCustomer().getFullName()
                : "Khách vãng lai";
        log.info("Thông báo đơn #{} của {}: {} -> {}",
                event.getOrder().getOrderId(),
                customer,
                event.getOldStatus(),
                event.getNewStatus());
    }
}
