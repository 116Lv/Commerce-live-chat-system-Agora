import { useState } from 'react';
import { Alert, Button, Card, Form } from 'react-bootstrap';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { LogIn } from 'lucide-react';
import { useAuth } from '../../auth/AuthContext.jsx';

export default function UserLoginPage() {
  const { loginUser } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const fromLocation = location.state?.from;
  const from = fromLocation ? `${fromLocation.pathname}${fromLocation.search || ''}` : '/';

  const handleChange = (event) => {
    setFormData((current) => ({
      ...current,
      [event.target.name]: event.target.value
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await loginUser(formData);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <main className="auth-shell">
      <Card className="auth-card">
        <Card.Body>
          <div className="auth-icon">
            <LogIn size={22} aria-hidden="true" />
          </div>
          <p className="page-eyebrow">계정</p>
          <h1>로그인</h1>
          {error ? <Alert variant="danger">{error}</Alert> : null}
          <Form className="d-grid gap-3" onSubmit={handleSubmit}>
            <Form.Group controlId="user-login-email">
              <Form.Label>이메일</Form.Label>
              <Form.Control
                name="email"
                type="email"
                autoComplete="username"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group controlId="user-login-password">
              <Form.Label>비밀번호</Form.Label>
              <Form.Control
                name="password"
                type="password"
                autoComplete="current-password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Button type="submit" variant="primary" disabled={isLoading}>
              {isLoading ? '로그인 중...' : '로그인'}
            </Button>
          </Form>
          <p className="auth-footer">
            아직 계정이 없나요? <Link to="/signup">회원가입</Link>
          </p>
        </Card.Body>
      </Card>
    </main>
  );
}
