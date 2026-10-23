package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.model.Order;
import com.restaurant.restaurant_management.model.Reservation;
import com.restaurant.restaurant_management.service.CustomerService;
import com.restaurant.restaurant_management.service.OrderService;
import com.restaurant.restaurant_management.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CustomerAccountController {

    private final CustomerService customerService;
    private final OrderService orderService;
    private final ReservationService reservationService;

    public CustomerAccountController(CustomerService customerService,
                                     OrderService orderService,
                                     ReservationService reservationService) {
        this.customerService = customerService;
        this.orderService = orderService;
        this.reservationService = reservationService;
    }

    @GetMapping("/my-account")
    public String showProfile(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem thông tin tài khoản!");
            return "redirect:/login";
        }

        // Lấy lại thông tin mới nhất (điểm tích lũy...)
        Customer currentCustomer = customerService.findById(customer.getId()).orElse(customer);
        session.setAttribute("loggedInCustomer", currentCustomer);

        List<Order> recentOrders = orderService.getOrdersByCustomerId(customer.getId());
        List<Reservation> recentReservations = reservationService.getReservationsByCustomerId(customer.getId());

        model.addAttribute("activePage", "account");
        model.addAttribute("pageTitle", "Tài khoản của tôi | GoD Fine Dining");
        model.addAttribute("customer", currentCustomer);
        model.addAttribute("orderCount", recentOrders.size());
        model.addAttribute("reservationCount", recentReservations.size());
        model.addAttribute("recentOrders", recentOrders.stream().limit(3).toList());
        model.addAttribute("recentReservations", recentReservations.stream().limit(3).toList());

        return "account/profile";
    }

    @GetMapping("/my-orders")
    public String showMyOrders(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem lịch sử đơn hàng!");
            return "redirect:/login?redirect=/my-orders";
        }

        List<Order> orders = orderService.getOrdersByCustomerId(customer.getId());
        model.addAttribute("activePage", "account");
        model.addAttribute("pageTitle", "Lịch sử đặt món | GoD Fine Dining");
        model.addAttribute("orders", orders);

        return "account/my-orders";
    }

    @GetMapping("/my-reservations")
    public String showMyReservations(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem lịch sử đặt bàn!");
            return "redirect:/login?redirect=/my-reservations";
        }

        List<Reservation> reservations = reservationService.getReservationsByCustomerId(customer.getId());
        model.addAttribute("activePage", "account");
        model.addAttribute("pageTitle", "Lịch sử đặt bàn | GoD Fine Dining");
        model.addAttribute("reservations", reservations);

        return "account/my-reservations";
    }
}
