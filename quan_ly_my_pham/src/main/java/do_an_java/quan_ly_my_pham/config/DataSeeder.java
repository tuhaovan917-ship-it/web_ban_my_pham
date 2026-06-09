package do_an_java.quan_ly_my_pham.config;

import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.model.UserRole;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin", "admin123", "Administrator", "admin@example.com", "0900000000", UserRole.ADMIN);
        seedUser("customer01", "customer123", "Demo Customer", "customer01@example.com", "0911111111", UserRole.CUSTOMER);
    }

    private void seedUser(
        String userName,
        String rawPassword,
        String fullName,
        String email,
        String phone,
        UserRole role
    ) {
        User user = userRepository.findByUserName(userName).orElseGet(User::new);
        user.setUserName(userName);
        if (user.getPassword() == null || user.getPassword().contains("replace_with_bcrypt_hash")) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }
}
