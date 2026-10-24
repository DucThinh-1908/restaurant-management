package com.restaurant.restaurant_management.model;

public class DiningTable {
    private Long id;
    private String tableNumber; // T1-01, VIP-01...
    private String area;        // Tầng 1, Tầng 2, VIP...
    private int capacity = 4;
    private String status = "AVAILABLE"; // AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE

    public DiningTable() {
    }

    public DiningTable(Long id, String tableNumber, String area, int capacity, String status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.area = area;
        this.capacity = capacity;
        this.status = status;
    }

    public String getStatusBadgeClass() {
        if ("AVAILABLE".equalsIgnoreCase(status)) return "badge-success";
        if ("OCCUPIED".equalsIgnoreCase(status)) return "badge-danger";
        if ("RESERVED".equalsIgnoreCase(status)) return "badge-warning";
        return "badge-info";
    }

    public String getStatusDisplayName() {
        if ("AVAILABLE".equalsIgnoreCase(status)) return "Bàn trống";
        if ("OCCUPIED".equalsIgnoreCase(status)) return "Có khách";
        if ("RESERVED".equalsIgnoreCase(status)) return "Đã đặt trước";
        if ("MAINTENANCE".equalsIgnoreCase(status)) return "Bảo trì";
        return status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
