import { useMemo, useState } from 'react';
import { Alert, Button, Col, Form, Row } from 'react-bootstrap';
import { Link, useLocation } from 'react-router-dom';
import { Search } from 'lucide-react';
import EmptyState from '../components/EmptyState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ProductCard from '../components/ProductCard.jsx';
import { getProducts, searchProducts } from '../api/productApi.js';
import { getRegions } from '../api/regionApi.js';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';
import { PRODUCT_CATEGORIES, getChildRegionOptions, getRegionSelectOptions } from './productFormUtils.js';

const HOME_QUERY = { keyword: '', category: '', regionId: '', page: 0, size: 20 };

function PlaceholderPage({ title, eyebrow, emptyTitle = '표시할 항목이 없어요', action }) {
  return (
    <section>
      <PageHeader title={title} eyebrow={eyebrow} action={action} />
      <EmptyState title={emptyTitle} />
    </section>
  );
}

export function HomePage() {
  const location = useLocation();
  const [draft, setDraft] = useState(HOME_QUERY);
  const [query, setQuery] = useState(HOME_QUERY);
  const [selectedParentRegionId, setSelectedParentRegionId] = useState('');
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
  const regions = getPageContent(regionsState.data);
  const parentRegionOptions = getRegionSelectOptions(regions);
  const childRegionOptions = getChildRegionOptions(regions, selectedParentRegionId);
  const hasChildRegionOptions = childRegionOptions.length > 0;
  const productRegistrationMessage = location.state?.productRegistrationMessage;

  const handleSubmit = (event) => {
    event.preventDefault();
    setQuery({ ...draft, page: 0 });
  };

  const handleReset = () => {
    setDraft(HOME_QUERY);
    setQuery(HOME_QUERY);
    setSelectedParentRegionId('');
  };

  const handleParentRegionChange = (event) => {
    const value = event.target.value;
    const nextChildren = getChildRegionOptions(regions, value);
    setSelectedParentRegionId(value);
    setDraft((current) => ({ ...current, regionId: nextChildren.length > 0 ? '' : value }));
  };

  return (
    <section>
      <PageHeader
        title="상품 둘러보기"
        eyebrow="Agora 마켓"
        action={
          <Button as={Link} to="/products" variant="outline-primary">
            전체 상품
          </Button>
        }
      />
      {productRegistrationMessage ? <Alert variant="success">{productRegistrationMessage}</Alert> : null}

      <Row className="g-4 marketplace-home-grid">
        <Col xs={12} lg={3}>
          <aside className="filter-sidebar" aria-label="검색 조건">
            <h2>검색 조건</h2>
            <Form className="stack-list" onSubmit={handleSubmit}>
              <Form.Group controlId="home-keyword">
                <Form.Label>검색어</Form.Label>
                <Form.Control
                  value={draft.keyword}
                  onChange={(event) => setDraft((current) => ({ ...current, keyword: event.target.value }))}
                  placeholder="상품명 입력"
                />
              </Form.Group>
              <Form.Group controlId="home-category">
                <Form.Label>카테고리</Form.Label>
                <Form.Select
                  value={draft.category}
                  onChange={(event) => setDraft((current) => ({ ...current, category: event.target.value }))}
                >
                  <option value="">전체 카테고리</option>
                  {PRODUCT_CATEGORIES.map((category) => (
                    <option key={category.value} value={category.value}>
                      {category.label}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
              <Form.Group controlId="home-region">
                <Form.Label>지역</Form.Label>
                <Form.Select
                  value={selectedParentRegionId}
                  onChange={handleParentRegionChange}
                  disabled={regionsState.loading || Boolean(regionsState.error)}
                >
                  <option value="">전체 지역</option>
                  {parentRegionOptions.map((region) => (
                    <option key={region.value} value={region.value}>
                      {region.label}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
              {hasChildRegionOptions ? (
                <Form.Group controlId="home-child-region">
                  <Form.Label>세부 지역</Form.Label>
                  <Form.Select
                    value={draft.regionId}
                    onChange={(event) => setDraft((current) => ({ ...current, regionId: event.target.value }))}
                    disabled={regionsState.loading || Boolean(regionsState.error)}
                  >
                    <option value="">전체 세부 지역</option>
                    {childRegionOptions.map((region) => (
                      <option key={region.value} value={region.value}>
                        {region.label}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              ) : null}
              <div className="d-grid gap-2">
                <Button type="submit">
                  <Search size={17} aria-hidden="true" />
                  검색
                </Button>
                <Button type="button" variant="outline-secondary" onClick={handleReset}>
                  초기화
                </Button>
              </div>
            </Form>
          </aside>
        </Col>
        <Col xs={12} lg={9}>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h2 className="section-title mb-0">상품 그리드</h2>
            <Button as={Link} to="/sell" variant="primary">
              판매하기
            </Button>
          </div>
          {productsState.loading ? <LoadingState label="상품 불러오는 중" /> : null}
          {productsState.error ? (
            <ErrorState title="상품을 불러오지 못했어요" message={productsState.error.message} onRetry={productsState.reload} />
          ) : null}
          {!productsState.loading && !productsState.error && products.length === 0 ? (
            <EmptyState title="등록된 상품이 없어요" />
          ) : null}
          {!productsState.loading && !productsState.error && products.length > 0 ? (
            <Row className="g-3" aria-label="상품 그리드">
              {products.map((product) => (
                <Col key={product.productId ?? product.id} xs={12} sm={6} xl={4}>
                  <ProductCard product={product} />
                </Col>
              ))}
            </Row>
          ) : null}
        </Col>
      </Row>
    </section>
  );
}

export function RegionSetupPage() {
  return <PlaceholderPage title="관심 지역 설정" eyebrow="계정" emptyTitle="선택된 관심 지역이 없어요" />;
}

export function NotFoundPage() {
  return (
    <PlaceholderPage
      title="페이지를 찾을 수 없어요"
      eyebrow="Agora"
      emptyTitle="존재하지 않는 페이지예요"
      action={
        <Button as={Link} to="/" variant="outline-primary">
          홈으로
        </Button>
      }
    />
  );
}
