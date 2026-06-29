import { useState } from 'react';
import { Alert, Button, Card, Form } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import { createReview, getTradeReviews } from '../api/reviewApi.js';
import EmptyState from '../components/EmptyState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import { PageHeader, formatDateTime, useApiResource } from './pageUtils.jsx';

export default function ReviewFormPage() {
  const { tradeId } = useParams();
  const [rating, setRating] = useState(5);
  const [content, setContent] = useState('');
  const [message, setMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { data: reviews = [], error, loading, reload } = useApiResource(() => getTradeReviews(tradeId), [tradeId]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    setErrorMessage('');
    setSubmitting(true);

    try {
      await createReview({ tradeId: Number(tradeId), rating: Number(rating), content });
      setContent('');
      setRating(5);
      setMessage('후기를 등록했어요.');
      await reload();
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section>
      <PageHeader
        title="후기 작성"
        eyebrow="Review"
        action={
          <Button as={Link} to={`/trades/${tradeId}`} variant="outline-primary">
            거래 보기
          </Button>
        }
      />
      {message ? <Alert variant="success">{message}</Alert> : null}
      {errorMessage ? <Alert variant="danger">{errorMessage}</Alert> : null}
      <div className="detail-panel mb-3">
        <Form onSubmit={handleSubmit} className="stack-list">
          <Form.Group controlId="reviewRating">
            <Form.Label>평점</Form.Label>
            <Form.Select value={rating} onChange={(event) => setRating(event.target.value)}>
              {[5, 4, 3, 2, 1].map((value) => (
                <option key={value} value={value}>
                  {value}점
                </option>
              ))}
            </Form.Select>
          </Form.Group>
          <Form.Group controlId="reviewContent">
            <Form.Label>내용</Form.Label>
            <Form.Control
              as="textarea"
              rows={5}
              maxLength={500}
              value={content}
              onChange={(event) => setContent(event.target.value)}
              placeholder="거래 후기를 입력해 주세요."
              required
            />
          </Form.Group>
          <Button type="submit" disabled={submitting}>
            {submitting ? '등록 중' : '후기 등록'}
          </Button>
        </Form>
      </div>
      <h2 className="section-title">거래 후기</h2>
      {loading ? <LoadingState label="후기 불러오는 중" /> : null}
      {error ? <ErrorState title="후기를 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && reviews.length === 0 ? <EmptyState title="등록된 후기가 없어요" /> : null}
      {!loading && !error && reviews.length > 0 ? (
        <div className="stack-list">
          {reviews.map((review) => (
            <Card key={review.reviewId} className="list-card">
              <Card.Body>
                <div className="list-card-row">
                  <div>
                    <Card.Title as="h2">후기 #{review.reviewId}</Card.Title>
                    <p className="text-muted mb-0">
                      {review.reviewerId} → {review.targetUserId}
                    </p>
                  </div>
                  <strong className="rating-text">{review.rating}점</strong>
                </div>
                <p className="review-content">{review.content}</p>
                <p className="text-muted mb-0 small">{formatDateTime(review.createdAt)}</p>
              </Card.Body>
            </Card>
          ))}
        </div>
      ) : null}
    </section>
  );
}
