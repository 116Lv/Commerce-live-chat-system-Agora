# Admin User Strong Separation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split admin accounts from user accounts at the entity, repository, principal, JWT, service, seed-data, and test levels.

**Architecture:** Users remain in `users` and use `UserRole.ROLE_USER` with `CustomUserDetails`. Admins move to a new `admins` table and use `AdminRole`, `AdminStatus`, and `AdminPrincipal`. JWT access tokens carry `accountType` so the filter loads the correct account source and rejects token/request-path mismatches.

**Tech Stack:** Spring Boot, Spring Security, Spring Data JPA, H2 local schema generation, Gradle tests.

---

### Task 1: Admin Account Model

- [x] Add `Admin`, `AdminRole`, `AdminStatus`, and `AdminRepository`.
- [x] Remove admin enum values from `UserRole`.
- [x] Move admin seed data to `admins`.

### Task 2: Principal And JWT Split

- [x] Add `AccountType` and `AdminPrincipal`.
- [x] Add `accountType` to `JwtClaims` and `JwtProvider`.
- [x] Route JWT loading to user or admin details services by token type.
- [x] Reject admin tokens on user API paths and user tokens on admin API paths.

### Task 3: Admin Services And Controllers

- [x] Update admin controllers to accept `AdminPrincipal`.
- [x] Update admin services to use `AdminRole` and admin ids.
- [x] Change admin account role updates to target `admins`, not `users`.

### Task 4: Data And Test Migration

- [x] Move admin seed rows from `users` to `admins`.
- [x] Update admin tests to construct `AdminPrincipal`.
- [x] Run `.\gradlew.bat test` and fix regressions.
