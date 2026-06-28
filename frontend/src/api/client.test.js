import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';
import { apiClient, getApiErrorMessage, selectAuthToken, unwrapApiResponse } from './client.js';
import { setAdminToken, setUserToken } from '../auth/tokenStorage.js';

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

test('selects admin token for admin API paths and user token otherwise', () => {
  setUserToken('user-token');
  setAdminToken('admin-token');

  assert.equal(selectAuthToken({ url: '/api/admin/products' }), 'admin-token');
  assert.equal(selectAuthToken({ url: '/api/products' }), 'user-token');
});

test('allows explicit token type when a request needs it', () => {
  setUserToken('user-token');
  setAdminToken('admin-token');

  assert.equal(selectAuthToken({ url: '/api/products', authType: 'admin' }), 'admin-token');
  assert.equal(selectAuthToken({ url: '/api/admin/products', authType: 'user' }), 'user-token');
});

test('unwraps ApiResponse data only when the response shape contains data', () => {
  assert.deepEqual(
    unwrapApiResponse({ data: { status: 'SUCCESS', message: 'ok', data: { id: 7 } } }),
    { id: 7 }
  );
  assert.deepEqual(unwrapApiResponse({ data: { items: [] } }), { items: [] });
});

test('extracts a readable backend error message', () => {
  assert.equal(
    getApiErrorMessage({ response: { data: { status: 'ERROR', message: 'Invalid login' } } }),
    'Invalid login'
  );
  assert.equal(getApiErrorMessage(new Error('Network down')), 'Network down');
});

test('configures the default backend base URL', () => {
  assert.equal(apiClient.defaults.baseURL, 'http://127.0.0.1:8080');
});
