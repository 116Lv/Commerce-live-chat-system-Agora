// Authorization 헤더의 JWT를 검증해 SecurityContext에 인증 정보를 채우는 필터
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authorization 헤더가 없으면 인증 없이 통과시키고(인가는 이후 단계에서 처리),
 * 헤더가 있는데 검증에 실패하면 SecurityContext를 비우고 즉시 에러 응답을 써서 체인을 끊는다.
 * admin 토큰과 user 토큰은 각각 /api/admin 경로와 그 외 경로에서만 유효하도록
 * matchesRequestPath로 교차 사용을 막는다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final AdminDetailsService adminDetailsService;

    public JwtAuthenticationFilter(
        JwtProvider jwtProvider,
        @Qualifier("customUserDetailsService") UserDetailsService userDetailsService,
        AdminDetailsService adminDetailsService
    ) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.adminDetailsService = adminDetailsService;
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
            if (!matchesRequestPath(request, claims.accountType())) {
                SecurityContextHolder.clearContext();
                AuthErrorResponseWriter.write(response, ErrorCode.FORBIDDEN);
                return;
            }
            UserDetails principal = loadPrincipal(claims);

            if (!principal.isEnabled()) {
                SecurityContextHolder.clearContext();
                AuthErrorResponseWriter.write(response, ErrorCode.INACTIVE_USER);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
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

    private boolean matchesRequestPath(HttpServletRequest request, AccountType accountType) {
        // Admin and user tokens are intentionally not interchangeable across API surfaces.
        boolean adminPath = request.getRequestURI().startsWith("/api/admin");
        if (adminPath) {
            return accountType == AccountType.ADMIN;
        }
        return accountType == AccountType.USER;
    }

    private UserDetails loadPrincipal(JwtClaims claims) {
        if (claims.accountType() == AccountType.ADMIN) {
            return adminDetailsService.loadUserByUsername(claims.email());
        }
        return userDetailsService.loadUserByUsername(claims.email());
    }
}
