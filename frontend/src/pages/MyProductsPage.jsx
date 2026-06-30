import { useEffect, useState } from 'react';
import { Alert, Button, ButtonGroup, Col, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import ProductCard from '../components/ProductCard.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { deleteProduct, getMyProducts, updateProductStatus } from '../api/productApi.js';
import { PageHeader, getPageContent, getProductId, useApiResource } from './pageUtils.jsx';
import { getProductStatusLabel } from './productFormUtils.js';

const SELLER_STATUS_ACTIONS = [
  { status: 'SELLING', label: '판매중으로 변경' },
  { status: 'RESERVED', label: '예약중으로 변경' },
  { status: 'SOLD', label: '판매완료로 변경' }
];

export default function MyProductsPage() {
  const { data, error, loading, reload } = useApiResource(() => getMyProducts(), []);
  const [actionError, setActionError] = useState('');
  const [deletingProductId, setDeletingProductId] = useState('');
  const [statusUpdatingProductId, setStatusUpdatingProductId] = useState('');
  const [products, setProducts] = useState([]);

  useEffect(() => {
    setProducts(getPageContent(data));
  }, [data]);

  const upsertProduct = (updatedProduct) => {
    const updatedProductId = getProductId(updatedProduct);
    if (!updatedProductId) {
      return;
    }

    setProducts((current) =>
      current.map((product) => (String(getProductId(product)) === String(updatedProductId) ? { ...product, ...updatedProduct } : product))
    );
  };

  const handleDeleteProduct = async (product) => {
    const productId = getProductId(product);
    if (!productId || !window.confirm('상품을 삭제할까요?')) {
      return;
    }

    setActionError('');
    setDeletingProductId(String(productId));

    try {
      await deleteProduct(productId);
      await reload();
    } catch (err) {
      setActionError(err.message);
    } finally {
      setDeletingProductId('');
    }
  };

  const handleUpdateStatus = async (product, status) => {
    const productId = getProductId(product);
    if (!productId) {
      return;
    }

    const busyKey = `${productId}:${status}`;
    setActionError('');
    setStatusUpdatingProductId(busyKey);

    try {
      const updatedProduct = await updateProductStatus(productId, status);
      upsertProduct(updatedProduct);
    } catch (err) {
      setActionError(err.message);
    } finally {
      setStatusUpdatingProductId('');
    }
  };

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
      {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
      {loading ? <LoadingState label="내 상품 불러오는 중" /> : null}
      {error ? <ErrorState title="내 상품을 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
      {!loading && !error && products.length === 0 ? <EmptyState title="등록한 상품이 없어요" /> : null}
      {!loading && !error && products.length > 0 ? (
        <Row className="g-3">
          {products.map((product) => {
            const productId = getProductId(product);
            const isDeleting = deletingProductId === String(productId);
            const currentStatus = String(product.status || '').toUpperCase();

            return (
              <Col key={productId} xs={12} sm={6} lg={4} xl={3}>
                <ProductCard product={product} footerActionLabel="수정" footerActionTo={`/products/${productId}/edit`} />
                <div className="seller-product-actions">
                  <div>
                    <span className="seller-product-status">{getProductStatusLabel(product)}</span>
                  </div>
                  <ButtonGroup size="sm" aria-label="판매 상품 작업">
                    <Button as={Link} to={`/products/${productId}/edit`} variant="outline-primary">
                      수정
                    </Button>
                    {SELLER_STATUS_ACTIONS.map((action) => {
                      const busyKey = `${productId}:${action.status}`;
                      const isCurrentStatus = currentStatus === action.status;
                      const isUpdatingStatus = statusUpdatingProductId === busyKey;

                      return (
                        <Button
                          key={action.status}
                          type="button"
                          variant={isCurrentStatus ? 'secondary' : 'outline-secondary'}
                          disabled={isCurrentStatus || Boolean(statusUpdatingProductId) || isDeleting}
                          onClick={() => handleUpdateStatus(product, action.status)}
                        >
                          {isUpdatingStatus ? '변경 중' : action.label}
                        </Button>
                      );
                    })}
                    <Button type="button" variant="outline-danger" disabled={isDeleting} onClick={() => handleDeleteProduct(product)}>
                      {isDeleting ? '삭제 중' : '삭제'}
                    </Button>
                  </ButtonGroup>
                </div>
              </Col>
            );
          })}
        </Row>
      ) : null}
    </section>
  );
}
