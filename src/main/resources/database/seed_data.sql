-- ========================================================
-- DỮ LIỆU KHỞI TẠO MẪU (SEED DATA) - PHIÊN BẢN 2.3
-- 8 Vai trò (có CUSTOMER), Tài khoản khách hàng, Giỏ hàng, Đơn online, Thực đơn & Kho
-- ========================================================

USE restaurant_db;

-- 1. Thêm danh mục món ăn (Categories)
INSERT INTO categories (id, name, code, description, display_order, is_active) VALUES
(1, 'Starters · Khai vị', 'starters', 'Món khai vị tươi ngon kích thích vị giác', 1, TRUE),
(2, 'Main courses · Món chính', 'main-courses', 'Món chính đậm đà mang phong vị Việt Nam hiện đại', 2, TRUE),
(3, 'Desserts · Tráng miệng', 'desserts', 'Món tráng miệng thanh nhẹ kết thúc bữa tiệc trọn vẹn', 3, TRUE);

-- 2. Thêm món ăn (Menu Items) - Khớp thực đơn website kèm hình ảnh
INSERT INTO menu_items (id, category_id, name, slug, description, price, image_url, is_available, is_featured) VALUES
-- Khai vị
(1, 1, 'Fresh Spring Rolls', 'fresh-spring-rolls', 'Herbs, seasonal greens, rice noodles and dipping sauce.', 95000.00, '/images/dishes/fresh-spring-rolls.jpg', TRUE, TRUE),
(2, 1, 'Crispy Lotus Root', 'crispy-lotus-root', 'Lightly crisp lotus root with a bright, savoury dressing.', 110000.00, '/images/dishes/crispy-lotus-root.jpg', TRUE, FALSE),
(3, 1, 'Pomelo & Prawn Salad', 'pomelo-prawn-salad', 'Pomelo, fresh herbs, prawns and toasted peanuts.', 145000.00, '/images/dishes/pomelo-prawn-salad.jpg', TRUE, TRUE),
(4, 1, 'Grilled Eggplant', 'grilled-eggplant', 'Smoky eggplant, spring onion oil and roasted sesame.', 105000.00, '/images/dishes/grilled-eggplant.jpg', TRUE, FALSE),

-- Món chính
(5, 2, 'Charcoal-Grilled Chicken', 'charcoal-grilled-chicken', 'Vietnamese spices, fragrant rice and seasonal vegetables.', 245000.00, '/images/dishes/charcoal-grilled-chicken.jpg', TRUE, TRUE),
(6, 2, 'Seared Sea Bass', 'seared-sea-bass', 'Fresh fish, ginger broth and green vegetables.', 320000.00, '/images/dishes/seared-sea-bass.jpg', TRUE, TRUE),
(7, 2, 'Slow-Braised Beef', 'slow-braised-beef', 'Tender beef, aromatic herbs and roasted root vegetables.', 350000.00, '/images/dishes/slow-braised-beef.jpg', TRUE, TRUE),
(8, 2, 'Seasonal Vegetarian Bowl', 'seasonal-vegetarian-bowl', 'Market vegetables, mushrooms, rice and house dressing.', 195000.00, '/images/dishes/seasonal-vegetarian-bowl.jpg', TRUE, FALSE),

-- Tráng miệng
(9, 3, 'Coconut Panna Cotta', 'coconut-panna-cotta', 'Coconut cream, tropical fruit and toasted coconut.', 95000.00, '/images/dishes/coconut-panna-cotta.jpg', TRUE, TRUE),
(10, 3, 'Seasonal Fruit Plate', 'seasonal-fruit-plate', 'A selection of fresh fruit from the season.', 85000.00, '/images/dishes/seasonal-fruit-plate.jpg', TRUE, FALSE),
(11, 3, 'Vietnamese Coffee Crème', 'vietnamese-coffee-creme', 'A silky dessert with the aroma of Vietnamese coffee.', 105000.00, '/images/dishes/vietnamese-coffee-creme.jpg', TRUE, FALSE),
(12, 3, 'Tea Selection', 'tea-selection', 'Ask our team about today\'s Vietnamese tea selection.', 75000.00, '/images/dishes/tea-selection.jpg', TRUE, FALSE);

-- 3. Khởi tạo đủ 8 vai trò hệ thống (Bổ sung CUSTOMER)
INSERT INTO roles (id, role_name, description) VALUES
(1, 'ADMIN', 'Quản trị viên toàn quyền hệ thống'),
(2, 'MANAGER', 'Quản lý nhà hàng, điều phối vận hành và phê duyệt'),
(3, 'RECEPTIONIST', 'Lễ tân: Tiếp nhận đặt bàn online, đón khách tại sảnh và xếp bàn'),
(4, 'WAITER', 'Nhân viên phục vụ bàn: Ghi order, phục vụ món'),
(5, 'CHEF', 'Bếp: Nhận đơn chế biến, yêu cầu xuất kho nguyên liệu'),
(6, 'CASHIER', 'Thu ngân: Kiểm tra bill, thanh toán và in hóa đơn'),
(7, 'STOREKEEPER', 'Thủ kho: Quản lý nhập - xuất - tồn kho và kiểm kê nguyên vật liệu'),
(8, 'CUSTOMER', 'Khách hàng thành viên: Đăng nhập website để xem ưu đãi, đặt món và đặt bàn');

-- 4. Thêm tài khoản nhân viên nội bộ mẫu (Employees)
-- Mật khẩu mặc định: 123456
INSERT INTO employees (id, role_id, username, password_hash, full_name, phone, email, status) VALUES
(1, 1, 'admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Quản Trị Viên', '0901000001', 'admin@godrestaurant.vn', 'ACTIVE'),
(2, 2, 'manager01', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Trần Quản Lý', '0901000002', 'manager@godrestaurant.vn', 'ACTIVE'),
(3, 3, 'reception01', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Hoàng Lễ Tân', '0901000003', 'receptionist@godrestaurant.vn', 'ACTIVE'),
(4, 4, 'waiter01', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Nguyễn Văn Phục Vụ', '0901000004', 'waiter01@godrestaurant.vn', 'ACTIVE'),
(5, 5, 'chef01', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Lê Bếp Trưởng', '0901000005', 'chef01@godrestaurant.vn', 'ACTIVE'),
(6, 6, 'cashier01', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Phạm Thu Ngân', '0901000006', 'cashier01@godrestaurant.vn', 'ACTIVE'),
(7, 7, 'storekeeper01', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Vũ Thủ Kho', '0901000007', 'kho@godrestaurant.vn', 'ACTIVE');

-- 5. Thêm tài khoản khách hàng thành viên mẫu (Customers)
-- Đăng nhập bằng email / mật khẩu: 123456
INSERT INTO customers (id, role_id, email, password_hash, full_name, phone, address, loyalty_points, status) VALUES
(1, 8, 'nam.nguyen@gmail.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Nguyễn Hoàng Nam', '0912345678', '123 Đường Bạch Đằng, Quận Hải Châu, Đà Nẵng', 150, 'ACTIVE'),
(2, 8, 'mai.le@gmail.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8/lYQ/L.N1u60Vq6s1h1y2sY073zKO', 'Lê Thị Mai', '0987654321', '45 Lê Duẩn, Quận Hải Châu, Đà Nẵng', 50, 'ACTIVE');

-- 6. Thêm danh sách bàn ăn (Dining Tables)
INSERT INTO dining_tables (id, table_number, area, capacity, status) VALUES
(1, 'T1-01', 'Tầng 1 - Sảnh chính', 2, 'AVAILABLE'),
(2, 'T1-02', 'Tầng 1 - Sảnh chính', 4, 'AVAILABLE'),
(3, 'T1-03', 'Tầng 1 - Sảnh chính', 4, 'OCCUPIED'),
(4, 'T1-04', 'Tầng 1 - Sảnh chính', 6, 'RESERVED'),
(5, 'T2-01', 'Tầng 2 - Ban công', 2, 'AVAILABLE'),
(6, 'T2-02', 'Tầng 2 - Ban công', 4, 'AVAILABLE'),
(7, 'VIP-01', 'Phòng VIP Hoàng Gia', 10, 'AVAILABLE');

-- 7. Giỏ hàng mẫu của khách hàng đang lướt xem web (Cart Items)
INSERT INTO cart_items (customer_id, menu_item_id, quantity, note) VALUES
(2, 5, 1, 'Ướp cay vừa'),
(2, 9, 2, 'Tráng miệng sau khi ăn');

-- 8. Đặt bàn từ website có liên kết tài khoản thành viên (Reservations)
INSERT INTO reservations (customer_id, customer_name, phone, email, reservation_date, reservation_time, guest_count, message, status, table_id, handled_by_employee_id) VALUES
(1, 'Nguyễn Hoàng Nam', '0912345678', 'nam.nguyen@gmail.com', CURDATE(), '19:00:00', 4, 'Kỷ niệm ngày cưới, mong muốn bàn gần cửa sổ.', 'CONFIRMED', 4, 3),
(2, 'Lê Thị Mai', '0987654321', 'mai.le@gmail.com', DATE_ADD(CURDATE(), INTERVAL 1 DAY), '12:00:00', 2, 'Cần chuẩn bị ghế trẻ em.', 'PENDING', NULL, NULL);

-- 9. Dữ liệu mẫu tin nhắn liên hệ từ website (Contact Messages)
INSERT INTO contact_messages (name, email, phone, subject, message, is_processed) VALUES
('Đỗ Thu Hà', 'ha.do@example.com', '0933123456', 'Đặt tiệc sinh nhật trọn gói', 'Tôi muốn hỏi gói đặt tiệc sinh nhật cho 15 người vào cuối tuần này.', FALSE);

-- 10. Dữ liệu Quản lý Kho: Nhà cung cấp (Suppliers)
INSERT INTO suppliers (id, name, contact_name, phone, email, address) VALUES
(1, 'Công ty Thực phẩm Sạch Đà Nẵng', 'Anh Minh', '0905111222', 'contact@thucphamsachdn.com', 'Quận Hải Châu, Đà Nẵng'),
(2, 'Hải Sản Tươi Sống Miền Trung', 'Chị Lan', '0905333444', 'sales@haisanmientrung.vn', 'Cảng Cá Thọ Quang, Đà Nẵng'),
(3, 'Nông Trại Rau Hữu Cơ Đà Lạt', 'Anh Tuấn', '0905555666', 'dalatfarm@organic.vn', 'TP. Đà Lạt, Lâm Đồng');

-- 11. Dữ liệu Quản lý Kho: Nguyên vật liệu (Ingredients)
INSERT INTO ingredients (id, code, name, unit, stock_quantity, min_stock_level, cost_price) VALUES
(1, 'ING-BEEF-WAGYU', 'Thịt bò Úc / Wagyu', 'kg', 25.500, 5.000, 450000.00),
(2, 'ING-SEABASS', 'Cá chẽm tươi phi lê', 'kg', 18.000, 4.000, 220000.00),
(3, 'ING-CHICKEN', 'Thịt gà ta thả vườn', 'kg', 30.000, 6.000, 110000.00),
(4, 'ING-PRAWN', 'Tôm sú tươi', 'kg', 12.000, 3.000, 280000.00),
(5, 'ING-COCONUT-MILK', 'Nước cốt dừa nguyên chất', 'lít', 15.000, 5.000, 65000.00),
(6, 'ING-RICE', 'Gạo thơm ST25', 'kg', 80.000, 20.000, 35000.00);

-- 12. Định lượng món ăn (Recipe Items / BOM)
INSERT INTO recipe_items (menu_item_id, ingredient_id, quantity_required, note) VALUES
(7, 1, 0.350, 'Thịt bò cắt khối ức/dẻ sườn'),
(6, 2, 0.300, 'Phi lê cá chẽm tươi'),
(5, 3, 0.400, 'Đùi gà ta ướp gia vị than hoa'),
(3, 4, 0.150, 'Tôm sú bóc vỏ hấp chín'),
(9, 5, 0.120, 'Nước cốt dừa tươi cho Panna Cotta');

-- 13. Phiếu nhập kho (Inventory Receipts)
INSERT INTO inventory_receipts (id, receipt_code, supplier_id, status, created_by_employee_id, received_by_employee_id, total_cost, note, created_at, received_at) VALUES
(1, 'PNK-20261020-001', 1, 'RECEIVED', 7, 7, 11475000.00, 'Nhập thịt bò Wagyu đầu tuần', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO inventory_receipt_items (receipt_id, ingredient_id, quantity, unit_price, total_price, expiry_date) VALUES
(1, 1, 25.500, 450000.00, 11475000.00, DATE_ADD(CURDATE(), INTERVAL 14 DAY));

-- 14. Thẻ kho / Lịch sử biến động kho (Inventory Transactions)
INSERT INTO inventory_transactions (ingredient_id, transaction_type, quantity, balance_after, unit_price, reference_type, reference_id, employee_id, note, created_at) VALUES
(1, 'IMPORT', 25.500, 25.500, 450000.00, 'INVENTORY_RECEIPT', 1, 7, 'Nhập kho thịt bò Wagyu từ Cty Thực phẩm Sạch', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 'EXPORT_KITCHEN', -2.000, 23.500, 450000.00, 'KITCHEN_REQUISITION', 101, 7, 'Xuất cho bếp trưởng Lê chế biến món bò om', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 'SPOILAGE', -0.200, 23.300, 450000.00, 'SPOILAGE_REPORT', 501, 5, 'Hao hụt gân mỡ không đạt tiêu chuẩn sơ chế', NOW());

-- 15. Dữ liệu Đơn hàng (Orders)
-- Đơn 1: Khách tại bàn T1-03 (DINE_IN)
INSERT INTO orders (id, order_code, order_type, customer_id, table_id, employee_id, customer_count, status, subtotal, discount, tax_percent, tax_amount, total_amount, note) VALUES
(1, 'ORD-20261022-001', 'DINE_IN', 1, 3, 4, 2, 'SERVING', 445000.00, 0.00, 8.00, 35600.00, 480600.00, 'Khách bàn 3');

INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, note, status) VALUES
(1, 1, 'Fresh Spring Rolls', 1, 95000.00, 'Ít cay', 'SERVED'),
(1, 7, 'Slow-Braised Beef', 1, 350000.00, 'Nấu chín mềm', 'COOKING');

-- Đơn 2: Khách hàng đăng nhập trên website đặt giao tận nơi (DELIVERY)
INSERT INTO orders (id, order_code, order_type, customer_id, table_id, employee_id, delivery_address, contact_phone, customer_count, status, subtotal, discount, tax_percent, tax_amount, total_amount, note) VALUES
(2, 'ORD-20261023-002', 'DELIVERY', 2, NULL, NULL, '45 Lê Duẩn, Quận Hải Châu, Đà Nẵng', '0987654321', 1, 'PENDING', 340000.00, 0.00, 8.00, 27200.00, 367200.00, 'Giao trước 12h trưa');

INSERT INTO order_items (order_id, menu_item_id, item_name, quantity, unit_price, note, status) VALUES
(2, 5, 'Charcoal-Grilled Chicken', 1, 245000.00, 'Kèm thêm nước chấm', 'PENDING'),
(2, 9, 'Coconut Panna Cotta', 1, 95000.00, 'Để lạnh', 'PENDING');
