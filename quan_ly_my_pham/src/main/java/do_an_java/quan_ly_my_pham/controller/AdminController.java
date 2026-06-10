package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.model.Order;
import do_an_java.quan_ly_my_pham.model.OrderStatus;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.service.DashboardService;
import do_an_java.quan_ly_my_pham.service.OrderService;
import do_an_java.quan_ly_my_pham.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final DashboardService dashboardService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CurrentUser currentUser;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("summary", dashboardService.getSummary());
        model.addAttribute("lowStockProducts", productService.findLowStockProducts());
        model.addAttribute("recentOrders", orderService.findAllOrders().stream().limit(8).toList());
        return "admin/dashboard";
    }

    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) OrderStatus status, Model model) {
        model.addAttribute("orders", status == null ? orderService.findAllOrders() : orderService.findOrdersByStatus(status));
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        Order order = orderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
        @PathVariable Integer id,
        @RequestParam OrderStatus status,
        @RequestParam(required = false) String note,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        try {
            User admin = currentUser.requireUser(authentication);
            orderService.updateStatus(id, status, admin.getId(), note);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }
}
