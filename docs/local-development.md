# Local Development

## Required Services

Install Docker Desktop first if `docker` is not available on your machine.

Start Redis with Docker Compose before running the Spring Boot application:

```bash
docker compose up -d redis
```

Check that Redis is healthy:

```bash
docker compose ps
docker exec agora-redis redis-cli ping
```

The `PING` command should return:

```text
PONG
```

Stop Redis when you are done:

```bash
docker compose down
```

## Backend

Run the backend from IntelliJ or from the project root:

```bash
./gradlew bootRun
```

On Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

The backend uses these local Redis defaults:

```text
REDIS_HOST=localhost
REDIS_PORT=6379
```

Backend API base URL:

```text
http://localhost:8080
```

## Frontend

Run the React frontend from the `frontend` directory:

```bash
npm install
npm run dev
```

Use the Vite URL printed in the terminal. The default is:

```text
http://localhost:5173
```

Useful entry points:

```text
User app: http://localhost:5173
Admin app: http://localhost:5173/admin
```

If port `5173` is already in use, Vite will print another port. Use the printed URL.

## Troubleshooting

If Redis fails to start because port `6379` is already in use, check for another local Redis process:

```powershell
Get-NetTCPConnection -LocalPort 6379
```

Stop the existing Redis process or service, then run Docker Compose again:

```bash
docker compose up -d redis
```
