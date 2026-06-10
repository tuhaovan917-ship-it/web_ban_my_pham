package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.model.UserRole;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import do_an_java.quan_ly_my_pham.service.dto.PasswordChangeRequest;
import do_an_java.quan_ly_my_pham.service.dto.ProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public User findById(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    }

    @Transactional
    public User lockUser(Integer userId) {
        User user = findById(userId);
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException("Không thể khóa tài khoản quản trị viên");
        }

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User unlockUser(Integer userId) {
        User user = findById(userId);
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Integer userId, ProfileUpdateRequest request) {
        User user = findById(userId);
        String email = request.email().trim();
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new BusinessException("Email này đã được tài khoản khác sử dụng");
        }

        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(blankToNull(request.phone()));
        user.setAddress(blankToNull(request.address()));
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Integer userId, PasswordChangeRequest request) {
        User user = findById(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu hiện tại không đúng");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("Xác nhận mật khẩu mới không khớp");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
