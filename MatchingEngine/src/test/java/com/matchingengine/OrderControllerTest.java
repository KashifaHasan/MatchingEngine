package com.matchingengine;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.matchingengine.controller.OrderController;
import com.matchingengine.model.Order;
import com.matchingengine.service.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void testAddOrder_Valid() throws Exception {
        String requestBody = "{ \"currencyPair\": \"EURUSD\", \"dealtCurrency\": \"USD\", \"direction\": \"BUY\", \"amount\": 10000, \"valueDate\": \"2025-01-30\", \"userId\": \"UserA\" }";

        when(orderService.addOrder(new Order("EURUSD", "USD", "BUY", new BigDecimal("10000"), LocalDate.of(2025, 1, 30), "UserA")))
                .thenReturn(new Order("EURUSD", "USD", "BUY", new BigDecimal("10000"), LocalDate.of(2025, 1, 30), "UserA"));

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void testAddOrder_InvalidDirection() throws Exception {
        String requestBody = "{ \"currencyPair\": \"EURUSD\", \"dealtCurrency\": \"USD\", \"direction\": \"INVALID\", \"amount\": 10000, \"valueDate\": \"2025-01-30\", \"userId\": \"UserA\" }";

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.direction").value("Direction must be BUY or SELL"));
    }

    @Test
    void testAddOrder_MissingFields() throws Exception {
        String requestBody = "{ \"currencyPair\": \"EURUSD\", \"dealtCurrency\": \"USD\", \"amount\": 10000, \"valueDate\": \"2025-01-30\" }";

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.direction").value("Direction is required"))
                .andExpect(jsonPath("$.userId").value("User ID is required"));
    }

    @Test
    void testGetMatchedAmount_ValidUser() throws Exception {
        when(orderService.getMatchedAmount("UserA")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/orders/UserA/matches"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMatchedAmount_NonExistingUser() throws Exception {
        when(orderService.getMatchedAmount("UnknownUser")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/orders/UnknownUser/matches"))
                .andExpect(status().isOk());
    }
}
