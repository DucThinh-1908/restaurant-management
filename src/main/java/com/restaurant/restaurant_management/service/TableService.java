package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.DiningTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TableService {

    private final JdbcTemplate jdbcTemplate;
    private final List<DiningTable> inMemoryTables = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(8);

    public TableService(@Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Khởi tạo 7 bàn mẫu dự phòng bộ nhớ (khớp seed_data.sql)
        inMemoryTables.add(new DiningTable(1L, "T1-01", "Tầng 1 - Sảnh chính", 2, "AVAILABLE"));
        inMemoryTables.add(new DiningTable(2L, "T1-02", "Tầng 1 - Sảnh chính", 4, "AVAILABLE"));
        inMemoryTables.add(new DiningTable(3L, "T1-03", "Tầng 1 - Sảnh chính", 4, "OCCUPIED"));
        inMemoryTables.add(new DiningTable(4L, "T1-04", "Tầng 1 - Sảnh chính", 6, "RESERVED"));
        inMemoryTables.add(new DiningTable(5L, "T2-01", "Tầng 2 - Ban công", 2, "AVAILABLE"));
        inMemoryTables.add(new DiningTable(6L, "T2-02", "Tầng 2 - Ban công", 4, "AVAILABLE"));
        inMemoryTables.add(new DiningTable(7L, "VIP-01", "Phòng VIP Hoàng Gia", 10, "AVAILABLE"));
    }

    private DiningTable mapRowToTable(ResultSet rs) throws SQLException {
        DiningTable table = new DiningTable();
        table.setId(rs.getLong("id"));
        table.setTableNumber(rs.getString("table_number"));
        table.setArea(rs.getString("area"));
        table.setCapacity(rs.getInt("capacity"));
        table.setStatus(rs.getString("status"));
        return table;
    }

    public synchronized List<DiningTable> getAllTables() {
        if (jdbcTemplate != null) {
            try {
                // Tự động đồng bộ trạng thái bàn ăn với các đơn đặt bàn đang ngồi / đang giữ bàn
                try {
                    jdbcTemplate.update(
                        "UPDATE dining_tables t " +
                        "JOIN reservations r ON r.table_id = t.id " +
                        "SET t.status = 'OCCUPIED' " +
                        "WHERE r.status = 'SEATED' AND t.status != 'OCCUPIED'"
                    );
                    jdbcTemplate.update(
                        "UPDATE dining_tables t " +
                        "JOIN reservations r ON r.table_id = t.id " +
                        "SET t.status = 'RESERVED' " +
                        "WHERE r.status = 'CONFIRMED' AND t.status = 'AVAILABLE'"
                    );
                } catch (Exception syncEx) {
                    // Không chặn truy vấn nếu bảng đang khóa hoặc có lỗi nhỏ
                }

                String sql = "SELECT id, table_number, area, capacity, status FROM dining_tables ORDER BY id ASC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToTable(rs));
            } catch (Exception e) {
                System.err.println("Lỗi truy vấn dining_tables từ MySQL: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryTables);
    }

    public synchronized Optional<DiningTable> findById(Long id) {
        if (id == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, table_number, area, capacity, status FROM dining_tables WHERE id = ?";
                List<DiningTable> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToTable(rs), id);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findById table #" + id + ": " + e.getMessage());
            }
        }
        return inMemoryTables.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public synchronized Optional<DiningTable> findByTableNumber(String tableNumber) {
        if (tableNumber == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, table_number, area, capacity, status FROM dining_tables WHERE table_number = ?";
                List<DiningTable> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToTable(rs), tableNumber.trim());
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findByTableNumber " + tableNumber + ": " + e.getMessage());
            }
        }
        return inMemoryTables.stream().filter(t -> t.getTableNumber().equalsIgnoreCase(tableNumber.trim())).findFirst();
    }

    public synchronized void updateStatus(Long id, String status) {
        if (id == null || status == null) return;
        if (jdbcTemplate != null) {
            try {
                String sql = "UPDATE dining_tables SET status = ? WHERE id = ?";
                jdbcTemplate.update(sql, status, id);
            } catch (Exception e) {
                System.err.println("Lỗi cập nhật status table #" + id + ": " + e.getMessage());
            }
        }
        findById(id).ifPresent(t -> t.setStatus(status));
    }

    public synchronized boolean addTable(DiningTable table) {
        if (table == null || table.getTableNumber() == null) return false;
        boolean exists = getAllTables().stream().anyMatch(t -> t.getTableNumber().equalsIgnoreCase(table.getTableNumber().trim()));
        if (exists) return false;

        if (jdbcTemplate != null) {
            try {
                String sql = "INSERT INTO dining_tables (table_number, area, capacity, status) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(sql, table.getTableNumber().trim(), table.getArea(), table.getCapacity(),
                        table.getStatus() != null ? table.getStatus() : "AVAILABLE");
                return true;
            } catch (Exception e) {
                System.err.println("Lỗi thêm bàn vào MySQL: " + e.getMessage());
            }
        }

        table.setId(idGenerator.getAndIncrement());
        inMemoryTables.add(table);
        return true;
    }
}
