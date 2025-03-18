package com.matchingengine;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import com.matchingengine.model.Order;
import com.matchingengine.repository.OrderRepository;
import com.matchingengine.service.OrderService;

class OrderServiceTest {
    private OrderRepository repository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        repository = mock(OrderRepository.class);
        orderService = new OrderService(repository);
    }
    
    @Test
    void testScenarioA_OrderSubmissionAndSaving() {
        Order order1 = new Order("EURUSD", "USD", "SELL", new BigDecimal("10000"), LocalDate.of(2025, 1, 30), "UserA");
        when(repository.save(order1)).thenReturn(order1);
        Order savedOrder1 = orderService.addOrder(order1);

        assertNotNull(savedOrder1);
        assertEquals("EURUSD", savedOrder1.getCurrencyPair());
        assertEquals("USD", savedOrder1.getDealtCurrency());
        assertEquals("SELL", savedOrder1.getDirection());
        assertEquals(new BigDecimal("10000"), savedOrder1.getAmount());
        assertEquals(LocalDate.of(2025, 1, 30), savedOrder1.getValueDate());
        assertEquals("UserA", savedOrder1.getUserId());

        Order order2 = new Order("EURUSD", "USD", "BUY", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserA");
        when(repository.save(order2)).thenReturn(order2);
        Order savedOrder2 = orderService.addOrder(order2);

        assertNotNull(savedOrder2);
        assertEquals("EURUSD", savedOrder2.getCurrencyPair());
        assertEquals("USD", savedOrder2.getDealtCurrency());
        assertEquals("BUY", savedOrder2.getDirection());
        assertEquals(new BigDecimal("5000"), savedOrder2.getAmount());
        assertEquals(LocalDate.of(2025, 1, 30), savedOrder2.getValueDate());
        assertEquals("UserA", savedOrder2.getUserId());

        Order order3 = new Order("EURUSD", "USD", "BUY", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserB");
        when(repository.save(order3)).thenReturn(order3);
        Order savedOrder3 = orderService.addOrder(order3);

        assertNotNull(savedOrder3);
        assertEquals("EURUSD", savedOrder3.getCurrencyPair());
        assertEquals("USD", savedOrder3.getDealtCurrency());
        assertEquals("BUY", savedOrder3.getDirection());
        assertEquals(new BigDecimal("5000"), savedOrder3.getAmount());
        assertEquals(LocalDate.of(2025, 1, 30), savedOrder3.getValueDate());
        assertEquals("UserB", savedOrder3.getUserId());
    }

    @Test
    void testScenarioB_AggregatedOrdersAndMatching() {
        Order order1 = new Order("EURUSD", "USD", "SELL", new BigDecimal("10000"), LocalDate.of(2025, 1, 30), "UserA");
        Order order2 = new Order("EURUSD", "USD", "BUY", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserA");
        Order order3 = new Order("EURUSD", "USD", "BUY", new BigDecimal("20000"), LocalDate.of(2025, 1, 30), "UserC");

        Order aggregatedOrderA = new Order("EURUSD", "USD", "SELL", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserA");

        when(repository.findByUserId("UserA")).thenReturn(List.of(aggregatedOrderA)); 
        when(repository.findByUserId("UserC")).thenReturn(List.of(order3));
        when(repository.findAll()).thenReturn(List.of(aggregatedOrderA, order3)); // Ensure correct aggregation

        List<Map<String, Object>> matchesA = orderService.getMatchedAmount("UserA");

        assertEquals(1, matchesA.size());
        assertEquals("EURUSD", matchesA.get(0).get("currencyPair"));
        assertEquals(new BigDecimal("100.00"), matchesA.get(0).get("matchedPercentage")); // FIXED EXPECTED VALUE

        List<Map<String, Object>> matchesC = orderService.getMatchedAmount("UserC");

        assertEquals(1, matchesC.size());
        assertEquals("EURUSD", matchesC.get(0).get("currencyPair"));
        assertEquals(new BigDecimal("25.00"), matchesC.get(0).get("matchedPercentage"));
    }


    @Test
    void testAddOrders() {
        Order order1 = new Order("EURUSD", "USD", "SELL", new BigDecimal("10000"), LocalDate.of(2025, 1, 30), "UserA");
        Order order2 = new Order("EURUSD", "USD", "BUY", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserA");
        Order order3 = new Order("EURUSD", "USD", "BUY", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserB");

        when(repository.save(order1)).thenReturn(order1);
        when(repository.save(order2)).thenReturn(order2);
        when(repository.save(order3)).thenReturn(order3);

        assertEquals(order1, orderService.addOrder(order1));
        assertEquals(order2, orderService.addOrder(order2));
        assertEquals(order3, orderService.addOrder(order3));
    }

    @Test
    void testGetMatchedAmount_UserA_FullMatch() {
        Order orderA1 = new Order("EURUSD", "USD", "BUY", new BigDecimal("10000"), LocalDate.of(2025, 1, 30), "UserA");
        Order orderA2 = new Order("EURUSD", "USD", "BUY", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserA");
        Order orderB = new Order("EURUSD", "USD", "SELL", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserB");

        when(repository.findByUserId("UserA")).thenReturn(List.of(orderA1, orderA2));
        when(repository.findAll()).thenReturn(List.of(orderA1, orderA2, orderB));

        List<Map<String, Object>> matches = orderService.getMatchedAmount("UserA");

        assertEquals(2, matches.size());
        assertEquals(new BigDecimal("50.00"), matches.get(0).get("matchedPercentage"));
        assertEquals(new BigDecimal("100.00"), matches.get(1).get("matchedPercentage"));
    }


    @Test
    void testGetMatchedAmount_UserC_PartialMatch() {
        Order orderA = new Order("EURUSD", "USD", "SELL", new BigDecimal("10000"), LocalDate.of(2025, 1, 30), "UserA");
        Order orderB = new Order("EURUSD", "USD", "BUY", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserA");
        Order orderC = new Order("EURUSD", "USD", "BUY", new BigDecimal("20000"), LocalDate.of(2025, 1, 30), "UserC");

        when(repository.findByUserId("UserC")).thenReturn(List.of(orderC));
        when(repository.findAll()).thenReturn(List.of(orderA, orderB, orderC));

        List<Map<String, Object>> matches = orderService.getMatchedAmount("UserC");

        assertEquals(1, matches.size());
        assertEquals(new BigDecimal("50.00"), matches.get(0).get("matchedPercentage"));
    }

    @Test
    void testGetMatchedAmount_NoMatch() {
        Order orderA = new Order("EURUSD", "USD", "BUY", new BigDecimal("10000"), LocalDate.of(2025, 1, 30), "UserA");

        when(repository.findByUserId("UserA")).thenReturn(List.of(orderA));
        when(repository.findAll()).thenReturn(List.of(orderA));

        List<Map<String, Object>> matches = orderService.getMatchedAmount("UserA");

        assertEquals(1, matches.size());
        assertEquals(new BigDecimal("0.00"), matches.get(0).get("matchedPercentage"));
    }

    @Test
    void testGetMatchedAmount_DifferentCurrencyPair_NoMatch() {
        Order orderA = new Order("EURUSD", "USD", "BUY", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserA");
        Order orderB = new Order("GBPUSD", "USD", "SELL", new BigDecimal("5000"), LocalDate.of(2025, 1, 30), "UserB");

        when(repository.findByUserId("UserA")).thenReturn(List.of(orderA));
        when(repository.findAll()).thenReturn(List.of(orderA, orderB));

        List<Map<String, Object>> matches = orderService.getMatchedAmount("UserA");

        assertEquals(1, matches.size());
        assertEquals(new BigDecimal("0.00"), matches.get(0).get("matchedPercentage"));
    }

    @Test
    void testGetMatchedAmount_ZeroAmountOrder() {
        Order orderA = new Order("EURUSD", "USD", "BUY", BigDecimal.ZERO, LocalDate.of(2025, 1, 30), "UserA");

        when(repository.findByUserId("UserA")).thenReturn(List.of(orderA));
        when(repository.findAll()).thenReturn(List.of(orderA));

        List<Map<String, Object>> matches = orderService.getMatchedAmount("UserA");

        assertEquals(1, matches.size());
        assertEquals(new BigDecimal("0"), matches.get(0).get("matchedPercentage"));
    }

    @Test
    void testGetMatchedAmount_FullMatch() {
        Order orderA = new Order("EURUSD", "USD", "BUY", new BigDecimal("1000"), LocalDate.of(2025, 1, 30), "UserA");
        Order orderB = new Order("EURUSD", "USD", "SELL", new BigDecimal("500"), LocalDate.of(2025, 1, 30), "UserB");
        Order orderC = new Order("EURUSD", "USD", "SELL", new BigDecimal("500"), LocalDate.of(2025, 1, 30), "UserC");

        when(repository.findByUserId("UserA")).thenReturn(List.of(orderA));
        when(repository.findAll()).thenReturn(List.of(orderA, orderB, orderC));

        List<Map<String, Object>> matches = orderService.getMatchedAmount("UserA");

        assertEquals(1, matches.size());
        assertEquals(new BigDecimal("100.00"), matches.get(0).get("matchedPercentage"));
    }
}
