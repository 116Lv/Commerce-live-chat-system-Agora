// 상품 이미지 업로드를 처리하는 서비스
package com.team7.agora.domain.product.service;

import com.team7.agora.domain.product.dto.response.ProductImageResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.exception.ProductException;
import com.team7.agora.domain.product.repository.ProductImageRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.global.exception.ErrorCode;
import com.team7.agora.global.storage.ImageStorageClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageStorageClient imageStorageClient;

    /**
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param productRepository 입력 값
     * @param productImageRepository 입력 값
     * @param imageStorageClient 입력 값
     */
    public ProductImageService(
        ProductRepository productRepository,
        ProductImageRepository productImageRepository,
        ImageStorageClient imageStorageClient
    ) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.imageStorageClient = imageStorageClient;
    }

    /**
     * 요청한 동작을 처리한다.
     * @param sellerId 입력 값
     * @param productId 입력 값
     * @param file 입력 값
     * @return 처리 결과
     */
    @Transactional
    public ProductImageResponse upload(Long sellerId, Long productId, MultipartFile file) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (!product.isSeller(sellerId)) {
            throw new ProductException(ErrorCode.FORBIDDEN, "상품 판매자만 이미지를 업로드할 수 있습니다.");
        }

        String imageUrl = imageStorageClient.store("products", file);
        int sortOrder = productImageRepository.countByProduct(product);
        ProductImage productImage = productImageRepository.save(ProductImage.create(product, imageUrl, sortOrder));

        return ProductImageResponse.from(productImage);
    }
}
