package com.team7.agora.global.config;

import com.team7.agora.global.auth.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param jwtAuthenticationFilter 입력 값
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 요청한 동작을 처리한다.
     * @return 처리 결과
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 요청한 동작을 처리한다.
     * @param http 입력 값
     * @return 처리 결과
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
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
                    "/ws"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/*/smile-score").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/coupon-events").permitAll()
                .requestMatchers("/api/admin/**").hasAnyAuthority(
                    "ROOT_ADMIN",
                    "USER_ADMIN",
                    "PRODUCT_ADMIN",
                    "SETTLEMENT_ADMIN"
                )
                .anyRequest().authenticated()
            )
            .build();
    }

    /**
     * 요청한 동작을 처리한다.
     * @return 처리 결과
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> response.sendError(401, "인증이 필요합니다.");
    }
}
