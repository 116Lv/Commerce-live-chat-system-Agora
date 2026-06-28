import { Badge } from 'react-bootstrap';

const statusMap = {
  ACTIVE: { label: 'Active', bg: 'success' },
  AVAILABLE: { label: 'Available', bg: 'success' },
  COMPLETED: { label: 'Completed', bg: 'secondary' },
  HIDDEN: { label: 'Hidden', bg: 'dark' },
  PENDING: { label: 'Pending', bg: 'warning' },
  REPORTED: { label: 'Reported', bg: 'danger' },
  RESERVED: { label: 'Reserved', bg: 'info' },
  SOLD: { label: 'Sold', bg: 'secondary' },
  SELLING: { label: 'Selling', bg: 'success' },
  SUSPENDED: { label: 'Suspended', bg: 'danger' }
};

export default function StatusBadge({ status }) {
  const normalized = String(status || 'PENDING').toUpperCase();
  const badge = statusMap[normalized] || { label: normalized, bg: 'secondary' };

  return <Badge bg={badge.bg}>{badge.label}</Badge>;
}
