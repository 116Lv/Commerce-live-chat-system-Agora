import assert from 'node:assert/strict';
import { beforeEach, test } from 'node:test';
import { AxiosHeaders } from 'axios';
import {
  apiClient,
  formatAuthorizationHeader,
  getApiErrorMessage,
  selectAuthToken,
  stripAuthorizationHeaders,
  unwrapApiResponse
} from './client.js';
import { loginAdmin, loginUser, logoutAdmin, logoutUser, signupUser } from './authApi.js';
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

test('does not select a token when auth is disabled for public endpoints', () => {
  setUserToken('expired-user-token');
  setAdminToken('expired-admin-token');

  assert.equal(selectAuthToken({ url: '/api/auth/login', authType: 'none' }), null);
  assert.equal(selectAuthToken({ url: '/api/admin/auth/login', authType: 'none' }), null);
});

test('formats Authorization safely for raw and already-prefixed tokens', () => {
  assert.equal(formatAuthorizationHeader('raw-token'), 'Bearer raw-token');
  assert.equal(formatAuthorizationHeader('Bearer prefixed-token'), 'Bearer prefixed-token');
  assert.equal(formatAuthorizationHeader('bearer lowercase-token'), 'bearer lowercase-token');
  assert.equal(formatAuthorizationHeader(''), null);
});

test('request interceptor sends one Bearer prefix for already-prefixed stored tokens', async () => {
  let requestConfig;
  setUserToken('Bearer existing-user-token');

  await apiClient.get('/api/users/me', {
    adapter: (config) => {
      requestConfig = config;
      return Promise.resolve({
        config,
        data: { status: 'SUCCESS', message: 'ok', data: null },
        headers: {},
        status: 200,
        statusText: 'OK'
      });
    }
  });

  assert.equal(requestConfig.headers.Authorization, 'Bearer existing-user-token');
});

test('request interceptor omits Authorization for explicitly public auth requests', async () => {
  let requestConfig;
  setUserToken('expired-user-token');

  await apiClient.post(
    '/api/auth/login',
    { email: 'user@example.com', password: 'password' },
    {
      authType: 'none',
      adapter: (config) => {
        requestConfig = config;
        return Promise.resolve({
          config,
          data: { status: 'SUCCESS', message: 'ok', data: { accessToken: 'new-token' } },
          headers: {},
          status: 200,
          statusText: 'OK'
        });
      }
    }
  );

  assert.equal(requestConfig.headers.Authorization, undefined);
  assert.equal(requestConfig.authType, undefined);
});

test('public login strips caller-provided authorization header in any casing', async () => {
  let requestConfig;

  await loginUser(
    { email: 'user@example.com', password: 'password' },
    {
      headers: { authorization: 'Bearer leaked' },
      adapter: (config) => {
        requestConfig = config;
        return Promise.resolve({
          config,
          data: { status: 'SUCCESS', message: 'ok', data: { accessToken: 'new-token' } },
          headers: {},
          status: 200,
          statusText: 'OK'
        });
      }
    }
  );

  assert.equal(requestConfig.headers.Authorization, undefined);
  assert.equal(requestConfig.headers.authorization, undefined);
});

test('stripAuthorizationHeaders removes authorization from AxiosHeaders', () => {
  const headers = new AxiosHeaders({ authorization: 'Bearer leaked' });

  stripAuthorizationHeaders(headers);

  assert.equal(headers.get('authorization'), undefined);
});

test('auth API login and signup requests are sent without stored tokens', async () => {
  const seenRequests = [];
  setUserToken('expired-user-token');
  setAdminToken('expired-admin-token');

  const adapter = (config) => {
    seenRequests.push(config);
    return Promise.resolve({
      config,
      data: { status: 'SUCCESS', message: 'ok', data: { accessToken: 'new-token' } },
      headers: {},
      status: 200,
      statusText: 'OK'
    });
  };

  await loginUser({ email: 'user@example.com', password: 'password' }, { adapter });
  await signupUser({ email: 'user@example.com', password: 'password', nickname: 'User' }, { adapter });
  await loginAdmin({ email: 'admin@example.com', password: 'password' }, { adapter });

  assert.deepEqual(
    seenRequests.map((request) => [request.url, request.headers.Authorization]),
    [
      ['/api/auth/login', undefined],
      ['/api/auth/signup', undefined],
      ['/api/admin/auth/login', undefined]
    ]
  );
});

test('auth API logout still sends the relevant stored token', async () => {
  const seenRequests = [];
  setUserToken('logout-token');
  setAdminToken('admin-logout-token');

  const adapter = (config) => {
    seenRequests.push(config);
    return Promise.resolve({
      config,
      data: { status: 'SUCCESS', message: 'ok', data: null },
      headers: {},
      status: 200,
      statusText: 'OK'
    });
  };

  await logoutUser({ adapter });
  await logoutAdmin({ adapter });

  assert.deepEqual(
    seenRequests.map((request) => [request.url, request.headers.Authorization]),
    [
      ['/api/auth/logout', 'Bearer logout-token'],
      ['/api/admin/auth/logout', 'Bearer admin-logout-token']
    ]
  );
});

test('admin logout accepts already-prefixed stored tokens without doubling Bearer', async () => {
  let requestConfig;
  setAdminToken('Bearer admin-prefixed-token');

  await logoutAdmin({
    adapter: (config) => {
      requestConfig = config;
      return Promise.resolve({
        config,
        data: { status: 'SUCCESS', message: 'ok', data: null },
        headers: {},
        status: 200,
        statusText: 'OK'
      });
    }
  });

  assert.equal(requestConfig.headers.Authorization, 'Bearer admin-prefixed-token');
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
