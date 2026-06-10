package do_an_java.quan_ly_my_pham.config;

import do_an_java.quan_ly_my_pham.security.RoleBasedLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final RoleBasedLoginSuccessHandler roleBasedLoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    pathPattern("/"),
                    pathPattern("/products"),
                    pathPattern("/product/**"),
                    pathPattern("/login"),
                    pathPattern("/css/**"),
                    pathPattern("/js/**"),
                    pathPattern("/images/**"),
                    pathPattern("/uploads/**")
                ).permitAll()
                .requestMatchers(pathPattern("/home")).hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(pathPattern("/admin/**")).hasRole("ADMIN")
                .requestMatchers(pathPattern("/profile/**")).hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(
                    pathPattern("/cart/**"),
                    pathPattern("/checkout/**"),
                    pathPattern("/orders/**"),
                    pathPattern("/reviews/**")
                ).hasRole("CUSTOMER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/dang-nhap")
                .successHandler(roleBasedLoginSuccessHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/403")
            )
            .rememberMe(remember -> remember
                .key("quan-ly-my-pham-remember-me")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
