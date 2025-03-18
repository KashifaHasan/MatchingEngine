package com.matchingengine.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.matchingengine.model.Order;
import com.matchingengine.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order addOrder(Order order) {
        return repository.save(order);
    }
    
    public List<Map<String, Object>> getMatchedAmount(String userId) {
        List<Order> userOrders = repository.findByUserId(userId);
        List<Order> allOrders = repository.findAll();

        return userOrders.stream()
            .map(userOrder -> {
                BigDecimal matchedAmount = allOrders.stream()
                    .filter(order -> !order.getUserId().equals(userOrder.getUserId()) && // Exclude same user
                                     order.getCurrencyPair().equals(userOrder.getCurrencyPair()) &&
                                     order.getDealtCurrency().equals(userOrder.getDealtCurrency()) &&
                                     order.getValueDate().equals(userOrder.getValueDate()) &&
                                     !order.getDirection().equals(userOrder.getDirection())) // Opposite direction
                    .map(Order::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal matchPercentage = userOrder.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? matchedAmount.min(userOrder.getAmount())
                        .divide(userOrder.getAmount(), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("currencyPair", userOrder.getCurrencyPair());
                resultMap.put("matchedPercentage", matchPercentage);

                return resultMap;
            })
            .collect(Collectors.toList());
    }


}
