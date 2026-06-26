// 요청마다 JWT를 검증하고 사용자 상태 확인 후 SecurityContext에 인증을 저장하는 필터
package com.team7.agora.global.auth;

import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 인증 처리를 담당하는 컴포넌트이다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param jwtProvider JWT 생성과 검증을 담당하는 컴포넌트
     * @param userDetailsService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
    public JwtAuthenticationFilter(
        JwtProvider jwtProvider,
        UserDetailsService userDetailsService
    ) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtClaims claims = jwtProvider.parse(jwtProvider.substringBearer(authorization));
            CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(claims.email());

            if (!userDetails.isEnabled()) {
                SecurityContextHolder.clearContext();
                AuthErrorResponseWriter.write(response, ErrorCode.INACTIVE_USER);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BusinessException e) {
            SecurityContextHolder.clearContext();
            AuthErrorResponseWriter.write(response, e.getErrorCode());
            return;
        } catch (UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            AuthErrorResponseWriter.write(response, ErrorCode.USER_NOT_FOUND);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
