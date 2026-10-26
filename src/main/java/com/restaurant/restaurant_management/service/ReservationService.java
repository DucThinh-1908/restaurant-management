package com.restaurant.restaurant_management.service;

import com.restaurant.restaurant_management.dto.ReservationRequest;
import com.restaurant.restaurant_management.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final JdbcTemplate jdbcTemplate;
    private final List<Reservation> inMemoryReservations = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(101);

    public ReservationService(@Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        // Dữ liệu mẫu dự phòng bộ nhớ
        Reservation sample = new Reservation(
                100L,
                1L,
                "Nguyễn Hoàng Nam",
                "0912345678",
                "nam.nguyen@gmail.com",
                LocalDate.now().plusDays(1).toString(),
                "19:00",
                "4",
                "Kỷ niệm ngày cưới, mong muốn bàn gần cửa sổ.",
                "CONFIRMED",
                "T1-04",
                "Hoàng Lễ Tân"
        );
        inMemoryReservations.add(sample);
    }

    private Reservation mapRowToReservation(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setId(rs.getLong("id"));

        long custId = rs.getLong("customer_id");
        if (!rs.wasNull()) {
            res.setCustomerId(custId);
        }

        res.setCustomerName(rs.getString("customer_name"));
        res.setPhone(rs.getString("phone"));
        res.setEmail(rs.getString("email"));

        java.sql.Date d = rs.getDate("reservation_date");
        if (d != null) {
            res.setDate(d.toString());
        }

        Time t = rs.getTime("reservation_time");
        if (t != null) {
            res.setTime(t.toString().substring(0, 5));
        }

        res.setGuests(String.valueOf(rs.getInt("guest_count")));
        res.setMessage(rs.getString("message"));
        res.setStatus(rs.getString("status"));
        res.setAssignedTableNumber(rs.getString("table_number"));
        res.setHandledByEmployeeName(rs.getString("handled_by_name"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            res.setCreatedAt(ts.toLocalDateTime());
        }
        return res;
    }

    public synchronized Reservation createReservation(ReservationRequest request, Long customerId) {
        log.info("Nhận yêu cầu đặt bàn mới từ: {} (SĐT: {}, Ngày: {}, Giờ: {}, Khách: {}, CustomerId: {})",
                request.getName(), request.getPhone(), request.getDate(), request.getTime(), request.getGuests(), customerId);

        Reservation res = new Reservation();
        res.setCustomerId(customerId);
        res.setCustomerName(request.getName());
        res.setPhone(request.getPhone());
        res.setEmail(request.getEmail());
        res.setDate(request.getDate());
        res.setTime(request.getTime());
        res.setGuests(request.getGuests());
        res.setMessage(request.getMessage());
        res.setStatus("PENDING");
        res.setCreatedAt(LocalDateTime.now());

        if (jdbcTemplate != null) {
            try {
                // Parse số lượng khách
                int guestCount = 2;
                try {
                    String num = request.getGuests().replaceAll("[^0-9]", "");
                    if (!num.isBlank()) guestCount = Integer.parseInt(num.substring(0, 1));
                } catch (Exception ignored) {}

                // Parse ngày và giờ
                LocalDate rDate = LocalDate.now().plusDays(1);
                try {
                    if (request.getDate() != null && !request.getDate().isBlank()) {
                        rDate = LocalDate.parse(request.getDate());
                    }
                } catch (Exception ignored) {}

                LocalTime rTime = LocalTime.of(19, 0);
                try {
                    if (request.getTime() != null && !request.getTime().isBlank()) {
                        String tStr = request.getTime().split("–")[0].trim().split(" ")[0].trim();
                        rTime = LocalTime.parse(tStr.length() == 5 ? tStr : (tStr + ":00"));
                    }
                } catch (Exception ignored) {}

                final LocalDate finalDate = rDate;
                final LocalTime finalTime = rTime;
                final int finalGuests = guestCount;

                KeyHolder keyHolder = new GeneratedKeyHolder();
                String insertSql = "INSERT INTO reservations (customer_id, customer_name, phone, email, reservation_date, reservation_time, guest_count, message, status, created_at) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', NOW())";
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                    if (customerId != null) ps.setLong(1, customerId); else ps.setNull(1, Types.INTEGER);
                    ps.setString(2, request.getName());
                    ps.setString(3, request.getPhone());
                    ps.setString(4, request.getEmail());
                    ps.setDate(5, Date.valueOf(finalDate));
                    ps.setTime(6, Time.valueOf(finalTime));
                    ps.setInt(7, finalGuests);
                    ps.setString(8, request.getMessage());
                    return ps;
                }, keyHolder);

                Number generatedId = keyHolder.getKey();
                long newId = generatedId != null ? generatedId.longValue() : idGenerator.getAndIncrement();
                res.setId(newId);
                return res;
            } catch (Exception e) {
                System.err.println("Lỗi createReservation trong MySQL: " + e.getMessage());
            }
        }

        res.setId(idGenerator.getAndIncrement());
        inMemoryReservations.add(0, res);
        return res;
    }

    public synchronized boolean createReservation(ReservationRequest request) {
        createReservation(request, null);
        return true;
    }

    public synchronized List<Reservation> getReservationsByCustomerId(Long customerId) {
        if (customerId == null) return Collections.emptyList();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT r.id, r.customer_id, r.customer_name, r.phone, r.email, " +
                             "r.reservation_date, r.reservation_time, r.guest_count, r.message, r.status, r.created_at, " +
                             "t.table_number, e.full_name AS handled_by_name " +
                             "FROM reservations r " +
                             "LEFT JOIN dining_tables t ON r.table_id = t.id " +
                             "LEFT JOIN employees e ON r.handled_by_employee_id = e.id " +
                             "WHERE r.customer_id = ? " +
                             "ORDER BY r.id DESC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToReservation(rs), customerId);
            } catch (Exception e) {
                System.err.println("Lỗi getReservationsByCustomerId từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryReservations.stream()
                .filter(r -> r.getCustomerId() != null && r.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    public synchronized List<Reservation> getAllReservations() {
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT r.id, r.customer_id, r.customer_name, r.phone, r.email, " +
                             "r.reservation_date, r.reservation_time, r.guest_count, r.message, r.status, r.created_at, " +
                             "t.table_number, e.full_name AS handled_by_name " +
                             "FROM reservations r " +
                             "LEFT JOIN dining_tables t ON r.table_id = t.id " +
                             "LEFT JOIN employees e ON r.handled_by_employee_id = e.id " +
                             "ORDER BY r.id DESC";
                return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToReservation(rs));
            } catch (Exception e) {
                System.err.println("Lỗi getAllReservations từ MySQL: " + e.getMessage());
            }
        }
        return new ArrayList<>(inMemoryReservations);
    }

    public synchronized Optional<Reservation> findById(Long id) {
        if (id == null) return Optional.empty();
        if (jdbcTemplate != null) {
            try {
                String sql = "SELECT r.id, r.customer_id, r.customer_name, r.phone, r.email, " +
                             "r.reservation_date, r.reservation_time, r.guest_count, r.message, r.status, r.created_at, " +
                             "t.table_number, e.full_name AS handled_by_name " +
                             "FROM reservations r " +
                             "LEFT JOIN dining_tables t ON r.table_id = t.id " +
                             "LEFT JOIN employees e ON r.handled_by_employee_id = e.id " +
                             "WHERE r.id = ?";
                List<Reservation> list = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToReservation(rs), id);
                if (!list.isEmpty()) {
                    return Optional.of(list.get(0));
                }
            } catch (Exception e) {
                System.err.println("Lỗi findById reservation từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryReservations.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public synchronized void updateReservation(Long id, String status, String tableNumber, Long employeeId, String handledByName) {
        if (id == null) return;
        if (jdbcTemplate != null) {
            try {
                Integer tableId = null;
                if (tableNumber != null && !tableNumber.isBlank()) {
                    List<Integer> tIds = jdbcTemplate.query("SELECT id FROM dining_tables WHERE table_number = ?",
                            (rs, rowNum) -> rs.getInt("id"), tableNumber.trim());
                    if (!tIds.isEmpty()) tableId = tIds.get(0);
                }

                String updateSql = "UPDATE reservations SET status = ?, table_id = COALESCE(?, table_id), handled_by_employee_id = COALESCE(?, handled_by_employee_id) WHERE id = ?";
                jdbcTemplate.update(updateSql, status, tableId, employeeId, id);
            } catch (Exception e) {
                System.err.println("Lỗi updateReservation trong MySQL: " + e.getMessage());
            }
        }

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

    public synchronized void updateReservation(Long id, String status, String tableNumber, String handledByName) {
        updateReservation(id, status, tableNumber, null, handledByName);
    }

    public synchronized void updateReservationStatus(Long id, String status) {
        if (id == null || status == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("UPDATE reservations SET status = ? WHERE id = ?", status, id);
            } catch (Exception e) {
                System.err.println("Lỗi updateReservationStatus trong MySQL: " + e.getMessage());
            }
        }
        findById(id).ifPresent(r -> r.setStatus(status));
    }

    public synchronized void deleteReservation(Long id) {
        if (id == null) return;
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.update("DELETE FROM reservations WHERE id = ?", id);
            } catch (Exception e) {
                System.err.println("Lỗi deleteReservation trong MySQL: " + e.getMessage());
            }
        }
        inMemoryReservations.removeIf(r -> r.getId().equals(id));
    }

    public synchronized long getPendingReservationCount() {
        if (jdbcTemplate != null) {
            try {
                Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations WHERE status = 'PENDING'", Long.class);
                return count != null ? count : 0;
            } catch (Exception e) {
                System.err.println("Lỗi đếm pending reservations từ MySQL: " + e.getMessage());
            }
        }
        return inMemoryReservations.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
    }
}
