package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.MenuItem;
import com.restaurant.restaurant_management.service.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menu")
    public String menu(Model model) {
        model.addAttribute("activePage", "menu");
        model.addAttribute("pageTitle", "Thực đơn món ăn | GoD Fine Dining");

        // Nhóm món theo danh mục và giữ thứ tự chuẩn: Khai vị, Món chính, Tráng miệng
        List<MenuItem> allItems = menuService.getAllMenuItems();
        Map<String, List<MenuItem>> groupedMenu = new LinkedHashMap<>();
        groupedMenu.put("Starters · Khai vị", allItems.stream().filter(m -> m.getCategory().contains("Khai vị")).collect(Collectors.toList()));
        groupedMenu.put("Main courses · Món chính", allItems.stream().filter(m -> m.getCategory().contains("Món chính")).collect(Collectors.toList()));
        groupedMenu.put("Desserts · Tráng miệng", allItems.stream().filter(m -> m.getCategory().contains("Tráng miệng")).collect(Collectors.toList()));

        model.addAttribute("groupedMenu", groupedMenu);
        return "menu";
    }
}
