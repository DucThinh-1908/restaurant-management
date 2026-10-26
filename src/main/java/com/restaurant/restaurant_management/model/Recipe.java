package com.restaurant.restaurant_management.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private Long menuItemId;
    private String dishName;
    private String category;
    private String imageUrl;
    private String prepTime;      // e.g. "15 phút"
    private String cookTime;      // e.g. "25 phút"
    private String allergens;     // e.g. "Đậu phộng, Hải sản"
    private String platingStyle;  // e.g. "Đĩa tròn gốm mộc, rắc lá chanh thái sợi"
    private String chefNote;      // e.g. "Lưu ý nhiệt độ áp chảo da cá phải đạt 190°C để da giòn"
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    private List<String> steps = new ArrayList<>();

    public Recipe() {
    }

    public Recipe(Long menuItemId, String dishName, String category, String imageUrl,
                  String prepTime, String cookTime, String allergens, String platingStyle,
                  String chefNote, List<RecipeIngredient> ingredients, List<String> steps) {
        this.menuItemId = menuItemId;
        this.dishName = dishName;
        this.category = category;
        this.imageUrl = imageUrl;
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.allergens = allergens;
        this.platingStyle = platingStyle;
        this.chefNote = chefNote;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
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

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getCookTime() {
        return cookTime;
    }

    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    public String getAllergens() {
        return allergens;
    }

    public void setAllergens(String allergens) {
        this.allergens = allergens;
    }

    public String getPlatingStyle() {
        return platingStyle;
    }

    public void setPlatingStyle(String platingStyle) {
        this.platingStyle = platingStyle;
    }

    public String getChefNote() {
        return chefNote;
    }

    public void setChefNote(String chefNote) {
        this.chefNote = chefNote;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
}
