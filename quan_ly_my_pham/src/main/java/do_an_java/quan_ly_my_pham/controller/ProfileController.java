package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.service.UserService;
import do_an_java.quan_ly_my_pham.service.dto.PasswordChangeRequest;
import do_an_java.quan_ly_my_pham.service.dto.ProfileUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final CurrentUser currentUser;
    private final UserService userService;

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = currentUser.requireUser(authentication);
        ensureFormObjects(model, user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
        @Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
        BindingResult bindingResult,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUser.requireUser(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("passwordChangeRequest", emptyPasswordRequest());
            return "profile";
        }

        try {
            userService.updateProfile(user.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thông tin cá nhân");
            return "redirect:/profile";
        } catch (BusinessException exception) {
            model.addAttribute("user", user);
            model.addAttribute("passwordChangeRequest", emptyPasswordRequest());
            model.addAttribute("profileError", exception.getMessage());
            return "profile";
        }
    }

    @PostMapping("/profile/password")
    public String changePassword(
        @Valid @ModelAttribute("passwordChangeRequest") PasswordChangeRequest request,
        BindingResult bindingResult,
        Authentication authentication,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        User user = currentUser.requireUser(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profileUpdateRequest", toProfileRequest(user));
            return "profile";
        }

        try {
            userService.changePassword(user.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đổi mật khẩu thành công");
            return "redirect:/profile";
        } catch (BusinessException exception) {
            model.addAttribute("user", user);
            model.addAttribute("profileUpdateRequest", toProfileRequest(user));
            model.addAttribute("passwordError", exception.getMessage());
            return "profile";
        }
    }

    private void ensureFormObjects(Model model, User user) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", user);
        }
        if (!model.containsAttribute("profileUpdateRequest")) {
            model.addAttribute("profileUpdateRequest", toProfileRequest(user));
        }
        if (!model.containsAttribute("passwordChangeRequest")) {
            model.addAttribute("passwordChangeRequest", emptyPasswordRequest());
        }
    }

    private ProfileUpdateRequest toProfileRequest(User user) {
        return new ProfileUpdateRequest(
            user.getFullName(),
            user.getEmail(),
            user.getPhone() == null ? "" : user.getPhone(),
            user.getAddress() == null ? "" : user.getAddress()
        );
    }

    private PasswordChangeRequest emptyPasswordRequest() {
        return new PasswordChangeRequest("", "", "");
    }
}
