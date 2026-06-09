package do_an_java.quan_ly_my_pham.security;

import do_an_java.quan_ly_my_pham.model.User;
import do_an_java.quan_ly_my_pham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
            .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay tai khoan"));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUserName())
            .password(user.getPassword())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
            .accountLocked(!Boolean.TRUE.equals(user.getActive()))
            .disabled(!Boolean.TRUE.equals(user.getActive()))
            .build();
    }
}
