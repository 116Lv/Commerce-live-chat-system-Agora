import { Button, Card } from 'react-bootstrap';
import { ArrowRight, Heart } from 'lucide-react';
import { Link } from 'react-router-dom';
import MoneyText from './MoneyText.jsx';
import {
  getProductCategoryLabel,
  getProductImageUrl,
  getProductPrice,
  getProductRegionLabel,
  getProductStatusLabel,
  getProductTitle
} from '../pages/productFormUtils.js';

export default function ProductCard({
  disableNavigation = false,
  product,
  footerActionDisabled = false,
  footerActionLabel = '보기',
  footerActionOnClick,
  footerActionTo,
  footerActionVariant = 'outline-primary',
  onLikeToggle
}) {
  const item = product || {};
  const productId = item.productId ?? item.id ?? '#';
  const detailTo = `/products/${productId}`;
  const title = getProductTitle(item);
  const imageAlt = title ? `${title} 이미지` : '상품 이미지';
  const imageUrl = getProductImageUrl(item);
  const price = getProductPrice(item);
  const likeCount = item.likeCount ?? 0;
  const categoryLabel = getProductCategoryLabel(item);
  const regionLabel = getProductRegionLabel(item);
  const statusLabel = getProductStatusLabel(item);

  const stopCardNavigation = (event) => {
    event.stopPropagation();
  };

  const handleFooterAction = (event) => {
    stopCardNavigation(event);
    footerActionOnClick?.(event);
  };

  const handleLikeToggle = (event) => {
    stopCardNavigation(event);
    onLikeToggle?.(item);
  };

  return (
    <Card className="product-card h-100">
      {disableNavigation ? null : (
        <Link className="product-card-link" to={detailTo} data-product-card-link={detailTo} aria-label={`${title || '상품'} 상세 보기`} />
      )}
      <div className="product-card-media">
        {imageUrl ? <img src={imageUrl} alt={imageAlt} /> : <span>이미지 없음</span>}
        <button
          type="button"
          className={`product-card-heart ${item.liked ? 'is-liked' : ''}`}
          aria-label={`관심 ${likeCount}개`}
          onClick={handleLikeToggle}
          disabled={!onLikeToggle}
        >
          <Heart size={15} aria-hidden="true" />
          {likeCount}
        </button>
      </div>
      <Card.Body>
        <div className="d-flex justify-content-between gap-2 align-items-start mb-2">
          <Card.Title as="h2">{title || '제목 없음'}</Card.Title>
          {item.status ? <span className="badge bg-secondary product-status-badge">{statusLabel}</span> : null}
        </div>
        <div className="product-card-tags">
          {regionLabel && regionLabel !== '-' ? <span>{regionLabel}</span> : null}
          {categoryLabel && categoryLabel !== '-' ? <span>{categoryLabel}</span> : null}
        </div>
        <MoneyText amount={price} className="product-card-price" />
        <div className="product-card-detail-affordance">
          상세 보기 <ArrowRight size={15} aria-hidden="true" />
        </div>
      </Card.Body>
      <Card.Footer>
        {footerActionOnClick ? (
          <Button type="button" variant={footerActionVariant} size="sm" disabled={footerActionDisabled} onClick={handleFooterAction}>
            {footerActionLabel}
          </Button>
        ) : disableNavigation ? (
          <Button type="button" variant={footerActionVariant} size="sm" disabled={footerActionDisabled} onClick={stopCardNavigation}>
            {footerActionLabel}
          </Button>
        ) : (
          <Button
            as={Link}
            to={footerActionTo || detailTo}
            variant={footerActionVariant}
            size="sm"
            disabled={footerActionDisabled}
            onClick={stopCardNavigation}
          >
            {footerActionLabel}
          </Button>
        )}
      </Card.Footer>
    </Card>
  );
}
