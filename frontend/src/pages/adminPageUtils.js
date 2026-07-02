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

export const ADMIN_COUPON_TYPE_LABELS = {
  FIRST_COME: '선착순',
  NEW_SIGNUP: '신규 가입',
  ADMIN_INDIVIDUAL: '관리자 개별 발급'
};

export const ADMIN_COUPON_STATUS_LABELS = {
  ACTIVE: '진행중',
  AVAILABLE: '발급 가능',
  ENDED: '종료',
  EXPIRED: '만료',
  ISSUED: '발급됨',
  PAUSED: '중지',
  PENDING: '대기',
  READY: '준비',
  SCHEDULED: '예정',
  SOLD_OUT: '소진',
  USED: '사용됨'
};

export const formatAdminCouponType = (type, fallback = '') => {
  const key = String(type || '').toUpperCase();
  return ADMIN_COUPON_TYPE_LABELS[key] || fallback || type || '-';
};

export const formatAdminCouponStatus = (status, fallback = '') => {
  const key = String(status || '').toUpperCase();
  return ADMIN_COUPON_STATUS_LABELS[key] || fallback || status || '-';
};

export const parseAdminCouponNumber = (value) => {
  if (value === null || value === undefined || value === '') {
    return 0;
  }

  const normalized = String(value).replace(/[^\d.-]/g, '');

  if (!normalized || normalized === '-' || normalized === '.') {
    return Number.NaN;
  }

  return Number(normalized);
};

export const formatAdminCouponMoneyInput = (value) => {
  const digits = String(value ?? '').replace(/[^\d]/g, '');

  if (!digits) {
    return '';
  }

  return new Intl.NumberFormat('ko-KR').format(Number(digits));
};

export const buildAdminCouponCreatePayload = (form = {}) => ({
  type: form.type,
  name: String(form.name || '').trim(),
  startAt: form.startAt,
  endAt: form.endAt,
  totalQuantity: parseAdminCouponNumber(form.totalQuantity),
  discountAmount: parseAdminCouponNumber(form.discountAmount),
  minOrderAmount: parseAdminCouponNumber(form.minOrderAmount),
  validDays: parseAdminCouponNumber(form.validDays)
});

export const validateAdminCouponForm = (form = {}) => {
  const payload = buildAdminCouponCreatePayload(form);
  const errors = {};
  const startDate = payload.startAt ? new Date(payload.startAt) : null;
  const endDate = payload.endAt ? new Date(payload.endAt) : null;

  if (!payload.name) {
    errors.name = '쿠폰명을 입력해 주세요.';
  }

  if (!payload.startAt || Number.isNaN(startDate?.getTime())) {
    errors.startAt = '시작 일시를 입력해 주세요.';
  }

  if (!payload.endAt || Number.isNaN(endDate?.getTime())) {
    errors.endAt = '종료 일시를 입력해 주세요.';
  } else if (startDate && !Number.isNaN(startDate.getTime()) && startDate >= endDate) {
    errors.endAt = '종료 일시는 시작 일시보다 늦어야 합니다.';
  }

  if (!Number.isFinite(payload.totalQuantity) || payload.totalQuantity <= 0) {
    errors.totalQuantity = '발급 수량은 1개 이상이어야 합니다.';
  }

  if (!Number.isFinite(payload.discountAmount) || payload.discountAmount <= 0) {
    errors.discountAmount = '할인 금액은 1원 이상이어야 합니다.';
  }

  if (!Number.isFinite(payload.minOrderAmount) || payload.minOrderAmount < 0) {
    errors.minOrderAmount = '최소 주문 금액은 0원 이상이어야 합니다.';
  }

  if (!Number.isFinite(payload.validDays) || payload.validDays <= 0) {
    errors.validDays = '유효 기간은 1일 이상이어야 합니다.';
  }

  return errors;
};

export const parseUserIdTokens = (value) => {
  const tokens = String(value || '')
    .split(/[,\s]+/)
    .map((item) => item.trim())
    .filter(Boolean);
  const seen = new Set();
  const validIds = [];
  const validTargets = [];
  const invalidTokens = [];

  tokens.forEach((token) => {
    if (/^\d+$/.test(token)) {
      const id = Number(token);

      if (Number.isSafeInteger(id) && id > 0 && !seen.has(token)) {
        seen.add(token);
        validIds.push(id);
        validTargets.push(token);
      }
      return;
    }

    if (/^-?\d+(?:\.\d+)?$/.test(token)) {
      invalidTokens.push(token);
      return;
    }

    if (!seen.has(token)) {
      seen.add(token);
      validTargets.push(token);
    }
  });

  return { validIds, validTargets, invalidTokens };
};

export const parseUserIds = (value) => parseUserIdTokens(value).validIds;

export const canAdminIssueCoupon = (detail) =>
  Boolean(detail) && detail.canIssue !== false && !detail.ended && !detail.soldOut;

export const getCouponTargetChipKey = (token, index) => `${token}-${index}`;

export const formatIssueRate = (issueRate) => {
  if (issueRate === null || issueRate === undefined || issueRate === '') {
    return '-';
  }

  const value = Number(issueRate);

  if (!Number.isFinite(value)) {
    return '-';
  }

  return `${Math.round(value * 100)}%`;
};

export const getAdminCouponStatusVariant = (event = {}) => {
  const status = String(event?.status || '').toUpperCase();

  if (event?.soldOut || status === 'SOLD_OUT') {
    return 'secondary';
  }

  if (event?.ended || status === 'ENDED' || status === 'EXPIRED') {
    return 'dark';
  }

  if (event?.canIssue === false || status === 'PAUSED') {
    return 'warning';
  }

  if (status === 'ACTIVE' || status === 'AVAILABLE' || status === 'ISSUED') {
    return 'success';
  }

  return 'secondary';
};

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

export const ADMIN_ROLE_LABELS = {
  ROOT_ADMIN: '최고 관리자',
  USER_ADMIN: '회원 관리자',
  PRODUCT_ADMIN: '상품 관리자',
  SETTLEMENT_ADMIN: '정산 관리자'
};

export const formatAdminRole = (role) => ADMIN_ROLE_LABELS[String(role || '').toUpperCase()] || role || '-';

export const REPORT_STATUS_FILTERS = [
  { key: 'PENDING', label: '대기' },
  { key: 'RESOLVED', label: '처리 완료' }
];

export const getReportStatusLabel = (status) => {
  const key = String(status || '').toUpperCase();
  return REPORT_STATUS_FILTERS.find((item) => item.key === key)?.label || statusTextFallback(key);
};

const statusTextFallback = (status) => status || '-';

export const reportMatchesSearch = (report = {}, search = '') => {
  const keyword = String(search || '').trim().toLowerCase();

  if (!keyword) {
    return true;
  }

  return [
    report.reportId,
    report.reporterId,
    report.reporterNickname,
    report.reporterEmail,
    report.reportedUserId,
    report.reportedUserNickname,
    report.reportedUserEmail,
    report.productId,
    report.productTitle,
    report.productSellerId,
    report.productSellerNickname,
    report.productSellerEmail,
    report.productPrice,
    report.productStatus,
    report.productApprovalStatus,
    report.reason,
    report.status,
    report.adminMemo,
    report.resolvedAt
  ]
    .filter((value) => value !== undefined && value !== null)
    .some((value) => String(value).toLowerCase().includes(keyword));
};

export const canSettlePayment = (payment = {}) =>
  payment?.settlementId !== undefined &&
  payment?.settlementId !== null &&
  String(payment?.settlementStatus || '').toUpperCase() === 'READY';

export const paymentVerificationSummary = (payment = {}) => {
  const status = String(payment?.status || '').toUpperCase();

  if (status === 'PAID') {
    return { label: '승인 완료', variant: 'success' };
  }

  if (status === 'READY' || status === 'CONFIRMING') {
    return { label: '승인 대기', variant: 'warning' };
  }

  if (status === 'FAILED' || status === 'CANCELLED') {
    return { label: '승인 실패', variant: 'danger' };
  }

  if (status === 'REFUNDED') {
    return { label: '환불 완료', variant: 'secondary' };
  }

  return { label: status || '-', variant: 'secondary' };
};

export const dashboardStats = (dashboard = {}) => {
  const formatMetric = (value, suffix) => `${Number(value || 0).toLocaleString()}${suffix}`;

  return [
    { label: '총 가입자', value: formatMetric(dashboard.totalUserCount, '명') },
    { label: '오늘 신규 가입자', value: formatMetric(dashboard.todayNewUserCount, '명') },
    { label: '오늘 신고 수', value: formatMetric(dashboard.todayReportCount, '건') },
    { label: '오늘 거래량', value: formatMetric(dashboard.todayTradeCount, '건') },
    { label: '오늘 등록요청', value: formatMetric(dashboard.todayProductRequestCount, '건') },
    { label: '등록된 상품 수', value: formatMetric(dashboard.registeredProductCount, '건') }
  ];
};
