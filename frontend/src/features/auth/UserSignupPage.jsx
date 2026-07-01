import { useState } from 'react';
import { Alert, Button, Card, Form } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { UserPlus } from 'lucide-react';
import { useAuth } from '../../auth/AuthContext.jsx';

export default function UserSignupPage() {
  const { signupUser } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: '', password: '', nickname: '', phone: '' });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

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
      await signupUser(formData);
      navigate('/regions/setup', { replace: true });
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
            <UserPlus size={22} aria-hidden="true" />
          </div>
          <p className="page-eyebrow">계정</p>
          <h1>회원가입</h1>
          {error ? <Alert variant="danger">{error}</Alert> : null}
          <Form className="d-grid gap-3" onSubmit={handleSubmit}>
            <Form.Group controlId="signup-email">
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
            <Form.Group controlId="signup-nickname">
              <Form.Label>닉네임</Form.Label>
              <Form.Control
                name="nickname"
                type="text"
                autoComplete="nickname"
                value={formData.nickname}
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Form.Group controlId="signup-phone">
              <Form.Label>휴대폰 번호</Form.Label>
              <Form.Control
                name="phone"
                type="tel"
                autoComplete="tel"
                value={formData.phone}
                onChange={handleChange}
                placeholder="01012345678"
                required
              />
            </Form.Group>
            <Form.Group controlId="signup-password">
              <Form.Label>비밀번호</Form.Label>
              <Form.Control
                name="password"
                type="password"
                autoComplete="new-password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </Form.Group>
            <Button type="submit" variant="primary" disabled={isLoading}>
              {isLoading ? '가입 중...' : '회원가입'}
            </Button>
          </Form>
          <p className="auth-footer">
            이미 계정이 있나요? <Link to="/login">로그인</Link>
          </p>
        </Card.Body>
      </Card>
    </main>
  );
}
