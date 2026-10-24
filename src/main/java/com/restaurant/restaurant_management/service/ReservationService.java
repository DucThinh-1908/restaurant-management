package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.dto.ReservationRequest;
import com.restaurant.restaurant_management.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(101);

    public ReservationService() {
        // Dữ liệu mẫu đặt bàn của khách ID 1 được Lễ tân Hoàng Lễ Tân xác nhận & gán bàn VIP-01
        Reservation sample = new Reservation(
                100L,
                1L,
                "Nguyễn Hoàng Nam",
                "0912345678",
                "nam.nguyen@gmail.com",
                LocalDate.now().plusDays(1).toString(),
                "19:00 – 22:00",
                "3–4 guests",
                "Kỷ niệm ngày cưới, mong muốn bàn gần cửa sổ.",
                "CONFIRMED",
                "T1-04",
                "Hoàng Lễ Tân"
        );
        reservations.add(sample);
    }

    public synchronized Reservation createReservation(ReservationRequest request, Long customerId) {
        log.info("Nhận yêu cầu đặt bàn mới từ: {} (SĐT: {}, Ngày: {}, Giờ: {}, Khách: {}, CustomerId: {})",
                request.getName(), request.getPhone(), request.getDate(), request.getTime(), request.getGuests(), customerId);

        Reservation res = new Reservation();
        res.setId(idGenerator.getAndIncrement());
        res.setCustomerId(customerId);
        res.setCustomerName(request.getName());
        res.setPhone(request.getPhone());
        res.setEmail(request.getEmail());
        res.setDate(request.getDate());
        res.setTime(request.getTime());
        res.setGuests(request.getGuests());
        res.setMessage(request.getMessage());
        res.setStatus("PENDING");

        reservations.add(0, res);
        return res;
    }

    public synchronized boolean createReservation(ReservationRequest request) {
        createReservation(request, null);
        return true;
    }

    public synchronized List<Reservation> getReservationsByCustomerId(Long customerId) {
        if (customerId == null) return Collections.emptyList();
        return reservations.stream()
                .filter(r -> r.getCustomerId() != null && r.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    // --- PHƯƠNG THỨC DÀNH CHO LỄ TÂN & ADMIN ---
    public synchronized List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public synchronized Optional<Reservation> findById(Long id) {
        if (id == null) return Optional.empty();
        return reservations.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public synchronized void updateReservation(Long id, String status, String tableNumber, String handledByName) {
        findById(id).ifPresent(r -> {
            r.setStatus(status);
            if (tableNumber != null && !tableNumber.isBlank()) {
                r.setAssignedTableNumber(tableNumber);
            }
            if (handledByName != null && !handledByName.isBlank()) {
                r.setHandledByEmployeeName(handledByName);
            }
        });
    }

    public synchronized void updateReservationStatus(Long id, String status) {
        findById(id).ifPresent(r -> r.setStatus(status));
    }

    public synchronized void deleteReservation(Long id) {
        reservations.removeIf(r -> r.getId().equals(id));
    }

    public synchronized long getPendingReservationCount() {
        return reservations.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
    }
}
