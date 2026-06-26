// 이메일로 사용자를 조회해 UserDetails를 제공하는 서비스
package com.team7.agora.global.auth;

import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication component for custom user details behavior.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Creates a custom user details service instance.
     * @param userRepository the user repository value
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns user by username data.
     * @param email the email value
     * @return the load user by username result
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email));
        return CustomUserDetails.from(user);
    }
}
