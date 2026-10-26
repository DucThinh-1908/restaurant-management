package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.DiningTable;
import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.model.MenuItem;
import com.restaurant.restaurant_management.model.Order;
import com.restaurant.restaurant_management.model.OrderItem;
import com.restaurant.restaurant_management.service.MenuService;
import com.restaurant.restaurant_management.service.OrderService;
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/waiter")
public class AdminWaiterController {

    private final TableService tableService;
    private final OrderService orderService;
    private final MenuService menuService;

    public AdminWaiterController(TableService tableService,
                                 OrderService orderService,
                                 MenuService menuService) {
        this.tableService = tableService;
        this.orderService = orderService;
        this.menuService = menuService;
    }

    @GetMapping
    public String showWaiterDesk(HttpSession session, Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        List<DiningTable> allTables = tableService.getAllTables();
        Map<Long, Order> tableOrderMap = new HashMap<>();

        for (DiningTable table : allTables) {
            orderService.getActiveOrderByTableId(table.getId())
                    .ifPresent(order -> tableOrderMap.put(table.getId(), order));
        }

        List<Order> readyOrders = orderService.getOrdersByStatus("READY");
        List<MenuItem> menuItems = menuService.getAllMenuItems();
        Map<String, List<MenuItem>> menuByCategory = menuService.getMenuItemsGroupedByCategory();

        List<DiningTable> availableTables = allTables.stream()
                .filter(t -> "AVAILABLE".equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());

        long availableCount = allTables.stream().filter(t -> "AVAILABLE".equalsIgnoreCase(t.getStatus())).count();
        long occupiedCount = allTables.stream().filter(t -> "OCCUPIED".equalsIgnoreCase(t.getStatus())).count();
        long reservedCount = allTables.stream().filter(t -> "RESERVED".equalsIgnoreCase(t.getStatus())).count();

        model.addAttribute("activeAdminPage", "waiter");
        model.addAttribute("pageTitle", "Trực Bàn & Phục Vụ | Duc Thinh Waiter");
        model.addAttribute("employee", emp);
        model.addAttribute("tables", allTables);
        model.addAttribute("tableOrderMap", tableOrderMap);
        model.addAttribute("readyOrders", readyOrders);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("menuByCategory", menuByCategory);
        model.addAttribute("availableTables", availableTables);

        model.addAttribute("availableCount", availableCount);
        model.addAttribute("occupiedCount", occupiedCount);
        model.addAttribute("reservedCount", reservedCount);
        model.addAttribute("readyCount", readyOrders.size());

        return "admin/waiter";
    }

    @PostMapping("/create-order")
    public String createOrderAtTable(@RequestParam("tableId") Long tableId,
                                     @RequestParam(value = "customerCount", defaultValue = "2") int customerCount,
                                     @RequestParam(value = "note", required = false) String note,
                                     @RequestParam(value = "menuItemId", required = false) List<Long> menuItemIds,
                                     @RequestParam(value = "quantity", required = false) List<Integer> quantities,
                                     @RequestParam(value = "itemNote", required = false) List<String> itemNotes,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        Optional<DiningTable> tableOpt = tableService.findById(tableId);
        if (tableOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin bàn ăn!");
            return "redirect:/admin/waiter";
        }
        DiningTable table = tableOpt.get();

        List<OrderItem> items = new ArrayList<>();
        if (menuItemIds != null && quantities != null) {
            for (int i = 0; i < menuItemIds.size(); i++) {
                Long mId = menuItemIds.get(i);
                int qty = quantities.get(i) != null ? quantities.get(i) : 0;
                String iNote = (itemNotes != null && i < itemNotes.size()) ? itemNotes.get(i) : null;
                if (qty > 0) {
                    Optional<MenuItem> mOpt = menuService.findById(mId);
                    if (mOpt.isPresent()) {
                        MenuItem m = mOpt.get();
                        OrderItem oi = new OrderItem(m.getId(), m.getName(), qty, m.getPriceValue(), iNote);
                        items.add(oi);
                    }
                }
            }
        }

        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 món ăn để gửi đơn xuống Bếp!");
            return "redirect:/admin/waiter";
        }

        orderService.createDineInOrder(tableId, table.getTableNumber(), emp.getId(), items, note, customerCount);
        tableService.updateStatus(tableId, "OCCUPIED");

        redirectAttributes.addFlashAttribute("successMessage",
                "Đã gọi món cho " + table.getTableNumber() + " thành công! Đơn đã chuyển tức thì đến Bếp (KDS).");
        return "redirect:/admin/waiter";
    }

    @PostMapping("/add-items")
    public String addItemsToOrder(@RequestParam("orderId") Long orderId,
                                  @RequestParam(value = "menuItemId", required = false) List<Long> menuItemIds,
                                  @RequestParam(value = "quantity", required = false) List<Integer> quantities,
                                  @RequestParam(value = "itemNote", required = false) List<String> itemNotes,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        List<OrderItem> items = new ArrayList<>();
        if (menuItemIds != null && quantities != null) {
            for (int i = 0; i < menuItemIds.size(); i++) {
                Long mId = menuItemIds.get(i);
                int qty = quantities.get(i) != null ? quantities.get(i) : 0;
                String iNote = (itemNotes != null && i < itemNotes.size()) ? itemNotes.get(i) : null;
                if (qty > 0) {
                    Optional<MenuItem> mOpt = menuService.findById(mId);
                    if (mOpt.isPresent()) {
                        MenuItem m = mOpt.get();
                        OrderItem oi = new OrderItem(m.getId(), m.getName(), qty, m.getPriceValue(), iNote);
                        items.add(oi);
                    }
                }
            }
        }

        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất 1 món để gọi thêm!");
            return "redirect:/admin/waiter";
        }

        orderService.addItemsToExistingOrder(orderId, items);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã gọi thêm " + items.size() + " món vào đơn #" + orderId + ". Bếp đã nhận được món bổ sung!");
        return "redirect:/admin/waiter";
    }

    @PostMapping("/serve-order")
    public String markOrderServed(@RequestParam("orderId") Long orderId,
                                  @RequestParam(value = "tableNumber", required = false) String tableNumber,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        orderService.markOrderServed(orderId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đã xác nhận bưng món ra " + (tableNumber != null ? tableNumber : "bàn") + " thành công!");
        return "redirect:/admin/waiter";
    }

    @PostMapping("/move-table")
    public String moveTable(@RequestParam("orderId") Long orderId,
                            @RequestParam("fromTableId") Long fromTableId,
                            @RequestParam("toTableId") Long toTableId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        Optional<DiningTable> toTableOpt = tableService.findById(toTableId);
        String toTableName = toTableOpt.map(DiningTable::getTableNumber).orElse("Bàn mới");

        orderService.moveTable(orderId, fromTableId, toTableId);
        tableService.updateStatus(fromTableId, "AVAILABLE");
        tableService.updateStatus(toTableId, "OCCUPIED");

        redirectAttributes.addFlashAttribute("successMessage",
                "Đã chuyển khách và đơn hàng sang " + toTableName + " thành công!");
        return "redirect:/admin/waiter";
    }

    @PostMapping("/clear-table")
    public String clearTable(@RequestParam("tableId") Long tableId,
                             @RequestParam(value = "orderId", required = false) Long orderId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        Optional<DiningTable> tableOpt = tableService.findById(tableId);
        String tableName = tableOpt.map(DiningTable::getTableNumber).orElse("Bàn");

        orderService.clearTable(tableId, orderId);
        tableService.updateStatus(tableId, "AVAILABLE");

        redirectAttributes.addFlashAttribute("successMessage",
                "Đã dọn dẹp xong " + tableName + ". Bàn hiện đã sẵn sàng đón lượt khách mới!");
        return "redirect:/admin/waiter";
    }

    @PostMapping("/request-bill")
    public String requestBill(@RequestParam("tableNumber") String tableNumber,
                              @RequestParam("orderCode") String orderCode,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        redirectAttributes.addFlashAttribute("successMessage",
                "Đã gửi yêu cầu in hóa đơn cho " + tableNumber + " (Mã: " + orderCode + ") đến quầy Thu Ngân!");
        return "redirect:/admin/waiter";
    }
}
