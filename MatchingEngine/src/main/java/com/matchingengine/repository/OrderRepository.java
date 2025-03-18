package com.matchingengine.repository;

import com.matchingengine.model.Order;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {
    private final Map<Order, Order> orderMap = new ConcurrentHashMap<>();

    public Order save(Order order) {
        return orderMap.merge(order, order, (existing, newOrder) -> {
            existing.setAmount(existing.getAmount().add(newOrder.getAmount()));
            return existing;
        });
    }

    public List<Order> findAll() {
        return List.copyOf(orderMap.values());
    }

    public List<Order> findByUserId(String userId) {
        return orderMap.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
