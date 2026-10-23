package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.dto.ReservationRequest;
import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    private final ReservationService reservationService;

    public ContactController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/contact")
    public String contact(Model model, HttpSession session) {
        model.addAttribute("activePage", "contact");
        model.addAttribute("pageTitle", "Liên hệ & Đặt bàn | GoD Fine Dining");

        if (!model.containsAttribute("reservationRequest")) {
            ReservationRequest request = new ReservationRequest();
            Customer customer = (Customer) session.getAttribute("loggedInCustomer");
            if (customer != null) {
                request.setName(customer.getFullName());
                request.setPhone(customer.getPhone());
                request.setEmail(customer.getEmail());
            }
            model.addAttribute("reservationRequest", request);
        }
        return "contact";
    }

    @PostMapping("/contact/reservation")
    public String handleReservation(@ModelAttribute ReservationRequest reservationRequest,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        Long customerId = customer != null ? customer.getId() : null;

        reservationService.createReservation(reservationRequest, customerId);

        String message = "Cảm ơn " + reservationRequest.getName() + "! Chúng tôi đã nhận được yêu cầu đặt bàn và Lễ tân sẽ liên hệ xác nhận sớm nhất.";
        if (customer != null) {
            message += " Bạn có thể theo dõi trạng thái yêu cầu tại mục 'Lịch sử đặt bàn' trong tài khoản.";
        }
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/contact#reservation";
    }
}
