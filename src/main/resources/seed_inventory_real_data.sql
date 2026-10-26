-- Script cập nhật dữ liệu THẬT cho phân hệ Kho & Vật Tư (Thủ kho)
-- Bổ sung đầy đủ nguyên liệu, nhà cung cấp, phiếu nhập, lịch sử xuất nhập tồn và định lượng món ăn

USE restaurant_db;

-- 1. Bổ sung / Cập nhật Nhà cung cấp thực tế
INSERT INTO suppliers (id, name, contact_name, phone, email, address, is_active)
VALUES
(1, 'Công ty CP Thực Phẩm Sạch Hoàng Gia', 'Anh Minh (Trưởng đại diện)', '0905111222', 'contact@thucphamhoanggia.vn', 'Quận Hải Châu, TP. Đà Nẵng', 1),
(2, 'Vựa Hải Sản Tươi Sống Vịnh Miền Trung', 'Chị Lan (Phụ trách bán sỉ)', '0905333444', 'sales@haisanmientrung.vn', 'Cảng Cá Thọ Quang, Sơn Trà, TP. Đà Nẵng', 1),
(3, 'Nông Trại Rau Củ Hữu Cơ Đà Lạt GAP', 'Anh Tuấn (Giám đốc nông trường)', '0905555666', 'dalatfarm@organic.vn', 'Phường 7, TP. Đà Lạt, Lâm Đồng', 1),
(4, 'Công ty Nước Mắm Truyền Thống Phú Quốc', 'Chị Thu (Điều phối NPP)', '0918999888', 'phuquoc@nuocmamtruyenthong.vn', 'Dương Đông, TP. Phú Quốc, Kiên Giang', 1),
(5, 'Nhà Phân Phối Bơ Sữa & Gia Vị Tân Nhất Hương', 'Anh Khoa (Bộ phận Horeca)', '0908777666', 'horeca@tannhathuong.com', 'Khu Công Nghiệp Hòa Khánh, Đà Nẵng', 1),
(6, 'Vựa Lúa Gạo Đặc Sản ST25 Sóc Trăng', 'Chú Sáu (Chủ vựa)', '0913888999', 'gaost25@soctrangrice.vn', 'Thị xã Ngã Năm, Tỉnh Sóc Trăng', 1)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    contact_name = VALUES(contact_name),
    phone = VALUES(phone),
    email = VALUES(email),
    address = VALUES(address);

-- 2. Bổ sung Danh mục Nguyên vật liệu đầy đủ cho 12 món ăn
INSERT INTO ingredients (id, code, name, unit, stock_quantity, min_stock_level, cost_price)
VALUES
(1, 'ING-BEEF-WAGYU', 'Thịt bò Úc / Wagyu', 'kg', 25.500, 5.000, 450000.00),
(2, 'ING-SEABASS', 'Cá chẽm tươi phi lê', 'kg', 26.500, 4.000, 210000.00),
(3, 'ING-CHICKEN', 'Thịt gà ta thả vườn', 'kg', 30.000, 6.000, 110000.00),
(4, 'ING-PRAWN', 'Tôm sú biển tươi', 'kg', 3.200, 4.000, 280000.00), -- ⚠️ Sắp hết
(5, 'ING-COCONUT-MILK', 'Nước cốt dừa nguyên chất', 'lít', 15.000, 5.000, 65000.00),
(6, 'ING-RICE', 'Gạo thơm ST25 Sóc Trăng', 'kg', 80.000, 20.000, 35000.00),
(7, 'ING-PORK-BELLY', 'Thịt ba chỉ heo quế sạch', 'kg', 18.000, 5.000, 165000.00),
(8, 'ING-RICE-PAPER', 'Bánh tráng phơi sương Trảng Bàng', 'xấp', 45.000, 10.000, 22000.00),
(9, 'ING-NOODLES', 'Bún tươi sợi nhỏ Sa Đéc', 'kg', 2.500, 5.000, 18000.00), -- ⚠️ Sắp hết
(10, 'ING-HERBS-SALAD', 'Rau sống & xà lách hữu cơ', 'kg', 12.000, 4.000, 45000.00),
(11, 'ING-LOTUS-ROOT', 'Củ sen Đồng Tháp tươi', 'kg', 14.000, 4.000, 75000.00),
(12, 'ING-TEMPURA-FLOUR', 'Bột chiên giòn Mikko cao cấp', 'gói', 35.000, 10.000, 18000.00),
(13, 'ING-COOKING-OIL', 'Dầu ăn Simply hảo hạng (Can 5L)', 'can', 8.000, 2.000, 245000.00),
(14, 'ING-POMELO', 'Bưởi da xanh Bến Tre ruột hồng', 'kg', 16.000, 5.000, 65000.00),
(15, 'ING-EGGPLANT', 'Cà tím tròn Đà Lạt', 'kg', 15.000, 5.000, 35000.00),
(16, 'ING-MUSHROOMS', 'Nấm đông cô & đùi gà tươi', 'kg', 9.000, 3.000, 120000.00),
(17, 'ING-TOFU', 'Đậu hũ non hữu cơ Nhật Bản', 'hộp', 20.000, 6.000, 15000.00),
(18, 'ING-BROCCOLI', 'Bông cải xanh Đà Lạt loại 1', 'kg', 2.800, 4.000, 55000.00), -- ⚠️ Sắp hết
(19, 'ING-CARROT', 'Cà rốt Đà Lạt hữu cơ', 'kg', 16.000, 5.000, 32000.00),
(20, 'ING-WHIPPING-CREAM', 'Kem béo Whipping Cream Anchor', 'lít', 12.000, 4.000, 165000.00),
(21, 'ING-BUTTER', 'Bơ lạt Pháp President cao cấp', 'kg', 8.500, 3.000, 280000.00),
(22, 'ING-BLACK-PEPPER', 'Tiêu đen Phú Quốc thơm nguyên hạt', 'kg', 6.000, 2.000, 190000.00),
(23, 'ING-RED-WINE', 'Rượu vang đỏ Bordeaux nấu ăn', 'chai', 10.000, 3.000, 250000.00),
(24, 'ING-HONEY', 'Mật ong hoa rừng tự nhiên', 'lít', 7.500, 2.000, 320000.00),
(25, 'ING-COFFEE-BEANS', 'Cà phê hạt Robusta Buôn Ma Thuột', 'kg', 15.000, 5.000, 210000.00),
(26, 'ING-LOTUS-TEA', 'Trà búp ướp hoa sen Tây Hồ', 'hộp', 18.000, 5.000, 380000.00),
(27, 'ING-FISH-SAUCE', 'Nước mắm Khải Hoàn 40 độ đạm', 'chai', 24.000, 6.000, 125000.00)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    unit = VALUES(unit),
    stock_quantity = VALUES(stock_quantity),
    min_stock_level = VALUES(min_stock_level),
    cost_price = VALUES(cost_price);

-- 3. Tạo các Phiếu Nhập Kho thực tế với đầy đủ đối tác
INSERT INTO inventory_receipts (id, receipt_code, supplier_id, status, created_by_employee_id, received_by_employee_id, total_cost, note, created_at, received_at)
VALUES
(1, 'PNK-20261020-001', 1, 'RECEIVED', 7, 7, 11475000.00, 'Nhập lô thịt bò Wagyu & gia vị đầu tuần', '2026-10-23 09:30:00', '2026-10-23 09:30:00'),
(2, 'PNK-20261022-002', 2, 'RECEIVED', 7, 7, 5600000.00, 'Nhập cá chẽm tươi phi lê & tôm sú Thọ Quang', '2026-10-24 08:15:00', '2026-10-24 08:15:00'),
(3, 'PNK-20261024-003', 3, 'RECEIVED', 7, 7, 2850000.00, 'Nhập rau củ quả hữu cơ Đà Lạt giao sáng sớm', '2026-10-25 07:45:00', '2026-10-25 07:45:00'),
(4, 'PNK-20261025-004', 5, 'RECEIVED', 7, 7, 4360000.00, 'Nhập bơ lạt President & kem béo Anchor cho tráng miệng', '2026-10-25 14:20:00', '2026-10-25 14:20:00'),
(5, 'PNK-20261026-005', 6, 'RECEIVED', 7, 7, 2800000.00, 'Nhập gạo thơm ST25 Sóc Trăng phục vụ ca tuần mới', '2026-10-26 10:00:00', '2026-10-26 10:00:00')
ON DUPLICATE KEY UPDATE 
    total_cost = VALUES(total_cost),
    note = VALUES(note);

-- Chi tiết từng phiếu nhập
INSERT INTO inventory_receipt_items (receipt_id, ingredient_id, quantity, unit_price, total_price, expiry_date)
VALUES
-- Phiếu 1
(1, 1, 25.500, 450000.00, 11475000.00, '2026-11-20'),
-- Phiếu 2
(2, 2, 20.000, 210000.00, 4200000.00, '2026-11-05'),
(2, 4, 5.000, 280000.00, 1400000.00, '2026-11-02'),
-- Phiếu 3
(3, 10, 15.000, 45000.00, 675000.00, '2026-10-31'),
(3, 11, 15.000, 75000.00, 1125000.00, '2026-11-10'),
(3, 19, 20.000, 32000.00, 640000.00, '2026-11-15'),
(3, 18, 7.500, 55000.00, 412500.00, '2026-10-31'),
-- Phiếu 4
(4, 20, 12.000, 165000.00, 1980000.00, '2026-12-15'),
(4, 21, 8.500, 280000.00, 2380000.00, '2026-12-30'),
-- Phiếu 5
(5, 6, 80.000, 35000.00, 2800000.00, '2027-04-26')
ON DUPLICATE KEY UPDATE 
    total_price = VALUES(total_price);

-- 4. Bổ sung Lịch sử Giao dịch Xuất Nhập Tồn phong phú
INSERT INTO inventory_transactions (ingredient_id, transaction_type, quantity, balance_after, unit_price, reference_type, reference_id, employee_id, note, created_at)
VALUES
(1, 'IMPORT', 25.500, 25.500, 450000.00, 'INVENTORY_RECEIPT', 1, 7, 'Nhập kho lô bò Wagyu từ Hoàng Gia', '2026-10-23 09:35:00'),
(2, 'IMPORT', 20.000, 20.000, 210000.00, 'INVENTORY_RECEIPT', 2, 7, 'Nhập cá chẽm tươi từ Cảng Thọ Quang', '2026-10-24 08:20:00'),
(4, 'IMPORT', 5.000, 5.000, 280000.00, 'INVENTORY_RECEIPT', 2, 7, 'Nhập tôm sú biển tươi sống', '2026-10-24 08:20:00'),
(1, 'EXPORT_KITCHEN', -2.000, 23.500, 450000.00, 'KITCHEN_REQUISITION', 101, 7, 'Xuất cho bếp trưởng Lê làm món bò om', '2026-10-25 15:00:00'),
(1, 'SPOILAGE', -0.200, 23.300, 450000.00, 'SPOILAGE_REPORT', 501, 5, 'Hao hụt gân mỡ không đạt tiêu chuẩn', '2026-10-25 17:30:00'),
(4, 'RECIPE_CONSUMPTION', -1.800, 3.200, 280000.00, 'ORDER_AUTO_DEDUCT', 102, 5, 'Chế biến các phần Gỏi bưởi tôm sú cho khách bàn T1', '2026-10-26 12:45:00'),
(18, 'RECIPE_CONSUMPTION', -4.700, 2.800, 55000.00, 'ORDER_AUTO_DEDUCT', 103, 5, 'Chế biến món Cơm thố rau củ mùa ca trưa', '2026-10-26 13:10:00'),
(9, 'EXPORT_KITCHEN', -3.000, 2.500, 18000.00, 'KITCHEN_REQUISITION', 104, 7, 'Xuất bún tươi Sa Đéc cuốn gỏi cuốn', '2026-10-26 14:00:00'),
(20, 'IMPORT', 12.000, 12.000, 165000.00, 'INVENTORY_RECEIPT', 4, 7, 'Nhập kem béo Whipping cream Anchor', '2026-10-25 14:25:00'),
(6, 'IMPORT', 80.000, 80.000, 35000.00, 'INVENTORY_RECEIPT', 5, 7, 'Nhập 80kg gạo đặc sản ST25', '2026-10-26 10:05:00')
ON DUPLICATE KEY UPDATE note = VALUES(note);

-- 5. Định lượng chi tiết liên kết Món ăn và Nguyên liệu (recipe_items)
DELETE FROM recipe_items;
INSERT INTO recipe_items (id, menu_item_id, ingredient_id, quantity_required, note)
VALUES
-- 1. Fresh Spring Rolls (Gỏi cuốn tôm thịt)
(1, 1, 4, 0.080, 'Tôm sú tươi hấp chín bóc vỏ (kg)'),
(2, 1, 7, 0.080, 'Thịt ba chỉ luộc thái mỏng (kg)'),
(3, 1, 8, 1.000, 'Bánh tráng phơi sương (xấp)'),
(4, 1, 9, 0.100, 'Bún tươi sợi nhỏ (kg)'),
(5, 1, 10, 0.060, 'Xà lách, rau thơm các loại (kg)'),

-- 2. Crispy Lotus Root (Củ sen chiên giòn)
(6, 2, 11, 0.250, 'Củ sen tươi cắt lát tròn (kg)'),
(7, 2, 12, 0.100, 'Bột chiên giòn Mikko (gói)'),
(8, 2, 13, 0.040, 'Dầu ăn Simply chiên ngập dầu (can)'),

-- 3. Pomelo & Prawn Salad (Gỏi bưởi tôm sú)
(9, 3, 14, 0.250, 'Tép bưởi da xanh tách múi (kg)'),
(10, 3, 4, 0.120, 'Tôm sú tươi luộc sơ (kg)'),
(11, 3, 10, 0.040, 'Rau răm, húng quế (kg)'),
(12, 3, 27, 0.030, 'Nước mắm trộn gỏi (chai)'),

-- 4. Grilled Eggplant (Cà tím nướng mỡ hành)
(13, 4, 15, 0.300, 'Cà tím nướng than hoa (kg)'),
(14, 4, 13, 0.030, 'Dầu ăn phi mỡ hành (can)'),
(15, 4, 27, 0.030, 'Nước mắm tỏi ớt chua ngọt (chai)'),

-- 5. Charcoal-Grilled Chicken (Gà nướng than hoa)
(16, 5, 3, 0.400, 'Thịt đùi gà ta rút xương (kg)'),
(17, 5, 24, 0.020, 'Mật ong hoa rừng quét da (lít)'),
(18, 5, 22, 0.010, 'Tiêu đen Phú Quốc ướp gà (kg)'),

-- 6. Seared Sea Bass (Cá chẽm áp chảo)
(19, 6, 2, 0.300, 'Phi lê cá chẽm tươi áp chảo (kg)'),
(20, 6, 21, 0.030, 'Bơ lạt Pháp sốt bơ chanh (kg)'),
(21, 6, 18, 0.100, 'Bông cải xanh ăn kèm (kg)'),

-- 7. Slow-Braised Beef (Bò om chậm kiểu Pháp)
(22, 7, 1, 0.350, 'Thịt bò Wagyu cắt khối vuông (kg)'),
(23, 7, 23, 0.080, 'Rượu vang đỏ Bordeaux om thịt (chai)'),
(24, 7, 22, 0.020, 'Tiêu đen Phú Quốc giã dập (kg)'),
(25, 7, 21, 0.030, 'Bơ lạt Anchor làm bóng sốt (kg)'),

-- 8. Seasonal Vegetarian Bowl (Cơm thố rau củ mùa)
(26, 8, 6, 0.200, 'Gạo thơm ST25 nấu cơm niêu (kg)'),
(27, 8, 16, 0.100, 'Nấm đông cô & đùi gà xào (kg)'),
(28, 8, 17, 1.000, 'Đậu hũ non chiên vàng (hộp)'),
(29, 8, 18, 0.080, 'Bông cải xanh Đà Lạt (kg)'),
(30, 8, 19, 0.060, 'Cà rốt tỉa hoa (kg)'),

-- 9. Coconut Panna Cotta (Panna Cotta dừa non)
(31, 9, 5, 0.120, 'Nước cốt dừa nguyên chất (lít)'),
(32, 9, 20, 0.080, 'Whipping cream tạo độ béo (lít)'),

-- 11. Vietnamese Coffee Crème (Kem cà phê phin Việt)
(33, 11, 25, 0.050, 'Cà phê Robusta pha phin (kg)'),
(34, 11, 20, 0.080, 'Kem béo Whipping cream (lít)'),

-- 12. Tea Selection (Trà sen Tây Hồ tuyển chọn)
(35, 12, 26, 0.050, 'Trà búp ướp hoa sen Tây Hồ (hộp)'),
(36, 12, 24, 0.015, 'Mật ong hoa rừng thanh ngọt (lít)');
