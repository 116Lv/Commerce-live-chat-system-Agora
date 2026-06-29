import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { Heart, PackagePlus, ReceiptText, Star, Ticket, UserRound } from 'lucide-react';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import { getMe, updateProfile } from '../api/mypageApi.js';
import { PageHeader, useApiResource } from './pageUtils.jsx';

function MetricTile({ icon: Icon, label, to }) {
  return (
    <Link to={to} className="metric-tile">
      <Icon size={20} aria-hidden="true" />
      <span>{label}</span>
    </Link>
  );
}

export default function MyPage() {
  const profileState = useApiResource(() => getMe(), []);
  const [nickname, setNickname] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (profileState.data?.nickname) {
      setNickname(profileState.data.nickname);
    }
  }, [profileState.data]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      await updateProfile({ nickname });
      setMessage('프로필을 저장했어요.');
      await profileState.reload();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section>
      <PageHeader title="마이페이지" eyebrow="계정" />
      {profileState.loading ? <LoadingState label="내 정보 불러오는 중" /> : null}
      {profileState.error ? (
        <ErrorState title="내 정보를 불러오지 못했어요" message={profileState.error.message} onRetry={profileState.reload} />
      ) : null}
      {!profileState.loading && !profileState.error ? (
        <>
          <Row className="g-3 mb-4">
            <Col xs={12} md={4}>
              <MetricTile icon={Heart} label="관심 상품" to="/me/likes" />
            </Col>
            <Col xs={12} md={4}>
              <MetricTile icon={PackagePlus} label="내 상품" to="/me/products" />
            </Col>
            <Col xs={12} md={4}>
              <MetricTile icon={ReceiptText} label="거래" to="/me/trades" />
            </Col>
            <Col xs={12} md={4}>
              <MetricTile icon={Star} label="후기" to="/me/reviews" />
            </Col>
            <Col xs={12} md={4}>
              <MetricTile icon={Ticket} label="쿠폰" to="/me/coupons" />
            </Col>
            <Col xs={12} md={4}>
              <MetricTile icon={UserRound} label="프로필" to="/me" />
            </Col>
          </Row>
          <Card className="form-card">
            <Card.Body>
              <Card.Title as="h2">프로필</Card.Title>
              {message ? <Alert variant="success">{message}</Alert> : null}
              {error ? <Alert variant="danger">{error}</Alert> : null}
              <dl className="compact-list mb-3">
                <div>
                  <dt>이메일</dt>
                  <dd>{profileState.data?.email}</dd>
                </div>
                <div>
                  <dt>상태</dt>
                  <dd>{profileState.data?.status}</dd>
                </div>
              </dl>
              <Form onSubmit={handleSubmit}>
                <Form.Label>닉네임</Form.Label>
                <div className="profile-form-row">
                  <Form.Control value={nickname} onChange={(event) => setNickname(event.target.value)} required />
                  <Button type="submit" disabled={submitting}>
                    {submitting ? '저장 중' : '저장'}
                  </Button>
                </div>
              </Form>
            </Card.Body>
          </Card>
        </>
      ) : null}
    </section>
  );
}
