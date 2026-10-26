package com.restaurant.restaurant_management.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InventoryTransaction {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String ingredientCode;
    private String unit;
    private String transactionType; // IMPORT, EXPORT_KITCHEN, RECIPE_CONSUMPTION, ADJUSTMENT, SPOILAGE
    private BigDecimal quantity = BigDecimal.ZERO;
    private BigDecimal balanceAfter = BigDecimal.ZERO;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private String referenceType;
    private Long referenceId;
    private Long employeeId;
    private String employeeName;
    private String note;
    private LocalDateTime createdAt;

    public InventoryTransaction() {
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFormattedUnitPrice() {
        if (unitPrice == null) return "0 đ";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(unitPrice) + " đ";
    }

    public String getTransactionTypeName() {
        if (transactionType == null) return "";
        switch (transactionType) {
            case "IMPORT": return "Nhập kho";
            case "EXPORT_KITCHEN": return "Xuất cho Bếp";
            case "RECIPE_CONSUMPTION": return "Trừ theo món gọi";
            case "ADJUSTMENT": return "Kiểm kê điều chỉnh";
            case "SPOILAGE": return "Báo hỏng / Hao hụt";
            default: return transactionType;
        }
    }

    public String getTransactionBadgeClass() {
        if (transactionType == null) return "status-pending";
        switch (transactionType) {
            case "IMPORT": return "status-completed"; // green
            case "EXPORT_KITCHEN": return "status-processing"; // blue
            case "SPOILAGE": return "status-cancelled"; // red
            case "ADJUSTMENT": return "status-pending"; // yellow/orange
            default: return "status-pending";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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
}
