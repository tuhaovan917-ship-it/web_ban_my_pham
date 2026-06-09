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

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerRequest", emptyRegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(registerRequest);
            return "redirect:/login?registered";
        } catch (BusinessException exception) {
            model.addAttribute("registerError", exception.getMessage());
            return "auth/register";
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
