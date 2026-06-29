import { useState } from 'react';
import { ButtonGroup, Card, ToggleButton } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import MoneyText from '../components/MoneyText.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getMyTrades } from '../api/mypageApi.js';
import { PageHeader, formatDateTime, getPageContent, roleText, statusText, useApiResource } from './pageUtils.jsx';

const roleOptions = [
  { value: 'all', label: '전체' },
  { value: 'buyer', label: '구매' },
  { value: 'seller', label: '판매' }
];

export default function MyTradesPage() {
  const [role, setRole] = useState('all');
  const { data, error, loading, reload } = useApiResource(() => getMyTrades({ role, page: 0, size: 20 }), [role]);
  const trades = getPageContent(data);

  return (
    <section>
      <PageHeader
        title="내 거래"
        eyebrow="마이페이지"
        action={
          <ButtonGroup aria-label="거래 역할">
            {roleOptions.map((option) => (
              <ToggleButton
                key={option.value}
                id={`trade-role-${option.value}`}
                type="radio"
                variant="outline-primary"
                name="trade-role"
                value={option.value}
                checked={role === option.value}
                onChange={(event) => setRole(event.currentTarget.value)}
              >
                {option.label}
              </ToggleButton>
            ))}
          </ButtonGroup>
        }
      />
      {loading ? <LoadingState label="거래 불러오는 중" /> : null}
      {error ? <ErrorState title="거래를 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && trades.length === 0 ? <EmptyState title="거래가 없어요" /> : null}
      {!loading && !error && trades.length > 0 ? (
        <div className="stack-list">
          {trades.map((trade) => (
            <Card key={trade.tradeId} className="list-card">
              <Card.Body>
                <div className="list-card-row">
                  <div>
                    <Card.Title as="h2">
                      <Link to={`/trades/${trade.tradeId}`}>{trade.productTitle || `거래 #${trade.tradeId}`}</Link>
                    </Card.Title>
                    <p className="text-muted mb-0">{trade.counterpartNickname || '상대 없음'}</p>
                  </div>
                  <MoneyText amount={trade.tradePrice ?? trade.productPrice} className="list-price" />
                </div>
                <dl className="compact-list compact-list-inline mt-3">
                  <div>
                    <dt>구분</dt>
                    <dd>{roleText(trade.role)}</dd>
                  </div>
                  <div>
                    <dt>상태</dt>
                    <dd>{statusText(trade.status)}</dd>
                  </div>
                  <div>
                    <dt>결제</dt>
                    <dd>{statusText(trade.paymentStatus)}</dd>
                  </div>
                  <div>
                    <dt>일시</dt>
                    <dd>{formatDateTime(trade.completedAt || trade.createdAt)}</dd>
                  </div>
                </dl>
              </Card.Body>
            </Card>
          ))}
        </div>
      ) : null}
    </section>
  );
}
