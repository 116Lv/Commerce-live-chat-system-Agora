import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';

const readPage = (name) => readFileSync(new URL(`./${name}`, import.meta.url), 'utf8');

test('sell product page renders an empty state when regions are unavailable', () => {
  const source = readPage('SellProductPage.jsx');

  assert.match(source, /import EmptyState from '\.\.\/components\/EmptyState\.jsx';/);
  assert.match(source, /!regionsState\.loading && !regionsState\.error && regions\.length === 0/);
  assert.match(source, /regions\.length > 0/);
});

test('my page renders an empty state when the profile payload is missing', () => {
  const source = readPage('MyPage.jsx');

  assert.match(source, /import EmptyState from '\.\.\/components\/EmptyState\.jsx';/);
  assert.match(source, /!profileState\.loading && !profileState\.error && !profileState\.data/);
});
