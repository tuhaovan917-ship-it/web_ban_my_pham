package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.model.UserRole;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import do_an_java.quan_ly_my_pham.service.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUserName(request.userName())) {
            throw new BusinessException("Ten dang nhap da ton tai");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email da ton tai");
        }

        User user = new User();
        user.setUserName(request.userName().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        return userRepository.save(user);
    }
}
