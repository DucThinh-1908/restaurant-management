package com.restaurant.restaurant_management.model;

public class Employee {
    private Long id;
    private String roleName; // ADMIN, MANAGER, RECEPTIONIST, WAITER, CHEF, CASHIER, STOREKEEPER
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED

    public Employee() {
    }

    public Employee(Long id, String roleName, String username, String password, String fullName, String phone, String email, String status) {
        this.id = id;
        this.roleName = roleName;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
