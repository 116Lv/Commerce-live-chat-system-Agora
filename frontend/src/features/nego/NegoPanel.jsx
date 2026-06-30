import { useEffect, useMemo, useState } from 'react';
import { Alert, Button, ButtonGroup, Form } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import {
  acceptOffer,
  approveOfferExtension,
  createOffer,
  getCurrentOffer,
  rejectOffer,
  rejectOfferExtension,
  requestOfferExtension
} from '../../api/negoApi.js';
import { getUserToken } from '../../auth/tokenStorage.js';
import MoneyText from '../../components/MoneyText.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { formatDateTime } from '../../pages/pageUtils.jsx';
import {
  buildPaymentHref,
  getOfferActions,
  getOfferPriceValidation,
  getRemainingTimeLabel,
  inferOfferRole,
  parseOfferPriceInput
} from './negoPanelUtils.js';

const actionHandlers = {
  accept: acceptOffer,
  reject: rejectOffer,
  extension: requestOfferExtension,
  extensionApprove: approveOfferExtension,
  extensionReject: rejectOfferExtension
};

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

export default function NegoPanel({ chatRoomId, onOfferChange, productPrice }) {
  const [offerPrice, setOfferPrice] = useState('');
  const [offer, setOffer] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [busyKey, setBusyKey] = useState('');
  const currentUserId = useMemo(() => getCurrentUserIdFromToken(), []);
  const offerRole = inferOfferRole(offer, currentUserId);
  const offerActions = getOfferActions(offer, { role: offerRole });
  const paymentHref = buildPaymentHref(offer);
  const remainingTimeLabel = getRemainingTimeLabel(offer?.expiresAt);

  useEffect(() => {
    let disposed = false;

    const loadCurrentOffer = async () => {
      setError('');

      try {
        const response = await getCurrentOffer(chatRoomId);
        if (!disposed) {
          setOffer(response || null);
          onOfferChange?.(response || null);
        }
      } catch (err) {
        if (!disposed && err.status !== 404) {
          setError(err.message);
        }
      }
    };

    if (chatRoomId) {
      loadCurrentOffer();
    }

    return () => {
      disposed = true;
    };
  }, [chatRoomId, onOfferChange]);

  const updateOffer = (nextOffer, nextMessage) => {
    setOffer(nextOffer);
    setMessage(nextMessage);
    onOfferChange?.(nextOffer);
  };

  const handleCreate = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');

    const validationMessage = getOfferPriceValidation(offerPrice, { productPrice });
    if (validationMessage) {
      setError(validationMessage);
      return;
    }

    setBusyKey('create');

    try {
      const response = await createOffer(chatRoomId, { offerPrice: parseOfferPriceInput(offerPrice) });
      setOfferPrice('');
      updateOffer(response, '가격 제안을 보냈어요.');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyKey('');
    }
  };

  const runAction = async (targetOffer, action) => {
    if (!targetOffer?.offerId) {
      setError('처리할 제안 정보를 찾을 수 없어요.');
      return;
    }

    setError('');
    setMessage('');
    setBusyKey(action.key);

    try {
      const response = await actionHandlers[action.key](targetOffer.offerId);
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
        <div className="nego-offer-summary mt-3" data-offer-id={offer.offerId}>
          <div className="d-flex justify-content-between align-items-start gap-2">
            <div>
              <strong>제안 #{offer.offerId}</strong>
              <p className="mb-0 text-muted small">
                만료 {formatDateTime(offer.expiresAt)}
                {remainingTimeLabel ? ` · ${remainingTimeLabel}` : ''}
              </p>
            </div>
            <StatusBadge status={offer.status} />
          </div>
          <MoneyText amount={offer.offerPrice} className="list-price mt-2" />
          {paymentHref ? (
            <div className="nego-payment-cta mt-3">
              <p className="mb-2 text-muted small">
                제안이 수락됐어요. 결제 화면에서 주문 정보를 확인하고 결제를 이어가세요.
              </p>
              <Button as={Link} to={paymentHref} variant="primary">
                결제하기
              </Button>
            </div>
          ) : null}
          {offerActions.length > 0 ? (
            <ButtonGroup className="nego-actions mt-3" aria-label="가격 제안 처리">
              {offerActions.map((action) => (
                <Button
                  key={action.key}
                  type="button"
                  variant={action.variant}
                  disabled={Boolean(busyKey)}
                  onClick={() => runAction(offer, action)}
                >
                  {busyKey === action.key ? '처리 중' : action.label}
                </Button>
              ))}
            </ButtonGroup>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
