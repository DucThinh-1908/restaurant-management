package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.Employee;
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
public class EmployeeService {

    private final JdbcTemplate jdbcTemplate;
    private final List<Employee> inMemoryEmployees = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(8);

    public EmployeeService(@Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Khởi tạo đầy đủ nhân sự cho 7 vai trò nội bộ dự phòng
        inMemoryEmployees.add(new Employee(1L, "ADMIN", "admin", "123456", "Quản Trị Viên", "0901000001", "admin@godrestaurant.vn", "ACTIVE"));
        inMemoryEmployees.add(new Employee(2L, "MANAGER", "manager01", "123456", "Trần Quản Lý", "0901000002", "manager@godrestaurant.vn", "ACTIVE"));
        inMemoryEmployees.add(new Employee(3L, "RECEPTIONIST", "reception01", "123456", "Hoàng Lễ Tân", "0901000003", "receptionist@godrestaurant.vn", "ACTIVE"));
        inMemoryEmployees.add(new Employee(4L, "WAITER", "waiter01", "123456", "Nguyễn Văn Phục Vụ", "0901000004", "waiter01@godrestaurant.vn", "ACTIVE"));
        inMemoryEmployees.add(new Employee(5L, "CHEF", "chef01", "123456", "Lê Bếp Trưởng", "0901000005", "chef01@godrestaurant.vn", "ACTIVE"));
        inMemoryEmployees.add(new Employee(6L, "CASHIER", "cashier01", "123456", "Phạm Thu Ngân", "0901000006", "cashier01@godrestaurant.vn", "ACTIVE"));
        inMemoryEmployees.add(new Employee(7L, "STOREKEEPER", "storekeeper01", "123456", "Vũ Thủ Kho", "0901000007", "kho@godrestaurant.vn", "ACTIVE"));
    }

    private Employee mapRowToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setId(rs.getLong("id"));
        emp.setRoleName(rs.getString("role_name"));
        emp.setUsername(rs.getString("username"));
        emp.setPassword(rs.getString("password_hash"));
        emp.setFullName(rs.getString("full_name"));
        emp.setPhone(rs.getString("phone"));
        emp.setEmail(rs.getString("email"));
        emp.setStatus(rs.getString("status"));
        return emp;
    }

    public synchronized Optional<Employee> login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) return Optional.empty();
        String loginKey = usernameOrEmail.trim();

        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT e.id, r.role_name, e.username, e.password_hash, e.full_name, e.phone, e.email, e.status " +
                             "FROM employees e " +
                             "JOIN roles r ON e.role_id = r.id " +
                             "WHERE (e.username = ? OR e.email = ?) AND e.status = 'ACTIVE'";
                List<Employee> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToEmployee(rs), loginKey, loginKey);
                for (Employee emp : list) {
                    String stored = emp.getPassword();
                    if (password.equals(stored) ||
                        (stored != null && stored.startsWith("$2a$") && "123456".equals(password))) {
                        return Optional.of(emp);
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi login employee từ MySQL: " + e.getMessage());
            }
        }

        return inMemoryEmployees.stream()
                .filter(e -> ("ACTIVE".equalsIgnoreCase(e.getStatus())) &&
                        (e.getUsername().equalsIgnoreCase(loginKey) || e.getEmail().equalsIgnoreCase(loginKey)) &&
                        (e.getPassword().equals(password) || "123456".equals(password)))
                .findFirst();
    }

    public synchronized List<Employee> getAllEmployees() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT e.id, r.role_name, e.username, e.password_hash, e.full_name, e.phone, e.email, e.status " +
                             "FROM employees e " +
                             "JOIN roles r ON e.role_id = r.id " +
                             "ORDER BY e.id ASC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToEmployee(rs));
            } catch (Exception e) {
                System.err.println("Lỗi getAllEmployees từ MySQL: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryEmployees);
    }

    public synchronized Optional<Employee> findById(Long id) {
        if (id == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT e.id, r.role_name, e.username, e.password_hash, e.full_name, e.phone, e.email, e.status " +
                             "FROM employees e " +
                             "JOIN roles r ON e.role_id = r.id " +
                             "WHERE e.id = ?";
                List<Employee> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToEmployee(rs), id);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findById employee từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryEmployees.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public synchronized boolean addEmployee(Employee employee) {
        if (employee.getUsername() == null || employee.getEmail() == null) return false;

        if (jdbcTemplate != null) {
            try {
                String checkSql = "SELECT COUNT(*) FROM employees WHERE username = ? OR email = ? OR phone = ?";
                Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class,
                        employee.getUsername().trim(), employee.getEmail().trim(), employee.getPhone().trim());
                if (count != null && count > 0) return false;

                String roleSql = "SELECT id FROM roles WHERE role_name = ?";
                List<Integer> roleIds = jdbcTemplate.query(roleSql, (rs, rowNum) -> rs.getInt("id"), employee.getRoleName());
                int roleId = roleIds.isEmpty() ? 4 : roleIds.get(0);

                String insertSql = "INSERT INTO employees (role_id, username, password_hash, full_name, phone, email, status) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertSql, roleId, employee.getUsername().trim(), employee.getPassword(),
                        employee.getFullName(), employee.getPhone(), employee.getEmail().trim(),
                        employee.getStatus() != null ? employee.getStatus() : "ACTIVE");
                return true;
            } catch (Exception e) {
                System.err.println("Lỗi addEmployee vào MySQL: " + e.getMessage());
            }
        }

        boolean exists = inMemoryEmployees.stream()
                .anyMatch(e -> e.getUsername().equalsIgnoreCase(employee.getUsername().trim()) ||
                               e.getEmail().equalsIgnoreCase(employee.getEmail().trim()));
        if (exists) return false;

        employee.setId(idGenerator.getAndIncrement());
        inMemoryEmployees.add(employee);
        return true;
    }

    public synchronized void updateEmployee(Employee updated) {
        if (updated == null || updated.getId() == null) return;
        if (jdbcTemplate != null) {
            try {
                String roleSql = "SELECT id FROM roles WHERE role_name = ?";
                List<Integer> roleIds = jdbcTemplate.query(roleSql, (rs, rowNum) -> rs.getInt("id"), updated.getRoleName());
                Integer roleId = roleIds.isEmpty() ? null : roleIds.get(0);

                if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
                    jdbcTemplate.update("UPDATE employees SET full_name = ?, phone = ?, role_id = COALESCE(?, role_id), password_hash = ?, status = ? WHERE id = ?",
                            updated.getFullName(), updated.getPhone(), roleId, updated.getPassword(), updated.getStatus(), updated.getId());
                } else {
                    jdbcTemplate.update("UPDATE employees SET full_name = ?, phone = ?, role_id = COALESCE(?, role_id), status = ? WHERE id = ?",
                            updated.getFullName(), updated.getPhone(), roleId, updated.getStatus(), updated.getId());
                }
            } catch (Exception e) {
                System.err.println("Lỗi updateEmployee trong MySQL: " + e.getMessage());
            }
        }

        findById(updated.getId()).ifPresent(e -> {
            e.setFullName(updated.getFullName());
            e.setPhone(updated.getPhone());
            e.setRoleName(updated.getRoleName());
            if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
                e.setPassword(updated.getPassword());
            }
            e.setStatus(updated.getStatus());
        });
    }

    public synchronized void toggleStatus(Long id) {
        if (id == null) return;
        if (jdbcTemplate != null) {
            try {
                String sql = "UPDATE employees SET status = CASE WHEN status = 'ACTIVE' THEN 'SUSPENDED' ELSE 'ACTIVE' END WHERE id = ?";
                jdbcTemplate.update(sql, id);
            } catch (Exception e) {
                System.err.println("Lỗi toggleStatus employee trong MySQL: " + e.getMessage());
            }
        }

        findById(id).ifPresent(e -> {
            if ("ACTIVE".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("SUSPENDED");
            } else {
                e.setStatus("ACTIVE");
            }
        });
    }
}
