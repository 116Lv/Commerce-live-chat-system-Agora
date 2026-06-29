export const getList = (payload) => {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (Array.isArray(payload?.content)) {
    return payload.content;
  }

  if (Array.isArray(payload?.items)) {
    return payload.items;
  }

  return [];
};

export const getId = (item, keys = ['id']) => keys.map((key) => item?.[key]).find((value) => value !== undefined && value !== null);

export const replaceById = (items, updated, idKey = 'id') => {
  const updatedId = updated?.[idKey];

  if (updatedId === undefined || updatedId === null) {
    return items;
  }

  return items.map((item) => (item?.[idKey] === updatedId ? { ...item, ...updated } : item));
};

export const removeById = (items, id, idKey = 'id') => items.filter((item) => item?.[idKey] !== id);

export const replaceInPayload = (payload, updated, idKey = 'id') => {
  if (Array.isArray(payload)) {
    return replaceById(payload, updated, idKey);
  }

  return { ...payload, content: replaceById(getList(payload), updated, idKey) };
};

export const removeFromPayload = (payload, id, idKey = 'id') => {
  if (Array.isArray(payload)) {
    return removeById(payload, id, idKey);
  }

  return { ...payload, content: removeById(getList(payload), id, idKey) };
};

export const parseUserIds = (value) =>
  String(value || '')
    .split(/[,\s]+/)
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isInteger(item) && item > 0);

export const getReportTabs = (admin = {}) => {
  const role = String(admin?.role || '').toUpperCase();

  if (role === 'ROOT_ADMIN') {
    return [
      { key: 'users', title: '회원 신고' },
      { key: 'products', title: '상품 신고' }
    ];
  }

  if (role === 'USER_ADMIN') {
    return [{ key: 'users', title: '회원 신고' }];
  }

  if (role === 'PRODUCT_ADMIN') {
    return [{ key: 'products', title: '상품 신고' }];
  }

  return [];
};

export const canUpdateAdminRoles = (admin = {}) => String(admin?.role || '').toUpperCase() === 'ROOT_ADMIN';

export const canSettlePayment = (payment = {}) => payment?.settlementId !== undefined && payment?.settlementId !== null;

export const dashboardStats = (dashboard = {}) => {
  const menus = Array.isArray(dashboard.accessibleMenus) ? dashboard.accessibleMenus : [];

  return [
    { label: '권한', value: dashboard.role || '-' },
    { label: '접근 메뉴', value: menus.length },
    { label: '메뉴 목록', value: menus.length ? menus.join(', ') : '-' }
  ];
};
