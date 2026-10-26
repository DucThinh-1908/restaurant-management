package com.restaurant.restaurant_management.controller;

import com.restaurant.restaurant_management.model.Employee;
import com.restaurant.restaurant_management.model.Ingredient;
import com.restaurant.restaurant_management.model.Supplier;
import com.restaurant.restaurant_management.service.InventoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String listInventory(@RequestParam(value = "tab", defaultValue = "stock") String tab,
                                @RequestParam(value = "filter", required = false) String filter,
                                HttpSession session,
                                Model model) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";
        if (!"STOREKEEPER".equalsIgnoreCase(emp.getRoleName()) && !"ADMIN".equalsIgnoreCase(emp.getRoleName()) && !"MANAGER".equalsIgnoreCase(emp.getRoleName())) {
            return "redirect:/admin/kitchen";
        }

        model.addAttribute("activeAdminPage", "inventory");
        model.addAttribute("pageTitle", "Quản Lý Kho Nguyên Vật Liệu | GoD Admin");
        model.addAttribute("employee", emp);
        model.addAttribute("currentTab", tab);
        model.addAttribute("currentFilter", filter);

        // Core data
        model.addAttribute("ingredients", inventoryService.getAllIngredients());
        model.addAttribute("lowStockCount", inventoryService.getLowStockCount());
        
        BigDecimal totalStockValue = inventoryService.getTotalStockValue();
        DecimalFormat df = new DecimalFormat("#,###");
        model.addAttribute("formattedTotalStockValue", df.format(totalStockValue) + " đ");
        model.addAttribute("receiptsCountThisMonth", inventoryService.getReceiptsCountThisMonth());

        model.addAttribute("suppliers", inventoryService.getAllSuppliers());
        model.addAttribute("receipts", inventoryService.getAllReceipts());
        model.addAttribute("receiptItems", inventoryService.getAllReceiptItems());
        model.addAttribute("transactions", inventoryService.getAllTransactions(100));

        return "admin/inventory";
    }

    @PostMapping("/update")
    public String updateStock(@RequestParam("id") Long id,
                              @RequestParam("stockQuantity") BigDecimal stockQuantity,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        inventoryService.updateStock(id, stockQuantity);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng tồn kho thành công!");
        return "redirect:/admin/inventory?tab=stock";
    }

    @PostMapping("/add")
    public String addIngredient(@RequestParam("code") String code,
                                @RequestParam("name") String name,
                                @RequestParam("unit") String unit,
                                @RequestParam("stockQuantity") BigDecimal stockQuantity,
                                @RequestParam("minStockLevel") BigDecimal minStockLevel,
                                @RequestParam("costPrice") BigDecimal costPrice,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (session.getAttribute("loggedInEmployee") == null) return "redirect:/admin/login";

        Ingredient ingredient = new Ingredient(null, code, name, unit, stockQuantity, minStockLevel, costPrice);
        boolean success = inventoryService.addIngredient(ingredient);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm nguyên liệu: " + name + "!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã nguyên liệu " + code + " đã tồn tại trong hệ thống!");
        }
        return "redirect:/admin/inventory?tab=stock";
    }

    @PostMapping("/create-receipt")
    public String createReceipt(@RequestParam("supplierId") Long supplierId,
                                @RequestParam("ingredientId") Long ingredientId,
                                @RequestParam("quantity") BigDecimal quantity,
                                @RequestParam("unitPrice") BigDecimal unitPrice,
                                @RequestParam(value = "expiryDate", required = false) String expiryDate,
                                @RequestParam(value = "note", required = false) String note,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        boolean success = inventoryService.createReceipt(supplierId, ingredientId, quantity, unitPrice, expiryDate, note, emp.getId());
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu nhập kho thành công! Đã tăng số lượng tồn kho và cập nhật đơn giá.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo phiếu nhập kho! Vui lòng kiểm tra lại thông tin.");
        }
        return "redirect:/admin/inventory?tab=receipts";
    }

    @PostMapping("/export-stock")
    public String exportStock(@RequestParam("ingredientId") Long ingredientId,
                              @RequestParam("quantity") BigDecimal quantity,
                              @RequestParam(value = "transactionType", defaultValue = "EXPORT_KITCHEN") String transactionType,
                              @RequestParam(value = "note", required = false) String note,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        boolean success = inventoryService.exportStock(ingredientId, quantity, transactionType, note, emp.getId());
        if (success) {
            String opText = "EXPORT_KITCHEN".equals(transactionType) ? "Xuất kho cho Bếp" :
                            ("SPOILAGE".equals(transactionType) ? "Ghi nhận hao hụt / hỏng" : "Điều chỉnh kiểm kê");
            redirectAttributes.addFlashAttribute("successMessage", opText + " thành công! Số lượng tồn kho đã được trừ tương ứng.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thực hiện! Số lượng trong kho không đủ hoặc thao tác bị lỗi.");
        }
        return "redirect:/admin/inventory?tab=export";
    }

    @PostMapping("/add-supplier")
    public String addSupplier(@RequestParam("name") String name,
                              @RequestParam(value = "contactName", required = false) String contactName,
                              @RequestParam("phone") String phone,
                              @RequestParam(value = "email", required = false) String email,
                              @RequestParam(value = "address", required = false) String address,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Employee emp = (Employee) session.getAttribute("loggedInEmployee");
        if (emp == null) return "redirect:/admin/login";

        Supplier supplier = new Supplier(null, name, contactName, phone, email, address, true);
        boolean success = inventoryService.addSupplier(supplier);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm nhà cung cấp: " + name + "!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu nhà cung cấp!");
        }
        return "redirect:/admin/inventory?tab=suppliers";
    }
}
