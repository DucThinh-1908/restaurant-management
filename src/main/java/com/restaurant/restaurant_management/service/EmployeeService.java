package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.Employee;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EmployeeService {

    private final List<Employee> employees = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(8);

    public EmployeeService() {
        // Khởi tạo đầy đủ nhân sự cho 7 vai trò nội bộ (Khớp seed_data.sql)
        employees.add(new Employee(1L, "ADMIN", "admin", "123456", "Quản Trị Viên", "0901000001", "admin@godrestaurant.vn", "ACTIVE"));
        employees.add(new Employee(2L, "MANAGER", "manager01", "123456", "Trần Quản Lý", "0901000002", "manager@godrestaurant.vn", "ACTIVE"));
        employees.add(new Employee(3L, "RECEPTIONIST", "reception01", "123456", "Hoàng Lễ Tân", "0901000003", "receptionist@godrestaurant.vn", "ACTIVE"));
        employees.add(new Employee(4L, "WAITER", "waiter01", "123456", "Nguyễn Văn Phục Vụ", "0901000004", "waiter01@godrestaurant.vn", "ACTIVE"));
        employees.add(new Employee(5L, "CHEF", "chef01", "123456", "Lê Bếp Trưởng", "0901000005", "chef01@godrestaurant.vn", "ACTIVE"));
        employees.add(new Employee(6L, "CASHIER", "cashier01", "123456", "Phạm Thu Ngân", "0901000006", "cashier01@godrestaurant.vn", "ACTIVE"));
        employees.add(new Employee(7L, "STOREKEEPER", "storekeeper01", "123456", "Vũ Thủ Kho", "0901000007", "kho@godrestaurant.vn", "ACTIVE"));
    }

    public synchronized Optional<Employee> login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) return Optional.empty();
        String loginKey = usernameOrEmail.trim();
        return employees.stream()
                .filter(e -> ("ACTIVE".equalsIgnoreCase(e.getStatus())) &&
                        (e.getUsername().equalsIgnoreCase(loginKey) || e.getEmail().equalsIgnoreCase(loginKey)) &&
                        e.getPassword().equals(password))
                .findFirst();
    }

    public synchronized List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    public synchronized Optional<Employee> findById(Long id) {
        if (id == null) return Optional.empty();
        return employees.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public synchronized boolean addEmployee(Employee employee) {
        if (employee.getUsername() == null || employee.getEmail() == null) return false;
        boolean exists = employees.stream()
                .anyMatch(e -> e.getUsername().equalsIgnoreCase(employee.getUsername().trim()) ||
                               e.getEmail().equalsIgnoreCase(employee.getEmail().trim()));
        if (exists) return false;

        employee.setId(idGenerator.getAndIncrement());
        employees.add(employee);
        return true;
    }

    public synchronized void updateEmployee(Employee updated) {
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
        findById(id).ifPresent(e -> {
            if ("ACTIVE".equalsIgnoreCase(e.getStatus())) {
                e.setStatus("SUSPENDED");
            } else {
                e.setStatus("ACTIVE");
            }
        });
    }
}
