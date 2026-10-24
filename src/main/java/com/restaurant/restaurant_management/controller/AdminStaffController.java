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

@Controller
@RequestMapping("/admin/staff")
public class AdminStaffController {

    private final EmployeeService employeeService;

    public AdminStaffController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public String listStaff(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        model.addAttribute("activeAdminPage", "staff");
        model.addAttribute("pageTitle", "Quản Lý Nhân Sự | GoD Admin");
        model.addAttribute("employee", emp);
        model.addAttribute("staffList", employeeService.getAllEmployees());
        return "admin/staff";
    }

    @PostMapping("/add")
    public String addStaff(@RequestParam("fullName") String fullName,
                           @RequestParam("username") String username,
                           @RequestParam("email") String email,
                           @RequestParam("phone") String phone,
                           @RequestParam("password") String password,
                           @RequestParam("roleName") String roleName,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        Employee newEmp = new Employee(null, roleName, username, password, fullName, phone, email, "ACTIVE");
        boolean success = employeeService.addEmployee(newEmp);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm nhân viên: " + fullName + " (" + roleName + ")!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập hoặc Email đã tồn tại trong hệ thống!");
        }
        return "redirect:/admin/staff";
    }

    @PostMapping("/toggle")
    public String toggleStaffStatus(@RequestParam("id") Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        employeeService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái hoạt động của nhân viên!");
        return "redirect:/admin/staff";
    }
}
