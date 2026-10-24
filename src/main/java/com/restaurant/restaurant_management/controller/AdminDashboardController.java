package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final OrderService orderService;
    private final ReservationService reservationService;
    private final MenuService menuService;
    private final TableService tableService;
    private final InventoryService inventoryService;

    public AdminDashboardController(OrderService orderService,
                                    ReservationService reservationService,
                                    MenuService menuService,
                                    TableService tableService,
                                    InventoryService inventoryService) {
        this.orderService = orderService;
        this.reservationService = reservationService;
        this.menuService = menuService;
        this.tableService = tableService;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) {
            return "redirect:/admin/login";
        }

        // Nếu là Lễ tân, Bếp, Thu ngân, Phục vụ, Thủ kho -> Chuyển thẳng về bàn làm việc chuyên môn
        String role = emp.getRoleName();
        if ("RECEPTIONIST".equalsIgnoreCase(role)) {
            return "redirect:/admin/reservations";
        } else if ("WAITER".equalsIgnoreCase(role)) {
            return "redirect:/admin/tables";
        } else if ("CHEF".equalsIgnoreCase(role) || "CASHIER".equalsIgnoreCase(role)) {
            return "redirect:/admin/orders";
        } else if ("STOREKEEPER".equalsIgnoreCase(role)) {
            return "redirect:/admin/inventory";
        }

        model.addAttribute("activeAdminPage", "dashboard");
        model.addAttribute("pageTitle", "Bảng Điều Khiển | GoD Admin");
        model.addAttribute("employee", emp);

        // Các chỉ số KPI (ADMIN & MANAGER)
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("pendingOrderCount", orderService.getPendingOrderCount());
        model.addAttribute("pendingReservationCount", reservationService.getPendingReservationCount());
        model.addAttribute("totalMenuItems", menuService.getAllMenuItems().size());
        model.addAttribute("totalTables", tableService.getAllTables().size());
        model.addAttribute("lowStockCount", inventoryService.getLowStockCount());

        // Danh sách hoạt động mới nhất
        model.addAttribute("recentOrders", orderService.getAllOrders().stream().limit(5).toList());
        model.addAttribute("recentReservations", reservationService.getAllReservations().stream().limit(5).toList());

        return "admin/dashboard";
    }
}
