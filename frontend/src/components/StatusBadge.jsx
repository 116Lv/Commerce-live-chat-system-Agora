import { Badge } from 'react-bootstrap';

const statusMap = {
  ACTIVE: { label: '진행중', bg: 'success' },
  ACCEPTED: { label: '수락됨', bg: 'success' },
  AVAILABLE: { label: '판매중', bg: 'success' },
  BLOCKED: { label: '차단', bg: 'danger' },
  CANCELLED: { label: '취소', bg: 'secondary' },
  CONFIRMING: { label: '확인중', bg: 'warning' },
  COMPLETED: { label: '완료', bg: 'secondary' },
  DELETED: { label: '삭제', bg: 'dark' },
  ENDED: { label: '종료', bg: 'secondary' },
  EXTENDED: { label: '연장됨', bg: 'info' },
  EXTENSION_REQUESTED: { label: '연장 요청', bg: 'warning' },
  EXPIRED: { label: '만료', bg: 'secondary' },
  FAILED: { label: '실패', bg: 'danger' },
  HELD: { label: '보류', bg: 'warning' },
  HIDDEN: { label: '숨김', bg: 'dark' },
  ISSUED: { label: '발급됨', bg: 'success' },
  OFFER_ACCEPTED: { label: '제안 수락', bg: 'success' },
  PAID: { label: '결제완료', bg: 'success' },
  PAYMENT_PENDING: { label: '결제대기', bg: 'warning' },
  PENDING: { label: '대기', bg: 'warning' },
  READY: { label: '준비', bg: 'info' },
  REPORTED: { label: '신고됨', bg: 'danger' },
  REFUNDED: { label: '환불', bg: 'secondary' },
  REJECTED: { label: '거절', bg: 'secondary' },
  RESERVED: { label: '예약중', bg: 'info' },
  RESOLVED: { label: '처리완료', bg: 'success' },
  SELLING: { label: '판매중', bg: 'success' },
  SETTLED: { label: '정산완료', bg: 'success' },
  SOLD: { label: '판매완료', bg: 'secondary' },
  SUSPENDED: { label: '정지', bg: 'danger' },
  USED: { label: '사용됨', bg: 'secondary' }
};

export default function StatusBadge({ status }) {
  const normalized = String(status || 'PENDING').toUpperCase();
  const badge = statusMap[normalized] || { label: normalized, bg: 'secondary' };

  return <Badge bg={badge.bg}>{badge.label}</Badge>;
}
