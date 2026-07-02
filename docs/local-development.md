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

## Full Nationwide Region Data (Optional)

The default local run (`./gradlew bootRun`, `local` profile) uses in-memory H2 seeded from `data.sql`, which only has 5 sample regions (enough to exercise the sido/sigungu/dong cascade, but not real nationwide coverage). To test against the full ~5,066-row dataset locally, run against a real MySQL instead of H2:

1. Start a local MySQL container:

   ```bash
   docker run -d --name agora-mysql-local -e MYSQL_ROOT_PASSWORD=localpass -e MYSQL_DATABASE=agora_db \
     -p 3307:3306 mysql:8 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
   ```

2. Apply the Flyway migrations (`V1` through the latest) in numeric order, and load the seed:

   ```bash
   for f in $(ls src/main/resources/db/migration/V*.sql | sort -V); do
     mysql --default-character-set=utf8mb4 -h 127.0.0.1 -P 3307 -uroot -plocalpass agora_db < "$f"
   done
   mysql --default-character-set=utf8mb4 -h 127.0.0.1 -P 3307 -uroot -plocalpass agora_db \
     < src/main/resources/db/seed/regions.sql
   ```

3. Run the backend with the `docker` profile pointed at this MySQL (Redis still needs to be running per the section above):

   ```powershell
   $env:SPRING_PROFILES_ACTIVE = "docker"
   $env:SPRING_DATASOURCE_URL = "jdbc:mysql://127.0.0.1:3307/agora_db?useUnicode=true&characterEncoding=UTF-8"
   $env:SPRING_DATASOURCE_USERNAME = "root"
   $env:SPRING_DATASOURCE_PASSWORD = "localpass"
   $env:REDIS_HOST = "localhost"
   $env:REDIS_PORT = "6379"
   $env:JWT_SECRET_KEY = "localDummySecretKeyForAgoraProject12345!"
   $env:CORS_ALLOWED_ORIGINS = "http://127.0.0.1:5173"
   $env:PORTONE_API_SECRET = "dummy-secret"
   $env:PORTONE_WEBHOOK_SECRET = "dummy-webhook-secret"
   .\gradlew.bat bootRun
   ```

This works even with the EC2/RDS server down, since it is a self-contained local MySQL container.

## Troubleshooting

If Redis fails to start because port `6379` is already in use, check for another local Redis process:

```powershell
Get-NetTCPConnection -LocalPort 6379
```

Stop the existing Redis process or service, then run Docker Compose again:

```bash
docker compose up -d redis
```
