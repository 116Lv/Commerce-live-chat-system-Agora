import { Col, Row } from 'react-bootstrap';
import ProductCard from '../components/ProductCard.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getMyLikedProducts } from '../api/productApi.js';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';

export default function LikedProductsPage() {
  const { data, error, loading, reload } = useApiResource(() => getMyLikedProducts(), []);
  const products = getPageContent(data);

  return (
    <section>
      <PageHeader title="관심 상품" eyebrow="마이페이지" />
      {loading ? <LoadingState label="관심 상품 불러오는 중" /> : null}
      {error ? <ErrorState title="관심 상품을 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && products.length === 0 ? <EmptyState title="관심 상품이 없어요" /> : null}
      {!loading && !error && products.length > 0 ? (
        <Row className="g-3">
          {products.map((product) => (
            <Col key={product.productId ?? product.id} xs={12} sm={6} lg={4} xl={3}>
              <ProductCard product={product} />
            </Col>
          ))}
        </Row>
      ) : null}
    </section>
  );
}
