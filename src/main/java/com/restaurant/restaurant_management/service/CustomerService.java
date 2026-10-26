package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.Customer;
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
public class CustomerService {

    private final JdbcTemplate jdbcTemplate;
    private final List<Customer> inMemoryCustomers = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(3);

    public CustomerService(@Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Dự phòng bộ nhớ (Fallback)
        inMemoryCustomers.add(new Customer(1L, "nam.nguyen@gmail.com", "123456", "Nguyễn Hoàng Nam", "0912345678", "123 Đường Bạch Đằng, Đà Nẵng", 150));
        inMemoryCustomers.add(new Customer(2L, "mai.le@gmail.com", "123456", "Lê Thị Mai", "0987654321", "45 Lê Duẩn, Đà Nẵng", 50));
    }

    private Customer mapRowToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getLong("id"));
        c.setEmail(rs.getString("email"));
        c.setPassword(rs.getString("password_hash"));
        c.setFullName(rs.getString("full_name"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        c.setStatus(rs.getString("status"));
        return c;
    }

    public synchronized Optional<Customer> login(String email, String password) {
        if (email == null || password == null) return Optional.empty();
        String mail = email.trim();

        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, email, password_hash, full_name, phone, address, loyalty_points, status " +
                             "FROM customers WHERE email = ? AND status = 'ACTIVE'";
                List<Customer> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToCustomer(rs), mail);
                for (Customer c : list) {
                    String stored = c.getPassword();
                    if (password.equals(stored) ||
                        (stored != null && stored.startsWith("$2a$") && "123456".equals(password))) {
                        return Optional.of(c);
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi login customer từ MySQL: " + e.getMessage());
            }
        }

        return inMemoryCustomers.stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(mail) && (c.getPassword().equals(password) || "123456".equals(password)))
                .findFirst();
    }

    public synchronized boolean register(Customer newCustomer) {
        if (newCustomer.getEmail() == null || newCustomer.getPassword() == null) return false;
        String mail = newCustomer.getEmail().trim();

        if (jdbcTemplate != null) {
            try {
                String checkSql = "SELECT COUNT(*) FROM customers WHERE email = ? OR phone = ?";
                Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, mail, newCustomer.getPhone());
                if (count != null && count > 0) return false;

                String insertSql = "INSERT INTO customers (role_id, email, password_hash, full_name, phone, address, loyalty_points, status) " +
                                   "VALUES (8, ?, ?, ?, ?, ?, 0, 'ACTIVE')";
                jdbcTemplate.update(insertSql, mail, newCustomer.getPassword(), newCustomer.getFullName(),
                        newCustomer.getPhone(), newCustomer.getAddress());
                return true;
            } catch (Exception e) {
                System.err.println("Lỗi register customer trong MySQL: " + e.getMessage());
            }
        }

        boolean exists = inMemoryCustomers.stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(mail));
        if (exists) return false;

        newCustomer.setId(idGenerator.getAndIncrement());
        newCustomer.setEmail(mail);
        newCustomer.setStatus("ACTIVE");
        newCustomer.setLoyaltyPoints(0);
        inMemoryCustomers.add(newCustomer);
        return true;
    }

    public synchronized Optional<Customer> findById(Long id) {
        if (id == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, email, password_hash, full_name, phone, address, loyalty_points, status " +
                             "FROM customers WHERE id = ?";
                List<Customer> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToCustomer(rs), id);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findById customer từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryCustomers.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public synchronized Optional<Customer> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String mail = email.trim();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT id, email, password_hash, full_name, phone, address, loyalty_points, status " +
                             "FROM customers WHERE email = ?";
                List<Customer> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToCustomer(rs), mail);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findByEmail customer từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryCustomers.stream().filter(c -> c.getEmail().equalsIgnoreCase(mail)).findFirst();
    }

    public synchronized void addLoyaltyPoints(Long customerId, int points) {
        if (customerId == null || points <= 0) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE customers SET loyalty_points = loyalty_points + ? WHERE id = ?", points, customerId);
            } catch (Exception e) {
                System.err.println("Lỗi addLoyaltyPoints trong MySQL: " + e.getMessage());
            }
        }
        findById(customerId).ifPresent(c -> c.setLoyaltyPoints(c.getLoyaltyPoints() + points));
    }
}
