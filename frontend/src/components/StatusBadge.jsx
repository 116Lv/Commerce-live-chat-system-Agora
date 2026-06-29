import { Badge } from 'react-bootstrap';

const statusMap = {
  ACTIVE: { label: '진행중', bg: 'success' },
  AVAILABLE: { label: '판매중', bg: 'success' },
  COMPLETED: { label: '완료', bg: 'secondary' },
  EXPIRED: { label: '만료', bg: 'secondary' },
  HIDDEN: { label: '숨김', bg: 'dark' },
  ISSUED: { label: '발급됨', bg: 'success' },
  PENDING: { label: '대기', bg: 'warning' },
  REPORTED: { label: '신고됨', bg: 'danger' },
  RESERVED: { label: '예약중', bg: 'info' },
  SELLING: { label: '판매중', bg: 'success' },
  SOLD: { label: '판매완료', bg: 'secondary' },
  SUSPENDED: { label: '정지', bg: 'danger' },
  USED: { label: '사용됨', bg: 'secondary' }
};

export default function StatusBadge({ status }) {
  const normalized = String(status || 'PENDING').toUpperCase();
  const badge = statusMap[normalized] || { label: normalized, bg: 'secondary' };

  return <Badge bg={badge.bg}>{badge.label}</Badge>;
}
