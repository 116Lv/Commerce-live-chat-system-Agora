# AGENTS.md — Agora (중고거래 커머스)

이 파일은 AI 에이전트가 작업 시작 전 **자동으로 먼저 읽는** 운영 지침이다.
설계·정책·도메인 지식은 여기에 복사하지 않는다 — 모두 GitHub Wiki가 단일 출처다.
이 파일은 "에이전트가 위키를 어떻게 읽고, 어떤 운영 규칙으로 일하는가"만 정의한다.

---

## 0. 진실의 출처(Source of Truth) · 우선순위
- 설계·정책·도메인·API·ERD·컨벤션의 단일 출처는 **GitHub Wiki**다.
- Wiki: https://github.com/sparta-spring4/Commerce-live-chat-system-Agora/wiki
- GitHub Wiki는 별도 repo(`*.wiki.git`)다. 위키 내용이 필요하면 직접 읽어온다(추측 금지).
- **우선순위:** 시스템/개발자/보안 지침 또는 사용자 명시 요청과 충돌하면 그 지침을 우선한다.
  그 외에는 Wiki를 따르고, 이 파일과 Wiki가 다르면 차이를 먼저 보고한다.

## 1. 파일 읽기 / 컨텍스트 규칙 (토큰 절약 — 통째로 읽지 않는다)
- `.agents/`나 스킬은 **이름/설명으로 먼저 확인**하고, 요청과 맞는 `SKILL.md`만 읽어 적용한다.
- 문서·코드는 `rg`(ripgrep), `git diff --name-only`, `git diff --stat` 등으로 **범위를 좁힌 뒤 필요한 파일만** 읽는다.
  위키 전체나 도메인 문서 묶음, `src/**/*` 를 한 번에 읽지 않는다.
- **PR 리뷰는 diff 범위만** 읽는다 (`git diff dev...HEAD`). 변경되지 않은 파일을 전수 조사하지 않는다.
- `CodeConvention`처럼 큰 문서는 **인덱스(목차)로만** 보고, 필요한 `Code-Convention-Examples`/세부 규칙만 펼쳐 읽는다.
- OS별 명령어가 다르면 **둘 다** 적는다. (아래 §7 참고)
- 셸별 줄 연결 문자가 다른 여러 줄 CLI 명령은 **가능하면 한 줄로** 적는다.

## 2. 모든 에이전트가 작업 전 반드시 읽을 문서
(AI-Workflow 문서의 규칙을 그대로 따른다. 단, §1대로 필요한 부분만 좁혀 읽는다.)
1. Home
2. Onboarding-Guide
3. Constitution
4. Policies
5. API-Contracts
6. 담당 도메인 문서 (아래 §3 매핑)
7. CodeConvention / Code-Convention-Examples (인덱스 먼저)
8. Github-Rules

## 3. 도메인 → 먼저 읽을 문서 (AI-Workflow §8)
각 Dev Agent는 자기 이슈 제목의 `[도메인]`에 해당하는 줄만 읽으면 된다.

| 도메인 | 먼저 읽을 문서 |
| --- | --- |
| 인증/회원 | Domain-Auth-Member, API-Contracts, CodeConvention |
| 상품/지역 | Domain-Product-Region, Policies, API-Contracts |
| 검색/캐싱 | Domain-Search-Caching, Caching-Strategy |
| 채팅/네고 | Domain-Chat-Nego, Chat-Nego-Flow |
| 거래/결제 | Domain-Trade-Payment-Settlement, Trade-Payment-Flow |
| 쿠폰/동시성 | Domain-Coupon-Concurrency, Concurrency-Strategy |
| 후기/관리자 | Domain-Review-Admin, Policies |

## 4. 절대 규칙 (autonomous 루프 안전장치)
- **실행하지 않은 테스트를 실행했다고 말하지 않는다.** (가장 중요 — 무인 루프는 transcript를 믿는다)
- Issue 없이 큰 기능을 바로 구현하지 않는다.
- 추측을 확정하지 않는다. 모르는 건 `확인 필요`로 분리한다.
- 변경한 파일 / 테스트 결과 / 남은 위험을 항상 보고한다.
- 요청 범위(이슈) 밖의 리팩토링을 임의로 하지 않는다.
- 내부 자료·평가셋·비공개 transcript를 공개 Issue/PR/문서에 노출하지 않는다.

## 5. Git / 브랜치 / worktree
- 통합 브랜치: `dev` (모든 작업 브랜치의 기준이자 PR 대상)
- `main`: 최종 배포/제출 — 직접 건드리지 않는다.
- 작업 브랜치: `feature/issue-<번호>-<짧은-영문-설명>`  <!-- 팀 컨벤션 feature/* + 이슈 추적성 결합 -->
- worktree 경로: `/tmp/agora_issue_<번호>`  <!-- TODO: repo 이름 확정 -->
- Dev Agent끼리 **write scope가 겹치지 않게** 한다. 서로 다른 이슈 + 서로 다른 worktree만 배정한다.
- subagent에게 명시: "혼자 작업하는 게 아니며, 다른 작업자의 변경을 되돌리지 마라."

## 6. 역할별 권한
- **Dev Agent**: 구현·테스트·commit·push·PR 생성까지만. **merge 금지.**
- **PR Review Agent**: 칭찬보다 버그·회귀·요구사항 누락·테스트 누락을 먼저 찾는다. diff 범위만 본다(§1).
- **Merge Coordinator**: merge와 issue close는 **오직 이 역할만** 수행한다.

## 7. 빌드 / 테스트 커맨드 (OS별 둘 다 명시)  <!-- TODO: 실제 값 확인 -->
- 테스트: macOS/Linux `./gradlew test` · Windows `.\gradlew.bat test`
- 빌드:   macOS/Linux `./gradlew build` · Windows `.\gradlew.bat build`
- 검색/캐싱·동시성·결제 도메인은 **예외/실패 재현 테스트 필수** (AI-Workflow §9 참고).

## 8. PR merge 전 체크리스트
- [ ] 연결된 Issue의 수용 기준 충족
- [ ] 테스트 또는 smoke command 결과 있음 (실제 실행 확인)
- [ ] 최신 `dev` 기준 conflict 없음
- [ ] PR 대상 브랜치가 `dev`
- [ ] 내부 자료 미노출
- [ ] merge 직전 head SHA 확인
- [ ] AI-Workflow §12 검증 체크리스트 통과

## 9. 이슈 / PR 형식
- Issue 제목: `[도메인] 작업 내용`  (예: `[Payment] 결제 사전 등록 API 구현`)
- Issue 본문: 구현 범위 / 제외 범위 / 수용 기준 / 테스트 기준 / 선행 작업
- PR: 하나의 이슈 중심, 대상 브랜치 `dev`, 본문은 Github-Rules의 PR 템플릿 사용, `close #<번호>`로 이슈 연결.
