package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.DiningTable;
import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.service.TableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tables")
public class AdminTableController {

    private final TableService tableService;

    public AdminTableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public String listTables(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        model.addAttribute("activeAdminPage", "tables");
        model.addAttribute("pageTitle", "Quản Lý Bàn Ăn | GoD Admin");
        model.addAttribute("employee", emp);
        model.addAttribute("tables", tableService.getAllTables());
        return "admin/tables";
    }

    @PostMapping("/add")
    public String addTable(@RequestParam("tableNumber") String tableNumber,
                           @RequestParam("area") String area,
                           @RequestParam("capacity") int capacity,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        DiningTable table = new DiningTable(null, tableNumber, area, capacity, "AVAILABLE");
        boolean success = tableService.addTable(table);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm bàn mới: " + tableNumber + "!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã bàn " + tableNumber + " đã tồn tại!");
        }
        return "redirect:/admin/tables";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("id") Long id,
                               @RequestParam("status") String status,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        tableService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái bàn ăn!");
        return "redirect:/admin/tables";
    }
}
