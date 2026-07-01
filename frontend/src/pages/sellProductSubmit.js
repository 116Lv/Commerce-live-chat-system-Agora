import { parsePriceInput } from './productFormUtils.js';

export const PRODUCT_APPROVAL_MESSAGE = '상품 등록이 완료되었습니다. 관리자 승인 이후 판매가 가능합니다.';

const getProductId = (product) => product?.productId ?? product?.id;

const getErrorMessage = (error) => error?.message || '이미지 업로드에 실패했습니다.';

export async function submitSellProduct(form, { createProduct, uploadProductImages }) {
  const product = await createProduct({
    title: form.title,
    description: form.description,
    price: parsePriceInput(form.price),
    category: form.category,
    regionId: Number(form.regionId)
  });

  let imageUploadError = '';
  const productId = getProductId(product);
  const images = Array.isArray(form.images) ? form.images : [];

  if (images.length > 0 && productId) {
    try {
      await uploadProductImages(productId, images);
    } catch (error) {
      imageUploadError = getErrorMessage(error);
    }
  }

  return { product, imageUploadError };
}
