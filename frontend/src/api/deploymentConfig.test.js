import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { test } from 'node:test';

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, '../../..');

test('frontend nginx proxies backend APIs, websocket, and uploaded files internally', () => {
  const nginx = readFileSync(resolve(root, 'frontend/nginx.conf'), 'utf8');

  assert.match(nginx, /client_max_body_size 100m;/);
  assert.match(nginx, /location \/api\//);
  assert.match(nginx, /location \/ws\//);
  assert.match(nginx, /location \/uploads\//);
  assert.match(nginx, /location = \/health/);
  assert.match(nginx, /proxy_pass http:\/\/backend:8080/);
});

test('production compose keeps backend off host ports and builds frontend for same-origin API calls', () => {
  const compose = readFileSync(resolve(root, 'docker-compose.prod.yml'), 'utf8');
  const dockerfile = readFileSync(resolve(root, 'frontend/Dockerfile'), 'utf8');

  assert.doesNotMatch(compose, /"\$\{BACKEND_PORT:-8080}:8080"/);
  assert.match(compose, /expose:\s*\n\s*-\s*"8080"/);
  assert.match(compose, /VITE_API_BASE_URL: \$\{PUBLIC_API_BASE_URL:-\/\}/);
  assert.match(dockerfile, /ARG VITE_API_BASE_URL=\//);
});

test('production compose gates frontend startup on backend health', () => {
  const compose = readFileSync(resolve(root, 'docker-compose.prod.yml'), 'utf8');
  const backendDockerfile = readFileSync(resolve(root, 'Dockerfile'), 'utf8');

  assert.match(backendDockerfile, /apk add --no-cache curl/);
  assert.match(compose, /backend:[\s\S]*healthcheck:[\s\S]*curl --fail --silent http:\/\/localhost:8080\/health/);
  assert.match(compose, /frontend:[\s\S]*depends_on:[\s\S]*backend:[\s\S]*condition: service_healthy/);
});

test('docker deployment uses the real payment client and persists uploaded files', () => {
  const compose = readFileSync(resolve(root, 'docker-compose.prod.yml'), 'utf8');
  const backendDockerfile = readFileSync(resolve(root, 'Dockerfile'), 'utf8');
  const localPaymentClient = readFileSync(
    resolve(root, 'src/main/java/com/team7/agora/domain/payment/client/LocalPaymentClient.java'),
    'utf8'
  );
  const portOnePaymentClient = readFileSync(
    resolve(root, 'src/main/java/com/team7/agora/domain/payment/client/PortOnePaymentClient.java'),
    'utf8'
  );

  assert.match(localPaymentClient, /@Profile\("local"\)/);
  assert.doesNotMatch(localPaymentClient, /docker/);
  assert.match(portOnePaymentClient, /@Profile\(\{"prod", "docker"\}\)/);

  assert.match(backendDockerfile, /mkdir -p \/app\/uploads/);
  assert.match(backendDockerfile, /chown -R agora:agora \/app/);
  assert.match(compose, /FILE_UPLOAD_DIR: \/app\/uploads/);
  assert.match(compose, /- agora-uploads:\/app\/uploads/);
  assert.match(compose, /volumes:\s*\n\s+agora-uploads:/);
});

test('deployment waits for the proxied backend API before reporting success', () => {
  const workflow = readFileSync(resolve(root, '.github/workflows/deploy.yml'), 'utf8');

  assert.match(workflow, /for attempt in \{1\.\.30\}/);
  assert.match(workflow, /http:\/\/localhost\/health/);
  assert.doesNotMatch(workflow, /http:\/\/localhost\/api\/products/);
  assert.match(workflow, /docker compose -f "\$COMPOSE_FILE" --env-file \.env logs --tail=200 backend frontend/);
});
