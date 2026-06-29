import { useState } from 'react';
import { Alert, Button, ButtonGroup, Form } from 'react-bootstrap';
import {
  acceptOffer,
  approveOfferExtension,
  createOffer,
  expireOffer,
  rejectOffer,
  rejectOfferExtension,
  requestOfferExtension
} from '../../api/negoApi.js';
import MoneyText from '../../components/MoneyText.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { formatDateTime } from '../../pages/pageUtils.jsx';

const actions = [
  { key: 'accept', label: '수락', handler: acceptOffer },
  { key: 'reject', label: '거절', handler: rejectOffer },
  { key: 'extension', label: '연장 요청', handler: requestOfferExtension },
  { key: 'extensionApprove', label: '연장 승인', handler: approveOfferExtension },
  { key: 'extensionReject', label: '연장 거절', handler: rejectOfferExtension },
  { key: 'expire', label: '만료', handler: expireOffer }
];

export default function NegoPanel({ chatRoomId, onOfferChange }) {
  const [offerPrice, setOfferPrice] = useState('');
  const [offerId, setOfferId] = useState('');
  const [offer, setOffer] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [busyKey, setBusyKey] = useState('');

  const updateOffer = (nextOffer, nextMessage) => {
    setOffer(nextOffer);
    setOfferId(nextOffer?.offerId ? String(nextOffer.offerId) : offerId);
    setMessage(nextMessage);
    onOfferChange?.(nextOffer);
  };

  const handleCreate = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    setBusyKey('create');

    try {
      const response = await createOffer(chatRoomId, { offerPrice: Number(offerPrice) });
      setOfferPrice('');
      updateOffer(response, '가격 제안을 보냈어요.');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyKey('');
    }
  };

  const runAction = async (action) => {
    if (!offerId.trim()) {
      setError('제안 ID를 입력해 주세요.');
      return;
    }

    setError('');
    setMessage('');
    setBusyKey(action.key);

    try {
      const response = await action.handler(offerId.trim());
      updateOffer(response, `${action.label} 처리했어요.`);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyKey('');
    }
  };

  return (
    <div className="detail-panel">
      <h2 className="section-title">가격 제안</h2>
      {message ? <Alert variant="success">{message}</Alert> : null}
      {error ? <Alert variant="danger">{error}</Alert> : null}
      <Form onSubmit={handleCreate} className="stack-list">
        <Form.Group controlId="offerPrice">
          <Form.Label>제안가</Form.Label>
          <Form.Control
            type="number"
            min="1"
            value={offerPrice}
            onChange={(event) => setOfferPrice(event.target.value)}
            placeholder="금액 입력"
            required
          />
        </Form.Group>
        <Button type="submit" disabled={busyKey === 'create'}>
          제안 보내기
        </Button>
      </Form>

      {offer ? (
        <div className="nego-offer-summary mt-3">
          <div className="d-flex justify-content-between align-items-start gap-2">
            <div>
              <strong>제안 #{offer.offerId}</strong>
              <p className="mb-0 text-muted small">만료 {formatDateTime(offer.expiresAt)}</p>
            </div>
            <StatusBadge status={offer.status} />
          </div>
          <MoneyText amount={offer.offerPrice} className="list-price mt-2" />
        </div>
      ) : null}

      <Form.Group controlId="offerId" className="mt-3">
        <Form.Label>처리할 제안 ID</Form.Label>
        <Form.Control value={offerId} onChange={(event) => setOfferId(event.target.value)} placeholder="offerId" />
      </Form.Group>
      <ButtonGroup className="nego-actions mt-2" aria-label="가격 제안 처리">
        {actions.map((action) => (
          <Button
            key={action.key}
            type="button"
            variant="outline-primary"
            disabled={!offerId.trim() || Boolean(busyKey)}
            onClick={() => runAction(action)}
          >
            {busyKey === action.key ? '처리 중' : action.label}
          </Button>
        ))}
      </ButtonGroup>
    </div>
  );
}
