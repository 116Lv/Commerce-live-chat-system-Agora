export const PRODUCT_CATEGORIES = [
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
];

const CATEGORY_VALUES = new Set(PRODUCT_CATEGORIES.map((category) => category.value));

const STATUS_LABELS = {
  ACTIVE: '진행중',
  AVAILABLE: '판매중',
  BLOCKED: '차단',
  COMPLETED: '완료',
  DELETED: '삭제',
  ENDED: '종료',
  HIDDEN: '숨김',
  PENDING: '대기',
  RESERVED: '예약중',
  SELLING: '판매중',
  SOLD: '판매완료'
};

const digitsOnly = (value) => String(value ?? '').replace(/\D/g, '');
const isValidPriceInput = (value) => /^\d+$|^\d{1,3}(,\d{3})+$/.test(String(value ?? '').trim());

const presentParts = (parts) =>
  parts.filter((part) => part !== undefined && part !== null && String(part).trim() !== '').map((part) => String(part).trim());

const uniqueParts = (parts) => {
  const seen = new Set();
  return presentParts(parts).filter((part) => {
    if (seen.has(part)) {
      return false;
    }

    seen.add(part);
    return true;
  });
};

const getRegionValue = (region) => region?.regionId ?? region?.id;

const toOption = (region) => ({
  value: String(getRegionValue(region)),
  label: normalizeRegionLabel(region)
});

export function formatPriceInput(value) {
  const digits = digitsOnly(value);

  if (!digits) {
    return '';
  }

  return Number(digits).toLocaleString('en-US');
}

export function parsePriceInput(value) {
  const digits = digitsOnly(value);

  if (!digits) {
    return 0;
  }

  return Number(digits);
}

export function validateProductForm(form = {}) {
  const errors = {};
  const title = String(form.title ?? '').trim();
  const category = String(form.category ?? '').trim();
  const description = String(form.description ?? '').trim();
  const regionId = form.regionId;

  if (!title) {
    errors.title = '제목을 입력해 주세요.';
  }

  if (!isValidPriceInput(form.price) || parsePriceInput(form.price) < 100) {
    errors.price = '가격은 100원 이상 입력해 주세요.';
  }

  if (!category || !CATEGORY_VALUES.has(category)) {
    errors.category = '카테고리를 선택해 주세요.';
  }

  if (regionId === undefined || regionId === null || String(regionId).trim() === '') {
    errors.regionId = '지역을 선택해 주세요.';
  }

  if (!description) {
    errors.description = '설명을 입력해 주세요.';
  } else if (description.length > 2000) {
    errors.description = '설명은 2000자 이하로 입력해 주세요.';
  }

  return errors;
}

export function normalizeRegionLabel(region) {
  if (!region) {
    return '';
  }

  if (region.name) {
    return String(region.name).trim();
  }

  const labelParts = region.sido
    ? presentParts([region.sido, region.sigungu, region.eupmyeondong])
    : region.province
      ? presentParts([region.province, region.city, region.district])
      : presentParts([region.city, region.district, region.neighborhood]);

  if (labelParts.length > 0) {
    return labelParts.join(' ');
  }

  const value = getRegionValue(region);
  return value === undefined || value === null ? '' : String(value);
}

export function getRegionSelectOptions(regions = []) {
  return regions.filter((region) => getRegionValue(region) !== undefined && getRegionValue(region) !== null).map(toOption);
}

export function getChildRegionOptions(regions = [], parentId) {
  if (parentId === undefined || parentId === null || String(parentId).trim() === '') {
    return [];
  }

  const normalizedParentId = String(parentId);
  const nestedParent = regions.find((region) => String(getRegionValue(region)) === normalizedParentId);
  const nestedChildren = Array.isArray(nestedParent?.children) ? nestedParent.children : [];
  const flatChildren = regions.filter((region) => {
    const childParentId = region?.parentId ?? region?.parentRegionId;
    return childParentId !== undefined && childParentId !== null && String(childParentId) === normalizedParentId;
  });

  return getRegionSelectOptions([...nestedChildren, ...flatChildren]);
}

export function getProductCategoryLabel(product = {}) {
  return product.categoryLabel || product.categoryName || product.category || '-';
}

export function getProductStatusLabel(product = {}) {
  if (product.statusLabel || product.statusName) {
    return product.statusLabel || product.statusName;
  }

  const key = String(product.status || '').toUpperCase();
  return STATUS_LABELS[key] || product.status || '-';
}

export function getProductRegionLabel(product = {}) {
  if (product.regionLabel || product.regionFullName) {
    return product.regionLabel || product.regionFullName;
  }

  const labelParts = product.parentRegionName
    ? uniqueParts([product.parentRegionName, product.eupmyeondong])
    : product.sido
      ? uniqueParts([product.sido, product.sigungu, product.eupmyeondong])
      : uniqueParts([typeof product.region === 'string' ? product.region : '']);

  if (labelParts.length > 0) {
    return labelParts.join(' ');
  }

  return normalizeRegionLabel(product.region) || '-';
}

export function normalizeProductImageUrl(value) {
  const url = String(value ?? '').trim();

  if (!url) {
    return '';
  }

  if (/^(?:https?:|blob:|data:|\/)/i.test(url)) {
    return url;
  }

  return url.startsWith('uploads/') ? `/${url}` : url;
}

export function getProductImageUrl(product = {}) {
  return normalizeProductImageUrl(
    product.primaryImageUrl || product.thumbnailUrl || product.thumbnailImageUrl || product.imageUrl || product.image || ''
  );
}

export function getProductImageUrls(product = {}) {
  if (Array.isArray(product.imageUrls) && product.imageUrls.length > 0) {
    return product.imageUrls.map(normalizeProductImageUrl).filter(Boolean);
  }

  const imageUrl = getProductImageUrl(product);
  return imageUrl ? [imageUrl] : [];
}

export function getProductTitle(product = {}) {
  return product.title || product.productTitle || product.name || '';
}

export function getProductPrice(product = {}) {
  return product.price ?? product.productPrice ?? product.salePrice ?? 0;
}
