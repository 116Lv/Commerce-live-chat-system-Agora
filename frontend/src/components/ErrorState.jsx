import { Alert, Button } from 'react-bootstrap';
import { CircleAlert } from 'lucide-react';

export default function ErrorState({ title = 'Something went wrong', message, onRetry }) {
  return (
    <Alert variant="danger" className="state-alert">
      <div className="d-flex gap-2 align-items-start">
        <CircleAlert size={20} className="mt-1 flex-shrink-0" aria-hidden="true" />
        <div className="flex-grow-1">
          <Alert.Heading as="h2">{title}</Alert.Heading>
          {message ? <p>{message}</p> : null}
          {onRetry ? (
            <Button variant="outline-danger" size="sm" onClick={onRetry}>
              Retry
            </Button>
          ) : null}
        </div>
      </div>
    </Alert>
  );
}
