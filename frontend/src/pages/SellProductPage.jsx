import { useEffect, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import ProductCard from '../components/ProductCard.jsx';
import LoadingState from '../components/LoadingState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import ProductImageDropzone from '../components/ProductImageDropzone.jsx';
import { createProduct, uploadProductImages } from '../api/productApi.js';
import { getRegions } from '../api/regionApi.js';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';
import { PRODUCT_APPROVAL_MESSAGE, submitSellProduct } from './sellProductSubmit.js';
import {
  PRODUCT_CATEGORIES,
  formatPriceInput,
  getChildRegionOptions,
  getRegionSelectOptions,
  parsePriceInput,
  validateProductForm
} from './productFormUtils.js';

const initialForm = { title: '', description: '', price: '', category: '', regionId: '', images: [] };

export default function SellProductPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const [selectedImagePreviews, setSelectedImagePreviews] = useState([]);
  const [selectedParentRegionId, setSelectedParentRegionId] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const regionsState = useApiResource(() => getRegions(), []);
  const regions = getPageContent(regionsState.data);
  const parentRegionOptions = getRegionSelectOptions(regions);
  const childRegionOptions = getChildRegionOptions(regions, selectedParentRegionId);
  const hasChildRegionOptions = childRegionOptions.length > 0;
  const regionOptionsUnavailable = !regionsState.loading && (Boolean(regionsState.error) || parentRegionOptions.length === 0);
  const selectedRegionOptions = hasChildRegionOptions ? childRegionOptions : parentRegionOptions;
  const selectedRegionLabel = selectedRegionOptions.find((region) => region.value === String(form.regionId))?.label || '';
  const selectedImagePreview = selectedImagePreviews[0]?.url || '';
  const previewProduct = {
    title: form.title || '상품 미리보기',
    price: parsePriceInput(form.price),
    category: form.category,
    categoryLabel: form.category,
    regionFullName: selectedRegionLabel,
    primaryImageUrl: selectedImagePreviews[0]?.url,
    status: 'SELLING',
    statusLabel: '판매중',
    likeCount: 0
  };

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

  const handleParentRegionChange = (event) => {
    const value = event.target.value;
    const nextChildren = getChildRegionOptions(regions, value);
    setSelectedParentRegionId(value);
    updateField('regionId', nextChildren.length > 0 ? '' : value);
  };

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

    const validationErrors = validateProductForm(form);
    setFieldErrors(validationErrors);
    setError('');

    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setSubmitting(true);

    try {
      const { imageUploadError } = await submitSellProduct(form, { createProduct, uploadProductImages });

      navigate('/', {
        replace: true,
        state: {
          productRegistrationMessage: imageUploadError
            ? `${PRODUCT_APPROVAL_MESSAGE} 다만 이미지 업로드에 실패했습니다. 관리자 승인 전 상품 수정에서 이미지를 다시 등록해 주세요.`
            : PRODUCT_APPROVAL_MESSAGE
        }
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section>
      <PageHeader title="상품 등록" />
      {regionsState.loading ? <LoadingState label="지역 불러오는 중" /> : null}
      {regionOptionsUnavailable ? <Alert variant="warning">지역 목록을 불러오지 못해 직접 입력으로 등록할 수 있어요.</Alert> : null}
      {!regionsState.loading && !regionsState.error && regions.length === 0 ? (
        <EmptyState title="지역 목록이 비어 있어요" message="직접 입력으로 계속 등록할 수 있어요." />
      ) : null}
      {regions.length > 0 ? null : null}
      {error ? <Alert variant="danger">{error}</Alert> : null}
      <Form noValidate onSubmit={handleSubmit}>
        <Row className="g-4 product-form-layout">
          <Col xs={12} lg={7}>
            <Card className="form-card">
              <Card.Body>
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
                    {regionOptionsUnavailable ? (
                      <Form.Control
                        value={form.regionId}
                        onChange={(event) => updateField('regionId', event.target.value)}
                        placeholder="吏??ID"
                        isInvalid={Boolean(fieldErrors.regionId)}
                        disabled={submitting}
                        required
                      />
                    ) : (
                      <Row className="g-2">
                        <Col xs={12} md={hasChildRegionOptions ? 6 : 12}>
                          <Form.Select value={selectedParentRegionId} onChange={handleParentRegionChange} disabled={submitting} required>
                            <option value="">시도 선택</option>
                            {parentRegionOptions.map((region) => (
                              <option key={region.value} value={region.value}>
                                {region.label}
                              </option>
                            ))}
                          </Form.Select>
                        </Col>
                        {hasChildRegionOptions ? (
                          <Col xs={12} md={6}>
                            <Form.Select
                              value={form.regionId}
                              onChange={(event) => updateField('regionId', event.target.value)}
                              isInvalid={Boolean(fieldErrors.regionId)}
                              disabled={submitting}
                              required
                            >
                              <option value="">구군 선택</option>
                              {childRegionOptions.map((region) => (
                                <option key={region.value} value={region.value}>
                                  {region.label}
                                </option>
                              ))}
                            </Form.Select>
                          </Col>
                        ) : null}
                      </Row>
                    )}
                    <Form.Control.Feedback type="invalid" className={fieldErrors.regionId ? 'd-block' : ''}>
                      {fieldErrors.regionId}
                    </Form.Control.Feedback>
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
                mainImageUrl={selectedImagePreview}
                disabled={submitting}
                onChange={(images) => updateField('images', images)}
                onRemove={removeSelectedImage}
              />
              <div className="product-form-preview">
                <ProductCard product={previewProduct} footerActionLabel="미리보기" footerActionDisabled disableNavigation />
              </div>
              <div className="form-actions">
                <Button as={Link} to="/products" variant="outline-secondary" disabled={submitting}>
                  취소
                </Button>
                <Button type="submit" disabled={submitting}>
                  {submitting ? '등록 중' : '등록'}
                </Button>
              </div>
            </div>
          </Col>
        </Row>
      </Form>
    </section>
  );
}
