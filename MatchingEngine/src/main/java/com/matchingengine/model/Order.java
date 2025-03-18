package com.matchingengine.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {
    @NotBlank(message = "Currency pair is required")
    private String currencyPair;

    @NotBlank(message = "Dealt currency is required")
    private String dealtCurrency;

    @NotBlank(message = "Direction is required")
    @Pattern(regexp = "^(BUY|SELL)$", message = "Direction must be BUY or SELL")
    private String direction;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Value date is required")
    private LocalDate valueDate;

    @NotBlank(message = "User ID is required")
    private String userId;

    public Order(String currencyPair, String dealtCurrency, String direction, BigDecimal amount, LocalDate valueDate, String userId) {
        this.currencyPair = currencyPair;
        this.dealtCurrency = dealtCurrency;
        this.direction = direction;
        this.amount = amount;
        this.valueDate = valueDate;
        this.userId = userId;
    }

    public String getCurrencyPair() { return currencyPair; }
    public void setCurrencyPair(String currencyPair) { this.currencyPair = currencyPair; }

    public String getDealtCurrency() { return dealtCurrency; }
    public void setDealtCurrency(String dealtCurrency) { this.dealtCurrency = dealtCurrency; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getValueDate() { return valueDate; }
    public void setValueDate(LocalDate valueDate) { this.valueDate = valueDate; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
