package com.restaurant.restaurant_management.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InventoryReceiptItem {
    private Long id;
    private Long receiptId;
    private String receiptCode;
    private String supplierName;
    private LocalDateTime receivedAt;
    private Long ingredientId;
    private String ingredientName;
    private String ingredientCode;
    private String unit;
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private LocalDate expiryDate;

    public InventoryReceiptItem() {
    }

    public InventoryReceiptItem(Long id, Long receiptId, Long ingredientId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice, LocalDate expiryDate) {
        this.id = id;
        this.receiptId = receiptId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.expiryDate = expiryDate;
    }

    public String getFormattedUnitPrice() {
        if (unitPrice == null) return "0 đ";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(unitPrice) + " đ";
    }

    public String getFormattedTotalPrice() {
        if (totalPrice == null) return "0 đ";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(totalPrice) + " đ";
    }

    public String getFormattedQuantity() {
        if (quantity == null) return "0";
        DecimalFormat df = new DecimalFormat("#,##0.###");
        return df.format(quantity);
    }

    public String getFormattedExpiryDate() {
        if (expiryDate == null) return "Không có";
        return expiryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedReceivedAt() {
        if (receivedAt == null) return "";
        return receivedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getIngredientCode() {
        return ingredientCode;
    }

    public void setIngredientCode(String ingredientCode) {
        this.ingredientCode = ingredientCode;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
