# Issue #11 [Auth] Spring Security JWT Filter — 체크리스트

대상: 원본 폴더 `Commerce-live-chat-system-Agora` (`com.team7.agora`)
참조: 복사본(`com.team11.agora`) 기존 구현

## 기반 (상태 검증 DB 조회를 위한 최소 의존성)
- [x] `global/exception/ErrorCode` — 인증 관련 코드 추가 → 검증: enum 컴파일
- [x] `global/exception/BusinessException` → 검증: ErrorCode 보유
- [x] `domain/user/enums/UserRole` (ROLE_USER + 4 admin)
- [x] `domain/user/enums/UserStatus` (ACTIVE/SUSPENDED/BLOCKED/DELETED)
- [x] `domain/user/entity/User` (id/email/password/nickname/role/status)
- [x] `domain/user/repository/UserRepository` (findByEmail)

## 인증 구성요소
- [x] `global/auth/JwtClaims` (record)
- [x] `global/auth/JwtProvider` — HS256 서명/만료 검증, 실패 시 ErrorCode 예외
- [x] `global/auth/CustomUserDetails` — status→isEnabled/isAccountNonLocked 매핑
- [x] `global/auth/CustomUserDetailsService` — email로 User 조회
- [x] `global/auth/AuthErrorResponseWriter` — 공통 JSON 에러 직렬화
- [x] `global/auth/JwtAuthenticationEntryPoint` — 미인증 401 JSON
- [x] `global/auth/JwtAccessDeniedHandler` — 인가 실패 403 JSON
- [x] `global/auth/JwtAuthenticationFilter` — OncePerRequest, DB 상태검증
- [x] `global/config/SecurityConfig` — Public/User/Admin 분리, STATELESS

## 테스트 (given/when/then)
- [x] `JwtProviderTest` — 생성/파싱, 만료, 변조 서명, Bearer 형식
- [x] `CustomUserDetailsTest` — 상태별 플래그 매핑
- [x] `JwtAuthenticationFilterTest` — 토큰없음/잘못된토큰/만료/정지/탈퇴/정상

## 완료 기준 검증
- [x] 인증 필요 API + 토큰 없음 → 401
- [x] 잘못된/만료 토큰 → 401
- [x] 정지/탈퇴 사용자 → 접근 실패(403)
- [x] `./gradlew test` 통과
