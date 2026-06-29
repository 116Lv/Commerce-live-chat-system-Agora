<!-- Claude Code 전용 작업 지침. 공용 운영 규칙은 AGENTS.md, 도메인·정책은 GitHub Wiki가 단일 출처다. -->
# CLAUDE.md — Agora (중고거래 커머스)

이 파일은 **Claude Code 세션이 자동으로 읽는** 작업 지침이다.
운영 규칙(위키 읽는 법, 역할, merge 권한 등)은 [`AGENTS.md`](AGENTS.md)에 이미 정리돼 있으니 **중복하지 않는다**.
여기에는 Claude가 이 저장소에서 자주 헛디뎠던 부분과 프로젝트 고유 함정만 적는다.

---

## 0. 시작 전에
- 작업 전 [`AGENTS.md`](AGENTS.md)를 먼저 읽는다. 설계·정책·도메인·API는 GitHub [Wiki](https://github.com/sparta-spring4/Commerce-live-chat-system-Agora/wiki)가 단일 출처다(추측 금지).
- 파일은 통째로 읽지 않는다. `rg`, `git diff --name-only`로 범위를 좁혀 필요한 부분만 읽는다.

## 1. 스택 한눈에
- Spring Boot 4.1 / Java 21 (Temurin) / Gradle Wrapper
- JPA(Hibernate) + QueryDSL, 기본 프로파일 H2(`MODE=MySQL`), 운영 프로파일 MySQL + Flyway
- Redis(Redisson) · WebSocket(채팅) · Spring Security
- 루트 패키지 `com.team7.agora`, 도메인별 패키지(`domain/<name>`), 공용은 `global/`

## 2. 빌드 / 테스트 — **CI와 똑같이 검증한다**
- CI(`.github/workflows`)는 PR/`dev` push 시 **`./gradlew clean build`**(전체 통합 테스트 포함)를 돈다.
- **단위 테스트 하나만 돌리고 "통과"라고 끝내지 않는다.** 통합 테스트는 전체 Spring 컨텍스트를 로딩하므로, 단위 테스트가 못 잡는 깨짐을 잡는다. 엔티티/스키마/설정/시드를 건드렸으면 반드시 `clean build`까지 돌린다.
- 명령 (OS별 둘 다)
  - 전체: macOS/Linux `./gradlew clean build` · Windows `.\gradlew.bat clean build`
  - 단일 테스트: `./gradlew test --tests "*NegoServiceTest"` (Windows는 `.\gradlew.bat`)
- Windows 비ASCII 경로 함정: 경로에 한글 등이 있으면 테스트 워커 classpath argfile이 깨진다. `~/.gradle/gradle.properties`에 `localBuildDir=C:/agora-build` 같은 ASCII 경로를 설정한다(커밋 금지). 자세한 건 `build.gradle` 상단 주석 참고.

## 3. ⚠️ DB 스키마는 **두 경로**다 — 컬럼 추가 시 세 곳을 동기화한다
이 저장소에서 가장 자주 터지는 회귀 지점이다.
- **기본/테스트 프로파일**: H2, `ddl-auto: create-drop`, Flyway 비활성, `src/main/resources/data.sql` 시드 실행.
- **운영(prod) 프로파일**: MySQL, `ddl-auto: validate`(Hibernate가 스키마를 만들지 않고 검증만 함), Flyway 마이그레이션(`src/main/resources/db/migration/V<N>__*.sql`)이 실제 스키마.

엔티티에 컬럼을 추가/변경하면 **반드시 세 곳을 함께** 바꾼다.
1. 엔티티 필드
2. `data.sql` 시드 INSERT (컬럼·값 추가) — 누락 시 H2 시드 삽입이 제약 위반 → **전체 통합 테스트 컨텍스트 로딩 실패**
3. 새 Flyway 마이그레이션 `V<N>__...sql` — 누락 시 운영 배포 때 `validate` 실패
- 실제 사례: `NegoOffer.extension_requested`(NOT NULL) 추가 시 `data.sql` 시드를 안 고쳐서 CI 통합 테스트가 전부 깨졌다(네고와 무관한 테스트까지). `data.sql` 보강 + `V4` 마이그레이션으로 해결.
- Hibernate `boolean`은 MySQL에서 `bit`으로 매핑된다(마이그레이션 컬럼 타입 기준).

## 4. AI가 자주 하는 실수 (이 저장소 세션 학습)
- **추측으로 접근하지 않는다.** 에러/로그 **원문**을 읽고 원인을 확정한 뒤 고친다. 키워드만 보고 "흔한 수정"을 먼저 적용하지 않는다.
- **추측이 빗나가면 추측을 더 쌓지 않는다.** 한 번 틀렸으면 멈추고, 상태를 직접 확인(로그/필드 출력/재현)한 뒤 다음 수를 둔다.
- **실행하지 않은 테스트를 실행했다고 말하지 않는다.** 결과는 실제 출력으로만 보고한다(`AGENTS.md` §4).
- 단위 테스트 통과 ≠ 완료. §2·§3대로 통합 영향까지 확인한다.
- 요청(이슈) 범위 밖 리팩토링을 임의로 하지 않는다. 무관한 변경은 같은 PR에 섞지 않는다.

### 셸 / 도구 함정
- 이 환경의 Bash 도구는 **POSIX sh**다. PowerShell here-string(`@'...'@`)을 쓰면 커밋 메시지에 리터럴 `@`가 들어간다. 여러 줄 메시지는 `git commit -F - <<'EOF' ... EOF`를 쓴다.
- Bash 호출마다 **작업 디렉터리가 리셋**된다. 명령마다 절대경로로 `cd` 한다.
- 리모트 구분: `origin` = 개인 포크, `sparta` = 업스트림(`sparta-spring4`). **PR/푸시는 `sparta`**, base는 `dev`. PR head 브랜치도 `sparta`에 푸시한다.

## 5. 코드 컨벤션 (이 저장소 고유)
- 시간은 항상 **`AgoraClock.now()`**(UTC)를 쓴다. `LocalDateTime.now()`를 직접 쓰지 않는다(테스트 시간 고정 위해).
- 예외는 `BusinessException` + `ErrorCode`로 던진다.
- 새 소스 파일 첫 줄에 **역할을 설명하는 한국어 한 줄 주석**을 단다.
- 한국어로 출력/문서/커밋을 쓸 때 문장은 마침표로 끝낸다(콜론으로 끝내지 않는다).

## 6. Git / PR
- 작업 브랜치: `feature/issue-<번호>-<짧은-영문>` (문서류는 `docs/<설명>`). base는 항상 `dev`. `main`·`merge`는 건드리지 않는다(`AGENTS.md` §5·§6).
- PR 본문은 `.github/pull_request_template.md` 형식을 따르고 `close #<번호>`로 이슈를 연결한다.
- **PR 본문에 AI 사용 내역을 적는다.** 템플릿에 항목이 없으면 "참고 사항"에 무엇을 AI로 했는지(어떤 작업/검증) 추가한다.

## 7. 세션 종료 시 — 학습 반영
- 이번 세션에서 막혔거나 헛디뎠고 **재발 가능성이 있는** 내용은 이 `CLAUDE.md`에 한 줄로 추가한다(장황한 설명·일회성 수정은 제외).
- 도구: `claude-md-management` 플러그인의 `/revise-claude-md`(세션 학습 반영), `claude-md-improver` 스킬(코드베이스와 정합성 감사)을 활용한다.
