package com.restaurant.restaurant_management.model;

import java.math.BigDecimal;

public class CartItem {
    private Long id;
    private Long customerId;
    private MenuItem menuItem;
    private int quantity = 1;
    private String note;

    public CartItem() {
    }

    public CartItem(Long id, Long customerId, MenuItem menuItem, int quantity, String note) {
        this.id = id;
        this.customerId = customerId;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.note = note;
    }

    public BigDecimal getSubtotal() {
        if (menuItem == null || menuItem.getPriceValue() == null) {
            return BigDecimal.ZERO;
        }
        return menuItem.getPriceValue().multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
