package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.service.CustomerService;
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

    public AuthController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "redirect", required = false) String redirect,
                                Model model,
                                HttpSession session) {
        if (session.getAttribute("loggedInCustomer") != null) {
            return "redirect:" + (redirect != null && !redirect.isBlank() ? redirect : "/my-account");
        }
        model.addAttribute("activePage", "login");
        model.addAttribute("pageTitle", "Đăng nhập | GoD Fine Dining");
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              @RequestParam(value = "redirect", required = false) String redirect,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Optional<Customer> customerOpt = customerService.login(email, password);
        if (customerOpt.isPresent()) {
            session.setAttribute("loggedInCustomer", customerOpt.get());
            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công! Chào mừng " + customerOpt.get().getFullName() + " trở lại.");
            if (redirect != null && !redirect.isBlank() && !redirect.contains("login")) {
                return "redirect:" + redirect;
            }
            return "redirect:/menu";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Email hoặc mật khẩu không chính xác. Vui lòng thử lại!");
            if (redirect != null && !redirect.isBlank()) {
                return "redirect:/login?redirect=" + redirect;
            }
            return "redirect:/login";
        }
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
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Bạn đã đăng xuất tài khoản thành công.");
        return "redirect:/";
    }
}
