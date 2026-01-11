package com.example.demo.component;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventListeners {

    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        System.out.println("User created: " + event.getUserId());
    }

    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("Order placed: " + event.getOrderId());
    }

    // Event classes
    public static class UserCreatedEvent {
        private final Long userId;
        public UserCreatedEvent(Long userId) { this.userId = userId; }
        public Long getUserId() { return userId; }
    }

    public static class OrderPlacedEvent {
        private final Long orderId;
        public OrderPlacedEvent(Long orderId) { this.orderId = orderId; }
        public Long getOrderId() { return orderId; }
    }
}
