package com.restaurant.restaurant_management.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {
    private Long id;
    private Long orderId;
    private String orderCode;
    private String tableNumber;
    private Long cashierId;
    private String cashierName;
    private BigDecimal amount = BigDecimal.ZERO;
    private String paymentMethod = "CASH"; // CASH, CREDIT_CARD, BANK_TRANSFER, E_WALLET, ONLINE_GATEWAY
    private String transactionCode;
    private LocalDateTime paidAt = LocalDateTime.now();

    public Payment() {
    }

    public Payment(Long id, Long orderId, String orderCode, String tableNumber, Long cashierId, String cashierName,
                   BigDecimal amount, String paymentMethod, String transactionCode, LocalDateTime paidAt) {
        this.id = id;
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.tableNumber = tableNumber;
        this.cashierId = cashierId;
        this.cashierName = cashierName;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionCode = transactionCode;
        this.paidAt = paidAt;
    }

    public String getFormattedPaidAt() {
        if (paidAt == null) return "";
        return paidAt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
    }

    public String getPaymentMethodDisplayName() {
        if ("CASH".equalsIgnoreCase(paymentMethod)) return "Tiền mặt";
        if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) return "Chuyển khoản QR";
        if ("CREDIT_CARD".equalsIgnoreCase(paymentMethod)) return "Quẹt thẻ POS";
        if ("E_WALLET".equalsIgnoreCase(paymentMethod)) return "Ví điện tử";
        if ("ONLINE_GATEWAY".equalsIgnoreCase(paymentMethod)) return "Cổng trực tuyến";
        return paymentMethod != null ? paymentMethod : "Khác";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Long getCashierId() {
        return cashierId;
    }

    public void setCashierId(Long cashierId) {
        this.cashierId = cashierId;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
