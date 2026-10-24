package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    private final EmployeeService employeeService;

    public AdminAuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/login")
    public String showAdminLoginForm(HttpSession session, Model model) {
        if (session.getAttribute("loggedInEmployee") != null) {
            Employee emp = (Employee) session.getAttribute("loggedInEmployee");
            return "redirect:" + getRedirectUrlForRole(emp.getRoleName());
        }
        model.addAttribute("pageTitle", "Đăng nhập Nội Bộ | GoD Admin Portal");
        return "admin/login";
    }

    @PostMapping("/login")
    public String handleAdminLogin(@RequestParam("username") String username,
                                   @RequestParam("password") String password,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Optional<Employee> employeeOpt = employeeService.login(username, password);
        if (employeeOpt.isPresent()) {
            Employee emp = employeeOpt.get();
            session.setAttribute("loggedInEmployee", emp);
            redirectAttributes.addFlashAttribute("successMessage", "Xin chào " + emp.getFullName() + " (" + emp.getRoleName() + ")!");
            return "redirect:" + getRedirectUrlForRole(emp.getRoleName());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu nội bộ không chính xác!");
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/logout")
    public String handleAdminLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("loggedInEmployee");
        redirectAttributes.addFlashAttribute("successMessage", "Đã đăng xuất khỏi cổng Quản trị nội bộ.");
        return "redirect:/admin/login";
    }

    private String getRedirectUrlForRole(String roleName) {
        if ("ADMIN".equalsIgnoreCase(roleName) || "MANAGER".equalsIgnoreCase(roleName)) {
            return "/admin";
        } else if ("RECEPTIONIST".equalsIgnoreCase(roleName)) {
            return "/admin/reservations";
        } else if ("WAITER".equalsIgnoreCase(roleName)) {
            return "/admin/tables";
        } else if ("CHEF".equalsIgnoreCase(roleName) || "CASHIER".equalsIgnoreCase(roleName)) {
            return "/admin/orders";
        } else if ("STOREKEEPER".equalsIgnoreCase(roleName)) {
            return "/admin/inventory";
        }
        return "/admin";
    }
}
