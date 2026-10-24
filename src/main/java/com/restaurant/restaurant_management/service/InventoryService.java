package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.Ingredient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InventoryService {

    private final List<Ingredient> ingredients = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(7);

    public InventoryService() {
        // Khởi tạo 6 nguyên liệu mẫu khớp seed_data.sql
        ingredients.add(new Ingredient(1L, "ING-BEEF-WAGYU", "Thịt bò Úc / Wagyu", "kg", new BigDecimal("25.500"), new BigDecimal("5.000"), new BigDecimal("450000")));
        ingredients.add(new Ingredient(2L, "ING-SEABASS", "Cá chẽm tươi phi lê", "kg", new BigDecimal("18.000"), new BigDecimal("4.000"), new BigDecimal("220000")));
        ingredients.add(new Ingredient(3L, "ING-CHICKEN", "Thịt gà ta thả vườn", "kg", new BigDecimal("30.000"), new BigDecimal("6.000"), new BigDecimal("110000")));
        ingredients.add(new Ingredient(4L, "ING-PRAWN", "Tôm sú tươi", "kg", new BigDecimal("3.200"), new BigDecimal("4.000"), new BigDecimal("280000"))); // Low stock để test cảnh báo!
        ingredients.add(new Ingredient(5L, "ING-COCONUT-MILK", "Nước cốt dừa nguyên chất", "lít", new BigDecimal("15.000"), new BigDecimal("5.000"), new BigDecimal("65000")));
        ingredients.add(new Ingredient(6L, "ING-RICE", "Gạo thơm ST25", "kg", new BigDecimal("80.000"), new BigDecimal("20.000"), new BigDecimal("35000")));
    }

    public synchronized List<Ingredient> getAllIngredients() {
        return new ArrayList<>(ingredients);
    }

    public synchronized Optional<Ingredient> findById(Long id) {
        if (id == null) return Optional.empty();
        return ingredients.stream().filter(i -> i.getId().equals(id)).findFirst();
    }

    public synchronized void updateStock(Long id, BigDecimal newQuantity) {
        findById(id).ifPresent(i -> i.setStockQuantity(newQuantity));
    }

    public synchronized boolean addIngredient(Ingredient ingredient) {
        boolean exists = ingredients.stream().anyMatch(i -> i.getCode().equalsIgnoreCase(ingredient.getCode().trim()));
        if (exists) return false;

        ingredient.setId(idGenerator.getAndIncrement());
        ingredients.add(ingredient);
        return true;
    }

    public synchronized long getLowStockCount() {
        return ingredients.stream().filter(Ingredient::isLowStock).count();
    }
}
