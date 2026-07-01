package com.team7.agora.global.config;

import com.team7.agora.global.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정이다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param jwtAuthenticationFilter HTTP 요청의 JWT를 인증 객체로 바꾸는 필터
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 비밀번호를 안전하게 해시하기 위해 BCrypt PasswordEncoder 빈을 등록한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 요청에 적용할 인증, 인가, CORS, JWT 필터 설정을 구성한다.
     * @param http HTTP 보안 설정 객체
     * @return 클라이언트에 반환할 API 응답
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedEntryPoint()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/reissue",
                    "/api/admin/auth/login",
                    "/api/regions",
                    "/api/v1/products/search",
                    "/api/v2/products/search",
                    "/api/v1/search/popular",
                    "/api/search/keywords/realtime",
                    "/api/search/keywords/daily",
                    "/api/search/keywords/weekly",
                    "/api/payments/webhook",
                    "/api/payments/webhooks/**",
                    "/uploads/**",
                    "/ws",
                    "/ws/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/coupon-events").permitAll()
                .requestMatchers("/api/admin/**").hasAnyAuthority(
                    "ROOT_ADMIN",
                    "USER_ADMIN",
                    "PRODUCT_ADMIN",
                    "SETTLEMENT_ADMIN",
                    "USER_MANAGE",
                    "REPORT_MANAGE",
                    "PRODUCT_MANAGE",
                    "PAYMENT_MANAGE",
                    "COUPON_MANAGE",
                    "ADMIN_ACCOUNT_MANAGE",
                    "APPROVAL_MANAGE"
                )
                .anyRequest().authenticated()
            )
            .build();
    }

    /**
     * 'unauthorizedEntryPoint' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @return 클라이언트에 반환할 API 응답
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> response.sendError(401, "인증이 필요합니다.");
    }
}
