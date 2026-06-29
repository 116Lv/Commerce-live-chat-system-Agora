import { useState } from 'react';
import { Alert, Button, Form, Modal } from 'react-bootstrap';
import { createProductReport, createUserReport } from '../../api/reportApi.js';

export default function ReportModal({ show, onHide, productId, userId }) {
  const [targetType, setTargetType] = useState(productId ? 'product' : 'user');
  const [targetId, setTargetId] = useState(userId || productId || '');
  const [reason, setReason] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      if (targetType === 'product') {
        await createProductReport({ productId: Number(targetId), reason });
      } else {
        await createUserReport({ reportedUserId: Number(targetId), reason });
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
          <Form.Group className="mb-3" controlId="reportTargetType">
            <Form.Label>대상</Form.Label>
            <Form.Select
              value={targetType}
              onChange={(event) => {
                const nextType = event.target.value;
                setTargetType(nextType);
                setTargetId(nextType === 'product' ? productId || '' : userId || '');
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
