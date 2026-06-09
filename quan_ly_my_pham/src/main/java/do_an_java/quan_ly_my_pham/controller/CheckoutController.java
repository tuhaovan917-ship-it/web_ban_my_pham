package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.CartItem;
import do_an_java.quan_ly_my_pham.model.Order;
import do_an_java.quan_ly_my_pham.model.PaymentMethod;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.service.CartService;
import do_an_java.quan_ly_my_pham.service.OrderService;
import do_an_java.quan_ly_my_pham.service.ProductService;
import do_an_java.quan_ly_my_pham.service.dto.CheckoutRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CheckoutController {
    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(30000);

    private final CurrentUser currentUser;
    private final CartService cartService;
    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping("/checkout")
    public String checkoutForm(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        User user = currentUser.requireUser(authentication);
        List<CartItem> items = cartService.getItems(user.getId());
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gio hang dang trong");
            return "redirect:/cart";
        }

        model.addAttribute("user", user);
        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal(items));
        model.addAttribute("shippingFee", DEFAULT_SHIPPING_FEE);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "checkout";
    }

    @PostMapping("/checkout")
    public String checkout(
        @RequestParam String receiverName,
        @RequestParam String receiverPhone,
        @RequestParam String shippingAddress,
        @RequestParam PaymentMethod paymentMethod,
        @RequestParam(required = false) String promotionCode,
        @RequestParam(required = false) BigDecimal shippingFee,
        @RequestParam(required = false) String note,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUser.requireUser(authentication);
        CheckoutRequest request = new CheckoutRequest(
            user.getId(),
            receiverName,
            receiverPhone,
            shippingAddress,
            paymentMethod,
            promotionCode,
            shippingFee == null ? DEFAULT_SHIPPING_FEE : shippingFee,
            note
        );

        try {
            Order order = orderService.checkout(request);
            redirectAttributes.addFlashAttribute("successMessage", "Dat hang thanh cong");
            return "redirect:/orders/" + order.getId();
        } catch (BusinessException | NotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/checkout";
        }
    }

    private BigDecimal subtotal(List<CartItem> items) {
        return items.stream()
            .map(item -> productService.currentPrice(item.getProduct()).multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
