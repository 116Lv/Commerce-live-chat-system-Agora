import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { test } from 'node:test';

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, '../../..');

test('frontend nginx proxies backend APIs, websocket, and uploaded files internally', () => {
  const nginx = readFileSync(resolve(root, 'frontend/nginx.conf'), 'utf8');

  assert.match(nginx, /location \/api\//);
  assert.match(nginx, /location \/ws\//);
  assert.match(nginx, /location \/uploads\//);
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
