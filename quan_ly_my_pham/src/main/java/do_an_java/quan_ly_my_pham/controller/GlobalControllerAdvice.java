package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final UserRepository userRepository;

    @ModelAttribute("currentUser")
    public User populateCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUserName(authentication.getName()).orElse(null);
    }
}
