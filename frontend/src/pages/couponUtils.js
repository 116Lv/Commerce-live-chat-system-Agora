const STATUS_META = {
  ACTIVE: { label: '발급 가능', variant: 'success' },
  AVAILABLE: { label: '발급 가능', variant: 'success' },
  ISSUABLE: { label: '발급 가능', variant: 'success' },
  ISSUED: { label: '사용 가능', variant: 'success' },
  USED: { label: '사용 완료', variant: 'secondary' },
  EXPIRED: { label: '만료', variant: 'dark' },
  ENDED: { label: '종료', variant: 'secondary' },
  CLOSED: { label: '종료', variant: 'secondary' },
  SOLD_OUT: { label: '소진', variant: 'danger' },
  PENDING: { label: '예정', variant: 'warning' }
};

const DAY_MS = 24 * 60 * 60 * 1000;

const toNumber = (value) => {
  const number = Number(value);
  return Number.isFinite(number) ? number : 0;
};

const toDate = (value) => {
  if (!value) {
    return null;
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
};

const startOfDay = (date) => new Date(date.getFullYear(), date.getMonth(), date.getDate());

export const formatCouponMoney = (value) => `${toNumber(value).toLocaleString('ko-KR')}원`;

export const getDiscountConditionText = (coupon) => {
  const discount = formatCouponMoney(coupon?.discountAmount);
  const minOrder = toNumber(coupon?.minOrderAmount);

  if (minOrder <= 0) {
    return `주문 금액 제한 없이 ${discount} 할인`;
  }

  return `${formatCouponMoney(minOrder)} 이상 주문 시 ${discount} 할인`;
};

export const getCouponStatusMeta = (coupon) => {
  const status = typeof coupon === 'string' ? coupon : coupon?.status;
  const key = String(status || '').toUpperCase();
  if (key === 'PAYMENT_PENDING') {
    return { label: '결제 시도중', variant: 'warning' };
  }
  return STATUS_META[key] || { label: status || '-', variant: 'light' };
};

export const toCouponFilter = (coupon, now = new Date()) => {
  const status = String(coupon?.status || '').toUpperCase();
  const expiresAt = toDate(coupon?.expiresAt ?? coupon?.endAt);

  if (status === 'USED') {
    return 'used';
  }

  if (status === 'PAYMENT_PENDING') {
    return 'pending';
  }

  if (status === 'EXPIRED' || (expiresAt && expiresAt < now)) {
    return 'expired';
  }

  return 'usable';
};

export const sortMyCoupons = (coupons, now = new Date()) => {
  const order = { usable: 0, pending: 1, used: 2, expired: 3 };

  return [...(coupons || [])].sort((a, b) => {
    const filterDiff = order[toCouponFilter(a, now)] - order[toCouponFilter(b, now)];
    if (filterDiff !== 0) {
      return filterDiff;
    }

    const aDate = toDate(a?.expiresAt)?.getTime() ?? Number.MAX_SAFE_INTEGER;
    const bDate = toDate(b?.expiresAt)?.getTime() ?? Number.MAX_SAFE_INTEGER;
    return aDate - bDate;
  });
};

export const filterCoupons = (coupons, filter, now = new Date()) => {
  if (!filter || filter === 'all') {
    return coupons || [];
  }

  return (coupons || []).filter((coupon) => toCouponFilter(coupon, now) === filter);
};

export const getExpiryPriority = (coupon, now = new Date()) => {
  if (toCouponFilter(coupon, now) !== 'usable') {
    return 'normal';
  }

  const expiresAt = toDate(coupon?.expiresAt ?? coupon?.endAt);
  if (!expiresAt) {
    return 'normal';
  }

  const diffDays = Math.ceil((startOfDay(expiresAt) - startOfDay(now)) / DAY_MS);
  return diffDays >= 0 && diffDays <= 3 ? 'soon' : 'normal';
};

export const formatCouponPeriod = (coupon, now = new Date()) => {
  const startAt = toDate(coupon?.startAt);
  const endAt = toDate(coupon?.endAt ?? coupon?.expiresAt);

  if (!startAt && !endAt) {
    return '기간 제한 없음';
  }

  if (endAt) {
    if (endAt < now) {
      return '기간 종료';
    }

    const diffDays = Math.ceil((startOfDay(endAt) - startOfDay(now)) / DAY_MS);

    if (diffDays < 0) {
      return '기간 종료';
    }

    if (diffDays === 0) {
      return '오늘까지';
    }

    if (diffDays === 1) {
      return '내일까지';
    }

    if (diffDays <= 7) {
      return `D-${diffDays}`;
    }
  }

  const formatter = new Intl.DateTimeFormat('ko-KR', { month: 'numeric', day: 'numeric' });
  const startText = startAt ? formatter.format(startAt) : '지금';
  const endText = endAt ? formatter.format(endAt) : '상시';
  return `${startText} - ${endText}`;
};

export const getRemainingQuantityText = (coupon) => {
  const total = coupon?.totalQuantity ?? coupon?.quantity;
  const issued = coupon?.issuedQuantity;

  if (total == null) {
    return '수량 제한 없음';
  }

  const remaining = Math.max(toNumber(total) - toNumber(issued), 0);
  return remaining > 0 ? `${remaining.toLocaleString('ko-KR')}장 남음` : '소진';
};

export const getIssueProgress = (coupon) => {
  const total = toNumber(coupon?.totalQuantity ?? coupon?.quantity);
  const issued = toNumber(coupon?.issuedQuantity);
  const rate = total > 0 ? Math.min(Math.round((issued / total) * 100), 100) : 0;

  return { issued, total, rate };
};

export const buildIssueButtonState = (event, options = {}) => {
  if (options.issuing) {
    return { disabled: true, label: '발급 중' };
  }

  if (event?.issuedByMe || event?.alreadyIssued) {
    return { disabled: true, label: '이미 발급됨' };
  }

  const status = String(event?.status || '').toUpperCase();
  if (status === 'SOLD_OUT') {
    return { disabled: true, label: '소진됨' };
  }

  if (['ENDED', 'EXPIRED', 'CLOSED'].includes(status) || toCouponFilter({ status, expiresAt: event?.endAt }) === 'expired') {
    return { disabled: true, label: '종료됨' };
  }

  const total = event?.totalQuantity ?? event?.quantity;
  if (total != null && toNumber(event?.issuedQuantity) >= toNumber(total)) {
    return { disabled: true, label: '소진됨' };
  }

  if (status && !['ACTIVE', 'AVAILABLE', 'ISSUABLE'].includes(status)) {
    return { disabled: true, label: status === 'PENDING' ? '발급 예정' : '발급 불가' };
  }

  return { disabled: false, label: '쿠폰 받기' };
};
