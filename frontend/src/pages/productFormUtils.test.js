import assert from 'node:assert/strict';
import { describe, test } from 'node:test';

import {
  PRODUCT_CATEGORIES,
  formatPriceInput,
  getProductCategoryLabel,
  getProductImageUrl,
  getProductImageUrls,
  getProductRegionLabel,
  getProductStatusLabel,
  getProductTitle,
  normalizeRegionLabel,
  parsePriceInput,
  parseRegionFilterValue,
  validateProductForm
} from './productFormUtils.js';

describe('product form utilities', () => {
  test('exports stable Korean product categories', () => {
    assert.deepEqual(PRODUCT_CATEGORIES, [
      { value: '디지털기기', label: '디지털기기' },
      { value: '생활가전', label: '생활가전' },
      { value: '가구/인테리어', label: '가구/인테리어' },
      { value: '의류', label: '의류' },
      { value: '도서', label: '도서' },
      { value: '스포츠/레저', label: '스포츠/레저' },
      { value: '취미/게임/음반', label: '취미/게임/음반' },
      { value: '뷰티/미용', label: '뷰티/미용' },
      { value: '반려동물용품', label: '반려동물용품' },
      { value: '식품', label: '식품' },
      { value: '기타', label: '기타' }
    ]);
  });

  test('uses product response display labels and media fallbacks', () => {
    const product = {
      category: 'RAW_CATEGORY',
      categoryLabel: 'Friendly category',
      status: 'SELLING',
      statusLabel: 'Friendly status',
      regionFullName: 'Parent region Child region',
      thumbnailImageUrl: '/thumb.jpg',
      primaryImageUrl: '/primary.jpg'
    };

    assert.equal(getProductCategoryLabel(product), 'Friendly category');
    assert.equal(getProductStatusLabel(product), 'Friendly status');
    assert.equal(getProductRegionLabel(product), 'Parent region Child region');
    assert.equal(getProductImageUrl(product), '/primary.jpg');
  });

  test('falls back to backend thumbnailUrl before legacy image fields', () => {
    assert.equal(getProductImageUrl({ thumbnailUrl: '/backend-thumb.jpg', imageUrl: '/legacy.jpg' }), '/backend-thumb.jpg');
  });

  test('normalizes uploaded image paths so nested routes can render them', () => {
    assert.equal(getProductImageUrl({ primaryImageUrl: 'uploads/products/main.svg' }), '/uploads/products/main.svg');
  });

  test('normalizes detail image arrays and falls back to the primary image', () => {
    assert.deepEqual(getProductImageUrls({ imageUrls: ['uploads/products/main.svg', '/uploads/products/detail.svg'] }), [
      '/uploads/products/main.svg',
      '/uploads/products/detail.svg'
    ]);
    assert.deepEqual(getProductImageUrls({ primaryImageUrl: 'uploads/products/main.svg' }), ['/uploads/products/main.svg']);
  });

  test('normalizes object-shaped detail image arrays from upload responses', () => {
    assert.deepEqual(
      getProductImageUrls({
        imageUrls: [
          { imageUrl: 'uploads/products/main.jpg', sortOrder: 0 },
          { url: '/uploads/products/detail.jpg', sortOrder: 1 }
        ]
      }),
      ['/uploads/products/main.jpg', '/uploads/products/detail.jpg']
    );
  });

  test('returns no product images while detail data is still null', () => {
    assert.deepEqual(getProductImageUrls(null), []);
  });

  test('normalizes ProductSearchResponse title and label aliases', () => {
    const product = {
      productTitle: 'Search title',
      categoryName: 'Search category',
      statusName: 'Search status',
      regionFullName: 'Seoul Mapo',
      thumbnailUrl: '/search-thumb.jpg'
    };

    assert.equal(getProductTitle(product), 'Search title');
    assert.equal(getProductCategoryLabel(product), 'Search category');
    assert.equal(getProductStatusLabel(product), 'Search status');
    assert.equal(getProductRegionLabel(product), 'Seoul Mapo');
    assert.equal(getProductImageUrl(product), '/search-thumb.jpg');
  });

  test('falls back to raw product fields when response labels are absent', () => {
    const product = {
      category: 'DIGITAL',
      status: 'AVAILABLE',
      sido: 'Seoul',
      sigungu: 'Gangnam',
      eupmyeondong: 'Yeoksam',
      imageUrl: '/legacy.jpg'
    };

    assert.equal(getProductCategoryLabel(product), 'DIGITAL');
    assert.equal(getProductStatusLabel(product), '판매중');
    assert.equal(getProductRegionLabel(product), 'Seoul Gangnam Yeoksam');
    assert.equal(getProductImageUrl(product), '/legacy.jpg');
  });

  test('uses object-shaped product region labels without stringifying the object', () => {
    assert.equal(getProductRegionLabel({ region: { sido: 'Seoul', sigungu: 'Mapo' } }), 'Seoul Mapo');
  });

  test('formats price input by stripping non-digits and adding commas', () => {
    assert.equal(formatPriceInput('10000'), '10,000');
    assert.equal(formatPriceInput('12,345원'), '12,345');
    assert.equal(formatPriceInput(9876543), '9,876,543');
    assert.equal(formatPriceInput(''), '');
    assert.equal(formatPriceInput('abc'), '');
  });

  test('parses price input into a number after stripping commas and non-digits', () => {
    assert.equal(parsePriceInput('10,000'), 10000);
    assert.equal(parsePriceInput('12,345원'), 12345);
    assert.equal(parsePriceInput(9876543), 9876543);
    assert.equal(parsePriceInput(''), 0);
    assert.equal(parsePriceInput('abc'), 0);
  });

  test('validates required product fields, minimum price, known category, and description length', () => {
    const errors = validateProductForm({
      title: ' ',
      price: '99',
      category: '없는 카테고리',
      regionId: '',
      description: 'a'.repeat(2001)
    });

    assert.deepEqual(errors, {
      title: '제목을 입력해 주세요.',
      price: '가격은 100원 이상 입력해 주세요.',
      category: '카테고리를 선택해 주세요.',
      regionId: '지역을 선택해 주세요.',
      description: '설명은 2000자 이하로 입력해 주세요.'
    });
  });

  test('rejects malformed signed and decimal price input during validation', () => {
    const validForm = {
      title: '의자',
      category: '가구/인테리어',
      regionId: 3,
      description: '상태가 좋아요.'
    };

    assert.equal(validateProductForm({ ...validForm, price: '-100' }).price, '가격은 100원 이상 입력해 주세요.');
    assert.equal(validateProductForm({ ...validForm, price: '99.99' }).price, '가격은 100원 이상 입력해 주세요.');
  });

  test('rejects malformed comma placement during price validation', () => {
    const validForm = {
      title: '의자',
      category: '가구/인테리어',
      regionId: 3,
      description: '상태가 좋아요.'
    };

    assert.equal(validateProductForm({ ...validForm, price: ',,,100' }).price, '가격은 100원 이상 입력해 주세요.');
    assert.equal(validateProductForm({ ...validForm, price: '1,,00' }).price, '가격은 100원 이상 입력해 주세요.');
    assert.equal(validateProductForm({ ...validForm, price: '100,' }).price, '가격은 100원 이상 입력해 주세요.');
  });

  test('accepts plain and properly comma-formatted price validation input', () => {
    const validForm = {
      title: '의자',
      category: '가구/인테리어',
      regionId: 3,
      description: '상태가 좋아요.'
    };

    assert.equal(validateProductForm({ ...validForm, price: '100' }).price, undefined);
    assert.equal(validateProductForm({ ...validForm, price: '1,000' }).price, undefined);
    assert.equal(validateProductForm({ ...validForm, price: '10,000' }).price, undefined);
    assert.equal(validateProductForm({ ...validForm, price: '1,234,567' }).price, undefined);
  });

  test('returns no validation errors for a complete valid product form', () => {
    assert.deepEqual(
      validateProductForm({
        title: '의자',
        price: '10,000',
        category: '가구/인테리어',
        regionId: 3,
        description: '상태가 좋아요.'
      }),
      {}
    );
  });

  test('normalizes region labels from common region shapes', () => {
    assert.equal(normalizeRegionLabel({ name: '서울 강남구 역삼동' }), '서울 강남구 역삼동');
    assert.equal(normalizeRegionLabel({ sido: '서울특별시', sigungu: '강남구', eupmyeondong: '역삼동' }), '서울특별시 강남구 역삼동');
    assert.equal(normalizeRegionLabel({ city: '서울특별시', district: '강남구', neighborhood: '역삼동' }), '서울특별시 강남구 역삼동');
    assert.equal(normalizeRegionLabel({ province: '경기도', city: '성남시', district: '분당구' }), '경기도 성남시 분당구');
    assert.equal(normalizeRegionLabel({ regionId: 7 }), '7');
    assert.equal(normalizeRegionLabel(null), '');
  });

  test('parses region filter values into regionId, sido, or sido+sigungu query params', () => {
    assert.deepEqual(parseRegionFilterValue(''), {});
    assert.deepEqual(parseRegionFilterValue(null), {});
    assert.deepEqual(parseRegionFilterValue('357'), { regionId: '357' });
    assert.deepEqual(parseRegionFilterValue('sido:서울특별시'), { sido: '서울특별시' });
    assert.deepEqual(parseRegionFilterValue('sigungu:서울특별시:강남구'), { sido: '서울특별시', sigungu: '강남구' });
  });

});
