import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, Form, Modal } from 'react-bootstrap';
import { createProductReport, createUserReport } from '../../api/reportApi.js';

export default function ReportModal({ show, onHide, productId, userId }) {
  const hasProductTarget = productId != null && productId !== '';
  const hasUserTarget = userId != null && userId !== '';
  const inferredTargetType = hasProductTarget ? 'product' : hasUserTarget ? 'user' : '';
  const inferredTargetId = hasProductTarget ? productId : hasUserTarget ? userId : '';
  const hasProvidedTarget = Boolean(inferredTargetType);
  const fallbackTargetType = inferredTargetType || 'product';
  const [targetType, setTargetType] = useState(fallbackTargetType);
  const [targetId, setTargetId] = useState(inferredTargetId);
  const [reason, setReason] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const targetLabel = useMemo(() => {
    const reportTargetType = hasProvidedTarget ? inferredTargetType : targetType;

    return reportTargetType === 'product' ? '상품' : '판매자';
  }, [hasProvidedTarget, inferredTargetType, targetType]);

  useEffect(() => {
    setTargetType(fallbackTargetType);
    setTargetId(inferredTargetId);
    setReason('');
    setMessage('');
    setError('');
    setSubmitting(false);
  }, [show, fallbackTargetType, inferredTargetId]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (submitting) {
      return;
    }

    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      const reportTargetType = hasProvidedTarget ? inferredTargetType : targetType;
      const reportTargetId = hasProvidedTarget ? inferredTargetId : targetId;
      const isMissingTargetId = reportTargetId == null || reportTargetId === '';

      if (!reportTargetType || isMissingTargetId) {
        throw new Error('신고 대상을 확인할 수 없어요.');
      }

      if (reportTargetType === 'product') {
        await createProductReport({ productId: Number(reportTargetId), reason });
      } else {
        await createUserReport({ reportedUserId: Number(reportTargetId), reason });
      }

      setReason('');
      setMessage('신고가 접수됐어요.');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const close = () => {
    setMessage('');
    setError('');
    onHide();
  };

  return (
    <Modal show={show} onHide={close} centered>
      <Modal.Header closeButton>
        <Modal.Title>신고하기</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          {message ? <Alert variant="success">{message}</Alert> : null}
          {error ? <Alert variant="danger">{error}</Alert> : null}
          {hasProvidedTarget ? <p className="report-target-summary">{targetLabel} 신고</p> : null}
          {!hasProvidedTarget ? (
            <>
              <Form.Group className="mb-3" controlId="reportTargetType">
                <Form.Label>대상</Form.Label>
                <Form.Select
                  value={targetType}
                  onChange={(event) => {
                    const nextType = event.target.value;
                    setTargetType(nextType);
                    setTargetId('');
                  }}
                >
                  <option value="product">상품</option>
                  <option value="user">사용자</option>
                </Form.Select>
              </Form.Group>
              <Form.Group className="mb-3" controlId="reportTargetId">
                <Form.Label>{targetType === 'product' ? '상품 ID' : '사용자 ID'}</Form.Label>
                <Form.Control
                  type="number"
                  min="1"
                  value={targetId}
                  onChange={(event) => setTargetId(event.target.value)}
                  required
                />
              </Form.Group>
            </>
          ) : null}
          <Form.Group controlId="reportReason">
            <Form.Label>사유</Form.Label>
            <Form.Control
              as="textarea"
              rows={4}
              maxLength={1000}
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              placeholder="신고 사유를 입력해 주세요."
              required
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="outline-secondary" type="button" onClick={close}>
            닫기
          </Button>
          <Button type="submit" disabled={submitting}>
            {submitting ? '접수 중' : '신고 접수'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}
