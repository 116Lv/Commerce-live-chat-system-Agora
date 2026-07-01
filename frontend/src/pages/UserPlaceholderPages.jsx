import { useMemo, useState } from 'react';
import { Alert, Button, Col, Form, Row } from 'react-bootstrap';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Search } from 'lucide-react';
import EmptyState from '../components/EmptyState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ProductCard from '../components/ProductCard.jsx';
import { getProducts, likeProduct, searchProducts, unlikeProduct } from '../api/productApi.js';
import { getRegions, updatePreferredRegions } from '../api/regionApi.js';
import { useAuth } from '../auth/AuthContext.jsx';
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
  const navigate = useNavigate();
  const location = useLocation();
  const { isUserAuthenticated } = useAuth();
  const [draft, setDraft] = useState(HOME_QUERY);
  const [query, setQuery] = useState(HOME_QUERY);
  const [selectedParentRegionId, setSelectedParentRegionId] = useState('');
  const [productOverrides, setProductOverrides] = useState({});
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
  const products = getPageContent(productsState.data).map((product) => {
    const productId = product.productId ?? product.id;
    return productOverrides[productId] ? { ...product, ...productOverrides[productId] } : product;
  });
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

  const handleProductLikeToggle = async (product) => {
    const productId = product.productId ?? product.id;
    if (!productId) {
      return;
    }
    if (!isUserAuthenticated) {
      navigate('/login', { state: { from: location } });
      return;
    }

    const liked = Boolean(product.liked);
    const likeCount = product.likeCount ?? 0;
    const optimistic = { liked: !liked, likeCount: liked ? Math.max(0, likeCount - 1) : likeCount + 1 };
    setProductOverrides((current) => ({ ...current, [productId]: optimistic }));

    try {
      const response = await (liked ? unlikeProduct(productId) : likeProduct(productId));
      setProductOverrides((current) => ({
        ...current,
        [productId]: {
          liked: Boolean(response.liked),
          likeCount: response.likeCount ?? optimistic.likeCount
        }
      }));
    } catch {
      setProductOverrides((current) => ({ ...current, [productId]: { liked, likeCount } }));
    }
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
                  <ProductCard product={product} onLikeToggle={handleProductLikeToggle} />
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
  const navigate = useNavigate();
  const regionsState = useApiResource(() => getRegions(), []);
  const regions = getPageContent(regionsState.data);
  const regionOptions = getRegionSelectOptions(regions);
  const [selectedRegionIds, setSelectedRegionIds] = useState([]);
  const [primaryRegionId, setPrimaryRegionId] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [actionError, setActionError] = useState('');
  const [saving, setSaving] = useState(false);

  const toggleRegion = (regionId) => {
    setActionError('');
    setSelectedRegionIds((current) => {
      if (current.includes(regionId)) {
        const next = current.filter((id) => id !== regionId);
        if (primaryRegionId === regionId) {
          setPrimaryRegionId(next[0] || '');
        }
        return next;
      }

      if (current.length >= 5) {
        setActionError('관심 지역은 최대 5개까지 선택할 수 있습니다.');
        return current;
      }

      const next = [...current, regionId];
      if (!primaryRegionId) {
        setPrimaryRegionId(regionId);
      }
      return next;
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setActionMessage('');
    setActionError('');

    if (selectedRegionIds.length < 3 || selectedRegionIds.length > 5) {
      setActionError('관심 지역은 3개 이상 5개 이하로 선택해 주세요.');
      return;
    }

    if (!primaryRegionId || !selectedRegionIds.includes(primaryRegionId)) {
      setActionError('대표 관심 지역을 선택해 주세요.');
      return;
    }

    setSaving(true);
    try {
      await updatePreferredRegions({
        regionIds: selectedRegionIds.map(Number),
        primaryRegionId: Number(primaryRegionId)
      });
      setActionMessage('관심 지역이 저장되었습니다.');
      navigate('/', { replace: true });
    } catch (err) {
      setActionError(err.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <section>
      <PageHeader title="관심 지역 설정" eyebrow="계정" />
      {actionMessage ? <Alert variant="success">{actionMessage}</Alert> : null}
      {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
      {regionsState.loading ? <LoadingState label="지역 불러오는 중" /> : null}
      {regionsState.error ? (
        <ErrorState title="지역을 불러오지 못했어요" message={regionsState.error.message} onRetry={regionsState.reload} />
      ) : null}
      {!regionsState.loading && !regionsState.error && regionOptions.length === 0 ? (
        <EmptyState title="선택할 수 있는 지역이 없어요" />
      ) : null}
      {!regionsState.loading && !regionsState.error && regionOptions.length > 0 ? (
        <Form className="detail-panel stack-list" onSubmit={handleSubmit}>
          <p className="text-muted mb-0">관심 지역은 3개 이상 5개 이하로 선택해 주세요.</p>
          <div className="stack-list">
            {regionOptions.map((region) => {
              const checked = selectedRegionIds.includes(region.value);
              return (
                <div className="d-flex align-items-center justify-content-between gap-3" key={region.value}>
                  <Form.Check
                    type="checkbox"
                    id={`preferred-region-${region.value}`}
                    label={region.label}
                    checked={checked}
                    onChange={() => toggleRegion(region.value)}
                  />
                  <Form.Check
                    type="radio"
                    id={`primary-region-${region.value}`}
                    name="primaryRegionId"
                    label="대표"
                    value={region.value}
                    checked={primaryRegionId === region.value}
                    disabled={!checked}
                    onChange={(event) => setPrimaryRegionId(event.target.value)}
                  />
                </div>
              );
            })}
          </div>
          <div className="d-flex justify-content-end gap-2">
            <Button type="button" variant="outline-secondary" onClick={() => navigate('/')}>
              나중에 설정
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? '저장 중...' : '관심 지역 저장'}
            </Button>
          </div>
        </Form>
      ) : null}
    </section>
  );
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
