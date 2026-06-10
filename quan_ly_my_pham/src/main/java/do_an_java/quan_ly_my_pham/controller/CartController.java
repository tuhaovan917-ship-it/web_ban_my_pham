package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final CurrentUser currentUser;

    @GetMapping("/cart")
    public String viewCart(Authentication authentication, Model model) {
        User user = currentUser.requireUser(authentication);
        model.addAttribute("items", cartService.getItems(user.getId()));
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(
        @RequestParam Integer productId,
        @RequestParam(defaultValue = "1") Integer quantity,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUser.requireUser(authentication);
        try {
            cartService.addItem(user.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng");
        } catch (BusinessException | NotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/product/" + productId;
    }

    @PostMapping("/cart/update")
    public String updateQuantity(
        @RequestParam Integer productId,
        @RequestParam Integer quantity,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUser.requireUser(authentication);
        try {
            cartService.updateQuantity(user.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật giỏ hàng");
        } catch (BusinessException | NotFoundException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeItem(
        @RequestParam Integer productId,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUser.requireUser(authentication);
        cartService.removeItem(user.getId(), productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng");
        return "redirect:/cart";
    }
}
