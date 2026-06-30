// 상품 찜 등록/취소/내 찜 목록을 처리하는 서비스
package com.team7.agora.domain.product.service;

import com.team7.agora.domain.product.dto.response.ProductLikeResponse;
import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductImage;
import com.team7.agora.domain.product.entity.ProductLike;
import com.team7.agora.domain.product.exception.ProductException;
import com.team7.agora.domain.product.repository.ProductImageRepository;
import com.team7.agora.domain.product.repository.ProductLikeRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 좋아요 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ProductLikeService {

    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;
    private final UserRepository userRepository;
    private final ProductImageRepository productImageRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productRepository 데이터를 조회하고 저장하는 리포지토리
     * @param productLikeRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public ProductLikeService(
        ProductRepository productRepository,
        ProductLikeRepository productLikeRepository,
        UserRepository userRepository,
        ProductImageRepository productImageRepository
    ) {
        this.productRepository = productRepository;
        this.productLikeRepository = productLikeRepository;
        this.userRepository = userRepository;
        this.productImageRepository = productImageRepository;
    }

    /**
     * 'like' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param userId 회원 ID
     * @param productId 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ProductLikeResponse like(Long userId, Long productId) {
        Product product = getActiveProduct(productId);
        User user = getUser(userId);

        if (productLikeRepository.existsByProductAndUser(product, user)) {
            throw new ProductException(ErrorCode.CONFLICT, "이미 찜한 상품입니다.");
        }

        try {
            productLikeRepository.save(ProductLike.create(product, user));
        } catch (DataIntegrityViolationException e) {
            throw new ProductException(ErrorCode.CONFLICT, "이미 찜한 상품입니다.");
        }
        productRepository.increaseLikeCount(productId);
        return ProductLikeResponse.of(product, true, productRepository.findLikeCountById(productId));
    }

    /**
     * 'unlike' 메서드가 맡은 기능을 수행하고 필요한 결과를 반환한다.
     * @param userId 회원 ID
     * @param productId 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ProductLikeResponse unlike(Long userId, Long productId) {
        Product product = getActiveProduct(productId);
        User user = getUser(userId);

        ProductLike productLike = productLikeRepository.findByProductAndUser(product, user)
            .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "찜하지 않은 상품입니다."));

        productLikeRepository.delete(productLike);
        productRepository.decreaseLikeCount(productId);
        return ProductLikeResponse.of(product, false, productRepository.findLikeCountById(productId));
    }

    /**
     * 'getMyLikedProducts' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param userId 회원 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public List<ProductResponse> getMyLikedProducts(Long userId) {
        User user = getUser(userId);
        List<Product> products = productLikeRepository.findAllByUser(user).stream()
            .map(ProductLike::getProduct)
            .toList();
        Map<Long, String> primaryImageUrls = findPrimaryImageUrls(products.stream().map(Product::getId).toList());
        return products.stream()
            .map(product -> ProductResponse.from(product, true, primaryImageUrls.get(product.getId())))
            .toList();
    }

    private Map<Long, String> findPrimaryImageUrls(List<Long> productIds) {
        Map<Long, String> primaryImageUrls = new LinkedHashMap<>();
        if (productIds.isEmpty()) {
            return primaryImageUrls;
        }
        for (ProductImage image : productImageRepository.findAllByProductIdInOrderByProductIdAscSortOrderAsc(productIds)) {
            primaryImageUrls.putIfAbsent(image.getProduct().getId(), image.getImageUrl());
        }
        return primaryImageUrls;
    }

    private Product getActiveProduct(Long productId) {
        return productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private User getUser(Long userId) {
        return userRepository.findByIdAndStatusAndDeletedAtIsNull(userId, UserStatus.ACTIVE)
            .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
