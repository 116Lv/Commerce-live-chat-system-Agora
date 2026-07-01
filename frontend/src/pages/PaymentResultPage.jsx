import { useEffect, useState } from 'react';
import { Alert, Button } from 'react-bootstrap';
import { Link, useSearchParams } from 'react-router-dom';
import { confirmPayment } from '../api/paymentApi.js';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { PageHeader, statusText } from './pageUtils.jsx';

export default function PaymentResultPage() {
  const [searchParams] = useSearchParams();
  const paymentId = searchParams.get('paymentId') || '';
  const paymentKey = searchParams.get('paymentKey') || '';
  const [payment, setPayment] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState(searchParams.get('message') || '');
  const [submitting, setSubmitting] = useState(false);
  const hasReturnApproval = Boolean(paymentId && paymentKey);

  const runConfirm = async () => {
    if (!paymentId || !paymentKey) {
      return;
    }

    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      const response = await confirmPayment(paymentId, { paymentKey });
      setPayment(response);
      setMessage('결제 결과를 확인했습니다.');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    runConfirm();
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
              ? '결제창에서 돌아온 승인 정보를 확인하고 있습니다.'
              : '결제창에서 돌아온 승인 정보가 없습니다. 거래 화면에서 결제를 다시 진행해 주세요.'}
          </p>
        </div>
        {submitting ? <p className="text-muted mt-3 mb-0">확인 중...</p> : null}
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
        ) : (
          <Button as={Link} to="/me/trades" variant="outline-secondary" className="mt-3">
            내 거래로 이동
          </Button>
        )}
      </div>
    </section>
  );
}
