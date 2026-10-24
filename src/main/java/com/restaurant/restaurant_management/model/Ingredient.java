package com.restaurant.restaurant_management.model;

import java.math.BigDecimal;

public class Ingredient {
    private Long id;
    private String code;
    private String name;
    private String unit;
    private BigDecimal stockQuantity = BigDecimal.ZERO;
    private BigDecimal minStockLevel = new BigDecimal("5.0");
    private BigDecimal costPrice = BigDecimal.ZERO;

    public Ingredient() {
    }

    public Ingredient(Long id, String code, String name, String unit, BigDecimal stockQuantity, BigDecimal minStockLevel, BigDecimal costPrice) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
        this.minStockLevel = minStockLevel;
        this.costPrice = costPrice;
    }

    public boolean isLowStock() {
        if (stockQuantity == null || minStockLevel == null) return false;
        return stockQuantity.compareTo(minStockLevel) <= 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(BigDecimal stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(BigDecimal minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }
}
