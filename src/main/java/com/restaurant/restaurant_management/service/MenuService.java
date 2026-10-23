package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.MenuItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final List<MenuItem> menuItems = new ArrayList<>();

    public MenuService() {
        // Starters · Khai vị
        MenuItem item1 = new MenuItem(1L, "Fresh Spring Rolls", "Herbs, seasonal greens, rice noodles and dipping sauce.", "95k", new BigDecimal("95000"), "Starters · Khai vị", true, true);
        item1.setImageUrl("/images/dishes/fresh-spring-rolls.jpg");
        menuItems.add(item1);

        MenuItem item2 = new MenuItem(2L, "Crispy Lotus Root", "Lightly crisp lotus root with a bright, savoury dressing.", "110k", new BigDecimal("110000"), "Starters · Khai vị", true, false);
        item2.setImageUrl("/images/dishes/crispy-lotus-root.jpg");
        menuItems.add(item2);

        MenuItem item3 = new MenuItem(3L, "Pomelo & Prawn Salad", "Pomelo, fresh herbs, prawns and toasted peanuts.", "145k", new BigDecimal("145000"), "Starters · Khai vị", true, true);
        item3.setImageUrl("/images/dishes/pomelo-prawn-salad.jpg");
        menuItems.add(item3);

        MenuItem item4 = new MenuItem(4L, "Grilled Eggplant", "Smoky eggplant, spring onion oil and roasted sesame.", "105k", new BigDecimal("105000"), "Starters · Khai vị", true, false);
        item4.setImageUrl("/images/dishes/grilled-eggplant.jpg");
        menuItems.add(item4);

        // Main courses · Món chính
        MenuItem item5 = new MenuItem(5L, "Charcoal-Grilled Chicken", "Vietnamese spices, fragrant rice and seasonal vegetables.", "245k", new BigDecimal("245000"), "Main courses · Món chính", true, true);
        item5.setImageUrl("/images/dishes/charcoal-grilled-chicken.jpg");
        menuItems.add(item5);

        MenuItem item6 = new MenuItem(6L, "Seared Sea Bass", "Fresh fish, ginger broth and green vegetables.", "320k", new BigDecimal("320000"), "Main courses · Món chính", true, true);
        item6.setImageUrl("/images/dishes/seared-sea-bass.jpg");
        menuItems.add(item6);

        MenuItem item7 = new MenuItem(7L, "Slow-Braised Beef", "Tender beef, aromatic herbs and roasted root vegetables.", "350k", new BigDecimal("350000"), "Main courses · Món chính", true, true);
        item7.setImageUrl("/images/dishes/slow-braised-beef.jpg");
        menuItems.add(item7);

        MenuItem item8 = new MenuItem(8L, "Seasonal Vegetarian Bowl", "Market vegetables, mushrooms, rice and house dressing.", "195k", new BigDecimal("195000"), "Main courses · Món chính", true, false);
        item8.setImageUrl("/images/dishes/seasonal-vegetarian-bowl.jpg");
        menuItems.add(item8);

        // Desserts · Tráng miệng
        MenuItem item9 = new MenuItem(9L, "Coconut Panna Cotta", "Coconut cream, tropical fruit and toasted coconut.", "95k", new BigDecimal("95000"), "Desserts · Tráng miệng", true, true);
        item9.setImageUrl("/images/dishes/coconut-panna-cotta.jpg");
        menuItems.add(item9);

        MenuItem item10 = new MenuItem(10L, "Seasonal Fruit Plate", "A selection of fresh fruit from the season.", "85k", new BigDecimal("85000"), "Desserts · Tráng miệng", true, false);
        item10.setImageUrl("/images/dishes/seasonal-fruit-plate.jpg");
        menuItems.add(item10);

        MenuItem item11 = new MenuItem(11L, "Vietnamese Coffee Crème", "A silky dessert with the aroma of Vietnamese coffee.", "105k", new BigDecimal("105000"), "Desserts · Tráng miệng", true, false);
        item11.setImageUrl("/images/dishes/vietnamese-coffee-creme.jpg");
        menuItems.add(item11);

        MenuItem item12 = new MenuItem(12L, "Tea Selection", "Ask our team about today's Vietnamese tea selection.", "75k", new BigDecimal("75000"), "Desserts · Tráng miệng", true, false);
        item12.setImageUrl("/images/dishes/tea-selection.jpg");
        menuItems.add(item12);
    }

    public List<MenuItem> getAllMenuItems() {
        return menuItems;
    }

    public Optional<MenuItem> findById(Long id) {
        if (id == null) return Optional.empty();
        return menuItems.stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public Map<String, List<MenuItem>> getMenuItemsGroupedByCategory() {
        return menuItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory));
    }
}
