package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.model.Ingredient;
import com.restaurant.restaurant_management.service.InventoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String listInventory(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";
        if (!"STOREKEEPER".equalsIgnoreCase(emp.getRoleName()) && !"ADMIN".equalsIgnoreCase(emp.getRoleName()) && !"MANAGER".equalsIgnoreCase(emp.getRoleName())) {
            return "redirect:/admin/kitchen";
        }

        model.addAttribute("activeAdminPage", "inventory");
        model.addAttribute("pageTitle", "Quản Lý Kho Nguyên Liệu | GoD Admin");
        model.addAttribute("employee", emp);
        model.addAttribute("ingredients", inventoryService.getAllIngredients());
        model.addAttribute("lowStockCount", inventoryService.getLowStockCount());
        return "admin/inventory";
    }

    @PostMapping("/update")
    public String updateStock(@RequestParam("id") Long id,
                              @RequestParam("stockQuantity") BigDecimal stockQuantity,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        inventoryService.updateStock(id, stockQuantity);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng tồn kho thành công!");
        return "redirect:/admin/inventory";
    }

    @PostMapping("/add")
    public String addIngredient(@RequestParam("code") String code,
                                @RequestParam("name") String name,
                                @RequestParam("unit") String unit,
                                @RequestParam("stockQuantity") BigDecimal stockQuantity,
                                @RequestParam("minStockLevel") BigDecimal minStockLevel,
                                @RequestParam("costPrice") BigDecimal costPrice,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        Ingredient ingredient = new Ingredient(null, code, name, unit, stockQuantity, minStockLevel, costPrice);
        boolean success = inventoryService.addIngredient(ingredient);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm nguyên liệu: " + name + "!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã nguyên liệu " + code + " đã tồn tại!");
        }
        return "redirect:/admin/inventory";
    }
}
