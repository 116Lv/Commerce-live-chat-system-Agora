---
name: github-workflow-agents
description: GitHub Issue와 Pull Request를 작업 큐로 사용해 에이전트형 개발 흐름을 운영한다. Use when the user wants Claude Code to create Korean issues, split work into issues, assign isolated git-worktree development agents, review PRs in a separate context, coordinate merges into dev, auto-file follow-up issues when problems are found, or keep an issue queue running until empty.
---

# GitHub Workflow Agents (Claude Code)

GitHub Issue를 작업의 **단일 출처(작업 큐)**로 삼고, 각 작업을 git worktree로 격리해 개발한 뒤,
PR 리뷰와 merge를 Coordinator가 관리하게 한다. 문제가 생기면 **새 이슈로 분리**해 큐에 넣고 멈추지 않는다.

> 이 스킬은 codex의 `.codex/skills/github-workflow-agents`를 Claude Code 기본 도구에 맞춰 옮긴 것이다.
> 시작 전 프로젝트 루트의 [`AGENTS.md`](../../../AGENTS.md)와 [`CLAUDE.md`](../../../CLAUDE.md)를 먼저 읽고, 이 스킬보다 프로젝트 지침을 우선한다.

## 언제 쓰나
사용자가 "이슈 만들기", "작업 쪼개기", "에이전트에게 배정", "PR 검사", "계속 일하게", "큐가 빌 때까지 반복", "문제 생기면 이슈로 만들어서 멈추지 않게" 같은 요청을 하면 이 스킬을 쓴다.

## 기본 흐름
```text
요구사항/위키 문서
  -> Issue Planner가 GitHub Issue 생성 (gh issue create)
  -> Coordinator가 의존성·병렬 가능성 판단, 안 겹치는 이슈 2~3개 선택
  -> Dev Agent가 이슈별 worktree에서 구현 -> 테스트 실제 실행 -> PR 생성
  -> PR Review Agent가 별도 컨텍스트(read-only)로 diff·테스트·요구사항 검사
  -> Coordinator가 merge 조건 충족 시 dev에 merge하고 이슈 close
  -> Experiment Agent가 smoke test로 실사용 검증
  -> 범위 밖 문제 발견 시 Issue Planner가 새 이슈 생성 (큐가 안 마름)
  -> 열린 이슈가 남아 있으면 반복
```

## 역할 선택
| 상황 | 역할 |
| --- | --- |
| 이슈를 만들거나 큰 덩어리를 쪼갠다 | Issue Planner |
| 열린 이슈 하나를 구현한다 | Dev Agent |
| 열린 PR의 안전성을 판단한다 | PR Review Agent (반드시 별도 세션) |
| 여러 이슈/PR을 계속 굴린다 | Merge Coordinator (진입점) |
| 구현된 기능을 실제 입력으로 검증한다 | Experiment Agent |

역할별 상세 프롬프트는 [`references/role-prompts.md`](references/role-prompts.md), Issue/PR 본문 틀은 [`references/templates.md`](references/templates.md)를 읽는다.

## Claude Code 도구 매핑 (codex와 다른 부분)
- **서브에이전트:** `Agent` 툴로 띄운다. 기본 `subagent_type`는 `general-purpose`, 탐색만 필요하면 `Explore`, 설계는 `Plan`.
- **격리:** 동시에 여러 Dev Agent가 파일을 고치면 `Agent` 툴의 `isolation: "worktree"`로 격리한다(또는 `.claude/worktrees/` 사용). worktree는 충돌이 실제로 있을 때만 쓴다(생성 비용 있음).
- **독립 리뷰:** PR Review는 **코드를 짠 세션과 분리된 새 `Agent`**로 띄운다. 깃허브 PR 전용 리뷰는 빌트인 `/review`(PR 번호 전달), 로컬 diff는 `/code-review`를 활용할 수 있다.
- **이슈/PR 조작:** 모두 `gh` CLI(`gh issue create`, `gh pr create`, `gh pr merge`, `gh issue close`, 라벨 부여)로 한다.
- **계속 일하게:** 세션이 열린 동안 주기 실행은 `/loop`, 무인 스케줄 실행은 `/schedule`(routine)을 제안한다. 자동화 프롬프트에는 작업만 적고 스케줄/워크스페이스 설정은 자동화 설정으로 분리한다.

## 기본 운영 규칙
- 응답과 GitHub Issue/PR 본문은 사용자의 언어를 따른다. 한국어 프로젝트에서는 한국어로 쓴다(문장은 마침표로 끝낸다).
- 통합 브랜치는 `dev`. 기능 브랜치는 `feature/issue-<번호>-<짧은-영문>`. `main`은 사람만 건드린다.
- 리모트 구분: `origin`=개인 포크, `sparta`=업스트림(`sparta-spring4`). PR/푸시·머지는 `sparta`, base는 `dev`.
- Dev Agent는 개발·테스트·commit·push·PR 생성까지만 한다. **merge·close는 Merge Coordinator만.**
- PR Review Agent는 칭찬보다 버그·회귀·요구사항 누락·테스트 누락을 먼저 찾는다.
- 실행하지 않은 테스트를 실행했다고 말하지 않는다. 모르는 건 `확인 필요`로 분리한다.
- 내부 자료·평가셋·비공개 transcript를 공개 Issue/PR/문서에 노출하지 않는다.

## 멈추지 않는 큐 (핵심)
- **작은 루프:** 리뷰 코멘트는 새 이슈가 아니라 **그 PR 안에서** Dev Agent가 고쳐 다시 push한다. `VERDICT: APPROVED`까지 반복.
- **큰 루프:** PR 범위 밖의 새 문제/새 기능·CI 실패의 근본 원인이 별도 작업이면 Issue Planner가 **새 이슈**를 만들어 큐에 넣고, 현재 작업은 계속 진행한다.
- **3회 실패** 이슈는 `blocked` 라벨을 달고 사람 확인용으로 남긴 뒤 다음 이슈로 넘어간다(한 이슈가 전체 루프를 막지 않게).
- 열린 이슈가 없고 품질 기준을 충족하면 **멈춘다.**

## Subagent 사용 규칙
- 서브에이전트는 사용자가 **명시적으로** 분배/병렬/위임을 요청했을 때만 만든다.
- Dev Agent를 여러 개 띄울 때는 각자 **다른 이슈 + 다른 worktree**, write scope가 겹치지 않게 한다.
- 계층은 평평하게(depth=1). 워커는 자기 서브에이전트를 만들지 않는다.
- Coordinator는 즉시 필요한 blocking 작업만 직접 하고, 독립 진행 가능한 개발/검토만 위임한다.
- 서브에이전트에게 "혼자 일하는 게 아니며 다른 작업자의 변경을 되돌리지 말라"고 명시한다.
- 완료된 에이전트 스레드는 닫는다. 동시 Dev는 2~3개로 시작한다(검사·테스트 처리량이 진짜 병목).

## PR merge 전 확인 (Coordinator)
- [ ] 연결 이슈의 수용 기준 충족
- [ ] 테스트 또는 smoke 결과 있음 (실제 실행 — CI는 `./gradlew clean build`)
- [ ] 최신 `dev` 기준 conflict 없음
- [ ] PR 대상 브랜치 = `dev`, head SHA 확인
- [ ] 리뷰 `VERDICT: APPROVED`
- [ ] 내부 자료 미노출
- `dev`까지만 merge한다. `main`은 사람이 확인 후.
