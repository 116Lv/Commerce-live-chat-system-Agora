import assert from 'node:assert/strict';
import { describe, test } from 'node:test';

import { submitSellProduct } from './sellProductSubmit.js';

const validForm = {
  title: '음식',
  description: '오늘 중으로 거래 희망합니다',
  price: '30,000',
  category: '식품',
  regionId: '10',
  images: [{ name: 'food.jpg' }]
};

describe('sell product submit flow', () => {
  test('treats product creation as success even when image upload fails', async () => {
    const createdProduct = { productId: 10 };
    const calls = [];

    const result = await submitSellProduct(validForm, {
      createProduct: async (payload) => {
        calls.push(['create', payload]);
        return createdProduct;
      },
      uploadProductImages: async (productId, images) => {
        const [image] = images;
        calls.push(['upload', productId, image]);
        throw new Error('Network Error');
      }
    });

    assert.deepEqual(result, {
      product: createdProduct,
      imageUploadError: 'Network Error'
    });
    assert.deepEqual(calls[0], [
      'create',
      {
        title: '음식',
        description: '오늘 중으로 거래 희망합니다',
        price: 30000,
        category: '식품',
        regionId: 10
      }
    ]);
    assert.equal(calls[1][0], 'upload');
    assert.equal(calls[1][1], 10);
  });

  test('uploads all selected images through the multi-image API', async () => {
    const createdProduct = { productId: 11 };
    const first = { name: 'first.jpg' };
    const second = { name: 'second.jpg' };
    const calls = [];

    const result = await submitSellProduct(
      { ...validForm, images: [first, second] },
      {
        createProduct: async () => createdProduct,
        uploadProductImages: async (productId, images) => {
          calls.push(['uploadAll', productId, images]);
          return images.map((image, index) => ({ imageId: index + 1, imageUrl: image.name, sortOrder: index }));
        }
      }
    );

    assert.equal(result.imageUploadError, '');
    assert.deepEqual(calls, [['uploadAll', 11, [first, second]]]);
  });
});
