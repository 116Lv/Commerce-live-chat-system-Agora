// 상품 찜 등록/취소/내 찜 목록을 처리하는 서비스
package com.team7.agora.domain.product.service;

import com.team7.agora.domain.product.dto.response.ProductLikeResponse;
import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.entity.ProductLike;
import com.team7.agora.domain.product.exception.ProductException;
import com.team7.agora.domain.product.repository.ProductLikeRepository;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductLikeService {

    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;
    private final UserRepository userRepository;

    public ProductLikeService(
        ProductRepository productRepository,
        ProductLikeRepository productLikeRepository,
        UserRepository userRepository
    ) {
        this.productRepository = productRepository;
        this.productLikeRepository = productLikeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProductLikeResponse like(Long userId, Long productId) {
        Product product = getActiveProduct(productId);
        User user = getUser(userId);

        if (productLikeRepository.existsByProductAndUser(product, user)) {
            throw new ProductException(ErrorCode.CONFLICT, "이미 찜한 상품입니다.");
        }

        productLikeRepository.save(ProductLike.create(product, user));
        product.increaseLikeCount();
        return ProductLikeResponse.of(product, true);
    }

    @Transactional
    public ProductLikeResponse unlike(Long userId, Long productId) {
        Product product = getActiveProduct(productId);
        User user = getUser(userId);

        ProductLike productLike = productLikeRepository.findByProductAndUser(product, user)
            .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "찜하지 않은 상품입니다."));

        productLikeRepository.delete(productLike);
        product.decreaseLikeCount();
        return ProductLikeResponse.of(product, false);
    }

    public List<ProductResponse> getMyLikedProducts(Long userId) {
        User user = getUser(userId);
        return productLikeRepository.findAllByUser(user).stream()
            .map(ProductLike::getProduct)
            .map(ProductResponse::from)
            .toList();
    }

    private Product getActiveProduct(Long productId) {
        return productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
