package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;

    public GlobalControllerAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("loggedInCustomer")
    public Customer getLoggedInCustomer(HttpSession session) {
        return (Customer) session.getAttribute("loggedInCustomer");
    }

    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer != null) {
            return cartService.getTotalItemCount(customer.getId());
        }
        return 0;
    }
}
