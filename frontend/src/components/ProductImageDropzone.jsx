import { useMemo, useState } from 'react';
import { Alert, Button } from 'react-bootstrap';
import { ImagePlus, Trash2 } from 'lucide-react';
import { useDropzone } from 'react-dropzone';

export const PRODUCT_IMAGE_MAX_FILES = 8;
export const PRODUCT_IMAGE_MAX_FILE_SIZE = 10 * 1024 * 1024;
export const PRODUCT_IMAGE_MAX_REQUEST_SIZE = 100 * 1000 * 1000;

const ACCEPTED_IMAGE_TYPES = {
  'image/jpeg': ['.jpg', '.jpeg'],
  'image/png': ['.png'],
  'image/webp': ['.webp'],
  'image/gif': ['.gif']
};

const formatMb = (bytes) => `${Math.floor(bytes / 1024 / 1024)}MB`;

const getRejectionMessage = (rejections) => {
  if (rejections.some((rejection) => rejection.errors.some((error) => error.code === 'file-too-large'))) {
    return `이미지는 파일당 ${formatMb(PRODUCT_IMAGE_MAX_FILE_SIZE)} 이하만 업로드할 수 있습니다.`;
  }

  if (rejections.some((rejection) => rejection.errors.some((error) => error.code === 'file-invalid-type'))) {
    return 'JPG, PNG, WebP, GIF 이미지 파일만 업로드할 수 있습니다.';
  }

  return '업로드할 수 없는 이미지가 포함되어 있습니다.';
};

export default function ProductImageDropzone({
  images,
  previews,
  mainImageUrl,
  disabled,
  onChange,
  onRemove,
  emptyLabel = '사진 추가'
}) {
  const [dropzoneError, setDropzoneError] = useState('');
  const currentSize = useMemo(() => images.reduce((total, image) => total + (image.size || 0), 0), [images]);
  const isFull = images.length >= PRODUCT_IMAGE_MAX_FILES;

  const handleDrop = (acceptedFiles, fileRejections) => {
    if (fileRejections.length > 0) {
      setDropzoneError(getRejectionMessage(fileRejections));
      return;
    }

    if (acceptedFiles.length === 0) {
      return;
    }

    const nextImages = [...images, ...acceptedFiles];
    const nextSize = currentSize + acceptedFiles.reduce((total, image) => total + (image.size || 0), 0);

    if (nextImages.length > PRODUCT_IMAGE_MAX_FILES) {
      setDropzoneError(`이미지는 최대 ${PRODUCT_IMAGE_MAX_FILES}개까지 업로드할 수 있습니다.`);
      return;
    }

    if (nextSize > PRODUCT_IMAGE_MAX_REQUEST_SIZE) {
      setDropzoneError(`이미지 전체 용량은 ${formatMb(PRODUCT_IMAGE_MAX_REQUEST_SIZE)} 이하만 업로드할 수 있습니다.`);
      return;
    }

    setDropzoneError('');
    onChange(nextImages);
  };

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: ACCEPTED_IMAGE_TYPES,
    disabled,
    maxSize: PRODUCT_IMAGE_MAX_FILE_SIZE,
    multiple: true,
    noClick: isFull,
    onDrop: handleDrop
  });

  return (
    <div className="product-image-upload">
      {dropzoneError ? (
        <Alert variant="warning" className="product-image-alert">
          {dropzoneError}
        </Alert>
      ) : null}
      <div
        {...getRootProps({
          className: `product-image-dropzone ${isDragActive ? 'is-active' : ''} ${isFull ? 'is-full' : ''}`
        })}
      >
        <input {...getInputProps()} />
        {mainImageUrl ? (
          <img src={mainImageUrl} alt="상품 이미지 미리보기" />
        ) : (
          <span className="product-image-dropzone-empty">
            <ImagePlus size={24} aria-hidden="true" />
            {emptyLabel}
          </span>
        )}
        {mainImageUrl ? <span className="product-image-main-badge">대표 이미지</span> : null}
      </div>
      <p className="product-image-policy">
        이미지 {PRODUCT_IMAGE_MAX_FILES}개까지, 파일당 {formatMb(PRODUCT_IMAGE_MAX_FILE_SIZE)} 이하, 총{' '}
        {formatMb(PRODUCT_IMAGE_MAX_REQUEST_SIZE)} 이하
      </p>
      {previews.length > 0 ? (
        <div className="product-image-preview-grid">
          {previews.map((preview, index) => (
            <div className="product-image-preview-tile" key={`${preview.file.name}-${index}`}>
              <img src={preview.url} alt={`선택한 상품 이미지 ${index + 1}`} />
              {index === 0 ? <span className="product-image-main-badge">대표 이미지</span> : null}
              <Button type="button" variant="outline-secondary" size="sm" onClick={() => onRemove(index)} disabled={disabled}>
                <Trash2 size={14} aria-hidden="true" />
                제거
              </Button>
            </div>
          ))}
        </div>
      ) : null}
    </div>
  );
}
