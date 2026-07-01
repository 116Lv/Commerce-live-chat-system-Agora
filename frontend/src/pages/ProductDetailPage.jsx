import { useEffect, useState } from 'react';
import { Alert, Button, ButtonGroup, Col, Row } from 'react-bootstrap';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { ChevronLeft, ChevronRight, Flag, Heart, MessageCircle } from 'lucide-react';
import MoneyText from '../components/MoneyText.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getProduct, likeProduct, unlikeProduct } from '../api/productApi.js';
import { getSmileScore } from '../api/mypageApi.js';
import { openChatRoom } from '../api/chatApi.js';
import { useAuth } from '../auth/AuthContext.jsx';
import ReportModal from '../features/reports/ReportModal.jsx';
import { PageHeader, formatDateTime, useApiResource } from './pageUtils.jsx';
import {
  getProductCategoryLabel,
  getProductImageUrls,
  getProductRegionLabel,
  getProductStatusLabel
} from './productFormUtils.js';

export default function ProductDetailPage() {
  const { productId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { isUserAuthenticated } = useAuth();
  const [actionMessage, setActionMessage] = useState('');
  const [actionError, setActionError] = useState('');
  const [submitting, setSubmitting] = useState('');
  const [showReport, setShowReport] = useState(false);
  const [activeImageIndex, setActiveImageIndex] = useState(0);
  const [sellerSmileScore, setSellerSmileScore] = useState(null);
  const { data: product, error, loading, reload } = useApiResource(() => getProduct(productId), [productId]);
  const productImageUrls = getProductImageUrls(product);
  const hasMultipleImages = productImageUrls.length > 1;
  const currentImageUrl = productImageUrls[activeImageIndex] || productImageUrls[0] || '';

  useEffect(() => {
    let disposed = false;

    const loadSellerSmileScore = async () => {
      if (!product?.sellerId) {
        setSellerSmileScore(null);
        return;
      }

      try {
        const response = await getSmileScore(product.sellerId);
        if (!disposed) {
          setSellerSmileScore(response.smileScore);
        }
      } catch {
        if (!disposed) {
          setSellerSmileScore(null);
        }
      }
    };

    loadSellerSmileScore();

    return () => {
      disposed = true;
    };
  }, [product?.sellerId]);

  useEffect(() => {
    setActiveImageIndex(0);
  }, [productId, productImageUrls.length]);

  const requireAuth = () => {
    if (!isUserAuthenticated) {
      navigate('/login', { state: { from: location } });
      return false;
    }

    return true;
  };

  const handleLikeToggle = async () => {
    if (!requireAuth()) {
      return;
    }

    const liked = Boolean(product.liked);
    setSubmitting('like');
    setActionMessage('');
    setActionError('');

    try {
      await (liked ? unlikeProduct(productId) : likeProduct(productId));
      setActionMessage(liked ? '관심 상품에서 해제했어요.' : '관심 상품에 추가했어요.');
      await reload();
    } catch (err) {
      setActionError(err.message);
    } finally {
      setSubmitting('');
    }
  };

  const handleOpenChat = async () => {
    if (!requireAuth()) {
      return;
    }

    setSubmitting('chat');
    setActionMessage('');
    setActionError('');

    try {
      const room = await openChatRoom(productId);
      navigate(`/chat/${room.chatRoomId}`);
    } catch (err) {
      setActionError(err.message);
    } finally {
      setSubmitting('');
    }
  };

  const handleReport = () => {
    if (!requireAuth()) {
      return;
    }

    setShowReport(true);
  };

  const handlePreviousImage = () => {
    setActiveImageIndex((current) => (current <= 0 ? productImageUrls.length - 1 : current - 1));
  };

  const handleNextImage = () => {
    setActiveImageIndex((current) => (current + 1) % productImageUrls.length);
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

  const categoryLabel = getProductCategoryLabel(product);
  const liked = Boolean(product.liked);
  const likeCount = product.likeCount ?? 0;
  const regionLabel = getProductRegionLabel(product);
  const statusLabel = getProductStatusLabel(product);
  const createdAtLabel = product.createdAt ? formatDateTime(product.createdAt) : '-';

  return (
    <section>
      <PageHeader title={product.title || '상품'} eyebrow="상품 상세" />
      {actionMessage ? <Alert variant="success">{actionMessage}</Alert> : null}
      {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
      <Row className="g-4">
        <Col xs={12} lg={7}>
          <div className="detail-panel product-detail-media">
            {productImageUrls.length > 0 ? (
              <div className="product-detail-carousel">
                {hasMultipleImages ? (
                  <>
                    <button
                      type="button"
                      className="product-detail-carousel-button product-detail-carousel-prev"
                      onClick={handlePreviousImage}
                      aria-label="이전 이미지"
                    >
                      <ChevronLeft size={22} aria-hidden="true" />
                    </button>
                    <button
                      type="button"
                      className="product-detail-carousel-button product-detail-carousel-next"
                      onClick={handleNextImage}
                      aria-label="다음 이미지"
                    >
                      <ChevronRight size={22} aria-hidden="true" />
                    </button>
                  </>
                ) : null}
                <img src={currentImageUrl} alt={`${product.title} 이미지 ${activeImageIndex + 1}`} />
                {hasMultipleImages ? (
                  <span className="product-detail-image-counter">
                    {activeImageIndex + 1} / {productImageUrls.length}
                  </span>
                ) : null}
              </div>
            ) : (
              <span>이미지 없음</span>
            )}
          </div>
        </Col>
        <Col xs={12} lg={5}>
          <div className="detail-panel product-detail-info">
            <div className="d-flex justify-content-between gap-2 align-items-start">
              <h2>{product.title}</h2>
              {product.status ? <span className="badge bg-secondary product-status-badge">{statusLabel}</span> : null}
            </div>
            <MoneyText amount={product.price} className="product-detail-price" />
            <dl className="compact-list">
              <div>
                <dt>상태</dt>
                <dd>{statusLabel}</dd>
              </div>
              <div>
                <dt>카테고리</dt>
                <dd>{categoryLabel}</dd>
              </div>
              <div>
                <dt>지역</dt>
                <dd>{regionLabel}</dd>
              </div>
              <div>
                <dt>판매자</dt>
                <dd>{product.sellerNickname || product.sellerId || '-'}</dd>
              </div>
              <div>
                <dt>스마일</dt>
                <dd>{sellerSmileScore == null ? '-' : String(sellerSmileScore) + '점'}</dd>
              </div>
              <div>
                <dt>관심</dt>
                <dd>{likeCount}개</dd>
              </div>
              <div>
                <dt>작성일</dt>
                <dd>{createdAtLabel}</dd>
              </div>
            </dl>
            <ButtonGroup className="product-action-group" aria-label="상품 작업">
              <Button onClick={handleLikeToggle} disabled={Boolean(submitting)} variant={liked ? 'primary' : 'outline-primary'}>
                <Heart size={17} aria-hidden="true" /> {liked ? '관심 해제' : '관심'} {likeCount}
              </Button>
              <Button onClick={handleOpenChat} disabled={Boolean(submitting)} variant="outline-primary">
                <MessageCircle size={17} aria-hidden="true" /> 채팅
              </Button>
              <Button onClick={handleReport} disabled={Boolean(submitting)} variant="outline-danger">
                <Flag size={17} aria-hidden="true" /> 신고
              </Button>
            </ButtonGroup>
          </div>
        </Col>
      </Row>
      <div className="detail-panel mt-4">
        <h2>설명</h2>
        <p className="mb-0 pre-line">{product.description || '설명이 없어요.'}</p>
      </div>
      <ReportModal show={showReport} onHide={() => setShowReport(false)} productId={Number(productId)} />
    </section>
  );
}
