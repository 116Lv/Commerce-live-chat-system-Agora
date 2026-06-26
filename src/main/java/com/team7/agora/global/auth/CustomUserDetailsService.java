// 이메일로 사용자를 조회해 UserDetails를 제공하는 서비스
package com.team7.agora.global.auth;

import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 처리를 담당하는 컴포넌트이다.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 'loadUserByUsername' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param email 이메일
     * @return 클라이언트에 반환할 API 응답
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException(email));
        return CustomUserDetails.from(user);
    }
}
