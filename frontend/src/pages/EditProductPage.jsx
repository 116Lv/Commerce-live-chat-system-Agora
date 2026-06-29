import { useEffect, useState } from 'react';
import { Alert, Button, Card, Form, Row, Col } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { getProduct, updateProduct, uploadProductImage } from '../api/productApi.js';
import { PageHeader, useApiResource } from './pageUtils.jsx';

export default function EditProductPage() {
  const { productId } = useParams();
  const productState = useApiResource(() => getProduct(productId), [productId]);
  const [form, setForm] = useState({ title: '', description: '', price: '', category: '', image: null });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (productState.data) {
      setForm({
        title: productState.data.title || '',
        description: productState.data.description || '',
        price: productState.data.price ?? '',
        category: productState.data.category || '',
        image: null
      });
    }
  }, [productState.data]);

  const updateField = (name, value) => setForm((current) => ({ ...current, [name]: value }));

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setMessage('');
    setError('');

    try {
      await updateProduct(productId, {
        title: form.title,
        description: form.description,
        price: Number(form.price),
        category: form.category
      });

      if (form.image) {
        await uploadProductImage(productId, form.image);
      }

      setMessage('수정했어요.');
      await productState.reload();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (productState.loading) {
    return <LoadingState label="상품 불러오는 중" />;
  }

  if (productState.error) {
    return <ErrorState title="상품을 불러오지 못했어요" message={productState.error.message} onRetry={productState.reload} />;
  }

  if (!productState.data) {
    return <EmptyState title="상품이 없어요" />;
  }

  return (
    <section>
      <PageHeader title="상품 수정" eyebrow="판매" />
      <Card className="form-card">
        <Card.Body>
          {message ? <Alert variant="success">{message}</Alert> : null}
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
              <Col xs={12}>
                <Form.Label>카테고리</Form.Label>
                <Form.Control value={form.category} onChange={(event) => updateField('category', event.target.value)} required />
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
              <Button as={Link} to={`/products/${productId}`} variant="outline-secondary">
                돌아가기
              </Button>
              <Button type="submit" disabled={submitting}>
                {submitting ? '저장 중' : '저장'}
              </Button>
            </div>
          </Form>
        </Card.Body>
      </Card>
    </section>
  );
}
