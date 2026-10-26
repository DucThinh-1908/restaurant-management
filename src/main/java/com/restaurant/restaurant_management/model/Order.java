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
    private Long tableId;
    private String tableNumber; // Ví dụ: T1-01, T1-03, VIP-01 (khi là DINE_IN)
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String note;
    private String status = "PENDING"; // PENDING, COOKING, SERVING, COMPLETED, CANCELLED
    private boolean billRequested = false;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal taxPercent = new BigDecimal("8.00");
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String paymentMethod = "CASH"; // CASH, BANK_TRANSFER, CREDIT_CARD, E_WALLET
    private String transactionCode;
    private String cashierName;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
    }

    public String getFormattedTotalAmount() {
        if (totalAmount == null) return "0 đ";
        return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalAmount) + " đ";
    }

    public String getFormattedSubtotal() {
        if (subtotal == null) return "0 đ";
        return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(subtotal) + " đ";
    }

    public long getMinutesWaiting() {
        if (createdAt == null) return 0;
        long minutes = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
        return Math.max(0, minutes);
    }

    public String getStatusBadgeClass() {
        if ("COMPLETED".equalsIgnoreCase(status)) return "badge-success";
        if ("COOKING".equalsIgnoreCase(status)) return "badge-warning";
        if ("READY".equalsIgnoreCase(status)) return "badge-info";
        if ("SERVING".equalsIgnoreCase(status)) return "badge-primary";
        if ("CANCELLED".equalsIgnoreCase(status)) return "badge-danger";
        return "badge-pending";
    }

    public String getStatusDisplayName() {
        if ("PENDING".equalsIgnoreCase(status)) return "Chờ chế biến";
        if ("COOKING".equalsIgnoreCase(status)) return "Đang nấu trên bếp";
        if ("READY".equalsIgnoreCase(status)) return "Đã nấu xong (Chờ bưng)";
        if ("SERVING".equalsIgnoreCase(status)) return "Khách đang dùng bữa";
        if ("COMPLETED".equalsIgnoreCase(status)) return "Đã hoàn thành";
        if ("CANCELLED".equalsIgnoreCase(status)) return "Đã hủy";
        return status;
    }

    public String getOrderTypeDisplayName() {
        if ("DELIVERY".equalsIgnoreCase(orderType)) return "Giao hàng tận nơi";
        if ("TAKE_AWAY".equalsIgnoreCase(orderType)) return "Đặt mang về";
        if ("DINE_IN".equalsIgnoreCase(orderType)) {
            return tableNumber != null && !tableNumber.isBlank() ? "Bàn " + tableNumber : "Dùng tại bàn";
        }
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

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getFormattedCompletedAt() {
        if (completedAt == null) return "";
        return completedAt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getPaymentMethodDisplayName() {
        if ("CASH".equalsIgnoreCase(paymentMethod)) return "Tiền mặt";
        if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) return "Chuyển khoản QR";
        if ("CREDIT_CARD".equalsIgnoreCase(paymentMethod)) return "Quẹt thẻ POS";
        if ("E_WALLET".equalsIgnoreCase(paymentMethod)) return "Ví điện tử";
        return paymentMethod != null ? paymentMethod : "Tiền mặt";
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public boolean isBillRequested() {
        return billRequested;
    }

    public void setBillRequested(boolean billRequested) {
        this.billRequested = billRequested;
    }
}
