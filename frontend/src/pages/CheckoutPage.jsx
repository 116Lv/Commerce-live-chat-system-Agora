import { useEffect, useState } from 'react';
import { Alert, Button } from 'react-bootstrap';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { preparePayment, requestPaymentApproval } from '../api/paymentApi.js';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { PageHeader, statusText } from './pageUtils.jsx';

export default function CheckoutPage() {
  const { tradeId } = useParams();
  const navigate = useNavigate();
  const [payment, setPayment] = useState(null);
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

  const handlePaymentApproval = async () => {
    if (!payment?.paymentId || !payment?.orderId) {
      return;
    }

    setMessage('');
    setError('');
    setConfirming(true);

    try {
      const approval = await requestPaymentApproval(payment, {
        redirectUrl: `${window.location.origin}/payments/result`
      });
      const params = new URLSearchParams({
        paymentId: String(approval.paymentId),
        paymentKey: approval.paymentKey
      });

      navigate(`/payments/result?${params.toString()}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setConfirming(false);
    }
  };

  const isPaid = String(payment?.status || '').toUpperCase() === 'PAID';

  return (
    <section>
      <PageHeader title="결제" eyebrow="Checkout" />
      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="danger">{error}</Alert> : null}
      <div className="detail-panel">
        {loading ? <p className="mb-0">결제 준비 중...</p> : null}
        {!loading && payment ? (
          <>
            <div className="checkout-order-summary d-flex justify-content-between align-items-start gap-2">
              <div>
                <h2 className="section-title">주문 결제 준비</h2>
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
            <p className="text-muted mt-3 mb-0">
              결제창을 열어 주문 정보를 확인하고 결제를 진행해 주세요.
            </p>
            <div className="form-actions">
              <Button as={Link} to={`/trades/${tradeId}`} variant="outline-secondary">
                거래로 이동
              </Button>
              {isPaid ? (
                <Button as={Link} to={`/trades/${tradeId}`} variant="primary">
                  결제 내역 보기
                </Button>
              ) : (
                <Button type="button" onClick={handlePaymentApproval} disabled={confirming || !payment.orderId}>
                  {confirming ? '확인 중' : '결제하기'}
                </Button>
              )}
            </div>
          </>
        ) : null}
      </div>
    </section>
  );
}
