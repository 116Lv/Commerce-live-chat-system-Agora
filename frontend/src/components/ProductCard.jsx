import { Button, Card } from 'react-bootstrap';
import { Heart, MessageCircle } from 'lucide-react';
import { Link } from 'react-router-dom';
import MoneyText from './MoneyText.jsx';
import StatusBadge from './StatusBadge.jsx';

export default function ProductCard({ product }) {
  const item = product || {};
  const productId = item.productId ?? item.id ?? '#';
  const imageAlt = item.title ? `${item.title} product image` : 'Product image';

  return (
    <Card className="product-card h-100">
      <div className="product-card-media">
        {item.imageUrl ? <img src={item.imageUrl} alt={imageAlt} /> : <span>No image</span>}
      </div>
      <Card.Body>
        <div className="d-flex justify-content-between gap-2 align-items-start mb-2">
          <Card.Title as="h2">{item.title || 'Untitled product'}</Card.Title>
          <StatusBadge status={item.status || 'SELLING'} />
        </div>
        <p className="product-card-region">{item.regionName || item.region || 'Local'}</p>
        <MoneyText amount={item.price} className="product-card-price" />
        <div className="product-card-meta" aria-label="Product activity">
          <span>
            <Heart size={15} aria-hidden="true" />
            {item.likeCount ?? 0}
          </span>
          <span>
            <MessageCircle size={15} aria-hidden="true" />
            {item.chatCount ?? 0}
          </span>
        </div>
      </Card.Body>
      <Card.Footer>
        <Button as={Link} to={`/products/${productId}`} variant="outline-primary" size="sm">
          View
        </Button>
      </Card.Footer>
    </Card>
  );
}
