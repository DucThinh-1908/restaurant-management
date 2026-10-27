package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.CartItem;
import com.restaurant.restaurant_management.model.Customer;
import com.restaurant.restaurant_management.model.Order;
import com.restaurant.restaurant_management.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final CartService cartService;
    private final CustomerService customerService;
    private final JdbcTemplate jdbcTemplate;

    // Dự phòng bộ nhớ (Fallback In-memory) nếu Database mất kết nối
    private final List<Order> inMemoryOrders = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(2001);

    public OrderService(CartService cartService,
                        CustomerService customerService,
                        @Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------------
    // ROW MAPPERS TỪ MYSQL RESULTSET
    // -------------------------------------------------------------
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderCode(rs.getString("order_code"));
        order.setOrderType(rs.getString("order_type"));
        
        long custId = rs.getLong("customer_id");
        if (!rs.wasNull()) {
            order.setCustomerId(custId);
        }
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerPhone(rs.getString("customer_phone"));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setNote(rs.getString("note"));
        order.setStatus(rs.getString("status"));
        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setDiscount(rs.getBigDecimal("discount"));
        order.setTaxPercent(rs.getBigDecimal("tax_percent"));
        order.setTaxAmount(rs.getBigDecimal("tax_amount"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));

        try {
            long tId = rs.getLong("table_id");
            if (!rs.wasNull()) order.setTableId(tId);
        } catch (SQLException ignored) {}

        try {
            order.setTableNumber(rs.getString("table_number"));
        } catch (SQLException ignored) {}

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            order.setCreatedAt(ts.toLocalDateTime());
        }

        try {
            Timestamp cts = rs.getTimestamp("completed_at");
            if (cts != null) order.setCompletedAt(cts.toLocalDateTime());
        } catch (SQLException ignored) {}

        try {
            String pm = rs.getString("payment_method");
            if (pm != null) order.setPaymentMethod(pm);
        } catch (SQLException ignored) {}

        try {
            String cn = rs.getString("cashier_name");
            if (cn != null) order.setCashierName(cn);
        } catch (SQLException ignored) {}

        try {
            String tc = rs.getString("transaction_code");
            if (tc != null) order.setTransactionCode(tc);
        } catch (SQLException ignored) {}

        try {
            order.setBillRequested(rs.getBoolean("is_bill_requested"));
        } catch (SQLException ignored) {}

        return order;
    }

    private OrderItem mapRowToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setId(rs.getLong("id"));
        item.setOrderId(rs.getLong("order_id"));
        long menuId = rs.getLong("menu_item_id");
        if (!rs.wasNull()) {
            item.setMenuItemId(menuId);
        }
        item.setItemName(rs.getString("item_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setNote(rs.getString("note"));
        item.setStatus(rs.getString("status"));
        return item;
    }

    private void attachItems(List<Order> orderList) {
        if (orderList == null || orderList.isEmpty() || jdbcTemplate == null) return;
        for (Order o : orderList) {
            try {
                String sql = "SELECT id, order_id, menu_item_id, item_name, quantity, unit_price, note, status " +
                             "FROM order_items WHERE order_id = ? ORDER BY id ASC";
                List<OrderItem> items = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToOrderItem(rs), o.getId());
                o.setItems(items);
            } catch (Exception e) {
                // Giữ nguyên items nếu lỗi
            }
        }
    }

    // -------------------------------------------------------------
    // NGHIỆP VỤ BẾP (KDS) & LẤY ĐƠN THEO TRẠNG THÁI TỪ DATABASE
    // -------------------------------------------------------------
    public synchronized List<Order> getOrdersByStatus(String status) {
        if (status == null) return Collections.emptyList();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT o.id, o.order_code, o.order_type, o.customer_id, " +
                             "COALESCE(c.full_name, 'Khách Vãng Lai') AS customer_name, " +
                             "COALESCE(o.contact_phone, c.phone, '') AS customer_phone, " +
                             "o.delivery_address, o.note, o.status, o.is_bill_requested, " +
                             "o.subtotal, o.discount, o.tax_percent, o.tax_amount, o.total_amount, " +
                             "o.created_at, t.table_number " +
                             "FROM orders o " +
                             "LEFT JOIN dining_tables t ON o.table_id = t.id " +
                             "LEFT JOIN customers c ON o.customer_id = c.id " +
                             "WHERE o.status = ? " +
                             "ORDER BY o.id DESC";
                List<Order> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToOrder(rs), status);
                attachItems(result);
                return result;
            } catch (Exception e) {
                System.err.println("Lỗi truy vấn orders theo status '" + status + "' từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryOrders.stream()
                .filter(o -> status.equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());
    }

    public synchronized Map<String, Integer> getActiveDishQueue() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT oi.item_name, SUM(oi.quantity) AS total_qty " +
                             "FROM order_items oi " +
                             "JOIN orders o ON oi.order_id = o.id " +
                             "WHERE oi.status IN ('PENDING', 'COOKING') AND o.status NOT IN ('COMPLETED', 'CANCELLED') " +
                             "GROUP BY oi.item_name " +
                             "ORDER BY total_qty DESC";
                Map<String, Integer> queue = new LinkedHashMap<>();
                jdbcTemplate.query(sql, (rs) -> {
                    queue.put(rs.getString("item_name"), rs.getInt("total_qty"));
                });
                return queue;
            } catch (Exception e) {
                System.err.println("Lỗi truy vấn getActiveDishQueue từ MySQL: " + e.getMessage());
            }
        }

        Map<String, Integer> queue = new LinkedHashMap<>();
        for (Order order : inMemoryOrders) {
            if ("PENDING".equalsIgnoreCase(order.getStatus()) || "COOKING".equalsIgnoreCase(order.getStatus())) {
                for (OrderItem item : order.getItems()) {
                    queue.put(item.getItemName(), queue.getOrDefault(item.getItemName(), 0) + item.getQuantity());
                }
            }
        }
        return queue;
    }

    public synchronized void updateOrderStatus(Long orderId, String status) {
        updateOrderStatus(orderId, status, null);
    }

    public synchronized void updateOrderStatus(Long orderId, String status, Long employeeId) {
        if (orderId == null || status == null) return;
        if (jdbcTemplate != null) {
            try {
                String sql = "UPDATE orders SET status = ?, completed_at = CASE WHEN ? = 'COMPLETED' THEN NOW() ELSE completed_at END WHERE id = ?";
                jdbcTemplate.update(sql, status, status, orderId);

                // Cập nhật trạng thái các món chi tiết
                if ("COOKING".equalsIgnoreCase(status)) {
                    jdbcTemplate.update("UPDATE order_items SET status = 'COOKING' WHERE order_id = ? AND status = 'PENDING'", orderId);
                    deductInventoryForOrder(orderId, employeeId);
                } else if ("COMPLETED".equalsIgnoreCase(status) || "SERVING".equalsIgnoreCase(status)) {
                    jdbcTemplate.update("UPDATE order_items SET status = 'SERVED' WHERE order_id = ? AND status != 'CANCELLED'", orderId);
                }
            } catch (Exception e) {
                System.err.println("Lỗi cập nhật order #" + orderId + " trong MySQL: " + e.getMessage());
            }
        }

        inMemoryOrders.stream().filter(o -> o.getId().equals(orderId)).findFirst().ifPresent(o -> o.setStatus(status));
    }

    private void deductInventoryForOrder(Long orderId, Long employeeId) {
        if (jdbcTemplate == null || orderId == null) return;
        try {
            // Kiểm tra đã trừ kho cho order này chưa để tránh trừ trùng lặp
            Integer existingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_transactions WHERE reference_type = 'ORDER' AND reference_id = ? AND transaction_type = 'RECIPE_CONSUMPTION'",
                Integer.class, orderId
            );
            if (existingCount != null && existingCount > 0) return;

            String query = "SELECT ri.ingredient_id, (ri.quantity_required * oi.quantity) AS total_qty_needed, " +
                           "m.name AS dish_name, oi.quantity AS dish_qty, i.cost_price " +
                           "FROM order_items oi " +
                           "JOIN recipe_items ri ON oi.menu_item_id = ri.menu_item_id " +
                           "JOIN menu_items m ON oi.menu_item_id = m.id " +
                           "JOIN ingredients i ON ri.ingredient_id = i.id " +
                           "WHERE oi.order_id = ? AND oi.status != 'CANCELLED'";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, orderId);
            long actingEmployeeId = (employeeId != null ? employeeId : 5L);

            for (Map<String, Object> row : rows) {
                Long ingredientId = ((Number) row.get("ingredient_id")).longValue();
                BigDecimal totalQtyNeeded = (BigDecimal) row.get("total_qty_needed");
                String dishName = (String) row.get("dish_name");
                Integer dishQty = ((Number) row.get("dish_qty")).intValue();
                BigDecimal costPrice = (BigDecimal) row.get("cost_price");

                // Trừ tồn kho trong DB (không để âm dưới 0)
                jdbcTemplate.update("UPDATE ingredients SET stock_quantity = GREATEST(0, stock_quantity - ?), updated_at = NOW() WHERE id = ?", totalQtyNeeded, ingredientId);

                // Lấy tồn kho mới sau trừ
                BigDecimal newBalance = jdbcTemplate.queryForObject("SELECT stock_quantity FROM ingredients WHERE id = ?", BigDecimal.class, ingredientId);

                // Ghi nhận nhật ký biến động kho (Audit log)
                String note = "Bếp chế biến " + dishQty + " phần " + dishName + " cho đơn #" + orderId;
                String insertTx = "INSERT INTO inventory_transactions (ingredient_id, transaction_type, quantity, balance_after, unit_price, reference_type, reference_id, employee_id, note) " +
                                  "VALUES (?, 'RECIPE_CONSUMPTION', ?, ?, ?, 'ORDER', ?, ?, ?)";
                jdbcTemplate.update(insertTx, ingredientId, totalQtyNeeded.negate(), newBalance, costPrice, orderId, actingEmployeeId, note);
            }
        } catch (Exception e) {
            System.err.println("Lỗi deductInventoryForOrder(" + orderId + "): " + e.getMessage());
        }
    }

    /**
     * Phân tích tồn kho nguyên liệu trước khi Bếp tiếp nhận chế biến
     */
    public synchronized void attachStockAnalysis(List<Order> orders) {
        if (orders == null || orders.isEmpty() || jdbcTemplate == null) return;

        for (Order order : orders) {
            if (order.getItems() == null || order.getItems().isEmpty()) continue;

            boolean orderAllSufficient = true;
            for (OrderItem item : order.getItems()) {
                if (item.getMenuItemId() == null) continue;

                try {
                    String sql = "SELECT ri.ingredient_id, i.name AS ingredient_name, i.unit, " +
                                 "i.stock_quantity, ri.quantity_required " +
                                 "FROM recipe_items ri " +
                                 "JOIN ingredients i ON ri.ingredient_id = i.id " +
                                 "WHERE ri.menu_item_id = ?";
                    List<Map<String, Object>> reqs = jdbcTemplate.queryForList(sql, item.getMenuItemId());

                    boolean itemSufficient = true;
                    for (Map<String, Object> req : reqs) {
                        String ingName = (String) req.get("ingredient_name");
                        String unit = (String) req.get("unit");
                        BigDecimal stockQty = (BigDecimal) req.get("stock_quantity");
                        BigDecimal qtyReq = (BigDecimal) req.get("quantity_required");

                        BigDecimal totalNeeded = qtyReq.multiply(BigDecimal.valueOf(item.getQuantity()));
                        if (stockQty == null || stockQty.compareTo(totalNeeded) < 0) {
                            itemSufficient = false;
                            orderAllSufficient = false;
                            BigDecimal current = stockQty != null ? stockQty : BigDecimal.ZERO;
                            String notice = "Thiếu " + ingName + ": cần " + totalNeeded.stripTrailingZeros().toPlainString() + " " + unit +
                                            " (kho còn " + current.stripTrailingZeros().toPlainString() + " " + unit + ")";
                            item.addMissingStockNotice(notice);
                            order.addStockCheckAlert(item.getItemName() + ": " + notice);
                        }
                    }
                    item.setHasEnoughStock(itemSufficient);
                } catch (Exception e) {
                    System.err.println("Lỗi attachStockAnalysis cho menuItem #" + item.getMenuItemId() + ": " + e.getMessage());
                }
            }
            order.setHasSufficientStock(orderAllSufficient);
        }
    }

    // -------------------------------------------------------------
    // TRUY VẤN ĐƠN HÀNG CHUNG & THEO KHÁCH HÀNG
    // -------------------------------------------------------------
    public synchronized List<Order> getAllOrders() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT o.id, o.order_code, o.order_type, o.customer_id, " +
                             "COALESCE(c.full_name, 'Khách Vãng Lai') AS customer_name, " +
                             "COALESCE(o.contact_phone, c.phone, '') AS customer_phone, " +
                             "o.delivery_address, o.note, o.status, o.is_bill_requested, " +
                             "o.subtotal, o.discount, o.tax_percent, o.tax_amount, o.total_amount, " +
                             "o.created_at, t.table_number " +
                             "FROM orders o " +
                             "LEFT JOIN dining_tables t ON o.table_id = t.id " +
                             "LEFT JOIN customers c ON o.customer_id = c.id " +
                             "ORDER BY o.id DESC";
                List<Order> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToOrder(rs));
                attachItems(result);
                return result;
            } catch (Exception e) {
                System.err.println("Lỗi truy vấn getAllOrders từ MySQL: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryOrders);
    }

    public synchronized Optional<Order> getOrderById(Long id) {
        if (id == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT o.id, o.order_code, o.order_type, o.customer_id, " +
                             "COALESCE(c.full_name, 'Khách Vãng Lai') AS customer_name, " +
                             "COALESCE(o.contact_phone, c.phone, '') AS customer_phone, " +
                             "o.delivery_address, o.note, o.status, o.is_bill_requested, " +
                             "o.subtotal, o.discount, o.tax_percent, o.tax_amount, o.total_amount, " +
                             "o.created_at, t.table_number " +
                             "FROM orders o " +
                             "LEFT JOIN dining_tables t ON o.table_id = t.id " +
                             "LEFT JOIN customers c ON o.customer_id = c.id " +
                             "WHERE o.id = ?";
                List<Order> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToOrder(rs), id);
                if (!result.isEmpty()) {
                    attachItems(result);
                    return Optional.of(result.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi getOrderById(" + id + ") từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryOrders.stream().filter(o -> o.getId().equals(id)).findFirst();
    }

    public synchronized List<Order> getOrdersByCustomerId(Long customerId) {
        if (customerId == null) return Collections.emptyList();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT o.id, o.order_code, o.order_type, o.customer_id, " +
                             "COALESCE(c.full_name, 'Khách Vãng Lai') AS customer_name, " +
                             "COALESCE(o.contact_phone, c.phone, '') AS customer_phone, " +
                             "o.delivery_address, o.note, o.status, o.is_bill_requested, " +
                             "o.subtotal, o.discount, o.tax_percent, o.tax_amount, o.total_amount, " +
                             "o.created_at, t.table_number " +
                             "FROM orders o " +
                             "LEFT JOIN dining_tables t ON o.table_id = t.id " +
                             "LEFT JOIN customers c ON o.customer_id = c.id " +
                             "WHERE o.customer_id = ? " +
                             "ORDER BY o.id DESC";
                List<Order> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToOrder(rs), customerId);
                attachItems(result);
                return result;
            } catch (Exception e) {
                System.err.println("Lỗi getOrdersByCustomerId(" + customerId + ") từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryOrders.stream()
                .filter(o -> o.getCustomerId() != null && o.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------
    // TẠO ĐƠN HÀNG TỪ GIỎ HÀNG & LƯU VÀO DATABASE
    // -------------------------------------------------------------
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

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String orderCode = "ORD-" + datePrefix + "-" + String.format("%04d", (System.currentTimeMillis() % 10000));
        String finalOrderType = orderType != null ? orderType : "DELIVERY";
        String finalPhone = contactPhone != null && !contactPhone.isBlank() ? contactPhone : customer.getPhone();
        String finalAddress = deliveryAddress != null && !deliveryAddress.isBlank() ? deliveryAddress : customer.getAddress();
        String finalPaymentMethod = paymentMethod != null ? paymentMethod : "CASH";

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            subtotal = subtotal.add(ci.getMenuItem().getPriceValue().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        final BigDecimal finalSubtotal = subtotal;
        final BigDecimal finalTaxAmount = taxAmount;
        final BigDecimal finalTotalAmount = totalAmount;
        final String finalNote = note;

        Order order = new Order();
        order.setOrderCode(orderCode);
        order.setOrderType(finalOrderType);
        order.setCustomerId(customerId);
        order.setCustomerName(customer.getFullName());
        order.setCustomerPhone(finalPhone);
        order.setDeliveryAddress(finalAddress);
        order.setNote(note);
        order.setStatus("PENDING");
        order.setPaymentMethod(finalPaymentMethod);
        order.setSubtotal(subtotal);
        order.setDiscount(BigDecimal.ZERO);
        order.setTaxPercent(new BigDecimal("8.00"));
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
        order.setCreatedAt(LocalDateTime.now());

        if (jdbcTemplate != null) {
            try {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                String insertOrderSql = "INSERT INTO orders (order_code, order_type, customer_id, delivery_address, contact_phone, customer_count, status, subtotal, discount, tax_percent, tax_amount, total_amount, note, created_at) " +
                                        "VALUES (?, ?, ?, ?, ?, 1, 'PENDING', ?, 0.00, 8.00, ?, ?, ?, NOW())";
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, orderCode);
                    ps.setString(2, finalOrderType);
                    ps.setLong(3, customerId);
                    ps.setString(4, finalAddress);
                    ps.setString(5, finalPhone);
                    ps.setBigDecimal(6, finalSubtotal);
                    ps.setBigDecimal(7, finalTaxAmount);
                    ps.setBigDecimal(8, finalTotalAmount);
                    ps.setString(9, finalNote);
                    return ps;
                }, keyHolder);

                Number generatedId = keyHolder.getKey();
                long newOrderId = generatedId != null ? generatedId.longValue() : idGenerator.getAndIncrement();
                order.setId(newOrderId);

                List<OrderItem> orderItems = new ArrayList<>();
                for (CartItem cartItem : cartItems) {
                    OrderItem orderItem = new OrderItem(
                            cartItem.getMenuItem().getId(),
                            cartItem.getMenuItem().getName(),
                            cartItem.getQuantity(),
                            cartItem.getMenuItem().getPriceValue(),
                            cartItem.getNote()
                    );
                    orderItem.setOrderId(newOrderId);
                    orderItems.add(orderItem);

                    String insertItemSql = "INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, note, status) " +
                                           "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
                    jdbcTemplate.update(insertItemSql,
                            newOrderId,
                            cartItem.getMenuItem().getId(),
                            cartItem.getMenuItem().getName(),
                            cartItem.getQuantity(),
                            cartItem.getMenuItem().getPriceValue(),
                            cartItem.getNote()
                    );
                }
                order.setItems(orderItems);

                // Tích điểm thành viên: 1 điểm cho mỗi 50.000 VND
                int pointsEarned = totalAmount.divide(new BigDecimal("50000"), RoundingMode.DOWN).intValue();
                if (pointsEarned > 0) {
                    customerService.addLoyaltyPoints(customerId, pointsEarned);
                }

                cartService.clearCart(customerId);
                return order;
            } catch (Exception e) {
                System.err.println("Lỗi lưu đơn hàng vào MySQL: " + e.getMessage());
            }
        }

        // Fallback in-memory
        long orderId = idGenerator.getAndIncrement();
        order.setId(orderId);
        List<OrderItem> orderItems = new ArrayList<>();
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
        }
        order.setItems(orderItems);
        inMemoryOrders.add(0, order);
        cartService.clearCart(customerId);
        return order;
    }

    public synchronized BigDecimal getTotalRevenue() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'COMPLETED'";
                return jdbcTemplate.queryForObject(sql, BigDecimal.class);
            } catch (Exception e) {
                System.err.println("Lỗi tính doanh thu từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryOrders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public synchronized long getPendingOrderCount() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT COUNT(*) FROM orders WHERE status = 'PENDING'";
                Long count = jdbcTemplate.queryForObject(sql, Long.class);
                return count != null ? count : 0;
            } catch (Exception e) {
                System.err.println("Lỗi đếm pending orders từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryOrders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count();
    }

    // -------------------------------------------------------------
    // NGHIỆP VỤ PHỤC VỤ BÀN (WAITER DESK)
    // -------------------------------------------------------------
    public synchronized Optional<Order> getActiveOrderByTableId(Long tableId) {
        if (tableId == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT o.id, o.order_code, o.order_type, o.customer_id, " +
                             "COALESCE(c.full_name, 'Khách Tại Bàn') AS customer_name, " +
                             "COALESCE(o.contact_phone, c.phone, '') AS customer_phone, " +
                             "o.delivery_address, o.note, o.status, o.is_bill_requested, " +
                             "o.subtotal, o.discount, o.tax_percent, o.tax_amount, o.total_amount, " +
                             "o.created_at, t.table_number " +
                             "FROM orders o " +
                             "LEFT JOIN dining_tables t ON o.table_id = t.id " +
                             "LEFT JOIN customers c ON o.customer_id = c.id " +
                             "WHERE o.table_id = ? AND o.status IN ('PENDING', 'COOKING', 'READY', 'SERVING') " +
                             "ORDER BY o.id DESC LIMIT 1";
                List<Order> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToOrder(rs), tableId);
                if (!list.isEmpty()) {
                    attachItems(list);
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi getActiveOrderByTableId(" + tableId + "): " + e.getMessage());
            }
        }
        return inMemoryOrders.stream()
                .filter(o -> o.getTableNumber() != null &&
                        ("PENDING".equalsIgnoreCase(o.getStatus()) || "COOKING".equalsIgnoreCase(o.getStatus()) || "SERVING".equalsIgnoreCase(o.getStatus())))
                .findFirst();
    }

    public synchronized Order createDineInOrder(Long tableId, String tableNumber, Long employeeId, List<OrderItem> items, String note, int customerCount) {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String orderCode = "ORD-DIN-" + datePrefix + "-" + String.format("%03d", (System.currentTimeMillis() % 1000));

        BigDecimal subtotal = BigDecimal.ZERO;
        if (items != null) {
            for (OrderItem item : items) {
                if (item.getUnitPrice() != null && item.getQuantity() > 0) {
                    subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        }
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        Order order = new Order();
        order.setOrderCode(orderCode);
        order.setOrderType("DINE_IN");
        order.setCustomerName("Khách Tại Bàn " + tableNumber);
        order.setTableNumber(tableNumber);
        order.setNote(note);
        order.setStatus("PENDING");
        order.setPaymentMethod("CASH");
        order.setSubtotal(subtotal);
        order.setDiscount(BigDecimal.ZERO);
        order.setTaxPercent(new BigDecimal("8.00"));
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(totalAmount);
        order.setCreatedAt(LocalDateTime.now());

        if (jdbcTemplate != null) {
            try {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                String insertOrderSql = "INSERT INTO orders (order_code, order_type, table_id, employee_id, customer_count, status, subtotal, discount, tax_percent, tax_amount, total_amount, note, created_at) " +
                                        "VALUES (?, 'DINE_IN', ?, ?, ?, 'PENDING', ?, 0.00, 8.00, ?, ?, ?, NOW())";
                final BigDecimal finalSub = subtotal;
                final BigDecimal finalTax = taxAmount;
                final BigDecimal finalTot = totalAmount;
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, orderCode);
                    ps.setLong(2, tableId);
                    if (employeeId != null) ps.setLong(3, employeeId); else ps.setNull(3, java.sql.Types.INTEGER);
                    ps.setInt(4, customerCount > 0 ? customerCount : 1);
                    ps.setBigDecimal(5, finalSub);
                    ps.setBigDecimal(6, finalTax);
                    ps.setBigDecimal(7, finalTot);
                    ps.setString(8, note);
                    return ps;
                }, keyHolder);

                Number generatedId = keyHolder.getKey();
                long newOrderId = generatedId != null ? generatedId.longValue() : idGenerator.getAndIncrement();
                order.setId(newOrderId);

                if (items != null) {
                    for (OrderItem item : items) {
                        item.setOrderId(newOrderId);
                        item.setStatus("PENDING");
                        String insertItemSql = "INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, note, status) " +
                                               "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
                        jdbcTemplate.update(insertItemSql, newOrderId, item.getMenuItemId(), item.getItemName(), item.getQuantity(), item.getUnitPrice(), item.getNote());
                    }
                }
                order.setItems(items);

                // Cập nhật bàn sang trạng thái OCCUPIED
                jdbcTemplate.update("UPDATE dining_tables SET status = 'OCCUPIED' WHERE id = ?", tableId);
                return order;
            } catch (Exception e) {
                System.err.println("Lỗi createDineInOrder MySQL: " + e.getMessage());
            }
        }

        long orderId = idGenerator.getAndIncrement();
        order.setId(orderId);
        if (items != null) {
            for (OrderItem it : items) it.setOrderId(orderId);
            order.setItems(items);
        }
        inMemoryOrders.add(0, order);
        return order;
    }

    public synchronized void addItemsToExistingOrder(Long orderId, List<OrderItem> newItems) {
        if (orderId == null || newItems == null || newItems.isEmpty()) return;
        if (jdbcTemplate != null) {
            try {
                for (OrderItem item : newItems) {
                    String insertItemSql = "INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, note, status) " +
                                           "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
                    jdbcTemplate.update(insertItemSql, orderId, item.getMenuItemId(), item.getItemName(), item.getQuantity(), item.getUnitPrice(), item.getNote());
                }

                // Cập nhật lại tổng tiền đơn hàng
                String sumSql = "SELECT COALESCE(SUM(quantity * unit_price), 0) FROM order_items WHERE order_id = ?";
                BigDecimal newSubtotal = jdbcTemplate.queryForObject(sumSql, BigDecimal.class, orderId);
                if (newSubtotal == null) newSubtotal = BigDecimal.ZERO;
                BigDecimal newTax = newSubtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
                BigDecimal newTotal = newSubtotal.add(newTax);

                // Cập nhật đơn hàng (đưa trạng thái về COOKING để Bếp biết có món bổ sung cần nấu)
                jdbcTemplate.update("UPDATE orders SET subtotal = ?, tax_amount = ?, total_amount = ?, status = 'PENDING' WHERE id = ?",
                        newSubtotal, newTax, newTotal, orderId);
                return;
            } catch (Exception e) {
                System.err.println("Lỗi addItemsToExistingOrder MySQL: " + e.getMessage());
            }
        }

        Optional<Order> orderOpt = inMemoryOrders.stream().filter(o -> o.getId().equals(orderId)).findFirst();
        if (orderOpt.isPresent()) {
            Order o = orderOpt.get();
            for (OrderItem it : newItems) {
                it.setOrderId(orderId);
                o.getItems().add(it);
            }
            BigDecimal subtotal = o.getItems().stream()
                    .map(it -> it.getUnitPrice().multiply(BigDecimal.valueOf(it.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
            o.setSubtotal(subtotal);
            o.setTaxAmount(taxAmount);
            o.setTotalAmount(subtotal.add(taxAmount));
            o.setStatus("PENDING");
        }
    }

    public synchronized void moveTable(Long orderId, Long fromTableId, Long toTableId) {
        if (orderId == null || toTableId == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE orders SET table_id = ? WHERE id = ?", toTableId, orderId);
                if (fromTableId != null) {
                    jdbcTemplate.update("UPDATE dining_tables SET status = 'AVAILABLE' WHERE id = ?", fromTableId);
                }
                jdbcTemplate.update("UPDATE dining_tables SET status = 'OCCUPIED' WHERE id = ?", toTableId);
                return;
            } catch (Exception e) {
                System.err.println("Lỗi moveTable MySQL: " + e.getMessage());
            }
        }
    }

    public synchronized void markOrderServed(Long orderId) {
        if (orderId == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE order_items SET status = 'SERVED' WHERE order_id = ?", orderId);
                jdbcTemplate.update("UPDATE orders SET status = 'SERVING' WHERE id = ?", orderId);
                return;
            } catch (Exception e) {
                System.err.println("Lỗi markOrderServed: " + e.getMessage());
            }
        }
    }

    public synchronized void requestBill(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE orders SET is_bill_requested = TRUE WHERE order_code = ?", orderCode);
                return;
            } catch (Exception e) {
                System.err.println("Lỗi requestBill: " + e.getMessage());
            }
        }
        inMemoryOrders.stream().filter(o -> orderCode.equalsIgnoreCase(o.getOrderCode()))
                .findFirst().ifPresent(o -> o.setBillRequested(true));
    }

    public synchronized boolean clearTable(Long tableId, Long orderId) {
        if (jdbcTemplate != null) {
            try {
                if (orderId != null) {
                    List<String> statuses = jdbcTemplate.query(
                            "SELECT status FROM orders WHERE id = ?", (rs, rowNum) -> rs.getString("status"), orderId);
                    if (!statuses.isEmpty() && !"COMPLETED".equalsIgnoreCase(statuses.get(0))) {
                        // Không cho phép dọn bàn nếu đơn chưa được thanh toán tại Thu Ngân!
                        return false;
                    }
                }
                if (tableId != null) {
                    jdbcTemplate.update("UPDATE dining_tables SET status = 'AVAILABLE' WHERE id = ?", tableId);
                }
                return true;
            } catch (Exception e) {
                System.err.println("Lỗi clearTable: " + e.getMessage());
            }
        }
        return false;
    }

    // -------------------------------------------------------------
    // NGHIỆP VỤ THU NGÂN (POS & THANH TOÁN HÓA ĐƠN)
    // -------------------------------------------------------------

    public synchronized List<Order> getActiveOrdersForCashier() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT o.id, o.order_code, o.order_type, o.table_id, o.customer_id, " +
                             "COALESCE(c.full_name, 'Khách vãng lai') AS customer_name, " +
                             "COALESCE(c.phone, o.contact_phone) AS customer_phone, " +
                             "o.delivery_address, o.note, o.status, o.is_bill_requested, " +
                             "o.subtotal, o.discount, o.tax_percent, o.tax_amount, o.total_amount, " +
                             "o.created_at, o.completed_at, t.table_number " +
                             "FROM orders o " +
                             "LEFT JOIN dining_tables t ON o.table_id = t.id " +
                             "LEFT JOIN customers c ON o.customer_id = c.id " +
                             "WHERE o.status IN ('PENDING', 'COOKING', 'READY', 'SERVING') " +
                             "ORDER BY o.is_bill_requested DESC, CASE WHEN o.order_type = 'DINE_IN' THEN 0 ELSE 1 END, o.id DESC";
                List<Order> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToOrder(rs));
                attachItems(result);
                return result;
            } catch (Exception e) {
                System.err.println("Lỗi getActiveOrdersForCashier: " + e.getMessage());
            }
        }
        return inMemoryOrders.stream()
                .filter(o -> !"COMPLETED".equalsIgnoreCase(o.getStatus()) && !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());
    }

    public synchronized List<Order> getCompletedOrdersToday() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT o.id, o.order_code, o.order_type, o.table_id, o.customer_id, " +
                             "COALESCE(c.full_name, 'Khách vãng lai') AS customer_name, " +
                             "COALESCE(c.phone, o.contact_phone) AS customer_phone, " +
                             "o.delivery_address, o.note, o.status, o.is_bill_requested, " +
                             "o.subtotal, o.discount, o.tax_percent, o.tax_amount, o.total_amount, " +
                             "o.created_at, o.completed_at, t.table_number, " +
                             "p.payment_method, p.transaction_code, e.full_name AS cashier_name " +
                             "FROM orders o " +
                             "LEFT JOIN dining_tables t ON o.table_id = t.id " +
                             "LEFT JOIN customers c ON o.customer_id = c.id " +
                             "LEFT JOIN payments p ON p.order_id = o.id " +
                             "LEFT JOIN employees e ON p.cashier_id = e.id " +
                             "WHERE o.status = 'COMPLETED' " +
                             "ORDER BY o.completed_at DESC LIMIT 50";
                List<Order> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToOrder(rs));
                attachItems(result);
                return result;
            } catch (Exception e) {
                System.err.println("Lỗi getCompletedOrdersToday: " + e.getMessage());
            }
        }
        return inMemoryOrders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());
    }

    public synchronized boolean checkoutOrder(Long orderId, Long cashierId, String paymentMethod, BigDecimal discount, String transactionCode) {
        if (orderId == null) return false;
        if (paymentMethod == null || paymentMethod.isBlank()) paymentMethod = "CASH";
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;

        if (jdbcTemplate != null) {
            try {
                // 1. Tính toán lại tổng tiền từ chi tiết món
                String sumSql = "SELECT COALESCE(SUM(quantity * unit_price), 0) FROM order_items WHERE order_id = ? AND status != 'CANCELLED'";
                BigDecimal subtotal = jdbcTemplate.queryForObject(sumSql, BigDecimal.class, orderId);
                if (subtotal == null) subtotal = BigDecimal.ZERO;

                if (discount.compareTo(subtotal) > 0) {
                    discount = subtotal;
                }

                BigDecimal afterDiscount = subtotal.subtract(discount);
                BigDecimal taxAmount = afterDiscount.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
                BigDecimal totalAmount = afterDiscount.add(taxAmount);

                // 2. Cập nhật đơn hàng sang COMPLETED
                String updateOrderSql = "UPDATE orders SET status = 'COMPLETED', is_bill_requested = FALSE, subtotal = ?, discount = ?, " +
                                        "tax_amount = ?, total_amount = ?, completed_at = NOW() WHERE id = ?";
                jdbcTemplate.update(updateOrderSql, subtotal, discount, taxAmount, totalAmount, orderId);

                // Đánh dấu toàn bộ món trong đơn là SERVED
                jdbcTemplate.update("UPDATE order_items SET status = 'SERVED' WHERE order_id = ? AND status != 'CANCELLED'", orderId);

                // 3. Ghi nhận thanh toán vào bảng payments
                String paymentSql = "INSERT INTO payments (order_id, cashier_id, amount, payment_method, transaction_code, paid_at) " +
                                    "VALUES (?, ?, ?, ?, ?, NOW()) " +
                                    "ON DUPLICATE KEY UPDATE cashier_id = VALUES(cashier_id), amount = VALUES(amount), " +
                                    "payment_method = VALUES(payment_method), transaction_code = VALUES(transaction_code), paid_at = NOW()";
                jdbcTemplate.update(paymentSql, orderId, cashierId, totalAmount, paymentMethod, transactionCode);

                // 4. Giải phóng bàn ăn và cập nhật đặt bàn nếu có
                List<Integer> tableIds = jdbcTemplate.query("SELECT table_id FROM orders WHERE id = ?", (rs, rowNum) -> rs.getInt("table_id"), orderId);
                if (!tableIds.isEmpty() && tableIds.get(0) != null) {
                    Integer tableId = tableIds.get(0);
                    jdbcTemplate.update("UPDATE dining_tables SET status = 'AVAILABLE' WHERE id = ?", tableId);
                    jdbcTemplate.update("UPDATE reservations SET status = 'COMPLETED' WHERE table_id = ? AND status = 'SEATED'", tableId);
                }

                // 5. Tích điểm thành viên nếu có (10.000đ = 1 điểm)
                List<Integer> custIds = jdbcTemplate.query("SELECT customer_id FROM orders WHERE id = ?", (rs, rowNum) -> rs.getInt("customer_id"), orderId);
                if (!custIds.isEmpty() && custIds.get(0) != null) {
                    int pointsEarned = totalAmount.divide(BigDecimal.valueOf(10000), 0, RoundingMode.FLOOR).intValue();
                    customerService.addLoyaltyPoints((long) custIds.get(0), pointsEarned);
                }

                return true;
            } catch (Exception e) {
                System.err.println("Lỗi checkoutOrder trong MySQL: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // In-memory fallback
        Optional<Order> orderOpt = getOrderById(orderId);
        if (orderOpt.isPresent()) {
            Order o = orderOpt.get();
            o.setStatus("COMPLETED");
            o.setDiscount(discount);
            o.setPaymentMethod(paymentMethod);
            o.setCompletedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }

    public synchronized Map<String, Object> getCashierStatsToday() {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal cashRevenue = BigDecimal.ZERO;
        BigDecimal transferRevenue = BigDecimal.ZERO;
        BigDecimal cardRevenue = BigDecimal.ZERO;
        long completedCount = 0;
        long activeCount = 0;

        if (jdbcTemplate != null) {
            try {
                String revSql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE DATE(paid_at) = CURRENT_DATE()";
                totalRevenue = jdbcTemplate.queryForObject(revSql, BigDecimal.class);
                if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

                String cashSql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_method = 'CASH' AND DATE(paid_at) = CURRENT_DATE()";
                cashRevenue = jdbcTemplate.queryForObject(cashSql, BigDecimal.class);
                if (cashRevenue == null) cashRevenue = BigDecimal.ZERO;

                String transSql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_method = 'BANK_TRANSFER' AND DATE(paid_at) = CURRENT_DATE()";
                transferRevenue = jdbcTemplate.queryForObject(transSql, BigDecimal.class);
                if (transferRevenue == null) transferRevenue = BigDecimal.ZERO;

                String cardSql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_method IN ('CREDIT_CARD', 'E_WALLET') AND DATE(paid_at) = CURRENT_DATE()";
                cardRevenue = jdbcTemplate.queryForObject(cardSql, BigDecimal.class);
                if (cardRevenue == null) cardRevenue = BigDecimal.ZERO;

                String countSql = "SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED' AND DATE(completed_at) = CURRENT_DATE()";
                Long cCount = jdbcTemplate.queryForObject(countSql, Long.class);
                if (cCount != null) completedCount = cCount;

                String activeSql = "SELECT COUNT(*) FROM orders WHERE status IN ('PENDING', 'COOKING', 'READY', 'SERVING')";
                Long aCount = jdbcTemplate.queryForObject(activeSql, Long.class);
                if (aCount != null) activeCount = aCount;
            } catch (Exception e) {
                System.err.println("Lỗi getCashierStatsToday: " + e.getMessage());
            }
        }

        stats.put("totalRevenueToday", totalRevenue);
        stats.put("cashRevenueToday", cashRevenue);
        stats.put("transferRevenueToday", transferRevenue);
        stats.put("cardRevenueToday", cardRevenue);
        stats.put("completedCountToday", completedCount);
        stats.put("activeCount", activeCount);
        return stats;
    }
}
