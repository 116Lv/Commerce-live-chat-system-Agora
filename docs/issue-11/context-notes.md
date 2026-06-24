# Issue #11 컨텍스트 노트

## 폴더/패키지 상황 (중요)
- Desktop에 두 폴더 존재.
  - `Commerce-live-chat-system-Agora` = **원본/작업대상**, 패키지 `com.team7.agora`, 리모트 `116Lv`, HEAD `f3babf3`(거의 빈 스켈레톤, `ApiResponse`만 존재).
  - `Commerce-live-chat-system-Agora - 복사본` = **참조용**, 패키지 `com.team11.agora`, 인증 전부 구현됨(74개 파일이 `AuthUser`에 의존).
- 원본 경로는 공백/한글 없음 → `./gradlew test` 직접 실행 가능(복사본의 ASCII 동기화 워크어라운드 불필요).

## 설계 결정
1. **principal = CustomUserDetails (UserDetails 구현)**. 복사본은 `AuthUser` record를 직접 썼지만, 원본은 신규라 표준 방식 채택. 이슈 구현 범위에 명시됨.
2. **상태 검증 = 요청마다 DB 조회**(사용자 선택). 필터에서 토큰 서명/만료 검증 후 `CustomUserDetailsService.loadUserByUsername(email)`로 User 로드, `isEnabled()`(=ACTIVE)로 차단. 이미 발급된 토큰도 정지/탈퇴 즉시 반영됨.
3. **권한은 DB role을 신뢰**(토큰 클레임의 role을 인가에 쓰지 않음). 토큰 변조/권한 변경 안전.
4. **공통 JSON 에러**: 필터/엔트리포인트/AccessDeniedHandler 모두 `ApiResponse.error` + ErrorCode 상태코드. `response.sendError`(HTML) 미사용.
5. **JWT 라이브러리 미추가**: 복사본처럼 JDK HMAC-SHA256 직접 구현(빌드 의존성 변경 최소화).

## 상태코드 매핑
- 토큰 없음 + 보호 API → 401 (EntryPoint, UNAUTHORIZED)
- Bearer 형식 오류/서명 오류 → 401 (INVALID_TOKEN)
- 만료 → 401 (EXPIRED_TOKEN)
- 사용자 없음 → 401 (USER_NOT_FOUND)
- 정지/차단/탈퇴 → 403 (INACTIVE_USER)
- 권한 부족 → 403 (AccessDeniedHandler, FORBIDDEN)

## 범위 밖(다른 이슈)
- 로그인/회원가입/재발급 컨트롤러·서비스. JwtProvider.createToken만 미리 제공(테스트·후속 이슈용).
- User 도메인 전체 기능(프로필/스마일점수 등). 여기선 인증에 필요한 최소 필드만.
