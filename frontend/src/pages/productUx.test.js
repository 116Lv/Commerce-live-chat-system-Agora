import assert from 'node:assert/strict';
import { describe, test } from 'node:test';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

const __dirname = dirname(fileURLToPath(import.meta.url));
const readSource = (relativePath) => readFileSync(resolve(__dirname, relativePath), 'utf8');

describe('product list and card UX source', () => {
  test('ProductCard uses backend display labels and image fallbacks', () => {
    const source = readSource('../components/ProductCard.jsx');

    assert.match(source, /getProductTitle/);
    assert.match(source, /getProductCategoryLabel/);
    assert.match(source, /getProductRegionLabel/);
    assert.match(source, /getProductStatusLabel/);
    assert.match(source, /getProductImageUrl/);
    assert.doesNotMatch(source, /item\.title \? `\$\{item\.title\} 이미지`/);
    assert.doesNotMatch(source, /item\.title \|\| '제목 없음'/);
    assert.doesNotMatch(source, /item\.category \? <span>\{item\.category\}/);
    assert.doesNotMatch(source, /item\.imageUrl \? <img/);
  });

  test('ProductListPage imports PRODUCT_CATEGORIES and renders category with Form.Select', () => {
    const source = readSource('./ProductListPage.jsx');

    assert.match(source, /PRODUCT_CATEGORIES/);
    assert.match(source, /<Form\.Select[\s\S]{0,200}value=\{draft\.category\}/);
    assert.match(source, /PRODUCT_CATEGORIES\.map/);
    assert.doesNotMatch(source, /<Form\.Control[\s\S]{0,200}value=\{draft\.category\}/);
  });

  test('ProductListPage uses staged normalized region select options', () => {
    const source = readSource('./ProductListPage.jsx');

    assert.match(source, /getRegionSelectOptions/);
    assert.match(source, /getChildRegionOptions/);
    assert.match(source, /selectedParentRegionId/);
    assert.match(source, /handleParentRegionChange/);
    assert.match(source, /parentRegionOptions\.map/);
    assert.match(source, /childRegionOptions\.map/);
  });

  test('ProductCard does not render chat metadata', () => {
    const source = readSource('../components/ProductCard.jsx');

    assert.doesNotMatch(source, /MessageCircle/);
    assert.doesNotMatch(source, /chatCount/);
  });

  test('ProductCard exposes a whole-card link layer without interactive card semantics', () => {
    const source = readSource('../components/ProductCard.jsx');

    assert.match(source, /className="product-card-link"/);
    assert.match(source, /data-product-card-link/);
    assert.match(source, /to=\{detailTo\}/);
    assert.doesNotMatch(source, /useNavigate/);
    assert.doesNotMatch(source, /role="link"/);
    assert.doesNotMatch(source, /onKeyDown/);
  });

  test('ProductCard renders an actionable heart button that stops propagation', () => {
    const source = readSource('../components/ProductCard.jsx');

    assert.match(source, /product-card-heart/);
    assert.match(source, /stopPropagation\(\)/);
    assert.match(source, /<Heart/);
    assert.match(source, /onLikeToggle/);
    assert.match(source, /<button[\s\S]{0,180}product-card-heart/);
    assert.match(source, /handleLikeToggle/);
  });

  test('ProductListPage wires card like actions to product APIs and local state', () => {
    const source = readSource('./ProductListPage.jsx');

    assert.match(source, /likeProduct/);
    assert.match(source, /unlikeProduct/);
    assert.match(source, /setProductOverrides/);
    assert.match(source, /handleProductLikeToggle/);
    assert.match(source, /onLikeToggle=\{handleProductLikeToggle\}/);
  });
});

describe('header and report modal UX source', () => {
  test('UserLayout renders account dropdown without a direct my page nav link', () => {
    const source = readSource('../layouts/UserLayout.jsx');

    assert.match(source, /NavDropdown/);
    assert.match(source, /account-dropdown/);
    assert.match(source, /getAccountLabel/);
    assert.match(source, /logoutUser/);
    assert.match(source, /useNavigate/);
    assert.match(source, /handleLogout/);
    assert.match(source, /navigate\('\/', \{ replace: true \}\)/);
    assert.doesNotMatch(source, /<UserNavLink to="\/me">/);
  });

  test('UserLayout account dropdown links to existing protected account routes', () => {
    const source = readSource('../layouts/UserLayout.jsx');

    for (const route of ['/me', '/me/products', '/me/likes', '/chat', '/me/trades']) {
      assert.match(source, new RegExp(`to: '${route}'`));
    }
  });

  test('ReportModal hides manual target controls when caller supplies a product or user target', () => {
    const source = readSource('../features/reports/ReportModal.jsx');

    assert.match(source, /const hasProductTarget = productId != null && productId !== ''/);
    assert.match(source, /const hasUserTarget = userId != null && userId !== ''/);
    assert.match(source, /const hasProvidedTarget = Boolean\(inferredTargetType\)/);
    assert.match(source, /!hasProvidedTarget \? \(/);
    assert.match(source, /controlId="reportTargetType"/);
    assert.match(source, /controlId="reportTargetId"/);
    assert.doesNotMatch(source, /productId \? 'product'/);
    assert.doesNotMatch(source, /productId \|\| userId \|\| ''/);
  });

  test('ReportModal submits inferred product and user targets from props', () => {
    const source = readSource('../features/reports/ReportModal.jsx');

    assert.match(source, /if \(submitting\) \{[\s\S]{0,40}return;[\s\S]{0,20}\}/);
    assert.match(source, /const reportTargetType = hasProvidedTarget \? inferredTargetType : targetType/);
    assert.match(source, /const reportTargetId = hasProvidedTarget \? inferredTargetId : targetId/);
    assert.match(source, /const isMissingTargetId = reportTargetId == null \|\| reportTargetId === ''/);
    assert.match(source, /if \(!reportTargetType \|\| isMissingTargetId\)/);
    assert.match(source, /createProductReport\(\{ productId: Number\(reportTargetId\), reason \}\)/);
    assert.match(source, /createUserReport\(\{ reportedUserId: Number\(reportTargetId\), reason \}\)/);
    assert.match(source, /useEffect\(\(\) => \{/);
  });
});

describe('sell and edit product form UX source', () => {
  const formPages = [
    ['SellProductPage', './SellProductPage.jsx'],
    ['EditProductPage', './EditProductPage.jsx']
  ];

  test('product form pages use price formatter/parser and validation helpers', () => {
    for (const [pageName, relativePath] of formPages) {
      const source = readSource(relativePath);

      assert.match(source, /formatPriceInput/, `${pageName} should format price input for display`);
      assert.match(source, /parsePriceInput/, `${pageName} should parse price before submit`);
      assert.match(source, /validateProductForm/, `${pageName} should validate before submit`);
      assert.match(source, /const validationErrors = validateProductForm/, `${pageName} should collect field errors`);
      assert.doesNotMatch(source, /type="number"/, `${pageName} should not render a native number input for price`);
      assert.doesNotMatch(source, /Number\(form\.price\)/, `${pageName} should not submit Number(form.price)`);
    }
  });

  test('edit product validation does not block on an uneditable missing region', () => {
    const sellSource = readSource('./SellProductPage.jsx');
    const editSource = readSource('./EditProductPage.jsx');

    assert.match(sellSource, /const validationErrors = validateProductForm\(form\)/);
    assert.match(editSource, /EDIT_VALIDATION_REGION_ID/);
    assert.match(editSource, /const validationErrors = validateProductForm\(\{[\s\S]{0,120}\.\.\.form,[\s\S]{0,120}regionId: form\.regionId \|\| EDIT_VALIDATION_REGION_ID[\s\S]{0,80}\}\)/);
  });

  test('product form pages disable native browser validation', () => {
    for (const [pageName, relativePath] of formPages) {
      const source = readSource(relativePath);

      assert.match(source, /<Form[\s\S]{0,120}noValidate/, `${pageName} should let validateProductForm control field errors`);
    }
  });

  test('product form pages render category with PRODUCT_CATEGORIES select options', () => {
    for (const [pageName, relativePath] of formPages) {
      const source = readSource(relativePath);

      assert.match(source, /PRODUCT_CATEGORIES/, `${pageName} should import product categories`);
      assert.match(source, /<Form\.Select[\s\S]{0,300}value=\{form\.category\}/, `${pageName} should use a category select`);
      assert.match(source, /PRODUCT_CATEGORIES\.map/, `${pageName} should render fixed category options`);
      assert.doesNotMatch(source, /<Form\.Control[\s\S]{0,200}value=\{form\.category\}/, `${pageName} should not use free-text category input`);
    }
  });

  test('product form pages expose image preview and local remove affordances', () => {
    for (const [pageName, relativePath] of formPages) {
      const source = readSource(relativePath);
      const dropzoneSource = readSource('../components/ProductImageDropzone.jsx');

      assert.match(source, /URL\.createObjectURL/, `${pageName} should create a local preview URL`);
      assert.match(source, /URL\.revokeObjectURL/, `${pageName} should clean up local preview URLs`);
      assert.match(source, /ProductImageDropzone/, `${pageName} should render the shared image dropzone`);
      assert.match(dropzoneSource, /product-image-upload/, `${pageName} should render a custom upload box`);
      assert.match(dropzoneSource, /product-image-main-badge/, `${pageName} should show the main-image badge`);
      assert.match(source, /removeSelectedImage/, `${pageName} should provide selected local image removal`);
      assert.match(dropzoneSource, /useDropzone/, `${pageName} should use dropzone file input handling`);
    }
  });

  test('edit product form clears selected dropzone images after successful save', () => {
    const source = readSource('./EditProductPage.jsx');

    assert.match(source, /await productState\.reload\(\);[\s\S]{0,120}updateField\('images', \[\]\)/);
  });

  test('product form pages show description guidance, max length, and counter markers', () => {
    for (const [pageName, relativePath] of formPages) {
      const source = readSource(relativePath);

      assert.match(source, /maxLength=\{2000\}/, `${pageName} should cap descriptions at 2000 characters`);
      assert.match(source, /form\.description\.length/, `${pageName} should render a live character count`);
      assert.match(source, /description-guide/, `${pageName} should render description guide text`);
    }
  });

  test('sell product form renders staged region selectors or fallback options and preview card', () => {
    const sellSource = readSource('./SellProductPage.jsx');

    assert.match(sellSource, /getRegionSelectOptions/);
    assert.match(sellSource, /getChildRegionOptions/);
    assert.match(sellSource, /selectedParentRegionId/);
    assert.match(sellSource, /product-form-preview/);
    assert.match(sellSource, /ProductCard/);
  });

  test('edit product form keeps region display-only and previews persisted region', () => {
    const editSource = readSource('./EditProductPage.jsx');

    assert.match(editSource, /persistedRegionLabel/);
    assert.match(editSource, /지역 변경은 새 상품 등록에서만 가능해요/);
    assert.match(editSource, /product-form-preview/);
    assert.match(editSource, /ProductCard/);
    assert.doesNotMatch(editSource, /getRegions/);
    assert.doesNotMatch(editSource, /getRegionSelectOptions/);
    assert.doesNotMatch(editSource, /getChildRegionOptions/);
    assert.doesNotMatch(editSource, /selectedParentRegionId/);
    assert.doesNotMatch(editSource, /updateField\('regionId'/);
    assert.doesNotMatch(editSource, /regionName: selectedRegionLabel/);
  });
});

describe('home product search UX source', () => {
  test('HomePage uses fixed categories and staged normalized region filters', () => {
    const source = readSource('./UserPlaceholderPages.jsx');

    assert.match(source, /PRODUCT_CATEGORIES/);
    assert.match(source, /getRegionSelectOptions/);
    assert.match(source, /getChildRegionOptions/);
    assert.match(source, /selectedParentRegionId/);
    assert.match(source, /handleParentRegionChange/);
    assert.match(source, /parentRegionOptions\.map/);
    assert.match(source, /childRegionOptions\.map/);
    assert.match(source, /<Form\.Select[\s\S]{0,300}value=\{draft\.category\}/);
    assert.doesNotMatch(source, /<Form\.Control[\s\S]{0,220}value=\{draft\.category\}/);
  });

  test('HomePage wires card like actions to product APIs and local state', () => {
    const source = readSource('./UserPlaceholderPages.jsx');

    assert.match(source, /likeProduct/);
    assert.match(source, /unlikeProduct/);
    assert.match(source, /setProductOverrides/);
    assert.match(source, /handleProductLikeToggle/);
    assert.match(source, /onLikeToggle=\{handleProductLikeToggle\}/);
  });
});

describe('product detail, favorites, and seller UX source', () => {
  test('ProductDetailPage toggles liked state using like and unlike APIs', () => {
    const source = readSource('./ProductDetailPage.jsx');

    assert.match(source, /likeProduct/);
    assert.match(source, /unlikeProduct/);
    assert.match(source, /handleLikeToggle/);
    assert.match(source, /product\.liked/);
    assert.match(source, /product\.likeCount/);
    assert.match(source, /liked \? unlikeProduct\(productId\) : likeProduct\(productId\)/);
    assert.doesNotMatch(source, /handleLike = async/);
  });

  test('ProductDetailPage renders response labels for status category region and seller', () => {
    const source = readSource('./ProductDetailPage.jsx');

    assert.match(source, /getProductCategoryLabel/);
    assert.match(source, /getProductRegionLabel/);
    assert.match(source, /getProductStatusLabel/);
    assert.match(source, /sellerNickname/);
    assert.match(source, /formatDateTime/);
    assert.match(source, /product\.createdAt/);
    assert.match(source, /작성일/);
  });

  test('ProductDetailPage guides buyers to chat instead of starting a trade directly', () => {
    const source = readSource('./ProductDetailPage.jsx');

    assert.match(source, /handleOpenChat/);
    assert.match(source, /openChatRoom/);
    assert.doesNotMatch(source, /startTrade/);
    assert.doesNotMatch(source, /handleStartTrade/);
    assert.doesNotMatch(source, /ShoppingCart/);
  });

  test('ProductDetailPage displays seller smile score', () => {
    const source = readSource('./ProductDetailPage.jsx');
    const apiSource = readSource('../api/mypageApi.js');

    assert.match(apiSource, /getSmileScore/);
    assert.match(source, /getSmileScore/);
    assert.match(source, /sellerSmileScore/);
    assert.match(source, /스마일/);
  });

  test('ProductDetailPage renders uploaded images with a direct image carousel', () => {
    const source = readSource('./ProductDetailPage.jsx');

    assert.match(source, /getProductImageUrls/);
    assert.match(source, /currentImageUrl/);
    assert.match(source, /<img src=\{currentImageUrl\}/);
    assert.match(source, /product-detail-carousel-button/);
    assert.match(source, /product-detail-image-counter/);
    assert.match(source, /activeImageIndex/);
    assert.doesNotMatch(source, /swiper\/react/);
    assert.doesNotMatch(source, /<Swiper/);
  });

  test('LikedProductsPage removes unliked cards immediately and shows product CTA empty state', () => {
    const source = readSource('./LikedProductsPage.jsx');

    assert.match(source, /unlikeProduct/);
    assert.match(source, /removedProductIds/);
    assert.match(source, /setRemovedProductIds/);
    assert.match(source, /handleRemoveLike/);
    assert.match(source, /empty-state-with-action/);
    assert.match(source, /to="\/products"/);
  });

  test('MyProductsPage exposes seller status and owner action affordances', () => {
    const source = readSource('./MyProductsPage.jsx');

    assert.match(source, /deleteProduct/);
    assert.match(source, /updateProductStatus/);
    assert.match(source, /getProductStatusLabel/);
    assert.match(source, /seller-product-actions/);
    assert.match(source, /handleDeleteProduct/);
    assert.match(source, /handleUpdateStatus/);
    assert.match(source, /setProducts/);
    assert.match(source, /upsertProduct/);
    assert.match(source, /statusUpdatingProductId/);
    assert.match(source, /SELLING/);
    assert.match(source, /RESERVED/);
    assert.match(source, /SOLD/);
    assert.match(source, /footerActionLabel="수정"/);
    assert.match(source, /판매중으로 변경|예약중으로 변경|판매완료로 변경/);
  });
});
