package com.example.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 允許 Swagger/OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()

                        // ===== 公開訪問（無需登入）=====
                        // 首頁和瀏覽功能
                        .requestMatchers("/", "/index", "/index.html").permitAll()
                        // 住宿瀏覽相關
                        .requestMatchers("/accommodations/**", "/accommodation/**").permitAll()
                        .requestMatchers("/api/accommodations/**").permitAll()
                        .requestMatchers("/api/room-types/**").permitAll()
                        // 評論查詢（公開）
                        .requestMatchers("/api/reviews/accommodation/**").permitAll()
                        // 靜態資源
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        // 認證相關
                        .requestMatchers("/login", "/register", "/api/auth/**").permitAll()
                        // 忘記密碼
                        .requestMatchers("/user/forgot-password", "/user/reset-password").permitAll()
                        // H2 Console（開發用）
                        .requestMatchers("/h2-console/**").permitAll()

                        // ===== 需要登入的功能 =====
                        // 預訂功能（需登入）
                        .requestMatchers("/api/bookings/**").authenticated()
                        // 收藏功能（需登入）
                        .requestMatchers("/user/favorites/**", "/user/favorites/api/**").authenticated()
                        // 評論新增/修改（需登入）
                        .requestMatchers("/api/reviews/**").authenticated()
                        // 用戶個人頁面
                        .requestMatchers("/user-bookings", "/user-profile", "/user/profile", "/user/api/**").authenticated()

                        // ===== 管理員專用 =====
                        .requestMatchers("/admin-dashboard", "/admin-accommodations", "/admin-bookings", "/admin-users").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**", "/api/bookings/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/export/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/statistics/admin/**").hasRole("ADMIN")

                        // ===== 房東專用 =====
                        .requestMatchers("/owner-dashboard", "/owner-accommodations", "/owner-bookings").hasRole("OWNER")
                        .requestMatchers("/api/owner/**").hasRole("OWNER")
                        .requestMatchers("/api/export/owner/**").hasRole("OWNER")
                        .requestMatchers("/api/statistics/owner/**").hasRole("OWNER")

                        // ===== 房型管理（Admin 和 Owner 都可訪問）=====
                        .requestMatchers("/room-type-management").hasAnyRole("ADMIN", "OWNER")

                        // 其他請求需要登入
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            // 檢查是否有重定向 URL
                            String redirectUrl = request.getParameter("redirect");

                            // 輸出調試資訊
                            System.out.println("登入成功，用戶：" + authentication.getName());
                            System.out.println("擁有權限：" + authentication.getAuthorities());

                            if (authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                                System.out.println("導向到管理員儀表板");
                                response.sendRedirect("/admin-dashboard");
                            } else if (authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"))) {
                                System.out.println("導向到房東儀表板");
                                response.sendRedirect("/owner-dashboard");
                            } else {
                                System.out.println("導向到首頁或原頁面");
                                // 如果有重定向 URL，導向該頁面，否則導向首頁
                                response.sendRedirect(redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**"));

        return http.build();
    }
}