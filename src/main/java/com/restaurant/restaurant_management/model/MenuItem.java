package com.restaurant.restaurant_management.model;

import java.math.BigDecimal;

public class MenuItem {
    private Long id;
    private String name;
    private String description;
    private String price; // Chuỗi hiển thị (ví dụ: "95k" hoặc "95,000 VND")
    private BigDecimal priceValue; // Giá trị số phục vụ tính toán
    private String category;
    private String imageUrl;
    private boolean available = true;
    private boolean featured = false;

    public MenuItem() {
    }

    public MenuItem(Long id, String name, String description, String price, BigDecimal priceValue, String category, boolean available, boolean featured) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.priceValue = priceValue;
        this.category = category;
        this.available = available;
        this.featured = featured;
    }

    public MenuItem(String name, String description, String price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.available = true;
        this.featured = false;
        // Parse tạm giá nếu có
        try {
            String num = price.replaceAll("[^0-9]", "");
            if (num.length() <= 3) {
                this.priceValue = new BigDecimal(num + "000");
            } else {
                this.priceValue = new BigDecimal(num);
            }
        } catch (Exception e) {
            this.priceValue = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public BigDecimal getPriceValue() {
        return priceValue != null ? priceValue : BigDecimal.ZERO;
    }

    public void setPriceValue(BigDecimal priceValue) {
        this.priceValue = priceValue;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}
