import { useMemo, useState } from 'react';
import { Button, Col, Form, Row } from 'react-bootstrap';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ArrowDownWideNarrow, ArrowUpWideNarrow, Search } from 'lucide-react';
import ProductCard from '../components/ProductCard.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import PopularKeywords from '../components/PopularKeywords.jsx';
import { getProducts, likeProduct, searchProducts, unlikeProduct } from '../api/productApi.js';
import { getRegions } from '../api/regionApi.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';
import { PRODUCT_CATEGORIES, getChildRegionOptions, getRegionSelectOptions } from './productFormUtils.js';

const SORT_OPTIONS = [
  { value: 'recent', label: '최신순' },
  { value: 'likes', label: '찜순' }
];

const PRODUCT_STATUS_OPTIONS = [
  { value: '', label: '전체' },
  { value: 'SELLING', label: '판매중' },
  { value: 'SOLD', label: '판매완료' }
];

const DEFAULT_QUERY = {
  keyword: '',
  category: '',
  regionId: '',
  status: '',
  page: 0,
  size: 20,
  sort: 'recent',
  direction: 'desc'
};

export default function ProductListPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isUserAuthenticated } = useAuth();
  const [draft, setDraft] = useState(DEFAULT_QUERY);
  const [query, setQuery] = useState(DEFAULT_QUERY);
  const [selectedParentRegionId, setSelectedParentRegionId] = useState('');
  const [productOverrides, setProductOverrides] = useState({});
  const params = useMemo(
    () => ({
      keyword: query.keyword.trim(),
      category: query.category.trim(),
      regionId: query.regionId || undefined,
      status: query.status,
      page: query.page,
      size: query.size,
      sort: query.sort,
      direction: query.direction
    }),
    [query]
  );

  const productsState = useApiResource(() => searchProducts(params), [params]);
  const regionsState = useApiResource(() => getRegions(), []);
  const products = getPageContent(productsState.data).map((product) => {
    const productId = product.productId ?? product.id;
    return productOverrides[productId] ? { ...product, ...productOverrides[productId] } : product;
  });
  const regions = getPageContent(regionsState.data);
  const parentRegionOptions = getRegionSelectOptions(regions);
  const childRegionOptions = getChildRegionOptions(regions, selectedParentRegionId);
  const hasChildRegionOptions = childRegionOptions.length > 0;

  const handleSubmit = (event) => {
    event.preventDefault();
    setQuery({ ...draft, page: 0 });
  };

  const handleSortChange = (event) => {
    const sort = event.target.value;
    setDraft((current) => ({ ...current, sort }));
    setQuery((current) => ({ ...current, sort, page: 0 }));
  };

  const handleStatusChange = (event) => {
    const status = event.target.value;
    setDraft((current) => ({ ...current, status }));
    setQuery((current) => ({ ...current, status, page: 0 }));
  };

  const handleDirectionToggle = () => {
    const direction = query.direction === 'asc' ? 'desc' : 'asc';
    setDraft((current) => ({ ...current, direction }));
    setQuery((current) => ({ ...current, direction, page: 0 }));
  };

  const handleKeywordSelect = (keyword) => {
    setDraft((current) => ({ ...current, keyword }));
    setQuery((current) => ({ ...current, keyword, page: 0 }));
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
        title="상품"
        eyebrow="마켓"
        action={
          <Button as={Link} to="/sell" variant="primary">
            판매하기
          </Button>
        }
      />

      <PopularKeywords onKeywordSelect={handleKeywordSelect} />

      <Form className="toolbar-panel mb-4" onSubmit={handleSubmit}>
        <Row className="g-2 align-items-end">
          <Col xs={12} lg={4}>
            <Form.Label>검색어</Form.Label>
            <Form.Control
              value={draft.keyword}
              onChange={(event) => setDraft((current) => ({ ...current, keyword: event.target.value }))}
              placeholder="상품명"
            />
          </Col>
          <Col xs={12} md={4} lg={2}>
            <Form.Label>카테고리</Form.Label>
            <Form.Select
              value={draft.category}
              onChange={(event) => setDraft((current) => ({ ...current, category: event.target.value }))}
            >
              <option value="">전체</option>
              {PRODUCT_CATEGORIES.map((category) => (
                <option key={category.value} value={category.value}>
                  {category.label}
                </option>
              ))}
            </Form.Select>
          </Col>
          <Col xs={12} md={4} lg={2}>
            <Form.Label>판매 상태</Form.Label>
            <Form.Select value={draft.status} onChange={handleStatusChange}>
              {PRODUCT_STATUS_OPTIONS.map((status) => (
                <option key={status.value || 'all'} value={status.value}>
                  {status.label}
                </option>
              ))}
            </Form.Select>
          </Col>
          <Col xs={12} md={4} lg={hasChildRegionOptions ? 2 : 3}>
            <Form.Label>지역</Form.Label>
            <Form.Select
              value={selectedParentRegionId}
              onChange={handleParentRegionChange}
              disabled={regionsState.loading || Boolean(regionsState.error)}
            >
              <option value="">전체</option>
              {parentRegionOptions.map((region) => (
                <option key={region.value} value={region.value}>
                  {region.label}
                </option>
              ))}
            </Form.Select>
          </Col>
          {hasChildRegionOptions ? (
            <Col xs={12} md={4} lg={2}>
              <Form.Label>세부 지역</Form.Label>
              <Form.Select
                value={draft.regionId}
                onChange={(event) => setDraft((current) => ({ ...current, regionId: event.target.value }))}
                disabled={regionsState.loading || Boolean(regionsState.error)}
              >
                <option value="">전체</option>
                {childRegionOptions.map((region) => (
                  <option key={region.value} value={region.value}>
                    {region.label}
                  </option>
                ))}
              </Form.Select>
            </Col>
          ) : null}
          <Col xs={12} md={4} lg={1}>
            <Button type="submit" className="w-100" aria-label="상품 검색">
              <Search size={17} aria-hidden="true" />
            </Button>
          </Col>
          <Col xs={12} md={4} lg={2}>
            <Form.Label>정렬</Form.Label>
            <Form.Select value={draft.sort} onChange={handleSortChange}>
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </Form.Select>
          </Col>
          <Col xs={12} md={4} lg={1}>
            <Button
              variant="outline-secondary"
              className="w-100"
              onClick={handleDirectionToggle}
              aria-label={query.direction === 'asc' ? '오름차순 정렬중, 내림차순으로 보기' : '내림차순 정렬중, 오름차순으로 보기'}
              title={query.direction === 'asc' ? '오름차순' : '내림차순'}
            >
              {query.direction === 'asc' ? (
                <ArrowUpWideNarrow size={17} aria-hidden="true" />
              ) : (
                <ArrowDownWideNarrow size={17} aria-hidden="true" />
              )}
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
              <ProductCard product={product} onLikeToggle={handleProductLikeToggle} />
            </Col>
          ))}
        </Row>
      ) : null}
    </section>
  );
}
