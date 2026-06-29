import { useCallback, useEffect, useState } from 'react';

export function PageHeader({ title, eyebrow, action }) {
  return (
    <div className="page-header">
      <div>
        {eyebrow ? <p className="page-eyebrow">{eyebrow}</p> : null}
        <h1>{title}</h1>
      </div>
      {action ? <div>{action}</div> : null}
    </div>
  );
}

export function useApiResource(loader, deps = []) {
  const [state, setState] = useState({ data: null, error: null, loading: true });

  const load = useCallback(async () => {
    setState((current) => ({ ...current, error: null, loading: true }));

    try {
      const data = await loader();
      setState({ data, error: null, loading: false });
    } catch (error) {
      setState({ data: null, error, loading: false });
    }
  }, deps);

  useEffect(() => {
    load();
  }, [load]);

  return { ...state, reload: load };
}

export const getPageContent = (payload) => {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (Array.isArray(payload?.content)) {
    return payload.content;
  }

  return [];
};

export const getProductId = (product) => product?.productId ?? product?.id;

export const formatDateTime = (value) => {
  if (!value) {
    return '-';
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(date);
};

export const statusText = (status) => {
  const labels = {
    ACTIVE: '진행중',
    ACCEPTED: '수락됨',
    AVAILABLE: '판매중',
    CANCELLED: '취소',
    CONFIRMING: '확인중',
    COMPLETED: '완료',
    EXTENDED: '연장됨',
    EXTENSION_REQUESTED: '연장 요청',
    EXPIRED: '만료',
    FAILED: '실패',
    HELD: '보류',
    ISSUED: '발급됨',
    OFFER_ACCEPTED: '제안 수락',
    PAID: '결제완료',
    PAYMENT_PENDING: '결제대기',
    PENDING: '대기',
    READY: '준비',
    REFUNDED: '환불',
    REJECTED: '거절',
    RESERVED: '예약중',
    SELLING: '판매중',
    SETTLED: '정산완료',
    SOLD: '판매완료',
    USED: '사용됨'
  };

  const key = String(status || '').toUpperCase();
  return labels[key] || status || '-';
};

export const roleText = (role) => {
  const labels = { BUYER: '구매', SELLER: '판매', ALL: '전체' };
  return labels[String(role || '').toUpperCase()] || role || '-';
};
