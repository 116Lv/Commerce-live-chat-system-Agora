import { useState } from 'react';
import { ButtonGroup, Card, ToggleButton } from 'react-bootstrap';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getMyReviews } from '../api/mypageApi.js';
import { PageHeader, formatDateTime, getPageContent, useApiResource } from './pageUtils.jsx';

const typeOptions = [
  { value: 'written', label: '작성한 후기' },
  { value: 'received', label: '받은 후기' }
];

export default function MyReviewsPage() {
  const [type, setType] = useState('written');
  const { data, error, loading, reload } = useApiResource(() => getMyReviews({ type, page: 0, size: 20 }), [type]);
  const reviews = getPageContent(data);

  return (
    <section>
      <PageHeader
        title="내 후기"
        eyebrow="마이페이지"
        action={
          <ButtonGroup aria-label="후기 구분">
            {typeOptions.map((option) => (
              <ToggleButton
                key={option.value}
                id={`review-type-${option.value}`}
                type="radio"
                variant="outline-primary"
                name="review-type"
                value={option.value}
                checked={type === option.value}
                onChange={(event) => setType(event.currentTarget.value)}
              >
                {option.label}
              </ToggleButton>
            ))}
          </ButtonGroup>
        }
      />
      {loading ? <LoadingState label="후기 불러오는 중" /> : null}
      {error ? <ErrorState title="후기를 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && reviews.length === 0 ? <EmptyState title="후기가 없어요" /> : null}
      {!loading && !error && reviews.length > 0 ? (
        <div className="stack-list">
          {reviews.map((review) => (
            <Card key={review.reviewId} className="list-card">
              <Card.Body>
                <div className="list-card-row">
                  <div>
                    <Card.Title as="h2">{review.productTitle}</Card.Title>
                    <p className="text-muted mb-0">
                      {review.reviewerNickname} → {review.targetNickname}
                    </p>
                  </div>
                  <strong className="rating-text">{review.rating}점</strong>
                </div>
                <p className="review-content">{review.content || '내용 없음'}</p>
                <p className="text-muted mb-0 small">{formatDateTime(review.createdAt)}</p>
              </Card.Body>
            </Card>
          ))}
        </div>
      ) : null}
    </section>
  );
}
