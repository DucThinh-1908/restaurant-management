package com.restaurant.restaurant_management.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reservation {
    private Long id;
    private Long customerId;
    private String customerName;
    private String phone;
    private String email;
    private String date;
    private String time;
    private String guests;
    private String message;
    private String status = "PENDING"; // PENDING, CONFIRMED, SEATED, CANCELLED
    private String assignedTableNumber; // Bàn được gán
    private String handledByEmployeeName; // Lễ tân trực tiếp xác nhận & gán bàn
    private LocalDateTime createdAt = LocalDateTime.now();

    public Reservation() {
    }

    public Reservation(Long id, Long customerId, String customerName, String phone, String email, String date, String time, String guests, String message, String status, String assignedTableNumber, String handledByEmployeeName) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.message = message;
        this.status = status;
        this.assignedTableNumber = assignedTableNumber;
        this.handledByEmployeeName = handledByEmployeeName;
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
    }

    public String getStatusBadgeClass() {
        if ("CONFIRMED".equalsIgnoreCase(status)) return "badge-success";
        if ("SEATED".equalsIgnoreCase(status)) return "badge-info";
        if ("CANCELLED".equalsIgnoreCase(status)) return "badge-danger";
        return "badge-warning";
    }

    public String getStatusDisplayName() {
        if ("PENDING".equalsIgnoreCase(status)) return "Chờ Lễ tân duyệt";
        if ("CONFIRMED".equalsIgnoreCase(status)) return "Đã xác nhận & Giữ bàn";
        if ("SEATED".equalsIgnoreCase(status)) return "Đang dùng bữa tại bàn";
        if ("CANCELLED".equalsIgnoreCase(status)) return "Đã hủy";
        return status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGuests() {
        return guests;
    }

    public void setGuests(String guests) {
        this.guests = guests;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTableNumber() {
        return assignedTableNumber;
    }

    public void setAssignedTableNumber(String assignedTableNumber) {
        this.assignedTableNumber = assignedTableNumber;
    }

    public String getHandledByEmployeeName() {
        return handledByEmployeeName;
    }

    public void setHandledByEmployeeName(String handledByEmployeeName) {
        this.handledByEmployeeName = handledByEmployeeName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
