import { Button, Card } from 'react-bootstrap';
import { Heart, MessageCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import MoneyText from './MoneyText.jsx';
import StatusBadge from './StatusBadge.jsx';

export default function ProductCard({ product, footerActionLabel = '보기', footerActionTo }) {
  const item = product || {};
  const productId = item.productId ?? item.id ?? '#';
  const imageAlt = item.title ? `${item.title} 이미지` : '상품 이미지';
  const likeCount = item.likeCount ?? 0;
  const chatCount = item.chatCount ?? 0;

  return (
    <Card className="product-card h-100">
      <div className="product-card-media">
        {item.imageUrl ? <img src={item.imageUrl} alt={imageAlt} /> : <span>이미지 없음</span>}
      </div>
      <Card.Body>
        <div className="d-flex justify-content-between gap-2 align-items-start mb-2">
          <Card.Title as="h2">{item.title || '제목 없음'}</Card.Title>
          {item.status ? <StatusBadge status={item.status} /> : null}
        </div>
        <div className="product-card-tags">
          {item.regionName || item.region ? <span>{item.regionName || item.region}</span> : null}
          {item.category ? <span>{item.category}</span> : null}
        </div>
        <MoneyText amount={item.price} className="product-card-price" />
        <div className="product-card-meta" aria-label="상품 활동">
          <span aria-label={`관심 ${likeCount}개`}>
            <Heart size={15} aria-hidden="true" />
            {likeCount}
          </span>
          <span aria-label={`채팅 ${chatCount}개`}>
            <MessageCircle size={15} aria-hidden="true" />
            {chatCount}
          </span>
        </div>
      </Card.Body>
      <Card.Footer>
        <Button as={Link} to={footerActionTo || `/products/${productId}`} variant="outline-primary" size="sm">
          {footerActionLabel}
        </Button>
      </Card.Footer>
    </Card>
  );
}
