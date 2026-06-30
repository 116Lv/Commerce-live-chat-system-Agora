const DEFAULT_MAX_DIMENSION = 1920;
const DEFAULT_QUALITY = 0.82;
const DEFAULT_MAX_BYTES = 2 * 1024 * 1024;

const canUseCanvas = () => typeof document !== 'undefined' && typeof document.createElement === 'function';

const isCompressibleImage = (file) =>
  file?.type?.startsWith('image/') && file.type !== 'image/gif' && canUseCanvas();

const loadImage = async (file) => {
  if (typeof createImageBitmap === 'function') {
    return createImageBitmap(file);
  }

  return new Promise((resolve, reject) => {
    const image = new Image();
    const objectUrl = URL.createObjectURL(file);

    image.onload = () => {
      URL.revokeObjectURL(objectUrl);
      resolve(image);
    };
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      reject(new Error('이미지를 불러오지 못했습니다.'));
    };
    image.src = objectUrl;
  });
};

const getTargetSize = (width, height, maxDimension) => {
  const scale = Math.min(1, maxDimension / Math.max(width, height));

  return {
    width: Math.max(1, Math.round(width * scale)),
    height: Math.max(1, Math.round(height * scale))
  };
};

const canvasToBlob = (canvas, type, quality) =>
  new Promise((resolve) => {
    canvas.toBlob(resolve, type, quality);
  });

const toNamedFile = (blob, sourceFile) => {
  const baseName = sourceFile.name?.replace(/\.[^.]+$/, '') || 'image';
  const fileName = `${baseName}.jpg`;

  if (typeof File === 'function') {
    return new File([blob], fileName, { type: blob.type, lastModified: Date.now() });
  }

  blob.name = fileName;
  return blob;
};

export async function compressImageForUpload(
  file,
  { maxDimension = DEFAULT_MAX_DIMENSION, quality = DEFAULT_QUALITY, maxBytes = DEFAULT_MAX_BYTES } = {}
) {
  if (!isCompressibleImage(file)) {
    return file;
  }

  try {
    const image = await loadImage(file);
    const sourceWidth = image.width || image.naturalWidth;
    const sourceHeight = image.height || image.naturalHeight;
    const targetSize = getTargetSize(sourceWidth, sourceHeight, maxDimension);

    if (file.size <= maxBytes && targetSize.width === sourceWidth && targetSize.height === sourceHeight) {
      image.close?.();
      return file;
    }

    const canvas = document.createElement('canvas');
    canvas.width = targetSize.width;
    canvas.height = targetSize.height;

    const context = canvas.getContext('2d');
    if (!context) {
      image.close?.();
      return file;
    }

    context.drawImage(image, 0, 0, targetSize.width, targetSize.height);
    image.close?.();

    const blob = await canvasToBlob(canvas, 'image/jpeg', quality);
    if (!blob || blob.size >= file.size) {
      return file;
    }

    return toNamedFile(blob, file);
  } catch {
    return file;
  }
}
