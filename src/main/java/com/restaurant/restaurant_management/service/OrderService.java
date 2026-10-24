package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.CartItem;
import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.model.Order;
import com.restaurant.restaurant_management.model.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final List<Order> orders = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1001);
    private final CartService cartService;
    private final CustomerService customerService;

    public OrderService(CartService cartService, CustomerService customerService) {
        this.cartService = cartService;
        this.customerService = customerService;

        // Khởi tạo 1 đơn mẫu cho khách hàng ID 1 (Nguyễn Hoàng Nam)
        Order sampleOrder = new Order();
        sampleOrder.setId(1000L);
        sampleOrder.setOrderCode("ORD-20261023-001");
        sampleOrder.setOrderType("DELIVERY");
        sampleOrder.setCustomerId(1L);
        sampleOrder.setCustomerName("Nguyễn Hoàng Nam");
        sampleOrder.setCustomerPhone("0912345678");
        sampleOrder.setDeliveryAddress("123 Đường Bạch Đằng, Quận Hải Châu, Đà Nẵng");
        sampleOrder.setNote("Giao trước 19h tối");
        sampleOrder.setStatus("COMPLETED");
        sampleOrder.setSubtotal(new BigDecimal("445000"));
        sampleOrder.setDiscount(BigDecimal.ZERO);
        sampleOrder.setTaxPercent(new BigDecimal("8.00"));
        sampleOrder.setTaxAmount(new BigDecimal("35600"));
        sampleOrder.setTotalAmount(new BigDecimal("480600"));
        sampleOrder.setPaymentMethod("BANK_TRANSFER");
        sampleOrder.setCreatedAt(LocalDateTime.now().minusDays(1));

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(1L, "Fresh Spring Rolls", 1, new BigDecimal("95000"), "Ít cay"));
        items.add(new OrderItem(7L, "Slow-Braised Beef", 1, new BigDecimal("350000"), "Chín mềm"));
        sampleOrder.setItems(items);
        orders.add(sampleOrder);
    }

    public synchronized Order createOrderFromCart(Long customerId, String orderType, String deliveryAddress, String contactPhone, String note, String paymentMethod) {
        if (customerId == null) {
            throw new IllegalArgumentException("Khách hàng bắt buộc phải đăng nhập trước khi đặt món!");
        }

        Customer customer = customerService.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin khách hàng!"));

        List<CartItem> cartItems = cartService.getCartByCustomerId(customerId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống!");
        }

        Order order = new Order();
        long orderId = idGenerator.getAndIncrement();
        order.setId(orderId);

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        order.setOrderCode("ORD-" + datePrefix + "-" + String.format("%04d", orderId % 10000));
        order.setOrderType(orderType != null ? orderType : "DELIVERY");
        order.setCustomerId(customerId);
        order.setCustomerName(customer.getFullName());
        order.setCustomerPhone(contactPhone != null && !contactPhone.isBlank() ? contactPhone : customer.getPhone());
        order.setDeliveryAddress(deliveryAddress != null && !deliveryAddress.isBlank() ? deliveryAddress : customer.getAddress());
        order.setNote(note);
        order.setStatus("PENDING");
        order.setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");
        order.setCreatedAt(LocalDateTime.now());

        // Snapshot chi tiết các món
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getMenuItem().getId(),
                    cartItem.getMenuItem().getName(),
                    cartItem.getQuantity(),
                    cartItem.getMenuItem().getPriceValue(),
                    cartItem.getNote()
            );
            orderItem.setOrderId(orderId);
            orderItems.add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);
        order.setDiscount(BigDecimal.ZERO);
        order.setTaxPercent(new BigDecimal("8.00"));

        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(subtotal.add(taxAmount));

        // Tích điểm thành viên: 1 điểm cho mỗi 50.000 VND
        int pointsEarned = order.getTotalAmount().divide(new BigDecimal("50000"), RoundingMode.DOWN).intValue();
        if (pointsEarned > 0) {
            customerService.addLoyaltyPoints(customerId, pointsEarned);
        }

        // Lưu đơn hàng và xóa giỏ hàng của khách
        orders.add(0, order); // Đưa lên đầu danh sách
        cartService.clearCart(customerId);

        return order;
    }

    public synchronized List<Order> getOrdersByCustomerId(Long customerId) {
        if (customerId == null) return Collections.emptyList();
        return orders.stream()
                .filter(o -> o.getCustomerId() != null && o.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public synchronized Optional<Order> getOrderById(Long id) {
        if (id == null) return Optional.empty();
        return orders.stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    // --- PHƯƠNG THỨC QUẢN TRỊ ADMIN ---
    public synchronized List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public synchronized void updateOrderStatus(Long orderId, String status) {
        getOrderById(orderId).ifPresent(o -> o.setStatus(status));
    }

    public synchronized BigDecimal getTotalRevenue() {
        return orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public synchronized long getPendingOrderCount() {
        return orders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count();
    }
}
