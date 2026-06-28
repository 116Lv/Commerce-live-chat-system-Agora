---
name: github-workflow-agents
description: GitHub Issue와 Pull Request를 작업 큐로 사용해 에이전트형 개발 흐름을 운영한다. Use when Codex should create Korean issues, assign isolated git worktree development agents, review PRs, coordinate merges, continue an issue queue, or run a repeatable GitHub-based multi-agent workflow in the Codex app.
---

# GitHub Workflow Agents

## 핵심 사용법

GitHub Issue를 작업의 단일 출처로 삼고, 각 작업을 git worktree로 격리해 개발한 뒤 PR 리뷰와 merge를 Coordinator가 관리하게 하라.

사용자가 "이슈 만들기", "에이전트에게 배정", "PR 검사", "계속 일하게", "이슈가 없을 때까지 반복" 같은 요청을 하면 이 skill을 사용한다.

프로젝트 루트에 `AGENTS.md`나 `.agents/`가 있으면 먼저 읽고, 이 skill보다 프로젝트 지침을 우선한다. 프로젝트 지침이 없으면 이 skill의 기본값을 사용한다.

## 기본 흐름

```text
요구사항/문서
  -> Issue Planner가 GitHub Issue 생성
  -> Merge Coordinator가 의존성과 병렬 가능성 판단
  -> Dev Agent가 이슈별 worktree에서 개발 후 PR 생성
  -> PR Review Agent가 diff, 테스트, 요구사항 충족 여부 검사
  -> Merge Coordinator가 승인된 PR을 통합 브랜치에 merge
  -> Experiment Agent가 필요한 smoke test와 결과 요약 수행
  -> 열린 이슈가 남아 있으면 반복
```

## 역할 선택

- Issue를 만들거나 작업을 쪼개야 하면 Issue Planner Agent를 사용한다.
- 열린 Issue 하나를 구현해야 하면 Dev Agent를 사용한다.
- 열린 PR의 안전성을 판단해야 하면 PR Review Agent를 사용한다.
- 여러 Issue/PR을 계속 굴려야 하면 Merge Coordinator Agent를 사용한다.
- 구현된 기능을 실제 입력으로 검증해야 하면 Experiment Agent를 사용한다.

역할별 상세 프롬프트가 필요하면 `references/role-prompts.md`를 읽는다. Issue/PR 본문 템플릿이 필요하면 `references/templates.md`를 읽는다.

## 기본 운영 규칙

- 기본 응답과 GitHub Issue/PR 설명은 사용자의 언어를 따른다. 한국어 프로젝트에서는 한국어로 작성한다.
- 통합 브랜치는 프로젝트 지침을 따른다. 이 Agora 프로젝트에서는 `dev`를 우선 후보로 두고, repo 관례를 확인한다.
- 기능 브랜치는 `issue-<번호>-<짧은-영문-설명>` 형식을 권장한다.
- worktree 경로는 `/tmp/<repo-name>_issue_<번호>` 형식을 권장한다.
- Dev Agent는 개발, 테스트, commit, push, PR 생성까지만 수행한다.
- Merge Coordinator만 merge와 issue close를 수행한다.
- PR Review Agent는 칭찬보다 버그, 회귀, 요구사항 누락, 테스트 누락을 먼저 찾는다.
- 내부 자료, 평가셋, 비공개 영상 내용, 민감한 transcript는 공개 Issue/PR/문서에 노출하지 않는다.

## Subagent 사용 규칙

Subagent는 사용자가 명시적으로 에이전트 분배, 병렬 작업, 위임을 요청했을 때만 만든다.

Dev Agent를 여러 개 만들 때는 각자 다른 Issue와 다른 worktree를 할당하고, write scope가 겹치지 않게 한다.

Coordinator는 즉시 필요한 blocking 작업은 직접 처리하고, 독립적으로 진행 가능한 개발 또는 검토만 subagent에게 맡긴다.

subagent에게는 "혼자 작업하는 것이 아니며 다른 작업자의 변경을 되돌리지 말라"고 명시한다.

## PR 병합 전 확인

- 연결된 Issue의 완료 조건을 충족한다.
- 테스트 또는 smoke command 결과가 있다.
- 최신 통합 브랜치 기준 conflict가 없다.
- PR 대상 브랜치가 맞다.
- 내부 자료가 노출되지 않았다.
- merge 전 head SHA를 확인했다.

## 계속 일하게 하기

사용자가 "계속 일하게", "주기적으로 확인", "큐가 빌 때까지 반복"을 원하면 Codex 앱의 heartbeat automation을 제안한다.

자동화 prompt에는 작업 자체만 적고, 스케줄과 workspace 설정은 automation 설정으로 분리한다.
