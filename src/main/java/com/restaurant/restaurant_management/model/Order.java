package com.restaurant.restaurant_management.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long id;
    private String orderCode;
    private String orderType = "DELIVERY"; // DINE_IN, TAKE_AWAY, DELIVERY
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String note;
    private String status = "PENDING"; // PENDING, SERVING, COMPLETED, CANCELLED
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal taxPercent = new BigDecimal("8.00");
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String paymentMethod = "CASH"; // CASH, BANK_TRANSFER
    private LocalDateTime createdAt = LocalDateTime.now();
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
    }

    public String getStatusBadgeClass() {
        if ("COMPLETED".equalsIgnoreCase(status)) return "badge-success";
        if ("SERVING".equalsIgnoreCase(status)) return "badge-warning";
        if ("CANCELLED".equalsIgnoreCase(status)) return "badge-danger";
        return "badge-info";
    }

    public String getStatusDisplayName() {
        if ("PENDING".equalsIgnoreCase(status)) return "Chờ xác nhận";
        if ("SERVING".equalsIgnoreCase(status)) return "Đang xử lý / Bếp nấu";
        if ("COMPLETED".equalsIgnoreCase(status)) return "Hoàn thành";
        if ("CANCELLED".equalsIgnoreCase(status)) return "Đã hủy";
        return status;
    }

    public String getOrderTypeDisplayName() {
        if ("DELIVERY".equalsIgnoreCase(orderType)) return "Giao hàng tận nơi";
        if ("TAKE_AWAY".equalsIgnoreCase(orderType)) return "Đặt mang về";
        if ("DINE_IN".equalsIgnoreCase(orderType)) return "Dùng tại bàn";
        return orderType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(BigDecimal taxPercent) {
        this.taxPercent = taxPercent;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
