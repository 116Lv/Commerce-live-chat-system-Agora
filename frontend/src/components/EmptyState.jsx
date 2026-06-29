import { Inbox } from 'lucide-react';

export default function EmptyState({ title = '표시할 항목이 없어요', message }) {
  return (
    <div className="state-panel empty-state">
      <Inbox size={28} aria-hidden="true" />
      <div>
        <h2>{title}</h2>
        {message ? <p>{message}</p> : null}
      </div>
    </div>
  );
}
