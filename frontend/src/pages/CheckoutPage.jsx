import { useEffect, useState } from 'react';
import { Alert, Button, Form } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import { confirmPayment, preparePayment } from '../api/paymentApi.js';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { PageHeader, statusText } from './pageUtils.jsx';

export default function CheckoutPage() {
  const { tradeId } = useParams();
  const [payment, setPayment] = useState(null);
  const [paymentKey, setPaymentKey] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [confirming, setConfirming] = useState(false);

  useEffect(() => {
    let disposed = false;

    const prepare = async () => {
      setLoading(true);
      setError('');

      try {
        const response = await preparePayment(tradeId);
        if (!disposed) {
          setPayment(response);
        }
      } catch (err) {
        if (!disposed) {
          setError(err.message);
        }
      } finally {
        if (!disposed) {
          setLoading(false);
        }
      }
    };

    prepare();

    return () => {
      disposed = true;
    };
  }, [tradeId]);

  const handleConfirm = async (event) => {
    event.preventDefault();
    setMessage('');
    setError('');
    setConfirming(true);

    try {
      const response = await confirmPayment(payment.paymentId, { paymentKey });
      setPayment(response);
      setMessage('결제가 확인됐어요.');
    } catch (err) {
      setError(err.message);
    } finally {
      setConfirming(false);
    }
  };

  return (
    <section>
      <PageHeader title="결제" eyebrow="Checkout" />
      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="danger">{error}</Alert> : null}
      <div className="detail-panel">
        {loading ? <p className="mb-0">결제 준비 중...</p> : null}
        {!loading && payment ? (
          <>
            <div className="d-flex justify-content-between align-items-start gap-2">
              <div>
                <h2 className="section-title">결제 #{payment.paymentId}</h2>
                <p className="text-muted mb-0">주문 {payment.orderId || '-'}</p>
              </div>
              <StatusBadge status={payment.status} />
            </div>
            <MoneyText amount={payment.amount} className="product-detail-price mt-2" />
            <dl className="compact-list mt-3">
              <div>
                <dt>거래</dt>
                <dd>{payment.tradeId}</dd>
              </div>
              <div>
                <dt>상태</dt>
                <dd>{statusText(payment.status)}</dd>
              </div>
            </dl>
            <Form onSubmit={handleConfirm} className="mt-3">
              <Form.Group controlId="paymentKey">
                <Form.Label>paymentKey</Form.Label>
                <Form.Control
                  value={paymentKey}
                  onChange={(event) => setPaymentKey(event.target.value)}
                  placeholder="결제 승인 키"
                  required
                />
              </Form.Group>
              <div className="form-actions">
                <Button as={Link} to={`/trades/${tradeId}`} variant="outline-secondary">
                  거래로 이동
                </Button>
                <Button type="submit" disabled={confirming}>
                  {confirming ? '확인 중' : '결제 확인'}
                </Button>
              </div>
            </Form>
          </>
        ) : null}
      </div>
    </section>
  );
}
