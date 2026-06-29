import { useMemo, useState } from 'react';
import { Button, Col, Form, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { Search } from 'lucide-react';
import ProductCard from '../components/ProductCard.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getProducts, searchProducts } from '../api/productApi.js';
import { getRegions } from '../api/regionApi.js';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';

const DEFAULT_QUERY = { keyword: '', category: '', regionId: '', page: 0, size: 20 };

export default function ProductListPage() {
  const [draft, setDraft] = useState(DEFAULT_QUERY);
  const [query, setQuery] = useState(DEFAULT_QUERY);
  const params = useMemo(
    () => ({
      keyword: query.keyword.trim(),
      category: query.category.trim(),
      regionId: query.regionId || undefined,
      page: query.page,
      size: query.size
    }),
    [query]
  );

  const productsState = useApiResource(
    () => (params.keyword || params.category ? searchProducts(params) : getProducts(params)),
    [params]
  );
  const regionsState = useApiResource(() => getRegions(), []);
  const products = getPageContent(productsState.data);

  const handleSubmit = (event) => {
    event.preventDefault();
    setQuery({ ...draft, page: 0 });
  };

  return (
    <section>
      <PageHeader
        title="상품"
        eyebrow="마켓"
        action={
          <Button as={Link} to="/sell" variant="primary">
            판매하기
          </Button>
        }
      />

      <Form className="toolbar-panel mb-4" onSubmit={handleSubmit}>
        <Row className="g-2 align-items-end">
          <Col xs={12} lg={5}>
            <Form.Label>검색어</Form.Label>
            <Form.Control
              value={draft.keyword}
              onChange={(event) => setDraft((current) => ({ ...current, keyword: event.target.value }))}
              placeholder="상품명"
            />
          </Col>
          <Col xs={12} md={4} lg={3}>
            <Form.Label>카테고리</Form.Label>
            <Form.Control
              value={draft.category}
              onChange={(event) => setDraft((current) => ({ ...current, category: event.target.value }))}
              placeholder="예: 디지털"
            />
          </Col>
          <Col xs={12} md={4} lg={3}>
            <Form.Label>지역</Form.Label>
            <Form.Select
              value={draft.regionId}
              onChange={(event) => setDraft((current) => ({ ...current, regionId: event.target.value }))}
              disabled={regionsState.loading || Boolean(regionsState.error)}
            >
              <option value="">전체</option>
              {getPageContent(regionsState.data).map((region) => (
                <option key={region.regionId} value={region.regionId}>
                  {region.name}
                </option>
              ))}
            </Form.Select>
          </Col>
          <Col xs={12} md={4} lg={1}>
            <Button type="submit" className="w-100" aria-label="상품 검색">
              <Search size={17} aria-hidden="true" />
            </Button>
          </Col>
        </Row>
      </Form>

      {productsState.loading ? <LoadingState label="상품 불러오는 중" /> : null}
      {productsState.error ? (
        <ErrorState title="상품을 불러오지 못했어요" message={productsState.error.message} onRetry={productsState.reload} />
      ) : null}
      {!productsState.loading && !productsState.error && products.length === 0 ? <EmptyState title="상품이 없어요" /> : null}
      {!productsState.loading && !productsState.error && products.length > 0 ? (
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
