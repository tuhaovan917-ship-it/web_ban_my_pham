package do_an_java.quan_ly_my_pham.service;

import do_an_java.quan_ly_my_pham.exception.BusinessException;
import do_an_java.quan_ly_my_pham.exception.NotFoundException;
import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.model.UserRole;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public User findById(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Khong tim thay nguoi dung"));
    }

    @Transactional
    public User lockUser(Integer userId) {
        User user = findById(userId);
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException("Khong the khoa tai khoan quan tri vien");
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
}
