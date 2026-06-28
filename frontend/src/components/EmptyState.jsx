import { Inbox } from 'lucide-react';

export default function EmptyState({ title = 'No items yet', message }) {
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
