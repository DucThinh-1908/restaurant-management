package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.DiningTable;
import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.service.ReservationService;
import com.restaurant.restaurant_management.service.TableService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final TableService tableService;

    public AdminReservationController(ReservationService reservationService, TableService tableService) {
        this.reservationService = reservationService;
        this.tableService = tableService;
    }

    @GetMapping
    public String listReservations(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        model.addAttribute("activeAdminPage", "reservations");
        model.addAttribute("pageTitle", "Bàn Làm Việc Lễ Tân - Quản Lý Đặt Bàn | GoD Admin");
        model.addAttribute("employee", emp);
        model.addAttribute("reservations", reservationService.getAllReservations());
        model.addAttribute("tables", tableService.getAllTables());
        return "admin/reservations";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("id") Long id,
                               @RequestParam("status") String status,
                               @RequestParam(value = "tableId", required = false) Long tableId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        String tableName = null;
        if (tableId != null) {
            Optional<DiningTable> tableOpt = tableService.findById(tableId);
            if (tableOpt.isPresent()) {
                tableName = tableOpt.get().getTableNumber();
                if ("CONFIRMED".equalsIgnoreCase(status)) {
                    tableService.updateStatus(tableId, "RESERVED");
                } else if ("SEATED".equalsIgnoreCase(status)) {
                    tableService.updateStatus(tableId, "OCCUPIED");
                }
            }
        }

        // Ghi nhận Lễ tân phụ trách duyệt đơn này
        String handledByName = emp.getFullName() + " (" + emp.getRoleName() + ")";
        reservationService.updateReservation(id, status, tableName, handledByName);

        redirectAttributes.addFlashAttribute("successMessage", 
                "Lễ tân " + emp.getFullName() + " đã cập nhật yêu cầu đặt bàn #" + id + " sang trạng thái '" + status + "' thành công!");
        return "redirect:/admin/reservations";
    }

    @PostMapping("/delete")
    public String deleteReservation(@RequestParam("id") Long id,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";
        reservationService.deleteReservation(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa yêu cầu đặt bàn #" + id + "!");
        return "redirect:/admin/reservations";
    }
}
