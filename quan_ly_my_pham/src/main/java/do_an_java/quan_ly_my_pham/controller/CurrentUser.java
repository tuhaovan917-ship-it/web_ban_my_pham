package do_an_java.quan_ly_my_pham.controller;

import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final UserRepository userRepository;

    public User requireUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("Ban chua dang nhap");
        }

        return userRepository.findByUserName(authentication.getName())
            .orElseThrow(() -> new NotFoundException("Khong tim thay nguoi dung dang dang nhap"));
    }
}
