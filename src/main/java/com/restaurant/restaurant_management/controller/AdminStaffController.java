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

        // Chỉ ADMIN và MANAGER mới được vào trang Quản lý nhân sự
        if (!"ADMIN".equalsIgnoreCase(emp.getRoleName()) && !"MANAGER".equalsIgnoreCase(emp.getRoleName())) {
            return "redirect:/admin";
        }

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
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        // Kiểm tra quyền hạn cơ bản
        if (!"ADMIN".equalsIgnoreCase(emp.getRoleName()) && !"MANAGER".equalsIgnoreCase(emp.getRoleName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này!");
            return "redirect:/admin";
        }

        // QUY TẮC BẢO MẬT 1: Chỉ ADMIN mới được phép tạo tài khoản có vai trò ADMIN
        if ("ADMIN".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(emp.getRoleName())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Từ chối quyền: Quản lý (MANAGER) không được phép tạo tài khoản vai trò ADMIN. Chỉ Quản Trị Viên (ADMIN) mới có quyền này!");
            return "redirect:/admin/staff";
        }

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
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        // Kiểm tra quyền hạn
        if (!"ADMIN".equalsIgnoreCase(emp.getRoleName()) && !"MANAGER".equalsIgnoreCase(emp.getRoleName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện chức năng này!");
            return "redirect:/admin";
        }

        Optional<Employee> targetOpt = employeeService.findById(id);
        if (targetOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin tài khoản nhân viên!");
            return "redirect:/admin/staff";
        }

        Employee target = targetOpt.get();

        // Không cho phép tự khóa tài khoản của chính mình
        if (emp.getId().equals(target.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự khóa tài khoản của chính mình!");
            return "redirect:/admin/staff";
        }

        // Không thể khóa tài khoản Admin gốc (id = 1)
        if (target.getId() == 1L) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể khóa tài khoản Quản trị viên gốc!");
            return "redirect:/admin/staff";
        }

        // QUY TẮC BẢO MẬT 2: Manager KHÔNG được phép khóa tài khoản ADMIN hoặc MANAGER
        if ("MANAGER".equalsIgnoreCase(emp.getRoleName())) {
            if ("ADMIN".equalsIgnoreCase(target.getRoleName())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Từ chối quyền: Quản lý (MANAGER) không được phép khóa tài khoản Quản Trị Viên (ADMIN)!");
                return "redirect:/admin/staff";
            }
            if ("MANAGER".equalsIgnoreCase(target.getRoleName())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "Từ chối quyền: Quản lý (MANAGER) không được phép khóa tài khoản của Quản lý khác. Chỉ có Quản Trị Viên (ADMIN) mới có quyền này!");
                return "redirect:/admin/staff";
            }
        }

        employeeService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("successMessage",
            "Đã cập nhật trạng thái hoạt động của nhân viên " + target.getFullName() + " (" + target.getRoleName() + ") thành công!");
        return "redirect:/admin/staff";
    }
}
