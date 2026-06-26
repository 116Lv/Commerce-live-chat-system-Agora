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
 * 상품 이미지 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageStorageClient imageStorageClient;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productRepository 데이터를 조회하고 저장하는 리포지토리
     * @param productImageRepository 데이터를 조회하고 저장하는 리포지토리
     * @param imageStorageClient 외부 시스템 또는 저장소와 통신하는 클라이언트
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
     * 상품 이미지 파일을 저장하고 상품 이미지 정보를 등록한다.
     * @param sellerId 상품 판매자 ID
     * @param productId 상품 ID
     * @param file 업로드 파일
     * @return 클라이언트에 반환할 API 응답
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
