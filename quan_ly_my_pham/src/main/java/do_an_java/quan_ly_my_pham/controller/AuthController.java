package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.service.AuthService;
import do_an_java.quan_ly_my_pham.service.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "register", required = false) String register, Model model) {
        if (register != null) {
            model.addAttribute("registerMode", true);
            model.addAttribute("registerRequest", emptyRegisterRequest());
        }
        return "auth/login";
    }

    @PostMapping(value = "/login", params = "register")
    public String register(
        @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
        BindingResult bindingResult,
        Model model
    ) {
        model.addAttribute("registerMode", true);
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            authService.register(registerRequest);
            model.addAttribute("registerSuccess", "Đăng ký thành công. Bạn có thể đăng nhập bằng tài khoản vừa tạo.");
            model.addAttribute("registeredUserName", registerRequest.userName().trim());
            model.addAttribute("registerRequest", emptyRegisterRequest());
            return "auth/login";
        } catch (BusinessException exception) {
            model.addAttribute("registerError", exception.getMessage());
            return "auth/login";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }

    private RegisterRequest emptyRegisterRequest() {
        return new RegisterRequest("", "", "", "", "", "");
    }
}
