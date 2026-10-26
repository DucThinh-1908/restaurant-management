package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final JdbcTemplate jdbcTemplate;
    private final List<MenuItem> inMemoryMenuItems = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(13);

    public MenuService(@Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Dự phòng bộ nhớ (Fallback)
        MenuItem item1 = new MenuItem(1L, "Fresh Spring Rolls", "Herbs, seasonal greens, rice noodles and dipping sauce.", "95k", new BigDecimal("95000"), "Starters · Khai vị", true, true);
        item1.setImageUrl("/images/dishes/fresh-spring-rolls.jpg");
        inMemoryMenuItems.add(item1);

        MenuItem item2 = new MenuItem(2L, "Crispy Lotus Root", "Lightly crisp lotus root with a bright, savoury dressing.", "110k", new BigDecimal("110000"), "Starters · Khai vị", true, false);
        item2.setImageUrl("/images/dishes/crispy-lotus-root.jpg");
        inMemoryMenuItems.add(item2);

        MenuItem item3 = new MenuItem(3L, "Pomelo & Prawn Salad", "Pomelo, fresh herbs, prawns and toasted peanuts.", "145k", new BigDecimal("145000"), "Starters · Khai vị", true, true);
        item3.setImageUrl("/images/dishes/pomelo-prawn-salad.jpg");
        inMemoryMenuItems.add(item3);

        MenuItem item4 = new MenuItem(4L, "Grilled Eggplant", "Smoky eggplant, spring onion oil and roasted sesame.", "105k", new BigDecimal("105000"), "Starters · Khai vị", true, false);
        item4.setImageUrl("/images/dishes/grilled-eggplant.jpg");
        inMemoryMenuItems.add(item4);

        MenuItem item5 = new MenuItem(5L, "Charcoal-Grilled Chicken", "Vietnamese spices, fragrant rice and seasonal vegetables.", "245k", new BigDecimal("245000"), "Main courses · Món chính", true, true);
        item5.setImageUrl("/images/dishes/charcoal-grilled-chicken.jpg");
        inMemoryMenuItems.add(item5);

        MenuItem item6 = new MenuItem(6L, "Seared Sea Bass", "Fresh fish, ginger broth and green vegetables.", "320k", new BigDecimal("320000"), "Main courses · Món chính", true, true);
        item6.setImageUrl("/images/dishes/seared-sea-bass.jpg");
        inMemoryMenuItems.add(item6);

        MenuItem item7 = new MenuItem(7L, "Slow-Braised Beef", "Tender beef, aromatic herbs and roasted root vegetables.", "350k", new BigDecimal("350000"), "Main courses · Món chính", true, true);
        item7.setImageUrl("/images/dishes/slow-braised-beef.jpg");
        inMemoryMenuItems.add(item7);

        MenuItem item8 = new MenuItem(8L, "Seasonal Vegetarian Bowl", "Market vegetables, mushrooms, rice and house dressing.", "195k", new BigDecimal("195000"), "Main courses · Món chính", true, false);
        item8.setImageUrl("/images/dishes/seasonal-vegetarian-bowl.jpg");
        inMemoryMenuItems.add(item8);

        MenuItem item9 = new MenuItem(9L, "Coconut Panna Cotta", "Coconut cream, tropical fruit and toasted coconut.", "95k", new BigDecimal("95000"), "Desserts · Tráng miệng", true, true);
        item9.setImageUrl("/images/dishes/coconut-panna-cotta.jpg");
        inMemoryMenuItems.add(item9);

        MenuItem item10 = new MenuItem(10L, "Seasonal Fruit Plate", "A selection of fresh fruit from the season.", "85k", new BigDecimal("85000"), "Desserts · Tráng miệng", true, false);
        item10.setImageUrl("/images/dishes/seasonal-fruit-plate.jpg");
        inMemoryMenuItems.add(item10);

        MenuItem item11 = new MenuItem(11L, "Vietnamese Coffee Crème", "A silky dessert with the aroma of Vietnamese coffee.", "105k", new BigDecimal("105000"), "Desserts · Tráng miệng", true, false);
        item11.setImageUrl("/images/dishes/vietnamese-coffee-creme.jpg");
        inMemoryMenuItems.add(item11);

        MenuItem item12 = new MenuItem(12L, "Tea Selection", "Ask our team about today's Vietnamese tea selection.", "75k", new BigDecimal("75000"), "Desserts · Tráng miệng", true, false);
        item12.setImageUrl("/images/dishes/tea-selection.jpg");
        inMemoryMenuItems.add(item12);
    }

    private MenuItem mapRowToMenuItem(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));

        BigDecimal priceVal = rs.getBigDecimal("price");
        item.setPriceValue(priceVal != null ? priceVal : BigDecimal.ZERO);
        if (priceVal != null) {
            item.setPrice(priceVal.divide(new BigDecimal(1000)).intValue() + "k");
        } else {
            item.setPrice("0k");
        }

        item.setCategory(rs.getString("category_name"));
        item.setImageUrl(rs.getString("image_url"));
        item.setAvailable(rs.getBoolean("is_available"));
        item.setFeatured(rs.getBoolean("is_featured"));
        return item;
    }

    public synchronized List<MenuItem> getAllMenuItems() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT m.id, m.name, m.description, m.price, m.image_url, m.is_available, m.is_featured, " +
                             "COALESCE(c.name, 'Chung') AS category_name " +
                             "FROM menu_items m " +
                             "LEFT JOIN categories c ON m.category_id = c.id " +
                             "ORDER BY m.id ASC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToMenuItem(rs));
            } catch (Exception e) {
                System.err.println("Lỗi getAllMenuItems từ MySQL: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryMenuItems);
    }

    public synchronized Optional<MenuItem> findById(Long id) {
        if (id == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT m.id, m.name, m.description, m.price, m.image_url, m.is_available, m.is_featured, " +
                             "COALESCE(c.name, 'Chung') AS category_name " +
                             "FROM menu_items m " +
                             "LEFT JOIN categories c ON m.category_id = c.id " +
                             "WHERE m.id = ?";
                List<MenuItem> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToMenuItem(rs), id);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findById menu item từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryMenuItems.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public synchronized Map<String, List<MenuItem>> getMenuItemsGroupedByCategory() {
        List<MenuItem> items = getAllMenuItems();
        return items.stream().collect(Collectors.groupingBy(
                MenuItem::getCategory,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    public synchronized void addMenuItem(MenuItem item) {
        if (item == null) return;
        if (item.getPrice() == null || item.getPrice().isBlank()) {
            item.setPrice(item.getPriceValue() != null ? (item.getPriceValue().divide(new BigDecimal(1000)).intValue() + "k") : "0k");
        }

        if (jdbcTemplate != null) {
            try {
                String catSql = "SELECT id FROM categories WHERE name = ?";
                List<Integer> catIds = jdbcTemplate.query(catSql, (rs, rowNum) -> rs.getInt("id"), item.getCategory());
                int catId = catIds.isEmpty() ? 1 : catIds.get(0);

                String slug = item.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
                String insertSql = "INSERT INTO menu_items (category_id, name, slug, description, price, image_url, is_available, is_featured) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, catId, item.getName(), slug, item.getDescription(),
                        item.getPriceValue(), item.getImageUrl(), item.isAvailable() ? 1 : 0, item.isFeatured() ? 1 : 0);
                return;
            } catch (Exception e) {
                System.err.println("Lỗi addMenuItem vào MySQL: " + e.getMessage());
            }
        }

        item.setId(idGenerator.getAndIncrement());
        inMemoryMenuItems.add(item);
    }

    public synchronized void updateMenuItem(MenuItem updated) {
        if (updated == null || updated.getId() == null) return;
        if (jdbcTemplate != null) {
            try {
                String catSql = "SELECT id FROM categories WHERE name = ?";
                List<Integer> catIds = jdbcTemplate.query(catSql, (rs, rowNum) -> rs.getInt("id"), updated.getCategory());
                Integer catId = catIds.isEmpty() ? null : catIds.get(0);

                String updateSql = "UPDATE menu_items SET name = ?, description = ?, price = ?, " +
                                   "category_id = COALESCE(?, category_id), image_url = COALESCE(?, image_url), " +
                                   "is_available = ?, is_featured = ? WHERE id = ?";
                jdbcTemplate.update(updateSql, updated.getName(), updated.getDescription(), updated.getPriceValue(),
                        catId, updated.getImageUrl(), updated.isAvailable() ? 1 : 0, updated.isFeatured() ? 1 : 0, updated.getId());
            } catch (Exception e) {
                System.err.println("Lỗi updateMenuItem trong MySQL: " + e.getMessage());
            }
        }

        findById(updated.getId()).ifPresent(m -> {
            m.setName(updated.getName());
            m.setCategory(updated.getCategory());
            m.setDescription(updated.getDescription());
            m.setPriceValue(updated.getPriceValue());
            m.setPrice(updated.getPriceValue() != null ? (updated.getPriceValue().divide(new BigDecimal(1000)).intValue() + "k") : updated.getPrice());
            if (updated.getImageUrl() != null && !updated.getImageUrl().isBlank()) {
                m.setImageUrl(updated.getImageUrl());
            }
            m.setAvailable(updated.isAvailable());
            m.setFeatured(updated.isFeatured());
        });
    }

    public synchronized void deleteMenuItem(Long id) {
        if (id == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("DELETE FROM menu_items WHERE id = ?", id);
            } catch (Exception e) {
                System.err.println("Lỗi deleteMenuItem trong MySQL: " + e.getMessage());
            }
        }
        inMemoryMenuItems.removeIf(m -> m.getId().equals(id));
    }

    public synchronized void toggleAvailability(Long id) {
        if (id == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE menu_items SET is_available = NOT is_available WHERE id = ?", id);
            } catch (Exception e) {
                System.err.println("Lỗi toggleAvailability trong MySQL: " + e.getMessage());
            }
        }
        findById(id).ifPresent(m -> m.setAvailable(!m.isAvailable()));
    }

    public synchronized void toggleFeatured(Long id) {
        if (id == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE menu_items SET is_featured = NOT is_featured WHERE id = ?", id);
            } catch (Exception e) {
                System.err.println("Lỗi toggleFeatured trong MySQL: " + e.getMessage());
            }
        }
        findById(id).ifPresent(m -> m.setFeatured(!m.isFeatured()));
    }
}
