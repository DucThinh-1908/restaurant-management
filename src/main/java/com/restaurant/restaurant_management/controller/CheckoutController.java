package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.CartItem;
import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.model.Order;
import com.restaurant.restaurant_management.service.CartService;
import com.restaurant.restaurant_management.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String showCheckoutPage(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tiến hành đặt món!");
            return "redirect:/login?redirect=/checkout";
        }

        List<CartItem> cartItems = cartService.getCartByCustomerId(customer.getId());
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống. Hãy chọn món trước khi đặt!");
            return "redirect:/menu";
        }

        BigDecimal subtotal = cartService.getTotalAmount(customer.getId());
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        model.addAttribute("activePage", "cart");
        model.addAttribute("pageTitle", "Xác nhận đặt món | GoD Fine Dining");
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("taxAmount", taxAmount);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("customer", customer);

        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("orderType") String orderType,
                                  @RequestParam(value = "deliveryAddress", required = false) String deliveryAddress,
                                  @RequestParam(value = "contactPhone", required = false) String contactPhone,
                                  @RequestParam(value = "note", required = false) String note,
                                  @RequestParam(value = "paymentMethod", defaultValue = "CASH") String paymentMethod,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để tiến hành đặt món!");
            return "redirect:/login?redirect=/checkout";
        }

        try {
            Order order = orderService.createOrderFromCart(
                    customer.getId(),
                    orderType,
                    deliveryAddress,
                    contactPhone,
                    note,
                    paymentMethod
            );
            return "redirect:/order-success/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tạo đơn hàng: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/order-success/{id}")
    public String showOrderSuccess(@PathVariable("id") Long id,
                                   Model model,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty() || !orderOpt.get().getCustomerId().equals(customer.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin đơn hàng này!");
            return "redirect:/my-orders";
        }

        model.addAttribute("order", orderOpt.get());
        model.addAttribute("pageTitle", "Đặt món thành công | GoD Fine Dining");
        return "order-success";
    }
}
