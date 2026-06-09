package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.Order;
import do_an_java.quan_ly_my_pham.model.OrderStatus;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final CurrentUser currentUser;
    private final OrderService orderService;

    @GetMapping("/orders")
    public String orderHistory(Authentication authentication, Model model) {
        User user = currentUser.requireUser(authentication);
        model.addAttribute("orders", orderService.findOrdersByUser(user.getId()));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(
        @PathVariable Integer id,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUser.requireUser(authentication);
        Order order = orderService.findById(id);
        if (!order.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ban khong co quyen xem don hang nay");
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("canCancel", order.getStatus() == OrderStatus.PENDING_CONFIRMATION);
        return "order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(
        @PathVariable Integer id,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUser.requireUser(authentication);
        try {
            orderService.cancelByCustomer(id, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Da huy don hang");
        } catch (BusinessException | NotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
