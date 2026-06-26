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
 * Spring configuration for security behavior.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Creates a security config instance.
     * @param jwtAuthenticationFilter the jwt authentication filter value
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Handles password encoder behavior.
     * @return the password encoder result
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Handles security filter chain behavior.
     * @param http the http value
     * @return the security filter chain result
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
     * Handles unauthorized entry point behavior.
     * @return the unauthorized entry point result
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> response.sendError(401, "인증이 필요합니다.");
    }
}
