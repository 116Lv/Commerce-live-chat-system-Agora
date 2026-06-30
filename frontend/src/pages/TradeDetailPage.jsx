import { useEffect, useState } from 'react';
import { Alert, Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import { completeTrade, getTradeDetail } from '../api/tradeApi.js';
import { getSmileScore } from '../api/mypageApi.js';
import { getUserToken } from '../auth/tokenStorage.js';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { PageHeader, formatDateTime, statusText, useApiResource } from './pageUtils.jsx';

const decodeBase64Url = (value) => {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');

  return globalThis.atob(padded);
};

const getCurrentUserIdFromToken = () => {
  try {
    const token = getUserToken()?.replace(/^Bearer\s+/i, '');
    const payload = token ? JSON.parse(decodeBase64Url(token.split('.')[1] || '')) : null;
    const userId = payload?.userId ?? payload?.id ?? payload?.sub;

    return userId == null ? null : String(userId);
  } catch {
    return null;
  }
};

export default function TradeDetailPage() {
  const { tradeId } = useParams();
  const [message, setMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [busyKey, setBusyKey] = useState('');
  const [counterpartSmileScore, setCounterpartSmileScore] = useState(null);
  const { data: trade, error, loading, reload } = useApiResource(() => getTradeDetail(tradeId), [tradeId]);
  const currentUserId = getCurrentUserIdFromToken();
  const tradeStatus = String(trade?.tradeStatus || trade?.status || '').toUpperCase();
  const paymentStatus = String(trade?.paymentStatus || '').toUpperCase();
  const isCurrentUserBuyer = currentUserId != null && String(trade?.buyerId) === currentUserId;
  const isCurrentUserParticipant =
    currentUserId != null && [trade?.buyerId, trade?.sellerId].some((participantId) => String(participantId) === currentUserId);
  const counterpartUserId = isCurrentUserBuyer ? trade?.sellerId : trade?.buyerId;
  const canCheckout = isCurrentUserBuyer && tradeStatus === 'PAYMENT_PENDING';
  const canCompleteTrade = isCurrentUserBuyer && tradeStatus === 'PAID';
  const canReviewTrade = isCurrentUserBuyer && tradeStatus === 'COMPLETED';
  const canRequestRefund = isCurrentUserBuyer && paymentStatus === 'PAID';

  useEffect(() => {
    let disposed = false;

    const loadCounterpartSmileScore = async () => {
      if (!counterpartUserId || !isCurrentUserParticipant) {
        setCounterpartSmileScore(null);
        return;
      }

      try {
        const response = await getSmileScore(counterpartUserId);
        if (!disposed) {
          setCounterpartSmileScore(response.smileScore);
        }
      } catch {
        if (!disposed) {
          setCounterpartSmileScore(null);
        }
      }
    };

    loadCounterpartSmileScore();

    return () => {
      disposed = true;
    };
  }, [counterpartUserId, isCurrentUserParticipant]);

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

  if (loading) {
    return <LoadingState label="거래 불러오는 중" />;
  }

  if (error) {
    return <ErrorState title="거래를 불러오지 못했습니다" message={error.message} onRetry={reload} />;
  }

  return (
    <section>
      <PageHeader
        title={`거래 #${tradeId}`}
        eyebrow="거래"
        action={
          canCheckout ? (
            <Button as={Link} to={`/checkout/${tradeId}`} variant="primary">
              결제
            </Button>
          ) : null
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
                <dt>상대 스마일</dt>
                <dd>{counterpartSmileScore == null ? '-' : String(counterpartSmileScore) + '점'}</dd>
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
            {canCompleteTrade || canReviewTrade ? (
              <div className="form-actions">
                {canCompleteTrade ? (
                  <Button
                    variant="outline-primary"
                    disabled={Boolean(busyKey)}
                    onClick={() => runTradeAction('complete', completeTrade, '거래를 완료했습니다.')}
                  >
                    {busyKey === 'complete' ? '처리 중' : '거래 완료'}
                  </Button>
                ) : null}
                {canReviewTrade ? (
                  <Button as={Link} to={`/trades/${tradeId}/review`} variant="primary">
                    후기 작성
                  </Button>
                ) : null}
              </div>
            ) : null}
          </div>
        </Col>
        {canRequestRefund ? (
          <Col xs={12} lg={5}>
            <div className="detail-panel">
              <h2 className="section-title">환불 안내</h2>
              <p className="text-muted mb-0">
                환불은 관리자 승인 및 결제사 검증 이후 처리됩니다. 환불이 필요하면 관리자에게 문의해 주세요.
              </p>
            </div>
          </Col>
        ) : null}
      </Row>
    </section>
  );
}
