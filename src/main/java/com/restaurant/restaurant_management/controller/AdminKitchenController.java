package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.model.Order;
import com.restaurant.restaurant_management.service.MenuService;
import com.restaurant.restaurant_management.service.OrderService;
import com.restaurant.restaurant_management.service.RecipeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/kitchen")
public class AdminKitchenController {

    private final OrderService orderService;
    private final MenuService menuService;
    private final RecipeService recipeService;

    public AdminKitchenController(OrderService orderService, MenuService menuService, RecipeService recipeService) {
        this.orderService = orderService;
        this.menuService = menuService;
        this.recipeService = recipeService;
    }

    @GetMapping
    public String showKitchenDisplay(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        // Lấy danh sách các đơn theo từng phân đoạn Bếp
        List<Order> ordersPending = orderService.getOrdersByStatus("PENDING");
        List<Order> ordersCooking = orderService.getOrdersByStatus("COOKING");
        List<Order> ordersReady = orderService.getOrdersByStatus("READY");
        List<Order> ordersCompleted = orderService.getOrdersByStatus("COMPLETED");

        // Tổng hợp hàng đợi các món đang chờ chế biến
        Map<String, Integer> dishQueue = orderService.getActiveDishQueue();

        model.addAttribute("activeAdminPage", "kitchen");
        model.addAttribute("pageTitle", "Điều Phối Bếp (KDS) | GoD Chef");
        model.addAttribute("employee", emp);
        model.addAttribute("ordersPending", ordersPending);
        model.addAttribute("ordersCooking", ordersCooking);
        model.addAttribute("ordersReady", ordersReady);
        model.addAttribute("ordersServing", ordersReady); // Tương thích template
        model.addAttribute("dishQueue", dishQueue);
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        model.addAttribute("pendingCount", ordersPending.size());
        model.addAttribute("cookingCount", ordersCooking.size());
        model.addAttribute("readyCount", ordersReady.size());
        model.addAttribute("servingCount", ordersReady.size());
        model.addAttribute("completedCount", ordersCompleted.size());

        return "admin/kitchen";
    }

    @PostMapping("/update-status")
    public String updateCookingStatus(@RequestParam("id") Long id,
                                      @RequestParam("status") String status,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        orderService.updateOrderStatus(id, status);

        String message = "Đã cập nhật trạng thái đơn #" + id + " sang: ";
        if ("COOKING".equalsIgnoreCase(status)) {
            message = " Bếp đã tiếp nhận đơn #" + id + " - Bắt đầu chế biến!";
        } else if ("READY".equalsIgnoreCase(status)) {
            message = " Đơn #" + id + " đã nấu xong - Báo phục vụ bưng ra bàn / quầy giao khách!";
        } else if ("SERVING".equalsIgnoreCase(status)) {
            message = " Đã phục vụ ra bàn đơn #" + id + "!";
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            message = " Đã hoàn tất phục vụ đơn #" + id + "!";
        }
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/admin/kitchen";
    }

    @PostMapping("/toggle-available")
    public String toggleDishAvailability(@RequestParam("id") Long id,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        menuService.toggleAvailability(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tình trạng món ăn tại Bếp thành công!");
        return "redirect:/admin/kitchen";
    }

    @GetMapping("/recipes")
    public String showRecipes(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        model.addAttribute("activeAdminPage", "recipes");
        model.addAttribute("pageTitle", "Sổ Tay Công Thức & Định Lượng | GoD Chef");
        model.addAttribute("employee", emp);
        model.addAttribute("recipes", recipeService.getAllRecipes());

        return "admin/recipes";
    }
}
