import { Button, Col, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import {
  Heart,
  MessageCircle,
  Package,
  PackagePlus,
  ReceiptText,
  Star,
  Ticket,
  UserRound
} from 'lucide-react';
import EmptyState from '../components/EmptyState.jsx';

function PageHeader({ title, eyebrow, action }) {
  return (
    <div className="page-header">
      <div>
        {eyebrow ? <p className="page-eyebrow">{eyebrow}</p> : null}
        <h1>{title}</h1>
      </div>
      {action ? <div>{action}</div> : null}
    </div>
  );
}

function PlaceholderPage({ title, eyebrow, emptyTitle = 'No items yet', action }) {
  return (
    <section>
      <PageHeader title={title} eyebrow={eyebrow} action={action} />
      <EmptyState title={emptyTitle} />
    </section>
  );
}

function MetricTile({ icon: Icon, label, to }) {
  return (
    <Link to={to} className="metric-tile">
      <Icon size={20} aria-hidden="true" />
      <span>{label}</span>
    </Link>
  );
}

export function HomePage() {
  return (
    <section>
      <PageHeader
        title="Marketplace"
        eyebrow="Agora"
        action={
          <Button as={Link} to="/sell" variant="primary">
            Sell
          </Button>
        }
      />
      <Row className="g-3 mb-4">
        <Col xs={12} md={4}>
          <MetricTile icon={Package} label="Products" to="/products" />
        </Col>
        <Col xs={12} md={4}>
          <MetricTile icon={Ticket} label="Events" to="/events" />
        </Col>
        <Col xs={12} md={4}>
          <MetricTile icon={MessageCircle} label="Chat" to="/chat" />
        </Col>
      </Row>
      <EmptyState title="No featured products yet" />
    </section>
  );
}

export function LoginPage() {
  return <PlaceholderPage title="Login" eyebrow="Account" emptyTitle="Login form placeholder" />;
}

export function SignupPage() {
  return <PlaceholderPage title="Sign up" eyebrow="Account" emptyTitle="Signup form placeholder" />;
}

export function RegionSetupPage() {
  return <PlaceholderPage title="Region setup" eyebrow="Account" emptyTitle="No preferred region selected" />;
}

export function ProductsPage() {
  return (
    <PlaceholderPage
      title="Products"
      eyebrow="Marketplace"
      emptyTitle="No products found"
      action={
        <Button as={Link} to="/sell" variant="primary">
          Sell
        </Button>
      }
    />
  );
}

export function ProductDetailPage() {
  const { productId } = useParams();

  return <PlaceholderPage title={`Product ${productId}`} eyebrow="Marketplace" emptyTitle="Product details unavailable" />;
}

export function SellPage() {
  return <PlaceholderPage title="Sell" eyebrow="Marketplace" emptyTitle="Product form placeholder" />;
}

export function CouponsPage() {
  return <PlaceholderPage title="Events" eyebrow="Coupons" emptyTitle="No coupon events yet" />;
}

export function ChatPage() {
  return <PlaceholderPage title="Chat" eyebrow="Messages" emptyTitle="No chat rooms yet" />;
}

export function ChatRoomPage() {
  const { chatRoomId } = useParams();

  return <PlaceholderPage title={`Chat room ${chatRoomId}`} eyebrow="Messages" emptyTitle="No messages yet" />;
}

export function MyPage() {
  return (
    <section>
      <PageHeader title="My Page" eyebrow="Account" />
      <Row className="g-3">
        <Col xs={12} md={4}>
          <MetricTile icon={Heart} label="Likes" to="/me/likes" />
        </Col>
        <Col xs={12} md={4}>
          <MetricTile icon={PackagePlus} label="My products" to="/me/products" />
        </Col>
        <Col xs={12} md={4}>
          <MetricTile icon={ReceiptText} label="Trades" to="/me/trades" />
        </Col>
        <Col xs={12} md={4}>
          <MetricTile icon={Star} label="Reviews" to="/me/reviews" />
        </Col>
        <Col xs={12} md={4}>
          <MetricTile icon={Ticket} label="Coupons" to="/me/coupons" />
        </Col>
        <Col xs={12} md={4}>
          <MetricTile icon={UserRound} label="Profile" to="/me" />
        </Col>
      </Row>
    </section>
  );
}

export function MyLikesPage() {
  return <PlaceholderPage title="Liked products" eyebrow="My Page" emptyTitle="No liked products yet" />;
}

export function MyProductsPage() {
  return <PlaceholderPage title="My products" eyebrow="My Page" emptyTitle="No listed products yet" />;
}

export function MyTradesPage() {
  return <PlaceholderPage title="My trades" eyebrow="My Page" emptyTitle="No trades yet" />;
}

export function MyReviewsPage() {
  return <PlaceholderPage title="My reviews" eyebrow="My Page" emptyTitle="No reviews yet" />;
}

export function MyCouponsPage() {
  return <PlaceholderPage title="My coupons" eyebrow="My Page" emptyTitle="No coupons yet" />;
}

export function NotFoundPage() {
  return (
    <PlaceholderPage
      title="Page not found"
      eyebrow="Agora"
      emptyTitle="This page does not exist"
      action={
        <Button as={Link} to="/" variant="outline-primary">
          Home
        </Button>
      }
    />
  );
}

