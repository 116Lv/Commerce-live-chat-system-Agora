import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';

const source = readFileSync(new URL('./AuthContext.jsx', import.meta.url), 'utf8');

test('user login persists the full access and refresh token pair', () => {
  assert.match(source, /const tokens = getUserTokenPair\(response\);/);
  assert.match(source, /persistUserTokens\(tokens\);/);
  assert.doesNotMatch(source, /persistUserToken\(token\);/);
});

test('user logout clears both user tokens and cached profile state', () => {
  assert.match(source, /clearUserTokens\(\);/);
  assert.match(source, /setUserProfile\(null\);/);
});
