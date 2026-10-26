-- ========================================================
-- CƠ SỞ DỮ LIỆU HỆ THỐNG QUẢN LÝ NHÀ HÀNG (RESTAURANT MANAGEMENT)
-- Phiên bản: 2.3 (Bổ sung Role Khách Hàng CUSTOMER, Bắt buộc đăng nhập để đặt món online, Giỏ hàng)
-- ========================================================

CREATE DATABASE IF NOT EXISTS restaurant_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE restaurant_db;

-- --------------------------------------------------------
-- 1. NHÓM BẢNG QUẢN LÝ THỰC ĐƠN (MENU & CATEGORIES)
-- Phục vụ: Khách xem thực đơn trên web & Nhân viên lên món
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Tên danh mục (Khai vị, Món chính, Tráng miệng, Đồ uống...)',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT 'Mã danh mục (starters, main-courses, desserts, drinks)',
    description TEXT NULL COMMENT 'Mô tả ngắn danh mục',
    display_order INT DEFAULT 0 COMMENT 'Thứ tự hiển thị trên website',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Ẩn/hiện trên website',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    name VARCHAR(150) NOT NULL COMMENT 'Tên món ăn',
    slug VARCHAR(180) UNIQUE NULL COMMENT 'Đường dẫn tĩnh phục vụ SEO',
    description TEXT NULL COMMENT 'Mô tả thành phần, hương vị món ăn',
    price DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT 'Giá niêm yết bán ra (VND)',
    image_url VARCHAR(255) NULL COMMENT 'Ảnh món ăn',
    is_available BOOLEAN DEFAULT TRUE COMMENT 'Còn món / Hết món trong bếp',
    is_featured BOOLEAN DEFAULT FALSE COMMENT 'Món tiêu biểu / Best seller hiển thị trang chủ',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_menu_item_price CHECK (price >= 0),
    CONSTRAINT fk_menu_item_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_menu_category ON menu_items(category_id);
CREATE INDEX idx_menu_available ON menu_items(is_available);

-- --------------------------------------------------------
-- 2. NHÓM BẢNG VAI TRÒ & NGƯỜI DÙNG (ROLES, EMPLOYEES & CUSTOMERS)
-- 8 Vai trò: ADMIN, MANAGER, RECEPTIONIST, WAITER, CHEF, CASHIER, STOREKEEPER, CUSTOMER
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL COMMENT 'Mã vai trò: ADMIN, MANAGER, RECEPTIONIST, WAITER, CHEF, CASHIER, STOREKEEPER, CUSTOMER',
    description VARCHAR(255) NULL COMMENT 'Mô tả quyền hạn của vai trò'
) ENGINE=InnoDB;

-- Bảng tài khoản nhân viên nội bộ
CREATE TABLE IF NOT EXISTS employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT 'Tài khoản đăng nhập nội bộ',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã băm (BCrypt)',
    full_name VARCHAR(100) NOT NULL COMMENT 'Họ tên nhân viên',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT 'Số điện thoại',
    email VARCHAR(100) UNIQUE NULL COMMENT 'Email nhân viên',
    status ENUM('ACTIVE', 'SUSPENDED', 'RESIGNED') DEFAULT 'ACTIVE' COMMENT 'Trạng thái hoạt động',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_employee_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_employee_username ON employees(username);

-- Bảng tài khoản khách hàng thành viên (Bắt buộc đăng nhập để đặt món trên web)
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL COMMENT 'Vai trò của khách hàng (mặc định role_id của CUSTOMER)',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT 'Email đăng nhập của khách hàng',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Mật khẩu đã băm (BCrypt)',
    full_name VARCHAR(100) NOT NULL COMMENT 'Họ tên khách hàng',
    phone VARCHAR(20) UNIQUE NOT NULL COMMENT 'Số điện thoại liên hệ nhận món/đặt bàn',
    address VARCHAR(255) NULL COMMENT 'Địa chỉ mặc định giao hàng',
    loyalty_points INT NOT NULL DEFAULT 0 COMMENT 'Điểm tích lũy thành viên',
    status ENUM('ACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái tài khoản',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_customer_points CHECK (loyalty_points >= 0),
    CONSTRAINT fk_customer_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_phone ON customers(phone);

-- --------------------------------------------------------
-- 3. NHÓM BẢNG QUẢN LÝ BÀN ĂN & KHU VỰC (TABLES & SEATING)
-- Đồng bộ trạng thái do tầng Service (Application Layer) đảm nhiệm
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS dining_tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_number VARCHAR(20) UNIQUE NOT NULL COMMENT 'Số bàn: B01, B02, VIP01...',
    area VARCHAR(50) DEFAULT 'Main Dining Room' COMMENT 'Khu vực: Tầng 1, Tầng 2, Sân vườn, VIP...',
    capacity INT NOT NULL DEFAULT 4 COMMENT 'Số lượng ghế tối đa',
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE') DEFAULT 'AVAILABLE' 
        COMMENT 'Trạng thái bàn: Trống, Có khách ngồi, Đã xếp cho khách đặt trước, Đang bảo trì',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_dining_table_capacity CHECK (capacity > 0)
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 4. NHÓM BẢNG ĐẶT BÀN & LIÊN HỆ TỪ WEBSITE (RESERVATIONS & CONTACTS)
-- Liên kết customer_id nếu khách đã đăng nhập khi đặt bàn
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NULL COMMENT 'ID khách hàng thành viên (nếu đã đăng nhập tài khoản)',
    customer_name VARCHAR(100) NOT NULL COMMENT 'Tên khách hàng đặt bàn',
    phone VARCHAR(20) NOT NULL COMMENT 'Số điện thoại liên hệ',
    email VARCHAR(100) NULL COMMENT 'Email khách hàng (tùy chọn)',
    reservation_date DATE NOT NULL COMMENT 'Ngày dùng bữa',
    reservation_time TIME NOT NULL COMMENT 'Giờ đặt dùng bữa chính xác (vd: 12:00:00, 19:30:00)',
    guest_count INT NOT NULL COMMENT 'Số lượng khách (số nguyên > 0)',
    message TEXT NULL COMMENT 'Ghi chú / Yêu cầu đặc biệt (sinh nhật, dị ứng...)',
    status ENUM('PENDING', 'CONFIRMED', 'SEATED', 'CANCELLED', 'NO_SHOW') DEFAULT 'PENDING'
        COMMENT 'PENDING: Mới gửi, CONFIRMED: Lễ tân đã xác nhận, SEATED: Đã vào bàn, CANCELLED: Đã hủy',
    table_id INT NULL COMMENT 'Bàn được Lễ tân/Quản lý xếp cho khách',
    handled_by_employee_id INT NULL COMMENT 'Nhân viên lễ tân/quản lý trực tiếp xử lý',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_reservation_guest_count CHECK (guest_count > 0),
    CONSTRAINT fk_reservation_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE SET NULL,
    CONSTRAINT fk_reservation_table FOREIGN KEY (table_id) REFERENCES dining_tables (id) ON DELETE SET NULL,
    CONSTRAINT fk_reservation_employee FOREIGN KEY (handled_by_employee_id) REFERENCES employees (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_reservation_date_time ON reservations(reservation_date, reservation_time);
CREATE INDEX idx_reservation_status ON reservations(status);

CREATE TABLE IF NOT EXISTS contact_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Họ tên người liên hệ',
    email VARCHAR(100) NOT NULL COMMENT 'Email người liên hệ',
    phone VARCHAR(20) NULL COMMENT 'Số điện thoại',
    subject VARCHAR(200) NULL COMMENT 'Chủ đề liên hệ / phản hồi',
    message TEXT NOT NULL COMMENT 'Nội dung tin nhắn',
    is_processed BOOLEAN DEFAULT FALSE COMMENT 'Trạng thái nhân viên đã xử lý phản hồi chưa',
    processed_by INT NULL COMMENT 'Nhân viên phụ trách phản hồi',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_contact_employee FOREIGN KEY (processed_by) REFERENCES employees (id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 5. GIỎ HÀNG KHÁCH HÀNG TRÊN WEBSITE (SHOPPING CART)
-- Khách đã đăng nhập lưu tạm các món ăn trước khi bấm Đặt món
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL COMMENT 'Khách hàng sở hữu giỏ hàng',
    menu_item_id INT NOT NULL COMMENT 'Món ăn chọn vào giỏ',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Số lượng',
    note VARCHAR(255) NULL COMMENT 'Ghi chú chế biến riêng',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_cart_item_quantity CHECK (quantity > 0),
    CONSTRAINT uq_customer_menu_cart UNIQUE (customer_id, menu_item_id),
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_menu_item FOREIGN KEY (menu_item_id) REFERENCES menu_items (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_cart_customer ON cart_items(customer_id);

-- --------------------------------------------------------
-- 6. NHÓM BẢNG ĐƠN HÀNG, GỌI MÓN & THANH TOÁN (ORDERS, ORDER_ITEMS & PAYMENTS)
-- Bắt buộc customer_id khi khách đặt online (ORDER_TYPE: TAKE_AWAY, DELIVERY)
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(30) UNIQUE NOT NULL COMMENT 'Mã đơn (vd: ORD-20261022-001)',
    order_type ENUM('DINE_IN', 'TAKE_AWAY', 'DELIVERY') NOT NULL DEFAULT 'DINE_IN'
        COMMENT 'Loại đơn: DINE_IN (Ăn tại bàn), TAKE_AWAY (Đặt mang về), DELIVERY (Giao hàng tận nơi)',
    customer_id INT NULL COMMENT 'Khách hàng thành viên đặt món (Bắt buộc đăng nhập khi đặt qua Website)',
    table_id INT NULL COMMENT 'Bàn sử dụng (NULL khi đơn mang về/giao hàng hoặc chưa xếp bàn)',
    employee_id INT NULL COMMENT 'Nhân viên phục vụ phụ trách (NULL khi đặt online hoặc chưa phân công)',
    reservation_id INT NULL COMMENT 'Liên kết đặt bàn trước (nếu có)',
    delivery_address VARCHAR(255) NULL COMMENT 'Địa chỉ nhận hàng (nếu là DELIVERY)',
    contact_phone VARCHAR(20) NULL COMMENT 'Số điện thoại nhận hàng của khách',
    customer_count INT NOT NULL DEFAULT 1 COMMENT 'Số lượng khách thực tế tại bàn',
    status ENUM('PENDING', 'COOKING', 'READY', 'SERVING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING'
        COMMENT 'Trạng thái order: PENDING (Chờ chế biến), COOKING (Đang nấu), READY (Bếp nấu xong/Chờ bưng), SERVING (Khách đang dùng bữa), COMPLETED, CANCELLED',
    is_bill_requested BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Phục vụ đã gửi yêu cầu thanh toán sang thu ngân',
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT 'Tổng tiền món (Service tính từ chi tiết món)',
    discount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT 'Giảm giá (VND) - không vượt quá subtotal',
    tax_percent DECIMAL(5, 2) NOT NULL DEFAULT 8.00 COMMENT 'Tỷ lệ thuế VAT (% từ 0 đến 100)',
    tax_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT 'Tiền thuế VAT (VND)',
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT 'Tổng tiền thanh toán cuối cùng do Service tính',
    note TEXT NULL COMMENT 'Ghi chú chung của đơn',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL COMMENT 'Thời điểm thanh toán hoàn tất',
    CONSTRAINT chk_order_customer_count CHECK (customer_count > 0),
    CONSTRAINT chk_order_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_order_discount CHECK (discount >= 0 AND discount <= subtotal),
    CONSTRAINT chk_order_tax_percent CHECK (tax_percent >= 0 AND tax_percent <= 100),
    CONSTRAINT chk_order_tax_amount CHECK (tax_amount >= 0),
    CONSTRAINT chk_order_total_amount CHECK (total_amount >= 0),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE SET NULL,
    CONSTRAINT fk_order_table FOREIGN KEY (table_id) REFERENCES dining_tables (id) ON DELETE SET NULL,
    CONSTRAINT fk_order_employee FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_order_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_customer ON orders(customer_id);
CREATE INDEX idx_order_created_at ON orders(created_at);

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_id INT NULL COMMENT 'Món ăn trong thực đơn (có thể NULL nếu sau này bị xóa)',
    item_name VARCHAR(150) NOT NULL COMMENT 'Tên món ăn tại thời điểm đặt (snapshot giữ lịch sử khi menu đổi tên)',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Số lượng gọi món',
    unit_price DECIMAL(12, 2) NOT NULL COMMENT 'Đơn giá lấy từ menu_items tại thời điểm gọi (snapshot giá)',
    note VARCHAR(255) NULL COMMENT 'Ghi chú chế biến (ít cay, không hành...)',
    status ENUM('PENDING', 'COOKING', 'SERVED', 'CANCELLED') DEFAULT 'PENDING'
        COMMENT 'Trạng thái món: Chờ bếp nhận, Đang nấu, Đã lên bàn/giao món, Đã hủy',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_order_item_order ON order_items(order_id);
CREATE INDEX idx_order_item_status ON order_items(status);

CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL UNIQUE,
    cashier_id INT NULL COMMENT 'Thu ngân nhận thanh toán (NULL nếu khách thanh toán online qua cổng thanh toán)',
    amount DECIMAL(12, 2) NOT NULL COMMENT 'Số tiền thực tế thanh toán',
    payment_method ENUM('CASH', 'CREDIT_CARD', 'BANK_TRANSFER', 'E_WALLET', 'ONLINE_GATEWAY') NOT NULL DEFAULT 'CASH'
        COMMENT 'Phương thức: Tiền mặt, Thẻ, Chuyển khoản QR, Ví điện tử, Cổng thanh toán trực tuyến',
    transaction_code VARCHAR(100) NULL COMMENT 'Mã tham chiếu giao dịch ngân hàng / POS / Cổng thanh toán',
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_amount CHECK (amount >= 0),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_cashier FOREIGN KEY (cashier_id) REFERENCES employees (id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- --------------------------------------------------------
-- 7. NHÓM BẢNG QUẢN LÝ KHO NGUYÊN LIỆU (INVENTORY MANAGEMENT)
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL COMMENT 'Tên nhà cung cấp thực phẩm',
    contact_name VARCHAR(100) NULL COMMENT 'Người đại diện liên hệ',
    phone VARCHAR(20) NOT NULL COMMENT 'Số điện thoại nhà cung cấp',
    email VARCHAR(100) NULL COMMENT 'Email nhà cung cấp',
    address VARCHAR(255) NULL COMMENT 'Địa chỉ nhà cung cấp',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Còn hợp tác hay không',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ingredients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL COMMENT 'Mã nguyên liệu (vd: ING-BEEF, ING-RICE)',
    name VARCHAR(150) NOT NULL COMMENT 'Tên nguyên liệu (Thịt bò Wagyu, Gạo tấm, Hành baro...)',
    unit VARCHAR(20) NOT NULL COMMENT 'Đơn vị tính (kg, g, lít, ml, quả, gói...)',
    stock_quantity DECIMAL(12, 3) NOT NULL DEFAULT 0.000 COMMENT 'Số lượng tồn kho thực tế',
    min_stock_level DECIMAL(12, 3) NOT NULL DEFAULT 5.000 COMMENT 'Ngưỡng cảnh báo tồn tối thiểu để báo nhập thêm',
    cost_price DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT 'Giá vốn ước tính trung bình',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_ingredient_stock CHECK (stock_quantity >= 0),
    CONSTRAINT chk_ingredient_min_stock CHECK (min_stock_level >= 0),
    CONSTRAINT chk_ingredient_cost_price CHECK (cost_price >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory_receipts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    receipt_code VARCHAR(50) UNIQUE NOT NULL COMMENT 'Mã phiếu nhập (vd: PNK-20261022-001)',
    supplier_id INT NOT NULL COMMENT 'Nhà cung cấp',
    status ENUM('DRAFT', 'RECEIVED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT'
        COMMENT 'Trạng thái phiếu: DRAFT (nháp), RECEIVED (đã nhập kho), CANCELLED (hủy)',
    created_by_employee_id INT NOT NULL COMMENT 'Nhân viên tạo phiếu nhập (thủ kho)',
    received_by_employee_id INT NULL COMMENT 'Nhân viên thực tế nhận và kiểm hàng',
    total_cost DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT 'Tổng giá trị nhập hàng (Service tính)',
    note TEXT NULL COMMENT 'Ghi chú đợt nhập hàng',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm lập phiếu',
    received_at TIMESTAMP NULL COMMENT 'Thời điểm thực tế nhận hàng và nhập vào kho',
    CONSTRAINT chk_receipt_total_cost CHECK (total_cost >= 0),
    CONSTRAINT fk_receipt_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_receipt_creator FOREIGN KEY (created_by_employee_id) REFERENCES employees (id) ON DELETE RESTRICT,
    CONSTRAINT fk_receipt_receiver FOREIGN KEY (received_by_employee_id) REFERENCES employees (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_receipt_status ON inventory_receipts(status);

CREATE TABLE IF NOT EXISTS inventory_receipt_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    receipt_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    quantity DECIMAL(12, 3) NOT NULL COMMENT 'Số lượng nhập',
    unit_price DECIMAL(12, 2) NOT NULL COMMENT 'Đơn giá nhập vào',
    total_price DECIMAL(12, 2) NOT NULL COMMENT 'Thành tiền = quantity * unit_price',
    expiry_date DATE NULL COMMENT 'Hạn sử dụng của lô nguyên liệu',
    CONSTRAINT chk_receipt_item_qty CHECK (quantity > 0),
    CONSTRAINT chk_receipt_item_price CHECK (unit_price >= 0),
    CONSTRAINT chk_receipt_item_total CHECK (total_price >= 0),
    CONSTRAINT fk_receipt_item_receipt FOREIGN KEY (receipt_id) REFERENCES inventory_receipts (id) ON DELETE CASCADE,
    CONSTRAINT fk_receipt_item_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS recipe_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    menu_item_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    quantity_required DECIMAL(12, 3) NOT NULL COMMENT 'Khối lượng nguyên liệu tiêu hao cho 1 phần ăn',
    note VARCHAR(150) NULL COMMENT 'Ghi chú sơ chế',
    CONSTRAINT chk_recipe_qty CHECK (quantity_required > 0),
    CONSTRAINT uq_recipe_menu_ingredient UNIQUE (menu_item_id, ingredient_id),
    CONSTRAINT fk_recipe_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id INT NOT NULL COMMENT 'Nguyên liệu biến động',
    transaction_type ENUM('IMPORT', 'EXPORT_KITCHEN', 'RECIPE_CONSUMPTION', 'ADJUSTMENT', 'SPOILAGE') NOT NULL 
        COMMENT 'Loại giao dịch: IMPORT (Nhập kho), EXPORT_KITCHEN (Xuất cho bếp), RECIPE_CONSUMPTION (Tiêu hao theo món), ADJUSTMENT (Điều chỉnh kiểm kê), SPOILAGE (Hao hụt/Hỏng/Hết hạn)',
    quantity DECIMAL(12, 3) NOT NULL COMMENT 'Số lượng biến động (+ tăng tồn kho, - giảm tồn kho)',
    balance_after DECIMAL(12, 3) NOT NULL COMMENT 'Số lượng tồn kho tức thời sau giao dịch',
    unit_price DECIMAL(12, 2) NULL COMMENT 'Đơn giá vốn tại thời điểm giao dịch',
    reference_type VARCHAR(50) NULL COMMENT 'Loại chứng từ tham chiếu (INVENTORY_RECEIPT, ORDER, AUDIT_SHEET...)',
    reference_id INT NULL COMMENT 'ID chứng từ tham chiếu',
    employee_id INT NOT NULL COMMENT 'Nhân viên thực hiện thao tác (Thủ kho/Bếp/Quản lý)',
    note TEXT NULL COMMENT 'Lý do / Diễn giải chi tiết',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_trans_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredients (id) ON DELETE RESTRICT,
    CONSTRAINT fk_inv_trans_employee FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_inv_trans_ingredient ON inventory_transactions(ingredient_id);
CREATE INDEX idx_inv_trans_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_inv_trans_created_at ON inventory_transactions(created_at);
