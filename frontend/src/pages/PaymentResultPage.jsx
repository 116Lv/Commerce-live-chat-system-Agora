import { useEffect, useState } from 'react';
import { Alert, Button, Form } from 'react-bootstrap';
import { Link, useSearchParams } from 'react-router-dom';
import { confirmPayment } from '../api/paymentApi.js';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { PageHeader, statusText } from './pageUtils.jsx';

export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const [paymentId, setPaymentId] = useState(searchParams.get('paymentId') || '');
  const [paymentKey, setPaymentKey] = useState(searchParams.get('paymentKey') || '');
  const [payment, setPayment] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState(searchParams.get('message') || '');
  const [submitting, setSubmitting] = useState(false);
  const hasReturnApproval = Boolean(paymentId && paymentKey);

  const runConfirm = async (event) => {
    event?.preventDefault();
    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      const response = await confirmPayment(paymentId, { paymentKey });
      setPayment(response);
      setMessage('결제 결과를 확인했어요.');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    if (paymentId && paymentKey) {
      runConfirm();
    }
  }, []);

  return (
    <section>
      <PageHeader title="결제 결과" eyebrow="Payment" />
      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="danger">{error}</Alert> : null}
      <div className="detail-panel">
        <div className="auto-confirm">
          <h2 className="section-title">결제 승인 확인</h2>
          <p className="text-muted mb-0">
            {hasReturnApproval
              ? '결제창에서 돌아온 승인 정보를 확인하고 있어요.'
              : '결제창에서 돌아온 값이 없으면 아래 보조 입력으로 승인 결과를 확인할 수 있어요.'}
          </p>
        </div>
        <details className="advanced-payment-confirm mt-3" open={!hasReturnApproval && !payment}>
          <summary>기술 승인 정보 직접 입력</summary>
          <Form onSubmit={runConfirm} className="stack-list mt-3">
            <Form.Group controlId="resultPaymentId">
              <Form.Label>결제 ID</Form.Label>
              <Form.Control value={paymentId} onChange={(event) => setPaymentId(event.target.value)} required />
            </Form.Group>
            <Form.Group controlId="resultPaymentKey">
              <Form.Label>결제사 승인값</Form.Label>
              <Form.Control value={paymentKey} onChange={(event) => setPaymentKey(event.target.value)} required />
            </Form.Group>
            <Button type="submit" disabled={submitting || !paymentId || !paymentKey}>
              {submitting ? '확인 중' : '승인 결과 확인'}
            </Button>
          </Form>
        </details>
        {payment ? (
          <div className="mt-3">
            <div className="d-flex justify-content-between align-items-start gap-2">
              <h2 className="section-title">결제 #{payment.paymentId}</h2>
              <StatusBadge status={payment.status} />
            </div>
            <MoneyText amount={payment.amount} className="product-detail-price" />
            <p className="text-muted mb-0">상태 {statusText(payment.status)}</p>
            <Button as={Link} to={`/trades/${payment.tradeId}`} variant="outline-primary" className="mt-3">
              거래 보기
            </Button>
          </div>
        ) : null}
      </div>
    </section>
  );
}
