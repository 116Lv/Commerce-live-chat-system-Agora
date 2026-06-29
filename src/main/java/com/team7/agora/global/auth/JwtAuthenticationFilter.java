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
