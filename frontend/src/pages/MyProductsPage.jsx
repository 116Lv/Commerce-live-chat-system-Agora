import { Button, Col, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import ProductCard from '../components/ProductCard.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getMyProducts } from '../api/productApi.js';
import { PageHeader, getPageContent, getProductId, useApiResource } from './pageUtils.jsx';

export default function MyProductsPage() {
  const { data, error, loading, reload } = useApiResource(() => getMyProducts(), []);
  const products = getPageContent(data);

  return (
    <section>
      <PageHeader
        title="내 상품"
        eyebrow="마이페이지"
        action={
          <Button as={Link} to="/sell" variant="primary">
            등록
          </Button>
        }
      />
      {loading ? <LoadingState label="내 상품 불러오는 중" /> : null}
      {error ? <ErrorState title="내 상품을 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && products.length === 0 ? <EmptyState title="등록한 상품이 없어요" /> : null}
      {!loading && !error && products.length > 0 ? (
        <Row className="g-3">
          {products.map((product) => (
            <Col key={getProductId(product)} xs={12} sm={6} lg={4} xl={3}>
              <ProductCard product={product} footerActionLabel="수정" footerActionTo={`/products/${getProductId(product)}/edit`} />
            </Col>
          ))}
        </Row>
      ) : null}
    </section>
  );
}
