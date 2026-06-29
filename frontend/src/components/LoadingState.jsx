import { Spinner } from 'react-bootstrap';

export default function LoadingState({ label = 'Loading' }) {
  return (
    <div className="state-panel" role="status" aria-live="polite">
      <Spinner animation="border" size="sm" className="me-2" />
      <span>{label}</span>
    </div>
  );
}
