import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';

const readSource = (path) => readFileSync(new URL(path, import.meta.url), 'utf8');

test('admin dashboard exposes operational metric cards', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');
  const utils = readSource('./adminPageUtils.js');

  assert.match(utils, /totalUserCount/);
  assert.match(utils, /todayNewUserCount/);
  assert.match(utils, /todayReportCount/);
  assert.match(utils, /todayTradeCount/);
  assert.match(utils, /todayProductRequestCount/);
  assert.match(utils, /registeredProductCount/);
  assert.match(utils, /총 가입자/);
  assert.match(utils, /오늘 신규 가입자/);
  assert.match(utils, /오늘 신고 수/);
  assert.match(utils, /오늘 거래량/);
  assert.match(utils, /오늘 등록요청/);
  assert.match(utils, /등록된 상품 수/);
  assert.match(pages, /md=\{4\} xl=\{2\}/);
});

test('admin dashboard renders pending report list with detail navigation', () => {
  const pages = readSource('./AdminPlaceholderPages.jsx');

  assert.match(pages, /pendingReports/);
  assert.match(pages, /미처리 신고 리스트/);
  assert.match(pages, /신고 ID/);
  assert.match(pages, /신고 대상/);
  assert.match(pages, /신고 사유/);
  assert.match(pages, /신고 일시/);
  assert.match(pages, /상태/);
  assert.match(pages, /상세/);
  assert.match(pages, /\/admin\/reports\?reportId=\$\{report\.reportId\}/);
  assert.match(pages, /보기/);
});
