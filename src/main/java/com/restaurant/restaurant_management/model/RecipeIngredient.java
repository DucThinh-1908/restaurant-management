package com.restaurant.restaurant_management.model;

public class RecipeIngredient {
    private String name;
    private String quantity;
    private String note;

    public RecipeIngredient() {
    }

    public RecipeIngredient(String name, String quantity, String note) {
        this.name = name;
        this.quantity = quantity;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
