package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.Ingredient;
import com.restaurant.restaurant_management.model.InventoryReceipt;
import com.restaurant.restaurant_management.model.InventoryReceiptItem;
import com.restaurant.restaurant_management.model.InventoryTransaction;
import com.restaurant.restaurant_management.model.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InventoryService {

    private final JdbcTemplate jdbcTemplate;
    private final List<Ingredient> inMemoryIngredients = new ArrayList<>();
    private final List<Supplier> inMemorySuppliers = new ArrayList<>();
    private final List<InventoryReceipt> inMemoryReceipts = new ArrayList<>();
    private final List<InventoryTransaction> inMemoryTransactions = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(7);

    public InventoryService(@Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Dự phòng bộ nhớ (Fallback)
        inMemoryIngredients.add(new Ingredient(1L, "ING-BEEF-WAGYU", "Thịt bò Úc / Wagyu", "kg", new BigDecimal("25.500"), new BigDecimal("5.000"), new BigDecimal("450000")));
        inMemoryIngredients.add(new Ingredient(2L, "ING-SEABASS", "Cá chẽm tươi phi lê", "kg", new BigDecimal("18.000"), new BigDecimal("4.000"), new BigDecimal("220000")));
        inMemoryIngredients.add(new Ingredient(3L, "ING-CHICKEN", "Thịt gà ta thả vườn", "kg", new BigDecimal("30.000"), new BigDecimal("6.000"), new BigDecimal("110000")));
        inMemoryIngredients.add(new Ingredient(4L, "ING-PRAWN", "Tôm sú tươi", "kg", new BigDecimal("3.200"), new BigDecimal("4.000"), new BigDecimal("280000")));
        inMemoryIngredients.add(new Ingredient(5L, "ING-COCONUT-MILK", "Nước cốt dừa nguyên chất", "lít", new BigDecimal("15.000"), new BigDecimal("5.000"), new BigDecimal("65000")));
        inMemoryIngredients.add(new Ingredient(6L, "ING-RICE", "Gạo thơm ST25", "kg", new BigDecimal("80.000"), new BigDecimal("20.000"), new BigDecimal("35000")));

        inMemorySuppliers.add(new Supplier(1L, "Công ty Thực phẩm Sạch Đà Nẵng", "Anh Minh", "0905111222", "contact@thucphamsachdn.com", "Hải Châu, Đà Nẵng", true));
        inMemorySuppliers.add(new Supplier(2L, "Hải Sản Tươi Sống Miền Trung", "Chị Lan", "0905333444", "sales@haisanmientrung.vn", "Cảng Cá Thọ Quang, Đà Nẵng", true));
        inMemorySuppliers.add(new Supplier(3L, "Nông Trại Rau Hữu Cơ Đà Lạt", "Anh Tuấn", "0905555666", "dalatfarm@organic.vn", "TP. Đà Lạt, Lâm Đồng", true));
    }

    private Ingredient mapRowToIngredient(ResultSet rs) throws SQLException {
        Ingredient ing = new Ingredient();
        ing.setId(rs.getLong("id"));
        ing.setCode(rs.getString("code"));
        ing.setName(rs.getString("name"));
        ing.setUnit(rs.getString("unit"));
        ing.setStockQuantity(rs.getBigDecimal("stock_quantity"));
        ing.setMinStockLevel(rs.getBigDecimal("min_stock_level"));
        ing.setCostPrice(rs.getBigDecimal("cost_price"));
        return ing;
    }

    public synchronized List<Ingredient> getAllIngredients() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, code, name, unit, stock_quantity, min_stock_level, cost_price FROM ingredients ORDER BY id ASC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToIngredient(rs));
            } catch (Exception e) {
                System.err.println("Lỗi getAllIngredients từ MySQL: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryIngredients);
    }

    public synchronized Optional<Ingredient> findById(Long id) {
        if (id == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, code, name, unit, stock_quantity, min_stock_level, cost_price FROM ingredients WHERE id = ?";
                List<Ingredient> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToIngredient(rs), id);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findById ingredient từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryIngredients.stream().filter(i -> i.getId().equals(id)).findFirst();
    }

    public synchronized void updateStock(Long id, BigDecimal newQuantity) {
        if (id == null || newQuantity == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE ingredients SET stock_quantity = ? WHERE id = ?", newQuantity, id);
            } catch (Exception e) {
                System.err.println("Lỗi updateStock trong MySQL: " + e.getMessage());
            }
        }
        findById(id).ifPresent(i -> i.setStockQuantity(newQuantity));
    }

    public synchronized boolean addIngredient(Ingredient ingredient) {
        if (ingredient == null || ingredient.getCode() == null) return false;

        if (jdbcTemplate != null) {
            try {
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ingredients WHERE code = ?", Integer.class, ingredient.getCode().trim());
                if (count != null && count > 0) return false;

                String insertSql = "INSERT INTO ingredients (code, name, unit, stock_quantity, min_stock_level, cost_price) " +
                                   "VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, ingredient.getCode().trim(), ingredient.getName(), ingredient.getUnit(),
                        ingredient.getStockQuantity(), ingredient.getMinStockLevel(), ingredient.getCostPrice());
                return true;
            } catch (Exception e) {
                System.err.println("Lỗi addIngredient vào MySQL: " + e.getMessage());
            }
        }

        boolean exists = inMemoryIngredients.stream().anyMatch(i -> i.getCode().equalsIgnoreCase(ingredient.getCode().trim()));
        if (exists) return false;

        ingredient.setId(idGenerator.getAndIncrement());
        inMemoryIngredients.add(ingredient);
        return true;
    }

    public synchronized long getLowStockCount() {
        if (jdbcTemplate != null) {
            try {
                Long count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM ingredients WHERE stock_quantity <= min_stock_level", Long.class);
                return count != null ? count : 0;
            } catch (Exception e) {
                System.err.println("Lỗi getLowStockCount từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryIngredients.stream().filter(Ingredient::isLowStock).count();
    }

    public synchronized BigDecimal getTotalStockValue() {
        if (jdbcTemplate != null) {
            try {
                BigDecimal val = jdbcTemplate.queryForObject(
                        "SELECT COALESCE(SUM(stock_quantity * cost_price), 0) FROM ingredients", BigDecimal.class);
                return val != null ? val : BigDecimal.ZERO;
            } catch (Exception e) {
                System.err.println("Lỗi getTotalStockValue từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryIngredients.stream()
                .map(i -> i.getStockQuantity().multiply(i.getCostPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public synchronized int getReceiptsCountThisMonth() {
        if (jdbcTemplate != null) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM inventory_receipts WHERE MONTH(created_at) = MONTH(CURRENT_DATE()) AND YEAR(created_at) = YEAR(CURRENT_DATE())", Integer.class);
                return count != null ? count : 0;
            } catch (Exception e) {
                System.err.println("Lỗi getReceiptsCountThisMonth: " + e.getMessage());
            }
        }
        return inMemoryReceipts.size();
    }

    // ==================== SUPPLIERS ====================
    public synchronized List<Supplier> getAllSuppliers() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, name, contact_name, phone, email, address, is_active, created_at, updated_at FROM suppliers ORDER BY id ASC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    Supplier s = new Supplier();
                    s.setId(rs.getLong("id"));
                    s.setName(rs.getString("name"));
                    s.setContactName(rs.getString("contact_name"));
                    s.setPhone(rs.getString("phone"));
                    s.setEmail(rs.getString("email"));
                    s.setAddress(rs.getString("address"));
                    s.setActive(rs.getBoolean("is_active"));
                    Timestamp cAt = rs.getTimestamp("created_at");
                    if (cAt != null) s.setCreatedAt(cAt.toLocalDateTime());
                    return s;
                });
            } catch (Exception e) {
                System.err.println("Lỗi getAllSuppliers: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemorySuppliers);
    }

    public synchronized boolean addSupplier(Supplier supplier) {
        if (supplier == null || supplier.getName() == null || supplier.getPhone() == null) return false;
        if (jdbcTemplate != null) {
            try {
                String sql = "INSERT INTO suppliers (name, contact_name, phone, email, address, is_active) VALUES (?, ?, ?, ?, ?, 1)";
                jdbcTemplate.update(sql, supplier.getName().trim(), supplier.getContactName(), supplier.getPhone().trim(), supplier.getEmail(), supplier.getAddress());
                return true;
            } catch (Exception e) {
                System.err.println("Lỗi addSupplier: " + e.getMessage());
            }
        }
        supplier.setId((long) (inMemorySuppliers.size() + 1));
        inMemorySuppliers.add(supplier);
        return true;
    }

    // ==================== INVENTORY RECEIPTS (NHẬP KHO) ====================
    public synchronized List<InventoryReceipt> getAllReceipts() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT r.id, r.receipt_code, r.supplier_id, s.name AS supplier_name, r.status, " +
                             "r.created_by_employee_id, e.full_name AS created_by_name, " +
                             "r.total_cost, r.note, r.created_at, r.received_at " +
                             "FROM inventory_receipts r " +
                             "LEFT JOIN suppliers s ON r.supplier_id = s.id " +
                             "LEFT JOIN employees e ON r.created_by_employee_id = e.id " +
                             "ORDER BY r.created_at DESC";
                List<InventoryReceipt> receipts = jdbcTemplate.query(sql, (rs, rowNum) -> {
                    InventoryReceipt rec = new InventoryReceipt();
                    rec.setId(rs.getLong("id"));
                    rec.setReceiptCode(rs.getString("receipt_code"));
                    rec.setSupplierId(rs.getLong("supplier_id"));
                    rec.setSupplierName(rs.getString("supplier_name"));
                    rec.setStatus(rs.getString("status"));
                    rec.setCreatedByEmployeeId(rs.getLong("created_by_employee_id"));
                    rec.setCreatedByName(rs.getString("created_by_name"));
                    rec.setTotalCost(rs.getBigDecimal("total_cost"));
                    rec.setNote(rs.getString("note"));
                    Timestamp cAt = rs.getTimestamp("created_at");
                    if (cAt != null) rec.setCreatedAt(cAt.toLocalDateTime());
                    Timestamp rAt = rs.getTimestamp("received_at");
                    if (rAt != null) rec.setReceivedAt(rAt.toLocalDateTime());
                    return rec;
                });

                if (!receipts.isEmpty()) {
                    String itemSql = "SELECT ri.id, ri.receipt_id, ri.ingredient_id, i.code AS ingredient_code, " +
                                     "i.name AS ingredient_name, i.unit, ri.quantity, ri.unit_price, ri.total_price, ri.expiry_date " +
                                     "FROM inventory_receipt_items ri " +
                                     "JOIN ingredients i ON ri.ingredient_id = i.id " +
                                     "ORDER BY ri.id ASC";
                    List<InventoryReceiptItem> allItems = jdbcTemplate.query(itemSql, (rs, rowNum) -> {
                        InventoryReceiptItem item = new InventoryReceiptItem();
                        item.setId(rs.getLong("id"));
                        item.setReceiptId(rs.getLong("receipt_id"));
                        item.setIngredientId(rs.getLong("ingredient_id"));
                        item.setIngredientCode(rs.getString("ingredient_code"));
                        item.setIngredientName(rs.getString("ingredient_name"));
                        item.setUnit(rs.getString("unit"));
                        item.setQuantity(rs.getBigDecimal("quantity"));
                        item.setUnitPrice(rs.getBigDecimal("unit_price"));
                        item.setTotalPrice(rs.getBigDecimal("total_price"));
                        Date exp = rs.getDate("expiry_date");
                        if (exp != null) item.setExpiryDate(exp.toLocalDate());
                        return item;
                    });

                    java.util.Map<Long, List<InventoryReceiptItem>> map = allItems.stream()
                        .collect(java.util.stream.Collectors.groupingBy(InventoryReceiptItem::getReceiptId));
                    for (InventoryReceipt r : receipts) {
                        r.setItems(map.getOrDefault(r.getId(), new ArrayList<>()));
                    }
                }
                return receipts;
            } catch (Exception e) {
                System.err.println("Lỗi getAllReceipts: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryReceipts);
    }

    public synchronized List<InventoryReceiptItem> getAllReceiptItems() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT ri.id, ri.receipt_id, r.receipt_code, s.name AS supplier_name, r.created_at AS received_at, " +
                             "ri.ingredient_id, i.code AS ingredient_code, i.name AS ingredient_name, i.unit, " +
                             "ri.quantity, ri.unit_price, ri.total_price, ri.expiry_date " +
                             "FROM inventory_receipt_items ri " +
                             "JOIN inventory_receipts r ON ri.receipt_id = r.id " +
                             "LEFT JOIN suppliers s ON r.supplier_id = s.id " +
                             "JOIN ingredients i ON ri.ingredient_id = i.id " +
                             "ORDER BY r.created_at DESC, ri.id ASC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    InventoryReceiptItem item = new InventoryReceiptItem();
                    item.setId(rs.getLong("id"));
                    item.setReceiptId(rs.getLong("receipt_id"));
                    item.setReceiptCode(rs.getString("receipt_code"));
                    item.setSupplierName(rs.getString("supplier_name"));
                    Timestamp rAt = rs.getTimestamp("received_at");
                    if (rAt != null) item.setReceivedAt(rAt.toLocalDateTime());
                    item.setIngredientId(rs.getLong("ingredient_id"));
                    item.setIngredientCode(rs.getString("ingredient_code"));
                    item.setIngredientName(rs.getString("ingredient_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setQuantity(rs.getBigDecimal("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    Date exp = rs.getDate("expiry_date");
                    if (exp != null) item.setExpiryDate(exp.toLocalDate());
                    return item;
                });
            } catch (Exception e) {
                System.err.println("Lỗi getAllReceiptItems: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    @Transactional
    public synchronized boolean createReceipt(Long supplierId, Long ingredientId, BigDecimal quantity,
                                             BigDecimal unitPrice, String expiryDateStr, String note, Long employeeId) {
        if (supplierId == null || ingredientId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (unitPrice == null) unitPrice = BigDecimal.ZERO;
        BigDecimal totalPrice = quantity.multiply(unitPrice);
        LocalDate expiryDate = null;
        if (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) {
            try {
                expiryDate = LocalDate.parse(expiryDateStr.trim());
            } catch (Exception ignored) {}
        }

        String receiptCode = "PNK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + (System.currentTimeMillis() % 1000);

        if (jdbcTemplate != null) {
            try {
                // 1. Insert inventory_receipts
                String insertReceiptSql = "INSERT INTO inventory_receipts (receipt_code, supplier_id, status, created_by_employee_id, received_by_employee_id, total_cost, note, received_at) " +
                                          "VALUES (?, ?, 'RECEIVED', ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                KeyHolder keyHolder = new GeneratedKeyHolder();
                LocalDate finalExpiryDate = expiryDate;
                BigDecimal finalUnitPrice = unitPrice;
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertReceiptSql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, receiptCode);
                    ps.setLong(2, supplierId);
                    ps.setLong(3, employeeId != null ? employeeId : 7L);
                    ps.setLong(4, employeeId != null ? employeeId : 7L);
                    ps.setBigDecimal(5, totalPrice);
                    ps.setString(6, note);
                    return ps;
                }, keyHolder);

                long receiptId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : 1L;

                // 2. Insert inventory_receipt_items
                String insertItemSql = "INSERT INTO inventory_receipt_items (receipt_id, ingredient_id, quantity, unit_price, total_price, expiry_date) " +
                                       "VALUES (?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertItemSql, receiptId, ingredientId, quantity, unitPrice, totalPrice, finalExpiryDate != null ? Date.valueOf(finalExpiryDate) : null);

                // 3. Update ingredient stock and price
                String updateIngSql = "UPDATE ingredients SET stock_quantity = stock_quantity + ?, cost_price = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                jdbcTemplate.update(updateIngSql, quantity, unitPrice, ingredientId);

                // 4. Retrieve new balance
                BigDecimal newBalance = jdbcTemplate.queryForObject("SELECT stock_quantity FROM ingredients WHERE id = ?", BigDecimal.class, ingredientId);

                // 5. Insert inventory_transactions
                String insertTxSql = "INSERT INTO inventory_transactions (ingredient_id, transaction_type, quantity, balance_after, unit_price, reference_type, reference_id, employee_id, note) " +
                                     "VALUES (?, 'IMPORT', ?, ?, ?, 'INVENTORY_RECEIPT', ?, ?, ?)";
                jdbcTemplate.update(insertTxSql, ingredientId, quantity, newBalance, unitPrice, receiptId, employeeId != null ? employeeId : 7L, note != null ? note : "Nhập kho theo phiếu " + receiptCode);

                return true;
            } catch (Exception e) {
                System.err.println("Lỗi createReceipt vào MySQL: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        // Memory fallback
        Optional<Ingredient> optIng = findById(ingredientId);
        if (optIng.isPresent()) {
            Ingredient ing = optIng.get();
            ing.setStockQuantity(ing.getStockQuantity().add(quantity));
            ing.setCostPrice(unitPrice);
        }
        return true;
    }

    // ==================== EXPORT / SPOILAGE / ADJUSTMENT ====================
    @Transactional
    public synchronized boolean exportStock(Long ingredientId, BigDecimal quantity, String transactionType, String note, Long employeeId) {
        if (ingredientId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (transactionType == null || transactionType.trim().isEmpty()) {
            transactionType = "EXPORT_KITCHEN";
        }

        if (jdbcTemplate != null) {
            try {
                // Check current stock
                BigDecimal currentStock = jdbcTemplate.queryForObject("SELECT stock_quantity FROM ingredients WHERE id = ?", BigDecimal.class, ingredientId);
                if (currentStock == null || currentStock.compareTo(quantity) < 0) {
                    System.err.println("Không đủ số lượng trong kho để xuất!");
                    return false;
                }

                BigDecimal newBalance = currentStock.subtract(quantity);

                // 1. Update ingredient stock
                jdbcTemplate.update("UPDATE ingredients SET stock_quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", newBalance, ingredientId);

                // Get current cost price
                BigDecimal costPrice = jdbcTemplate.queryForObject("SELECT cost_price FROM ingredients WHERE id = ?", BigDecimal.class, ingredientId);

                // 2. Insert transaction
                String insertTxSql = "INSERT INTO inventory_transactions (ingredient_id, transaction_type, quantity, balance_after, unit_price, reference_type, employee_id, note) " +
                                     "VALUES (?, ?, ?, ?, ?, 'MANUAL_OPERATION', ?, ?)";
                jdbcTemplate.update(insertTxSql, ingredientId, transactionType, quantity.negate(), newBalance, costPrice, employeeId != null ? employeeId : 7L, note);

                return true;
            } catch (Exception e) {
                System.err.println("Lỗi exportStock trong MySQL: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        // Memory fallback
        Optional<Ingredient> optIng = findById(ingredientId);
        if (optIng.isPresent()) {
            Ingredient ing = optIng.get();
            if (ing.getStockQuantity().compareTo(quantity) >= 0) {
                ing.setStockQuantity(ing.getStockQuantity().subtract(quantity));
                return true;
            }
        }
        return false;
    }

    // ==================== AUDIT TRANSACTIONS (LỊCH SỬ BIẾN ĐỘNG) ====================
    public synchronized List<InventoryTransaction> getAllTransactions(int limit) {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT t.id, t.ingredient_id, i.code AS ingredient_code, i.name AS ingredient_name, i.unit, " +
                             "t.transaction_type, t.quantity, t.balance_after, t.unit_price, t.reference_type, t.reference_id, " +
                             "t.employee_id, e.full_name AS employee_name, t.note, t.created_at " +
                             "FROM inventory_transactions t " +
                             "LEFT JOIN ingredients i ON t.ingredient_id = i.id " +
                             "LEFT JOIN employees e ON t.employee_id = e.id " +
                             "ORDER BY t.created_at DESC LIMIT ?";
                return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    InventoryTransaction tx = new InventoryTransaction();
                    tx.setId(rs.getLong("id"));
                    tx.setIngredientId(rs.getLong("ingredient_id"));
                    tx.setIngredientCode(rs.getString("ingredient_code"));
                    tx.setIngredientName(rs.getString("ingredient_name"));
                    tx.setUnit(rs.getString("unit"));
                    tx.setTransactionType(rs.getString("transaction_type"));
                    tx.setQuantity(rs.getBigDecimal("quantity"));
                    tx.setBalanceAfter(rs.getBigDecimal("balance_after"));
                    tx.setUnitPrice(rs.getBigDecimal("unit_price"));
                    tx.setReferenceType(rs.getString("reference_type"));
                    tx.setReferenceId(rs.getLong("reference_id"));
                    tx.setEmployeeId(rs.getLong("employee_id"));
                    tx.setEmployeeName(rs.getString("employee_name"));
                    tx.setNote(rs.getString("note"));
                    Timestamp cAt = rs.getTimestamp("created_at");
                    if (cAt != null) tx.setCreatedAt(cAt.toLocalDateTime());
                    return tx;
                }, limit);
            } catch (Exception e) {
                System.err.println("Lỗi getAllTransactions: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryTransactions);
    }
}
