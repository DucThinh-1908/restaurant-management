package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.CartItem;
import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("menuItemId") Long menuItemId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            @RequestParam(value = "note", required = false) String note,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để chọn món và đặt hàng!");
            return "redirect:/login?redirect=/menu";
        }

        cartService.addToCart(customer.getId(), menuItemId, quantity, note);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm món vào giỏ hàng thành công!");
        return "redirect:/menu";
    }

    @GetMapping
    public String viewCart(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để xem giỏ hàng của bạn!");
            return "redirect:/login?redirect=/cart";
        }

        List<CartItem> cartList = cartService.getCartByCustomerId(customer.getId());
        BigDecimal totalAmount = cartService.getTotalAmount(customer.getId());

        model.addAttribute("activePage", "cart");
        model.addAttribute("pageTitle", "Giỏ hàng của bạn | GoD Fine Dining");
        model.addAttribute("cartItems", cartList);
        model.addAttribute("totalAmount", totalAmount);
        return "cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam("cartItemId") Long cartItemId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer != null) {
            cartService.updateQuantity(customer.getId(), cartItemId, quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam("cartItemId") Long cartItemId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer != null) {
            cartService.removeItem(customer.getId(), cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa món khỏi giỏ hàng.");
        }
        return "redirect:/cart";
    }
}
