# Docker and CI/CD Guide

This guide covers the first deployment-ready step for Agora:

- CI verifies backend and frontend builds.
- Docker builds backend and frontend images.
- Docker Compose can run backend, frontend, MySQL, and Redis together.

## CI

`.github/workflows/ci.yml` runs on pull requests and pushes to `main` and `dev`.

It verifies:

```bash
./gradlew clean build
cd frontend
npm ci
npm test
npm run build
```

## Local Image Builds

From the project root:

```bash
docker build -t agora-backend:local .
docker build -t agora-frontend:local ./frontend
```

For the frontend image, inject the browser-visible backend URL at build time:

```bash
docker build ^
  --build-arg VITE_API_BASE_URL=http://localhost:8080 ^
  -t agora-frontend:local ^
  ./frontend
```

## Compose Runtime

Create a local `.env` file next to `docker-compose.prod.yml`.

```dotenv
MYSQL_DATABASE=agora
MYSQL_USER=agora
MYSQL_PASSWORD=change-me
MYSQL_ROOT_PASSWORD=change-root-me

JWT_SECRET_KEY=change-this-to-a-long-random-secret
CORS_ALLOWED_ORIGINS=http://localhost,http://localhost:5173

PORTONE_API_SECRET=local-portone-secret
PORTONE_WEBHOOK_SECRET=local-webhook-secret

PUBLIC_API_BASE_URL=http://localhost:8080
BACKEND_PORT=8080
FRONTEND_PORT=80
MYSQL_PORT=3306
```

Then run:

```bash
docker compose -f docker-compose.prod.yml up --build -d
docker compose -f docker-compose.prod.yml ps
```

Open:

```text
Frontend: http://localhost
Backend:  http://localhost:8080
```

## AWS Path

For an EC2-based first deployment:

1. Install Docker and Docker Compose on EC2.
2. Copy or create the production `.env` on the server.
3. Set `PUBLIC_API_BASE_URL` to the public backend URL.
4. Run `docker compose -f docker-compose.prod.yml up --build -d`.
5. Add GitHub Actions deployment after image registry and SSH secrets are decided.

For a later production-grade setup, move MySQL to RDS, Redis to ElastiCache, images to ECR, and frontend static assets to S3 + CloudFront.

## Region Seed Data (One-Time, Manual)

The `migrate` service only mounts `src/main/resources/db/migration` (versioned Flyway schema changes). It does **not** run `src/main/resources/db/seed/regions.sql`, which loads the ~5,066 nationwide 읍/면/동 rows into the `regions` table. This is intentional — it is reference/master data, not a schema change — but it means it has to be loaded manually once per database (RDS included).

Run it from anywhere that can reach the database (typically an SSH session on the EC2 host, since RDS security groups usually only allow the app's VPC):

```bash
docker run --rm -i mysql:8 mysql --default-character-set=utf8mb4 \
  -h <rds-endpoint> -u <user> -p<password> <database> \
  < src/main/resources/db/seed/regions.sql
```

Notes:

- `--default-character-set=utf8mb4` is required — without it the Korean region names are stored corrupted (mojibake), even though the file itself is valid UTF-8.
- The script is safe to re-run (`ON DUPLICATE KEY UPDATE` on the unique `code` column), so it does not need to be wired into the automated deploy pipeline. Run it once after the first deploy that includes the region feature; skip it on later deploys.
- No app restart is needed after loading — the region endpoints (`/api/regions/**`) query the table directly on every request.
