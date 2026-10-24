package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.model.DiningTable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TableService {

    private final List<DiningTable> tables = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(8);

    public TableService() {
        // Khởi tạo 7 bàn mẫu (khớp seed_data.sql)
        tables.add(new DiningTable(1L, "T1-01", "Tầng 1 - Sảnh chính", 2, "AVAILABLE"));
        tables.add(new DiningTable(2L, "T1-02", "Tầng 1 - Sảnh chính", 4, "AVAILABLE"));
        tables.add(new DiningTable(3L, "T1-03", "Tầng 1 - Sảnh chính", 4, "OCCUPIED"));
        tables.add(new DiningTable(4L, "T1-04", "Tầng 1 - Sảnh chính", 6, "RESERVED"));
        tables.add(new DiningTable(5L, "T2-01", "Tầng 2 - Ban công", 2, "AVAILABLE"));
        tables.add(new DiningTable(6L, "T2-02", "Tầng 2 - Ban công", 4, "AVAILABLE"));
        tables.add(new DiningTable(7L, "VIP-01", "Phòng VIP Hoàng Gia", 10, "AVAILABLE"));
    }

    public synchronized List<DiningTable> getAllTables() {
        return new ArrayList<>(tables);
    }

    public synchronized Optional<DiningTable> findById(Long id) {
        if (id == null) return Optional.empty();
        return tables.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public synchronized Optional<DiningTable> findByTableNumber(String tableNumber) {
        if (tableNumber == null) return Optional.empty();
        return tables.stream().filter(t -> t.getTableNumber().equalsIgnoreCase(tableNumber.trim())).findFirst();
    }

    public synchronized void updateStatus(Long id, String status) {
        findById(id).ifPresent(t -> t.setStatus(status));
    }

    public synchronized boolean addTable(DiningTable table) {
        boolean exists = tables.stream().anyMatch(t -> t.getTableNumber().equalsIgnoreCase(table.getTableNumber().trim()));
        if (exists) return false;

        table.setId(idGenerator.getAndIncrement());
        tables.add(table);
        return true;
    }
}
