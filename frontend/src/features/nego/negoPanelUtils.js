const ACTIVE_RESPONSE_STATUSES = new Set(['PENDING', 'EXTENDED']);

const ACTIONS = {
  accept: { key: 'accept', label: '제안 수락', variant: 'primary' },
  reject: { key: 'reject', label: '제안 거절', variant: 'outline-primary' },
  extension: { key: 'extension', label: '응답 기한 연장 요청', variant: 'outline-primary' },
  extensionApprove: { key: 'extensionApprove', label: '연장 승인', variant: 'primary' },
  extensionReject: { key: 'extensionReject', label: '연장 거절', variant: 'outline-primary' }
};

export const normalizeOfferStatus = (status) => String(status || '').toUpperCase();

export const inferOfferRole = (offer, currentUserId) => {
  if (currentUserId == null || offer?.requesterId == null) {
    return null;
  }

  return String(offer.requesterId) === String(currentUserId) ? 'buyer' : 'seller';
};

export const getOfferActions = (offer, { role } = {}) => {
  const status = normalizeOfferStatus(offer?.status);

  if (ACTIVE_RESPONSE_STATUSES.has(status)) {
    if (role === 'seller') {
      return [ACTIONS.accept, ACTIONS.reject];
    }

    if (role === 'buyer') {
      return status === 'PENDING' ? [ACTIONS.extension] : [];
    }

    return status === 'PENDING' ? [ACTIONS.accept, ACTIONS.reject, ACTIONS.extension] : [ACTIONS.accept, ACTIONS.reject];
  }

  if (status === 'EXTENSION_REQUESTED') {
    return role === 'seller' || role == null ? [ACTIONS.extensionApprove, ACTIONS.extensionReject] : [];
  }

  return [];
};

export const parseOfferPriceInput = (value) => {
  const normalized = String(value || '').replace(/[^\d]/g, '');
  return normalized ? Number.parseInt(normalized, 10) : 0;
};

export const getOfferPriceValidation = (value, { productPrice } = {}) => {
  const offerPrice = parseOfferPriceInput(value);
  const listPrice = productPrice == null || productPrice === '' ? null : Number(productPrice);

  if (!Number.isFinite(offerPrice) || offerPrice <= 0) {
    return '제안가는 0원보다 커야 해요.';
  }

  if (Number.isFinite(listPrice) && listPrice > 0 && offerPrice >= listPrice) {
    return '제안가는 상품 가격보다 낮아야 해요.';
  }

  return '';
};

export const buildPaymentHref = (offer) => {
  const status = normalizeOfferStatus(offer?.status);

  return status === 'ACCEPTED' && offer?.tradeId ? `/checkout/${offer.tradeId}` : '';
};

export const getRemainingTimeLabel = (value, now = new Date()) => {
  if (!value) {
    return '';
  }

  const expiresAt = new Date(value);

  if (Number.isNaN(expiresAt.getTime())) {
    return '';
  }

  const diffMs = expiresAt.getTime() - now.getTime();

  if (diffMs <= 0) {
    return '기한이 지났어요.';
  }

  const totalMinutes = Math.ceil(diffMs / 60000);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  if (hours >= 24) {
    const days = Math.floor(hours / 24);
    const remainingHours = hours % 24;
    return remainingHours > 0 ? `${days}일 ${remainingHours}시간 남음` : `${days}일 남음`;
  }

  return hours > 0 ? `${hours}시간 ${minutes}분 남음` : `${minutes}분 남음`;
};
