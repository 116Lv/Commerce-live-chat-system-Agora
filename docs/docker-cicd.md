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
