package com.matchingengine.controller;

import com.matchingengine.model.Order;
import com.matchingengine.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Order> addOrder(@Valid @RequestBody Order order) {
        return ResponseEntity.ok(service.addOrder(order));
    }

    @GetMapping("/{userId}/matches")
    public ResponseEntity<List<Map<String, Object>>> getMatchedAmount(@PathVariable String userId) {
        return ResponseEntity.ok(service.getMatchedAmount(userId));
    }
}
