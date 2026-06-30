import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import ProductCard from '../components/ProductCard.jsx';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import ProductImageDropzone from '../components/ProductImageDropzone.jsx';
import { getProduct, updateProduct, uploadProductImages } from '../api/productApi.js';
import { PageHeader, useApiResource } from './pageUtils.jsx';
import {
  PRODUCT_CATEGORIES,
  formatPriceInput,
  getProductImageUrl,
  getProductRegionLabel,
  parsePriceInput,
  validateProductForm
} from './productFormUtils.js';

const EDIT_VALIDATION_REGION_ID = 1;

export default function EditProductPage() {
  const { productId } = useParams();
  const productState = useApiResource(() => getProduct(productId), [productId]);
  const [form, setForm] = useState({ title: '', description: '', price: '', category: '', regionId: '', images: [] });
  const [fieldErrors, setFieldErrors] = useState({});
  const [selectedImagePreviews, setSelectedImagePreviews] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (productState.data) {
      const nextRegionId = productState.data.regionId ?? productState.data.region?.regionId ?? '';

      setForm({
        title: productState.data.title || '',
        description: productState.data.description || '',
        price: formatPriceInput(productState.data.price ?? ''),
        category: productState.data.category || '',
        regionId: nextRegionId,
        images: []
      });
      setFieldErrors({});
    }
  }, [productState.data]);

  useEffect(() => {
    if (form.images.length === 0) {
      setSelectedImagePreviews([]);
      return undefined;
    }

    const previews = form.images.map((image) => ({ file: image, url: URL.createObjectURL(image) }));
    setSelectedImagePreviews(previews);

    return () => previews.forEach((preview) => URL.revokeObjectURL(preview.url));
  }, [form.images]);

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }));
    setFieldErrors((current) => {
      if (!current[name]) {
        return current;
      }

      const next = { ...current };
      delete next[name];
      return next;
    });
  };

  const handlePriceChange = (event) => updateField('price', formatPriceInput(event.target.value));

  const removeSelectedImage = (indexToRemove = 0) => {
    updateField(
      'images',
      form.images.filter((_, index) => index !== indexToRemove)
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (submitting) {
      return;
    }

    const validationErrors = validateProductForm({ ...form, regionId: form.regionId || EDIT_VALIDATION_REGION_ID });
    setFieldErrors(validationErrors);
    setMessage('');
    setError('');

    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setSubmitting(true);

    try {
      await updateProduct(productId, {
        title: form.title,
        description: form.description,
        price: parsePriceInput(form.price),
        category: form.category
      });

      if (form.images.length > 0) {
        await uploadProductImages(productId, form.images);
      }

      setMessage('수정했어요.');
      await productState.reload();
      updateField('images', []);
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

  const selectedImagePreview = selectedImagePreviews[0]?.url || '';
  const visibleImageUrl = selectedImagePreview || getProductImageUrl(productState.data);
  const persistedRegionLabel = getProductRegionLabel(productState.data);
  const previewProduct = {
    ...productState.data,
    title: form.title || productState.data.title,
    price: parsePriceInput(form.price),
    category: form.category,
    categoryLabel: form.category,
    regionName: persistedRegionLabel,
    primaryImageUrl: visibleImageUrl
  };

  return (
    <section>
      <PageHeader title="상품 수정" />
      <Form noValidate onSubmit={handleSubmit}>
        <Row className="g-4 product-form-layout">
          <Col xs={12} lg={7}>
            <Card className="form-card">
              <Card.Body>
                {message ? <Alert variant="success">{message}</Alert> : null}
                {error ? <Alert variant="danger">{error}</Alert> : null}
                <Row className="g-3">
                  <Col xs={12} md={8}>
                    <Form.Label>제목</Form.Label>
                    <Form.Control
                      value={form.title}
                      onChange={(event) => updateField('title', event.target.value)}
                      placeholder="상품명을 입력해 주세요"
                      isInvalid={Boolean(fieldErrors.title)}
                      disabled={submitting}
                      required
                    />
                    <Form.Control.Feedback type="invalid">{fieldErrors.title}</Form.Control.Feedback>
                  </Col>
                  <Col xs={12} md={4}>
                    <Form.Label>가격</Form.Label>
                    <div className={`product-price-input ${fieldErrors.price ? 'is-invalid' : ''}`}>
                      <Form.Control
                        type="text"
                        inputMode="numeric"
                        value={form.price}
                        onChange={handlePriceChange}
                        placeholder="10,000"
                        isInvalid={Boolean(fieldErrors.price)}
                        disabled={submitting}
                        required
                      />
                      <span className="product-price-suffix">원</span>
                    </div>
                    <Form.Control.Feedback type="invalid" className={fieldErrors.price ? 'd-block' : ''}>
                      {fieldErrors.price}
                    </Form.Control.Feedback>
                  </Col>
                  <Col xs={12} md={6}>
                    <Form.Label>카테고리</Form.Label>
                    <Form.Select
                      value={form.category}
                      onChange={(event) => updateField('category', event.target.value)}
                      isInvalid={Boolean(fieldErrors.category)}
                      disabled={submitting}
                      required
                    >
                      <option value="">카테고리 선택</option>
                      {PRODUCT_CATEGORIES.map((category) => (
                        <option key={category.value} value={category.value}>
                          {category.label}
                        </option>
                      ))}
                    </Form.Select>
                    <Form.Control.Feedback type="invalid">{fieldErrors.category}</Form.Control.Feedback>
                  </Col>
                  <Col xs={12} md={6}>
                    <Form.Label>지역</Form.Label>
                    <Form.Control value={persistedRegionLabel} disabled aria-describedby="edit-product-region-help" />
                    <Form.Text id="edit-product-region-help" muted>
                      지역 변경은 새 상품 등록에서만 가능해요
                    </Form.Text>
                  </Col>
                  <Col xs={12}>
                    <div className="description-heading">
                      <Form.Label>설명</Form.Label>
                      <span className="description-counter">{form.description.length}/2000</span>
                    </div>
                    <p className="description-guide">상품 상태, 사용 기간, 거래 희망 조건을 구체적으로 적어 주세요.</p>
                    <Form.Control
                      as="textarea"
                      rows={5}
                      value={form.description}
                      onChange={(event) => updateField('description', event.target.value)}
                      placeholder="구매 시기, 하자 여부, 구성품, 선호하는 거래 방식을 적어 주세요."
                      maxLength={2000}
                      isInvalid={Boolean(fieldErrors.description)}
                      disabled={submitting}
                      required
                    />
                    <Form.Control.Feedback type="invalid">{fieldErrors.description}</Form.Control.Feedback>
                  </Col>
                </Row>
              </Card.Body>
            </Card>
          </Col>
          <Col xs={12} lg={5}>
            <div className="product-form-side">
              <Form.Label>이미지</Form.Label>
              <ProductImageDropzone
                images={form.images}
                previews={selectedImagePreviews}
                mainImageUrl={visibleImageUrl}
                disabled={submitting}
                onChange={(images) => updateField('images', images)}
                onRemove={removeSelectedImage}
              />
              <div className="product-form-preview">
                <ProductCard product={previewProduct} footerActionLabel="미리보기" footerActionDisabled disableNavigation />
              </div>
              <div className="form-actions">
                <Button as={Link} to={`/products/${productId}`} variant="outline-secondary" disabled={submitting}>
                  돌아가기
                </Button>
                <Button type="submit" disabled={submitting}>
                  {submitting ? '저장 중' : '저장'}
                </Button>
              </div>
            </div>
          </Col>
        </Row>
      </Form>
    </section>
  );
}
