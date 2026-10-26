package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.model.Order;
import com.restaurant.restaurant_management.service.OrderService;
import com.restaurant.restaurant_management.service.TableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/cashier")
public class AdminCashierController {

    private final OrderService orderService;
    private final TableService tableService;

    public AdminCashierController(OrderService orderService, TableService tableService) {
        this.orderService = orderService;
        this.tableService = tableService;
    }

    @GetMapping
    public String cashierDashboard(@RequestParam(value = "tab", defaultValue = "active") String tab,
                                   @RequestParam(value = "search", required = false) String search,
                                   HttpSession session,
                                   Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        // Chỉ cho phép CASHIER, ADMIN, MANAGER
        String role = emp.getRoleName();
        if (!"CASHIER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            return "redirect:/admin";
        }

        List<Order> activeOrders = orderService.getActiveOrdersForCashier();
        List<Order> completedOrders = orderService.getCompletedOrdersToday();
        Map<String, Object> stats = orderService.getCashierStatsToday();

        // Lọc tìm kiếm nếu có
        if (search != null && !search.isBlank()) {
            String kw = search.trim().toLowerCase();
            activeOrders = activeOrders.stream()
                    .filter(o -> (o.getOrderCode() != null && o.getOrderCode().toLowerCase().contains(kw)) ||
                                 (o.getTableNumber() != null && o.getTableNumber().toLowerCase().contains(kw)) ||
                                 (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(kw)) ||
                                 (o.getCustomerPhone() != null && o.getCustomerPhone().contains(kw)))
                    .toList();

            completedOrders = completedOrders.stream()
                    .filter(o -> (o.getOrderCode() != null && o.getOrderCode().toLowerCase().contains(kw)) ||
                                 (o.getTableNumber() != null && o.getTableNumber().toLowerCase().contains(kw)) ||
                                 (o.getCustomerName() != null && o.getCustomerName().toLowerCase().contains(kw)) ||
                                 (o.getCustomerPhone() != null && o.getCustomerPhone().contains(kw)))
                    .toList();
        }

        model.addAttribute("activeAdminPage", "cashier");
        model.addAttribute("pageTitle", "Quầy Thu Ngân & POS | GoD Admin");
        model.addAttribute("employee", emp);
        model.addAttribute("currentTab", tab);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("stats", stats);
        model.addAttribute("tables", tableService.getAllTables());

        return "admin/cashier";
    }

    @PostMapping("/checkout")
    public String checkoutOrder(@RequestParam("orderId") Long orderId,
                                @RequestParam(value = "paymentMethod", defaultValue = "CASH") String paymentMethod,
                                @RequestParam(value = "discount", defaultValue = "0") BigDecimal discount,
                                @RequestParam(value = "transactionCode", required = false) String transactionCode,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        boolean success = orderService.checkoutOrder(orderId, emp.getId(), paymentMethod, discount, transactionCode);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Thu ngân " + emp.getFullName() + " đã thanh toán & xuất hóa đơn cho đơn #" + orderId + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hoàn tất thanh toán cho đơn #" + orderId + "!");
        }
        return "redirect:/admin/cashier";
    }

    @GetMapping("/receipt/{id}")
    @ResponseBody
    public Map<String, Object> getReceiptData(@PathVariable("id") Long id, HttpSession session) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return Map.of("error", "Unauthorized");

        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty()) {
            return Map.of("error", "Order not found");
        }
        Order o = orderOpt.get();

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", o.getId());
        data.put("orderCode", o.getOrderCode() != null ? o.getOrderCode() : "");
        data.put("orderType", o.getOrderTypeDisplayName());
        data.put("tableNumber", o.getTableNumber() != null ? o.getTableNumber() : "—");
        data.put("customerName", o.getCustomerName() != null ? o.getCustomerName() : "Khách vãng lai");
        data.put("customerPhone", o.getCustomerPhone() != null ? o.getCustomerPhone() : "");
        data.put("createdAt", o.getFormattedCreatedAt());
        data.put("completedAt", o.getFormattedCompletedAt());
        data.put("subtotal", o.getSubtotal());
        data.put("discount", o.getDiscount());
        data.put("taxAmount", o.getTaxAmount());
        data.put("totalAmount", o.getTotalAmount());
        data.put("paymentMethod", o.getPaymentMethodDisplayName());
        data.put("cashierName", o.getCashierName() != null ? o.getCashierName() : emp.getFullName());
        data.put("items", o.getItems());
        return data;
    }
}
