import { Button, Card, Col, Form, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { LogIn } from 'lucide-react';
import EmptyState from '../components/EmptyState.jsx';

function AdminPageHeader({ title, eyebrow }) {
  return (
    <div className="page-header admin-page-header">
      <div>
        {eyebrow ? <p className="page-eyebrow">{eyebrow}</p> : null}
        <h1>{title}</h1>
      </div>
    </div>
  );
}

function AdminPlaceholderPage({ title, emptyTitle = 'No records yet' }) {
  return (
    <section>
      <AdminPageHeader title={title} eyebrow="Admin" />
      <EmptyState title={emptyTitle} />
    </section>
  );
}

function StatCard({ label, value }) {
  return (
    <Card className="admin-stat-card">
      <Card.Body>
        <span>{label}</span>
        <strong>{value}</strong>
      </Card.Body>
    </Card>
  );
}

export function AdminLoginPage() {
  return (
    <main className="admin-login-shell">
      <Card className="admin-login-card">
        <Card.Body>
          <div className="admin-login-icon">
            <LogIn size={22} aria-hidden="true" />
          </div>
          <h1>Admin Login</h1>
          <Form className="d-grid gap-3">
            <Form.Group controlId="admin-email">
              <Form.Label>Email</Form.Label>
              <Form.Control type="email" autoComplete="username" />
            </Form.Group>
            <Form.Group controlId="admin-password">
              <Form.Label>Password</Form.Label>
              <Form.Control type="password" autoComplete="current-password" />
            </Form.Group>
            <Button as={Link} to="/admin" variant="primary">
              Login
            </Button>
          </Form>
        </Card.Body>
      </Card>
    </main>
  );
}

export function AdminDashboardPage() {
  return (
    <section>
      <AdminPageHeader title="Dashboard" eyebrow="Admin" />
      <Row className="g-3 mb-4">
        <Col xs={12} md={4}>
          <StatCard label="Open reports" value="0" />
        </Col>
        <Col xs={12} md={4}>
          <StatCard label="Pending payments" value="0" />
        </Col>
        <Col xs={12} md={4}>
          <StatCard label="Active events" value="0" />
        </Col>
      </Row>
      <EmptyState title="No dashboard activity yet" />
    </section>
  );
}

export function AdminProductsPage() {
  return <AdminPlaceholderPage title="Products" emptyTitle="No products to review" />;
}

export function AdminUsersPage() {
  return <AdminPlaceholderPage title="Users" emptyTitle="No users found" />;
}

export function AdminReportsPage() {
  return <AdminPlaceholderPage title="Reports" emptyTitle="No open reports" />;
}

export function AdminPaymentsPage() {
  return <AdminPlaceholderPage title="Payments" emptyTitle="No payments found" />;
}

export function AdminCouponsPage() {
  return <AdminPlaceholderPage title="Coupons" emptyTitle="No coupon events yet" />;
}
