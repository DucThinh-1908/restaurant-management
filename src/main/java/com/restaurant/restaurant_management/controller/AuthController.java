package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.service.CustomerService;
import com.restaurant.restaurant_management.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final CustomerService customerService;
    private final EmployeeService employeeService;

    public AuthController(CustomerService customerService, EmployeeService employeeService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "redirect", required = false) String redirect,
                                Model model,
                                HttpSession session) {
        if (session.getAttribute("loggedInCustomer") != null) {
            return "redirect:" + (redirect != null && !redirect.isBlank() ? redirect : "/my-account");
        }
        if (session.getAttribute("loggedInEmployee") != null) {
            Employee emp = (Employee) session.getAttribute("loggedInEmployee");
            return "redirect:" + getRedirectUrlForEmployee(emp.getRoleName());
        }

        model.addAttribute("activePage", "login");
        model.addAttribute("pageTitle", "Đăng nhập | GoD Fine Dining");
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("email") String loginKey,
                              @RequestParam("password") String password,
                              @RequestParam(value = "redirect", required = false) String redirect,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        // 1. Kiểm tra nếu là Khách hàng (CUSTOMER)
        Optional<Customer> customerOpt = customerService.login(loginKey, password);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            session.setAttribute("loggedInCustomer", customer);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đăng nhập thành công! Chào mừng khách hàng " + customer.getFullName() + ".");
            if (redirect != null && !redirect.isBlank() && !redirect.contains("login")) {
                return "redirect:" + redirect;
            }
            return "redirect:/menu";
        }

        // 2. Nếu không phải khách hàng, kiểm tra nếu là Nhân viên/Admin (7 Roles nội bộ)
        Optional<Employee> employeeOpt = employeeService.login(loginKey, password);
        if (employeeOpt.isPresent()) {
            Employee emp = employeeOpt.get();
            session.setAttribute("loggedInEmployee", emp);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đăng nhập thành công! Xin chào " + emp.getFullName() + " (" + emp.getRoleName() + ").");

            // Tự động điều hướng đến đúng bàn làm việc chuyên trách theo vai trò
            return "redirect:" + getRedirectUrlForEmployee(emp.getRoleName());
        }

        // 3. Nếu cả 2 đều không khớp -> Báo lỗi
        redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập / Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại!");
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:/login?redirect=" + redirect;
        }
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model, HttpSession session) {
        if (session.getAttribute("loggedInCustomer") != null) {
            return "redirect:/my-account";
        }
        model.addAttribute("activePage", "register");
        model.addAttribute("pageTitle", "Đăng ký thành viên | GoD Fine Dining");
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam("fullName") String fullName,
                                 @RequestParam("email") String email,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("password") String password,
                                 @RequestParam(value = "address", required = false) String address,
                                 RedirectAttributes redirectAttributes) {
        Customer newCustomer = new Customer();
        newCustomer.setFullName(fullName);
        newCustomer.setEmail(email);
        newCustomer.setPhone(phone);
        newCustomer.setPassword(password);
        newCustomer.setAddress(address);

        boolean success = customerService.register(newCustomer);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập để bắt đầu đặt món.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Email này đã được sử dụng. Vui lòng chọn email khác hoặc đăng nhập!");
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("loggedInCustomer");
        session.removeAttribute("loggedInEmployee");
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Bạn đã đăng xuất tài khoản thành công.");
        return "redirect:/";
    }

    private String getRedirectUrlForEmployee(String roleName) {
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
