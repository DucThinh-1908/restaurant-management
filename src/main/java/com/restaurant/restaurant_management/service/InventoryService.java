package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.Ingredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InventoryService {

    private final JdbcTemplate jdbcTemplate;
    private final List<Ingredient> inMemoryIngredients = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(7);

    public InventoryService(@Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Dự phòng bộ nhớ (Fallback)
        inMemoryIngredients.add(new Ingredient(1L, "ING-BEEF-WAGYU", "Thịt bò Úc / Wagyu", "kg", new BigDecimal("25.500"), new BigDecimal("5.000"), new BigDecimal("450000")));
        inMemoryIngredients.add(new Ingredient(2L, "ING-SEABASS", "Cá chẽm tươi phi lê", "kg", new BigDecimal("18.000"), new BigDecimal("4.000"), new BigDecimal("220000")));
        inMemoryIngredients.add(new Ingredient(3L, "ING-CHICKEN", "Thịt gà ta thả vườn", "kg", new BigDecimal("30.000"), new BigDecimal("6.000"), new BigDecimal("110000")));
        inMemoryIngredients.add(new Ingredient(4L, "ING-PRAWN", "Tôm sú tươi", "kg", new BigDecimal("3.200"), new BigDecimal("4.000"), new BigDecimal("280000")));
        inMemoryIngredients.add(new Ingredient(5L, "ING-COCONUT-MILK", "Nước cốt dừa nguyên chất", "lít", new BigDecimal("15.000"), new BigDecimal("5.000"), new BigDecimal("65000")));
        inMemoryIngredients.add(new Ingredient(6L, "ING-RICE", "Gạo thơm ST25", "kg", new BigDecimal("80.000"), new BigDecimal("20.000"), new BigDecimal("35000")));
    }

    private Ingredient mapRowToIngredient(ResultSet rs) throws SQLException {
        Ingredient ing = new Ingredient();
        ing.setId(rs.getLong("id"));
        ing.setCode(rs.getString("code"));
        ing.setName(rs.getString("name"));
        ing.setUnit(rs.getString("unit"));
        ing.setStockQuantity(rs.getBigDecimal("stock_quantity"));
        ing.setMinStockLevel(rs.getBigDecimal("min_stock_level"));
        ing.setCostPrice(rs.getBigDecimal("cost_price"));
        return ing;
    }

    public synchronized List<Ingredient> getAllIngredients() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, code, name, unit, stock_quantity, min_stock_level, cost_price FROM ingredients ORDER BY id ASC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToIngredient(rs));
            } catch (Exception e) {
                System.err.println("Lỗi getAllIngredients từ MySQL: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryIngredients);
    }

    public synchronized Optional<Ingredient> findById(Long id) {
        if (id == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, code, name, unit, stock_quantity, min_stock_level, cost_price FROM ingredients WHERE id = ?";
                List<Ingredient> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToIngredient(rs), id);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findById ingredient từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryIngredients.stream().filter(i -> i.getId().equals(id)).findFirst();
    }

    public synchronized void updateStock(Long id, BigDecimal newQuantity) {
        if (id == null || newQuantity == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE ingredients SET stock_quantity = ? WHERE id = ?", newQuantity, id);
            } catch (Exception e) {
                System.err.println("Lỗi updateStock trong MySQL: " + e.getMessage());
            }
        }
        findById(id).ifPresent(i -> i.setStockQuantity(newQuantity));
    }

    public synchronized boolean addIngredient(Ingredient ingredient) {
        if (ingredient == null || ingredient.getCode() == null) return false;

        if (jdbcTemplate != null) {
            try {
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ingredients WHERE code = ?", Integer.class, ingredient.getCode().trim());
                if (count != null && count > 0) return false;

                String insertSql = "INSERT INTO ingredients (code, name, unit, stock_quantity, min_stock_level, cost_price) " +
                                   "VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, ingredient.getCode().trim(), ingredient.getName(), ingredient.getUnit(),
                        ingredient.getStockQuantity(), ingredient.getMinStockLevel(), ingredient.getCostPrice());
                return true;
            } catch (Exception e) {
                System.err.println("Lỗi addIngredient vào MySQL: " + e.getMessage());
            }
        }

        boolean exists = inMemoryIngredients.stream().anyMatch(i -> i.getCode().equalsIgnoreCase(ingredient.getCode().trim()));
        if (exists) return false;

        ingredient.setId(idGenerator.getAndIncrement());
        inMemoryIngredients.add(ingredient);
        return true;
    }

    public synchronized long getLowStockCount() {
        if (jdbcTemplate != null) {
            try {
                Long count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM ingredients WHERE stock_quantity <= min_stock_level", Long.class);
                return count != null ? count : 0;
            } catch (Exception e) {
                System.err.println("Lỗi getLowStockCount từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryIngredients.stream().filter(Ingredient::isLowStock).count();
    }
}
