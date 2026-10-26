package com.restaurant.restaurant_management.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InventoryReceipt {
    private Long id;
    private String receiptCode;
    private Long supplierId;
    private String supplierName;
    private String status = "RECEIVED";
    private Long createdByEmployeeId;
    private String createdByName;
    private Long receivedByEmployeeId;
    private String receivedByName;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime receivedAt;
    private List<InventoryReceiptItem> items = new ArrayList<>();

    public InventoryReceipt() {
    }

    public String getFormattedTotalCost() {
        if (totalCost == null) return "0 đ";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(totalCost) + " đ";
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedByEmployeeId() {
        return createdByEmployeeId;
    }

    public void setCreatedByEmployeeId(Long createdByEmployeeId) {
        this.createdByEmployeeId = createdByEmployeeId;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Long getReceivedByEmployeeId() {
        return receivedByEmployeeId;
    }

    public void setReceivedByEmployeeId(Long receivedByEmployeeId) {
        this.receivedByEmployeeId = receivedByEmployeeId;
    }

    public String getReceivedByName() {
        return receivedByName;
    }

    public void setReceivedByName(String receivedByName) {
        this.receivedByName = receivedByName;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public List<InventoryReceiptItem> getItems() {
        return items;
    }

    public void setItems(List<InventoryReceiptItem> items) {
        this.items = items;
    }
}
