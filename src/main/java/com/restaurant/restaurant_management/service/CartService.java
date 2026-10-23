package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.CartItem;
import com.restaurant.restaurant_management.model.MenuItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final List<CartItem> cartItems = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final MenuService menuService;

    public CartService(MenuService menuService) {
        this.menuService = menuService;
    }

    public synchronized List<CartItem> getCartByCustomerId(Long customerId) {
        if (customerId == null) return new ArrayList<>();
        return cartItems.stream()
                .filter(item -> item.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public synchronized void addToCart(Long customerId, Long menuItemId, int quantity, String note) {
        if (customerId == null || menuItemId == null || quantity <= 0) return;

        Optional<MenuItem> itemOpt = menuService.findById(menuItemId);
        if (itemOpt.isEmpty()) return;

        // Kiểm tra xem món này đã có trong giỏ của khách chưa
        Optional<CartItem> existing = cartItems.stream()
                .filter(item -> item.getCustomerId().equals(customerId) && item.getMenuItem().getId().equals(menuItemId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            if (note != null && !note.isBlank()) {
                item.setNote(note);
            }
        } else {
            CartItem newItem = new CartItem(idGenerator.getAndIncrement(), customerId, itemOpt.get(), quantity, note);
            cartItems.add(newItem);
        }
    }

    public synchronized void updateQuantity(Long customerId, Long cartItemId, int quantity) {
        if (customerId == null || cartItemId == null) return;
        if (quantity <= 0) {
            removeItem(customerId, cartItemId);
            return;
        }
        cartItems.stream()
                .filter(item -> item.getCustomerId().equals(customerId) && item.getId().equals(cartItemId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }

    public synchronized void removeItem(Long customerId, Long cartItemId) {
        if (customerId == null || cartItemId == null) return;
        cartItems.removeIf(item -> item.getCustomerId().equals(customerId) && item.getId().equals(cartItemId));
    }

    public synchronized void clearCart(Long customerId) {
        if (customerId == null) return;
        cartItems.removeIf(item -> item.getCustomerId().equals(customerId));
    }

    public synchronized BigDecimal getTotalAmount(Long customerId) {
        return getCartByCustomerId(customerId).stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public synchronized int getTotalItemCount(Long customerId) {
        return getCartByCustomerId(customerId).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
