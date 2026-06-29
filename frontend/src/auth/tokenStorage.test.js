import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';
import {
  clearAdminToken,
  clearUserToken,
  getAdminToken,
  getUserToken,
  setAdminToken,
  setUserToken
} from './tokenStorage.js';

const createStorage = () => {
  const store = new Map();

  return {
    getItem: (key) => (store.has(key) ? store.get(key) : null),
    setItem: (key, value) => store.set(key, String(value)),
    removeItem: (key) => store.delete(key)
  };
};

beforeEach(() => {
  globalThis.localStorage = createStorage();
});

test('stores user and admin tokens under separate keys', () => {
  setUserToken('user-token');
  setAdminToken('admin-token');

  assert.equal(getUserToken(), 'user-token');
  assert.equal(getAdminToken(), 'admin-token');
  assert.equal(globalThis.localStorage.getItem('agora.user.accessToken'), 'user-token');
  assert.equal(globalThis.localStorage.getItem('agora.admin.accessToken'), 'admin-token');
});

test('clears one token type without clearing the other', () => {
  setUserToken('user-token');
  setAdminToken('admin-token');

  clearUserToken();

  assert.equal(getUserToken(), null);
  assert.equal(getAdminToken(), 'admin-token');

  clearAdminToken();

  assert.equal(getAdminToken(), null);
});
