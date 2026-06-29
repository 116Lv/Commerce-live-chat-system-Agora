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

test('product detail protects like action and does not expose edit without owner data', () => {
  const source = readPage('ProductDetailPage.jsx');

  assert.match(source, /import \{ useAuth \} from '\.\.\/auth\/AuthContext\.jsx';/);
  assert.match(source, /const \{ isUserAuthenticated \} = useAuth\(\);/);
  assert.match(source, /if \(!isUserAuthenticated\) \{/);
  assert.match(source, /navigate\('\/login'/);
  assert.doesNotMatch(source, /\/edit/);
});

test('coupon event issue action redirects unauthenticated users to login', () => {
  const source = readPage('CouponEventsPage.jsx');

  assert.match(source, /import \{ useAuth \} from '\.\.\/auth\/AuthContext\.jsx';/);
  assert.match(source, /const \{ isUserAuthenticated \} = useAuth\(\);/);
  assert.match(source, /if \(!isUserAuthenticated\) \{/);
  assert.match(source, /navigate\('\/login'/);
});
