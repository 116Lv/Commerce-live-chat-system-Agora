import assert from 'node:assert/strict';
import { test } from 'node:test';
import {
  createProduct,
  deleteProduct,
  getMyLikedProducts,
  getMyProducts,
  getProduct,
  getProducts,
  likeProduct,
  searchProducts,
  unlikeProduct,
  updateProduct,
  updateProductStatus,
  uploadProductImage,
  uploadProductImages
} from './productApi.js';
import { getCouponEvents, getMyCoupons, issueCoupon } from './couponApi.js';
import { getRegions, updatePreferredRegions } from './regionApi.js';
import { getMe, getMyReviews, getMyTrades, updateProfile } from './mypageApi.js';

const captureRequest = () => {
  const requests = [];
  const adapter = (config) => {
    requests.push(config);
    return Promise.resolve({
      config,
      data: { status: 'SUCCESS', message: 'ok', data: { ok: true } },
      headers: {},
      status: 200,
      statusText: 'OK'
    });
  };

  return { requests, config: { adapter } };
};

const parseJsonBody = (data) => (typeof data === 'string' ? JSON.parse(data) : data);

const summarizeRequest = ({ method, url, params, data }) => ({ method, url, params, data: parseJsonBody(data) });

test('product API maps marketplace endpoints', async () => {
  const { requests, config } = captureRequest();
  const product = { title: 'Bike', description: 'Good condition', price: 30000, category: 'Sports', regionId: 1 };

  await getProducts({ regionId: 1, page: 2, size: 12 }, config);
  await getProduct(7, config);
  await createProduct(product, config);
  await updateProduct(7, { title: 'Bike', description: 'Good condition', price: 30000, category: 'Sports' }, config);
  await updateProductStatus(7, 'RESERVED', config);
  await deleteProduct(7, config);
  await likeProduct(7, config);
  await unlikeProduct(7, config);
  await getMyProducts(config);
  await getMyLikedProducts(config);
  await searchProducts({ keyword: 'bike', category: 'Sports', page: 0, size: 5 }, config);

  assert.deepEqual(
    requests.map(summarizeRequest),
    [
      { method: 'get', url: '/api/products', params: { regionId: 1, page: 2, size: 12 }, data: undefined },
      { method: 'get', url: '/api/products/7', params: undefined, data: undefined },
      { method: 'post', url: '/api/products', params: undefined, data: product },
      {
        method: 'patch',
        url: '/api/products/7',
        params: undefined,
        data: { title: 'Bike', description: 'Good condition', price: 30000, category: 'Sports' }
      },
      { method: 'patch', url: '/api/products/7/status', params: undefined, data: { status: 'RESERVED' } },
      { method: 'delete', url: '/api/products/7', params: undefined, data: undefined },
      { method: 'post', url: '/api/products/7/likes', params: undefined, data: null },
      { method: 'delete', url: '/api/products/7/likes', params: undefined, data: undefined },
      { method: 'get', url: '/api/users/me/products', params: undefined, data: undefined },
      { method: 'get', url: '/api/users/me/likes', params: undefined, data: undefined },
      {
        method: 'get',
        url: '/api/v2/products/search',
        params: { keyword: 'bike', category: 'Sports', page: 0, size: 5 },
        data: undefined
      }
    ]
  );
});

test('product image upload helper sends one multipart images field', async () => {
  const { requests, config } = captureRequest();
  const image = new Blob(['image-bytes'], { type: 'image/png' });

  await uploadProductImage(7, image, config);

  assert.equal(requests[0].method, 'post');
  assert.equal(requests[0].url, '/api/products/7/images');
  assert.notEqual(requests[0].headers?.['Content-Type'], 'multipart/form-data');
  assert.equal(requests[0].data.get('images').size, image.size);
  assert.equal(requests[0].data.get('images').type, image.type);
});

test('product images upload sends multiple multipart images fields', async () => {
  const { requests, config } = captureRequest();
  const first = new Blob(['first-image'], { type: 'image/png' });
  const second = new Blob(['second-image'], { type: 'image/jpeg' });

  await uploadProductImages(7, [first, second], config);

  assert.equal(requests[0].method, 'post');
  assert.equal(requests[0].url, '/api/products/7/images');
  assert.notEqual(requests[0].headers?.['Content-Type'], 'multipart/form-data');
  assert.deepEqual(
    requests[0].data.getAll('images').map((image) => image.type),
    ['image/png', 'image/jpeg']
  );
});

test('coupon and region APIs map user endpoints', async () => {
  const { requests, config } = captureRequest();

  await getCouponEvents(config);
  await issueCoupon(3, config);
  await getMyCoupons(config);
  await getRegions({ keyword: 'Seoul' }, config);
  await updatePreferredRegions({ regionIds: [1, 2], primaryRegionId: 1 }, config);

  assert.deepEqual(
    requests.map(summarizeRequest),
    [
      { method: 'get', url: '/api/coupon-events', params: undefined, data: undefined },
      { method: 'post', url: '/api/coupon-events/3/issue', params: undefined, data: null },
      { method: 'get', url: '/api/users/me/coupons', params: undefined, data: undefined },
      { method: 'get', url: '/api/regions', params: { keyword: 'Seoul' }, data: undefined },
      {
        method: 'put',
        url: '/api/users/me/regions',
        params: undefined,
        data: { regionIds: [1, 2], primaryRegionId: 1 }
      }
    ]
  );
});

test('mypage APIs map profile, trades, and reviews endpoints', async () => {
  const { requests, config } = captureRequest();

  await getMe(config);
  await updateProfile({ nickname: 'Agora' }, config);
  await getMyTrades({ role: 'buyer', page: 1, size: 10 }, config);
  await getMyReviews({ type: 'received', page: 0, size: 5 }, config);

  assert.deepEqual(
    requests.map(summarizeRequest),
    [
      { method: 'get', url: '/api/users/me', params: undefined, data: undefined },
      { method: 'patch', url: '/api/users/me/profile', params: undefined, data: { nickname: 'Agora' } },
      { method: 'get', url: '/api/users/me/trades', params: { role: 'buyer', page: 1, size: 10 }, data: undefined },
      { method: 'get', url: '/api/users/me/reviews', params: { type: 'received', page: 0, size: 5 }, data: undefined }
    ]
  );
});
