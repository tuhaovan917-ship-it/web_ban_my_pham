package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;
    private final CurrentUser currentUser;

    @GetMapping
    public String users(Model model, Authentication authentication) {
        User admin = currentUser.requireUser(authentication);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentUserId", admin.getId());
        return "admin/users";
    }

    @PostMapping("/{id}/lock")
    public String lockUser(
        @PathVariable Integer id,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        try {
            User admin = currentUser.requireUser(authentication);
            if (admin.getId().equals(id)) {
                throw new BusinessException("Không thể khóa tài khoản đang đăng nhập");
            }

            User user = userService.lockUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản " + user.getUserName());
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        User user = userService.unlockUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản " + user.getUserName());
        return "redirect:/admin/users";
    }
}
