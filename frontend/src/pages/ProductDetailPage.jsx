import { useState } from 'react';
import { Alert, Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import { Heart } from 'lucide-react';
import MoneyText from '../components/MoneyText.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getProduct, likeProduct } from '../api/productApi.js';
import { PageHeader, getProductId, statusText, useApiResource } from './pageUtils.jsx';

export default function ProductDetailPage() {
  const { productId } = useParams();
  const [actionMessage, setActionMessage] = useState('');
  const [actionError, setActionError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { data: product, error, loading, reload } = useApiResource(() => getProduct(productId), [productId]);

  const handleLike = async () => {
    setSubmitting(true);
    setActionMessage('');
    setActionError('');

    try {
      await likeProduct(productId);
      setActionMessage('관심 상품에 추가했어요.');
      await reload();
    } catch (err) {
      setActionError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <LoadingState label="상품 불러오는 중" />;
  }

  if (error) {
    return <ErrorState title="상품을 불러오지 못했어요" message={error.message} onRetry={reload} />;
  }

  if (!product) {
    return <EmptyState title="상품이 없어요" />;
  }

  return (
    <section>
      <PageHeader
        title={product.title || '상품'}
        eyebrow="상품 상세"
        action={
          <Button as={Link} to={`/products/${getProductId(product)}/edit`} variant="outline-primary">
            수정
          </Button>
        }
      />
      {actionMessage ? <Alert variant="success">{actionMessage}</Alert> : null}
      {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
      <Row className="g-4">
        <Col xs={12} lg={7}>
          <div className="detail-panel product-detail-media">
            {product.imageUrl ? <img src={product.imageUrl} alt={`${product.title} 이미지`} /> : <span>이미지 없음</span>}
          </div>
        </Col>
        <Col xs={12} lg={5}>
          <div className="detail-panel product-detail-info">
            <div className="d-flex justify-content-between gap-2 align-items-start">
              <h2>{product.title}</h2>
              <StatusBadge status={product.status} />
            </div>
            <MoneyText amount={product.price} className="product-detail-price" />
            <dl className="compact-list">
              <div>
                <dt>상태</dt>
                <dd>{statusText(product.status)}</dd>
              </div>
              <div>
                <dt>카테고리</dt>
                <dd>{product.category || '-'}</dd>
              </div>
              <div>
                <dt>지역</dt>
                <dd>{product.regionName || product.region || '-'}</dd>
              </div>
            </dl>
            <Button onClick={handleLike} disabled={submitting} variant="primary">
              <Heart size={17} aria-hidden="true" /> 관심
            </Button>
          </div>
        </Col>
      </Row>
      <div className="detail-panel mt-4">
        <h2>설명</h2>
        <p className="mb-0 pre-line">{product.description || '설명이 없어요.'}</p>
      </div>
    </section>
  );
}
