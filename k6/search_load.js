// 검색 API v1(DB)/v2(Redis 캐시) 부하 테스트 — vUser 점진 증가로 TPS·포화점 측정
import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE || 'http://localhost:8080';
const TARGET = __ENV.TARGET || 'v1';
const PATH = TARGET === 'v2' ? '/api/v2/products/search' : '/api/v1/products/search';
const REGION = __ENV.REGION || '1';

export const options = {
  // vUser 점진 증가(Ramp-up) → 50 → 100 → 200 → 300, 마지막에 0으로 감소
  stages: [
    { duration: '15s', target: 50 },
    { duration: '15s', target: 100 },
    { duration: '15s', target: 200 },
    { duration: '15s', target: 300 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.05'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'max'],
};

export default function () {
  const res = http.get(`${BASE}${PATH}?regionId=${REGION}&page=0&size=20`);
  check(res, { 'status is 200': (r) => r.status === 200 });
}
