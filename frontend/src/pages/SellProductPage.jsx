import { useState } from 'react';
import { Alert, Button, Card, Form, Row, Col } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import { createProduct, uploadProductImage } from '../api/productApi.js';
import { getRegions } from '../api/regionApi.js';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';

const initialForm = { title: '', description: '', price: '', category: '', regionId: '', image: null };

export default function SellProductPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const regionsState = useApiResource(() => getRegions(), []);
  const regions = getPageContent(regionsState.data);

  const updateField = (name, value) => setForm((current) => ({ ...current, [name]: value }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const product = await createProduct({
        title: form.title,
        description: form.description,
        price: Number(form.price),
        category: form.category,
        regionId: Number(form.regionId)
      });

      if (form.image) {
        await uploadProductImage(product.productId, form.image);
      }

      navigate(`/products/${product.productId}`, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section>
      <PageHeader title="상품 등록" eyebrow="판매" />
      {regionsState.loading ? <LoadingState label="지역 불러오는 중" /> : null}
      {regionsState.error ? (
        <ErrorState title="지역을 불러오지 못했어요" message={regionsState.error.message} onRetry={regionsState.reload} />
      ) : null}
      {!regionsState.loading && !regionsState.error ? (
        <Card className="form-card">
          <Card.Body>
            {error ? <Alert variant="danger">{error}</Alert> : null}
            <Form onSubmit={handleSubmit}>
              <Row className="g-3">
                <Col xs={12} md={8}>
                  <Form.Label>제목</Form.Label>
                  <Form.Control value={form.title} onChange={(event) => updateField('title', event.target.value)} required />
                </Col>
                <Col xs={12} md={4}>
                  <Form.Label>가격</Form.Label>
                  <Form.Control
                    type="number"
                    min="1"
                    value={form.price}
                    onChange={(event) => updateField('price', event.target.value)}
                    required
                  />
                </Col>
                <Col xs={12} md={6}>
                  <Form.Label>카테고리</Form.Label>
                  <Form.Control value={form.category} onChange={(event) => updateField('category', event.target.value)} required />
                </Col>
                <Col xs={12} md={6}>
                  <Form.Label>지역</Form.Label>
                  <Form.Select value={form.regionId} onChange={(event) => updateField('regionId', event.target.value)} required>
                    <option value="">선택</option>
                    {regions.map((region) => (
                      <option key={region.regionId} value={region.regionId}>
                        {region.name}
                      </option>
                    ))}
                  </Form.Select>
                </Col>
                <Col xs={12}>
                  <Form.Label>설명</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={5}
                    value={form.description}
                    onChange={(event) => updateField('description', event.target.value)}
                    required
                  />
                </Col>
                <Col xs={12}>
                  <Form.Label>이미지</Form.Label>
                  <Form.Control type="file" accept="image/*" onChange={(event) => updateField('image', event.target.files?.[0] || null)} />
                </Col>
              </Row>
              <div className="form-actions">
                <Button as={Link} to="/products" variant="outline-secondary">
                  취소
                </Button>
                <Button type="submit" disabled={submitting}>
                  {submitting ? '등록 중' : '등록'}
                </Button>
              </div>
            </Form>
          </Card.Body>
        </Card>
      ) : null}
    </section>
  );
}
