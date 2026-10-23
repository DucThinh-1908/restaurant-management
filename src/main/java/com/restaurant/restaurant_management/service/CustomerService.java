package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.Customer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CustomerService {
    private final List<Customer> customers = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(3);

    public CustomerService() {
        // Tài khoản khách hàng mẫu sẵn sàng đăng nhập
        customers.add(new Customer(1L, "nam.nguyen@gmail.com", "123456", "Nguyễn Hoàng Nam", "0912345678", "123 Đường Bạch Đằng, Đà Nẵng", 150));
        customers.add(new Customer(2L, "mai.le@gmail.com", "123456", "Lê Thị Mai", "0987654321", "45 Lê Duẩn, Đà Nẵng", 50));
    }

    public synchronized Optional<Customer> login(String email, String password) {
        if (email == null || password == null) return Optional.empty();
        return customers.stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email.trim()) && c.getPassword().equals(password))
                .findFirst();
    }

    public synchronized boolean register(Customer newCustomer) {
        if (newCustomer.getEmail() == null || newCustomer.getPassword() == null) return false;
        boolean exists = customers.stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(newCustomer.getEmail().trim()));
        if (exists) return false;

        newCustomer.setId(idGenerator.getAndIncrement());
        newCustomer.setEmail(newCustomer.getEmail().trim());
        newCustomer.setStatus("ACTIVE");
        newCustomer.setLoyaltyPoints(0);
        customers.add(newCustomer);
        return true;
    }

    public synchronized Optional<Customer> findById(Long id) {
        if (id == null) return Optional.empty();
        return customers.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public synchronized Optional<Customer> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return customers.stream().filter(c -> c.getEmail().equalsIgnoreCase(email.trim())).findFirst();
    }

    public synchronized void addLoyaltyPoints(Long customerId, int points) {
        findById(customerId).ifPresent(c -> c.setLoyaltyPoints(c.getLoyaltyPoints() + points));
    }
}
