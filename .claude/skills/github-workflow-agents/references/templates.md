# templates.md — Issue / PR 본문 템플릿 (Claude Code)

`github-workflow-agents` 스킬이 Issue/PR을 만들 때 쓰는 본문 틀이다.
모든 본문은 한국어로 쓴다(문장은 마침표로 끝낸다). 작성 전 `AGENTS.md` + `CLAUDE.md`를 따른다.

---

## 1. Issue 본문 템플릿
제목: `[도메인] 작업 내용` (예: `[Product] N+1 해결 + 상품 상태 검증 추가`)

```md
## 배경 / 문제
<무엇이 왜 문제인지 1~3줄. 파일·라인 근거 포함>
- 근거: `경로/파일.java:라인` — <증상>

## 구현 범위 (이 이슈에서 할 것)
- [ ] <변경 1>
- [ ] <변경 2>

## 제외 범위 (이 이슈에서 안 할 것)
- <다른 이슈로 넘길 것 — 범위 폭주 방지>

## 수용 기준 (완료 조건 · 측정 가능하게)
- [ ] <"깔끔하게" 금지. 검증 가능한 문장으로>
- [ ] 예) ProductService.getMyProducts()가 FETCH JOIN으로 단일 쿼리 실행
- [ ] 예) 관련 단위/통합 테스트 green

## 테스트 기준
- [ ] <실행할 테스트 또는 추가할 테스트>
- [ ] 예) `./gradlew test --tests ProductServiceTest`
- 엔티티/스키마/시드/설정 변경 시 `./gradlew clean build`까지. (CLAUDE.md §2·§3)
- 동시성/결제/검색 도메인은 예외·실패 재현 테스트 필수.

## 선행 / 충돌 주의
- 선행 이슈: <#번호 또는 없음>
- 같은 파일 동시 작업 주의: <파일명 — 다른 이슈와 worktree 충돌 가능 여부>

## 참고 문서 (Wiki)
- <담당 도메인 문서명 — AGENTS.md §3 매핑>
```

생성: `gh issue create --title "[도메인] ..." --body "..." [--label "..."]`

### 작성 규칙
- 한 이슈 = 한 PR로 끝낼 **작은 단위.** 거대한 "전부 리팩토링" 이슈 금지.
- **같은 파일/같은 로직을 건드리는 항목은 한 이슈로 묶는다.** (worktree 병렬 충돌 방지)
- 수용 기준은 사람이 ✅/❌ 판단 가능해야 한다.

---

## 2. PR 본문 템플릿
제목: `[도메인] 작업 내용 (#이슈번호)` · 저장소 `.github/pull_request_template.md` 형식을 우선한다.

```md
## 연결 이슈
close #<이슈번호>

## 변경 요약
- <무엇을 어떻게 바꿨는지 핵심 1~3줄>

## 변경 파일
- `경로/파일.java` — <변경 내용>

## 테스트 결과 (실제 실행 결과만)
- [ ] `./gradlew clean build` 또는 `./gradlew test --tests ...` → <PASS/FAIL + 핵심 수치>
- 실행하지 않은 테스트를 했다고 적지 않는다.

## 수용 기준 충족
- [ ] 이슈의 수용 기준 항목별 충족 여부

## AI 사용 내역
- 어떤 작업을 어떤 AI(모델)로 했는지, 무엇을 검증했는지 적는다.
- (저장소 PR 템플릿에 해당 항목이 없으면 "참고 사항"에 이 내용을 추가한다.)

## 남은 위험 / 후속
- <알려진 한계, 범위 밖에서 발견한 문제 → Coordinator 보고 후 새 이슈 후보>

## 대상 브랜치
- base: `dev`
```

생성: `gh pr create --base dev --head feature/issue-<번호>-<설명> --title "..." --body "..."`

### 작성 규칙
- 대상 브랜치는 항상 `dev`. (`main` 직접 PR 금지) 푸시·PR은 `sparta` 리모트.
- 리뷰 코멘트는 **같은 PR에서** 수정해 다시 push한다(작은 루프). 범위 밖 문제는 새 이슈로.

---

## 3. PR Review 코멘트 형식 (PR Review Agent용)
```md
## 리뷰 결과
### P0 (머지 막음)
- `파일:라인` — <문제 + 근거>
### P1 (고쳐야 함)
- ...
### P2 (제안)
- ...

VERDICT: APPROVED   # 또는 REVISE (P0/P1이 하나라도 있으면 REVISE)
```
- 인라인 코멘트는 `gh api .../pulls/<번호>/comments`, 일반 코멘트는 `gh pr comment <번호>`.

---

## 4. merge 전 최종 체크 (Coordinator용)
- [ ] 연결 이슈의 수용 기준 충족
- [ ] 테스트/smoke 결과 있음 (실제 실행 — CI는 `./gradlew clean build`)
- [ ] 최신 `dev` 기준 conflict 없음
- [ ] PR 대상 브랜치 = `dev`
- [ ] 내부 자료·평가셋 미노출
- [ ] merge 직전 head SHA 확인
- merge: `gh pr merge <번호> --merge` 후 `gh issue close <번호>`. `dev`까지만, `main`은 사람이.
