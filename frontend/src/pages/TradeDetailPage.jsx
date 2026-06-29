import { useState } from 'react';
import { Alert, Button, Col, Form, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import { completeTrade, expireReservation, getTradeDetail, requestRatingMessage } from '../api/tradeApi.js';
import { getRefundStatus, refundPayment } from '../api/paymentApi.js';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { PageHeader, formatDateTime, statusText, useApiResource } from './pageUtils.jsx';

export default function TradeDetailPage() {
  const { tradeId } = useParams();
  const [message, setMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [busyKey, setBusyKey] = useState('');
  const [paymentId, setPaymentId] = useState('');
  const [refundReason, setRefundReason] = useState('');
  const [refundStatus, setRefundStatus] = useState(null);
  const { data: trade, error, loading, reload } = useApiResource(() => getTradeDetail(tradeId), [tradeId]);

  const runTradeAction = async (key, action, success) => {
    setMessage('');
    setErrorMessage('');
    setBusyKey(key);

    try {
      await action(tradeId);
      setMessage(success);
      await reload();
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setBusyKey('');
    }
  };

  const handleRefund = async (event) => {
    event.preventDefault();
    setMessage('');
    setErrorMessage('');
    setBusyKey('refund');

    try {
      const response = await refundPayment(paymentId, { reason: refundReason });
      setMessage('환불 요청이 처리됐어요.');
      setRefundStatus(response);
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setBusyKey('');
    }
  };

  const handleRefundStatus = async () => {
    setMessage('');
    setErrorMessage('');
    setBusyKey('refundStatus');

    try {
      const response = await getRefundStatus(paymentId);
      setRefundStatus(response);
      setMessage('환불 상태를 확인했어요.');
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setBusyKey('');
    }
  };

  if (loading) {
    return <LoadingState label="거래 불러오는 중" />;
  }

  if (error) {
    return <ErrorState title="거래를 불러오지 못했어요" message={error.message} onRetry={reload} />;
  }

  return (
    <section>
      <PageHeader
        title={`거래 #${tradeId}`}
        eyebrow="거래"
        action={
          <Button as={Link} to={`/checkout/${tradeId}`} variant="primary">
            결제
          </Button>
        }
      />
      {message ? <Alert variant="success">{message}</Alert> : null}
      {errorMessage ? <Alert variant="danger">{errorMessage}</Alert> : null}
      <Row className="g-3">
        <Col xs={12} lg={7}>
          <div className="detail-panel">
            <div className="d-flex justify-content-between align-items-start gap-2">
              <h2 className="section-title">거래 정보</h2>
              <StatusBadge status={trade.tradeStatus || trade.status} />
            </div>
            <MoneyText amount={trade.price} className="product-detail-price" />
            <dl className="compact-list mt-3">
              <div>
                <dt>상품</dt>
                <dd>{trade.productId}</dd>
              </div>
              <div>
                <dt>판매자</dt>
                <dd>{trade.sellerId}</dd>
              </div>
              <div>
                <dt>구매자</dt>
                <dd>{trade.buyerId}</dd>
              </div>
              <div>
                <dt>거래 상태</dt>
                <dd>{statusText(trade.tradeStatus || trade.status)}</dd>
              </div>
              <div>
                <dt>결제 상태</dt>
                <dd>{statusText(trade.paymentStatus)}</dd>
              </div>
              <div>
                <dt>정산 상태</dt>
                <dd>{statusText(trade.settlementStatus)}</dd>
              </div>
              <div>
                <dt>완료일</dt>
                <dd>{formatDateTime(trade.completedAt)}</dd>
              </div>
            </dl>
            <div className="form-actions">
              <Button
                variant="outline-primary"
                disabled={Boolean(busyKey)}
                onClick={() => runTradeAction('complete', completeTrade, '거래를 완료했어요.')}
              >
                {busyKey === 'complete' ? '처리 중' : '거래 완료'}
              </Button>
              <Button
                variant="outline-secondary"
                disabled={Boolean(busyKey)}
                onClick={() => runTradeAction('expire', expireReservation, '예약을 만료했어요.')}
              >
                {busyKey === 'expire' ? '처리 중' : '예약 만료'}
              </Button>
              <Button
                variant="outline-primary"
                disabled={Boolean(busyKey)}
                onClick={() => runTradeAction('rating', requestRatingMessage, '후기 요청 메시지를 보냈어요.')}
              >
                {busyKey === 'rating' ? '처리 중' : '후기 요청'}
              </Button>
              <Button as={Link} to={`/trades/${tradeId}/review`} variant="primary">
                후기 작성
              </Button>
            </div>
          </div>
        </Col>
        <Col xs={12} lg={5}>
          <div className="detail-panel">
            <h2 className="section-title">환불</h2>
            <Form onSubmit={handleRefund} className="stack-list">
              <Form.Group controlId="refundPaymentId">
                <Form.Label>결제 ID</Form.Label>
                <Form.Control
                  type="number"
                  min="1"
                  value={paymentId}
                  onChange={(event) => setPaymentId(event.target.value)}
                  required
                />
              </Form.Group>
              <Form.Group controlId="refundReason">
                <Form.Label>환불 사유</Form.Label>
                <Form.Control
                  value={refundReason}
                  onChange={(event) => setRefundReason(event.target.value)}
                  maxLength={500}
                  required
                />
              </Form.Group>
              <div className="form-actions">
                <Button
                  type="button"
                  variant="outline-primary"
                  disabled={!paymentId || Boolean(busyKey)}
                  onClick={handleRefundStatus}
                >
                  상태 조회
                </Button>
                <Button type="submit" disabled={Boolean(busyKey)}>
                  {busyKey === 'refund' ? '요청 중' : '환불 요청'}
                </Button>
              </div>
            </Form>
            {refundStatus ? (
              <dl className="compact-list mt-3">
                <div>
                  <dt>환불 상태</dt>
                  <dd>{statusText(refundStatus.status)}</dd>
                </div>
                <div>
                  <dt>환불일</dt>
                  <dd>{formatDateTime(refundStatus.refundedAt)}</dd>
                </div>
              </dl>
            ) : null}
          </div>
        </Col>
      </Row>
    </section>
  );
}
