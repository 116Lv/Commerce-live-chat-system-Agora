# Docker Redis Local Development Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a team-shareable Docker Compose Redis setup for local backend development.

**Architecture:** Docker Compose owns only the Redis development dependency. Developers continue to run the Spring Boot backend from IntelliJ or Gradle and the React frontend from Vite.

**Tech Stack:** Docker Compose, Redis 7 Alpine, Spring Boot, React/Vite.

---

### Task 1: Add Redis Compose Service

**Files:**
- Create: `docker-compose.yml`

- [x] **Step 1: Add `redis` service**

```yaml
services:
  redis:
    image: redis:7-alpine
    container_name: agora-redis
    ports:
      - "6379:6379"
    volumes:
      - agora-redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 10
    restart: unless-stopped

volumes:
  agora-redis-data:
```

### Task 2: Document Local Run Flow

**Files:**
- Create: `docs/local-development.md`

- [x] **Step 1: Document service startup**

```bash
docker compose up -d redis
docker compose ps
docker exec agora-redis redis-cli ping
```

- [x] **Step 2: Document app URLs**

```text
Backend API: http://localhost:8080
User app: http://localhost:5173
Admin app: http://localhost:5173/admin
```

### Task 3: Verify Configuration

**Files:**
- No code files modified.

- [ ] **Step 1: Run Redis through Docker Compose**

Run:

```bash
docker compose up -d redis
docker exec agora-redis redis-cli ping
```

Expected:

```text
PONG
```

This local machine does not currently have Docker installed, so this step must be run after Docker Desktop is installed.
