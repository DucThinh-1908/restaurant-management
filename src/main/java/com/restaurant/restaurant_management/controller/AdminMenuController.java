package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.model.MenuItem;
import com.restaurant.restaurant_management.service.MenuService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/menu")
public class AdminMenuController {

    private final MenuService menuService;

    public AdminMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public String listMenu(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";
        if (!"ADMIN".equalsIgnoreCase(emp.getRoleName()) && !"MANAGER".equalsIgnoreCase(emp.getRoleName())) {
            return "redirect:/admin/kitchen";
        }

        model.addAttribute("activeAdminPage", "menu");
        model.addAttribute("pageTitle", "Quản Lý Thực Đơn | GoD Admin");
        model.addAttribute("employee", emp);
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        return "admin/menu";
    }

    @PostMapping("/add")
    public String addMenuItem(@RequestParam("name") String name,
                              @RequestParam("category") String category,
                              @RequestParam("priceValue") BigDecimal priceValue,
                              @RequestParam("description") String description,
                              @RequestParam(value = "imageUrl", required = false) String imageUrl,
                              @RequestParam(value = "featured", defaultValue = "false") boolean featured,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        MenuItem item = new MenuItem();
        item.setName(name);
        item.setCategory(category);
        item.setPriceValue(priceValue);
        item.setDescription(description);
        item.setImageUrl(imageUrl != null && !imageUrl.isBlank() ? imageUrl : "/images/restaurant-bg.png");
        item.setAvailable(true);
        item.setFeatured(featured);

        menuService.addMenuItem(item);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm món mới: " + name + " thành công!");
        return "redirect:/admin/menu";
    }

    @PostMapping("/edit")
    public String editMenuItem(@RequestParam("id") Long id,
                               @RequestParam("name") String name,
                               @RequestParam("category") String category,
                               @RequestParam("priceValue") BigDecimal priceValue,
                               @RequestParam("description") String description,
                               @RequestParam(value = "imageUrl", required = false) String imageUrl,
                               @RequestParam(value = "available", defaultValue = "false") boolean available,
                               @RequestParam(value = "featured", defaultValue = "false") boolean featured,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        MenuItem item = new MenuItem();
        item.setId(id);
        item.setName(name);
        item.setCategory(category);
        item.setPriceValue(priceValue);
        item.setDescription(description);
        item.setImageUrl(imageUrl);
        item.setAvailable(available);
        item.setFeatured(featured);

        menuService.updateMenuItem(item);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật món " + name + " thành công!");
        return "redirect:/admin/menu";
    }

    @PostMapping("/toggle-available")
    public String toggleAvailable(@RequestParam("id") Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";
        menuService.toggleAvailability(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái còn món/hết món!");
        return "redirect:/admin/menu";
    }

    @PostMapping("/toggle-featured")
    public String toggleFeatured(@RequestParam("id") Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";
        menuService.toggleFeatured(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái món nổi bật!");
        return "redirect:/admin/menu";
    }

    @PostMapping("/delete")
    public String deleteMenuItem(@RequestParam("id") Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";
        menuService.deleteMenuItem(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món ăn khỏi thực đơn!");
        return "redirect:/admin/menu";
    }
}
