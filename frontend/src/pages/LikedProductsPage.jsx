import { useState } from 'react';
import { Alert, Button, Col, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import ProductCard from '../components/ProductCard.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getMyLikedProducts, unlikeProduct } from '../api/productApi.js';
import { PageHeader, getPageContent, getProductId, useApiResource } from './pageUtils.jsx';

export default function LikedProductsPage() {
  const { data, error, loading, reload } = useApiResource(() => getMyLikedProducts(), []);
  const [removedProductIds, setRemovedProductIds] = useState(() => new Set());
  const [actionError, setActionError] = useState('');
  const products = getPageContent(data).filter((product) => !removedProductIds.has(String(getProductId(product))));

  const handleRemoveLike = async (product) => {
    const productId = getProductId(product);
    if (!productId) {
      return;
    }

    const normalizedId = String(productId);
    setActionError('');
    setRemovedProductIds((current) => new Set(current).add(normalizedId));

    try {
      await unlikeProduct(productId);
    } catch (err) {
      setRemovedProductIds((current) => {
        const next = new Set(current);
        next.delete(normalizedId);
        return next;
      });
      setActionError(err.message);
    }
  };

  return (
    <section>
      <PageHeader title="관심 상품" eyebrow="마이페이지" />
      {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
      {loading ? <LoadingState label="관심 상품 불러오는 중" /> : null}
      {error ? <ErrorState title="관심 상품을 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && products.length === 0 ? (
        <div className="empty-state-with-action">
          <EmptyState title="관심 상품이 없어요" message="상품 목록에서 마음에 드는 상품을 찾아보세요." />
          <Button as={Link} to="/products" variant="primary">
            상품 보러가기
          </Button>
        </div>
      ) : null}
      {!loading && !error && products.length > 0 ? (
        <Row className="g-3">
          {products.map((product) => (
            <Col key={getProductId(product)} xs={12} sm={6} lg={4} xl={3}>
              <ProductCard
                product={{ ...product, liked: true }}
                footerActionLabel="관심 해제"
                footerActionOnClick={() => handleRemoveLike(product)}
                footerActionVariant="outline-danger"
              />
            </Col>
          ))}
        </Row>
      ) : null}
    </section>
  );
}
