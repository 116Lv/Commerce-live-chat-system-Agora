package com.team7.agora.domain.product.service;

import com.team7.agora.domain.product.dto.request.ProductCreateRequest;
import com.team7.agora.domain.product.dto.request.ProductUpdateRequest;
import com.team7.agora.domain.product.dto.response.ProductResponse;
import com.team7.agora.domain.product.entity.Product;
import com.team7.agora.domain.product.enums.ProductStatus;
import com.team7.agora.domain.product.repository.ProductRepository;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.region.repository.UserRegionRepository;
import com.team7.agora.domain.search.service.ProductSearchService;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;
    private final ProductSearchService productSearchService;

    public ProductService(
            ProductRepository productRepository,
            UserRepository userRepository,
            RegionRepository regionRepository,
            UserRegionRepository userRegionRepository,
            ProductSearchService productSearchService
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.userRegionRepository = userRegionRepository;
        this.productSearchService = productSearchService;
    }

    @Transactional
    public ProductResponse create(Long sellerId, ProductCreateRequest request) {
        User seller = getUser(sellerId);
        Region region = getRegion(request.regionId());

        Product product = Product.create(
                seller,
                region,
                request.title(),
                request.description(),
                request.price(),
                request.category()
        );
        Product savedProduct = productRepository.save(product);
        productSearchService.evictSearchCache();
        return ProductResponse.from(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long requesterId, Long productId, ProductUpdateRequest request) {
        Product product = getActiveProduct(productId);
        validateSeller(product, requesterId);
        product.update(request.title(), request.description(), request.price(), request.category());
        productSearchService.evictSearchCache();
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long requesterId, Long productId) {
        Product product = getActiveProduct(productId);
        validateSeller(product, requesterId);
        product.delete();
        productSearchService.evictSearchCache();
    }

    public ProductResponse getProduct(Long productId) {
        return ProductResponse.from(getActiveProduct(productId));
    }

    public List<ProductResponse> getProducts(Long viewerId, Long regionId, Pageable pageable) {
        List<Long> regionIds = resolveRegionIds(viewerId, regionId);
        Page<Product> products = (regionIds == null)
                ? productRepository.findAllByDeletedAtIsNullAndStatusNot(ProductStatus.HIDDEN, pageable)
                : productRepository.findAllByRegionIdInAndDeletedAtIsNullAndStatusNot(regionIds, ProductStatus.HIDDEN, pageable);
        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }

    public List<ProductResponse> getMyProducts(Long sellerId) {
        User seller = getUser(sellerId);
        return productRepository.findAllBySellerAndDeletedAtIsNull(seller).stream()
                .map(ProductResponse::from)
                .toList();
    }

    private List<Long> resolveRegionIds(Long viewerId, Long regionId) {
        if (regionId != null) {
            return List.of(regionId);
        }
        if (viewerId == null) {
            return null;
        }
        List<UserRegion> preferredRegions = userRegionRepository.findAllByUser(getUser(viewerId));
        if (preferredRegions.isEmpty()) {
            return null;
        }
        return preferredRegions.stream()
                .map(userRegion -> userRegion.getRegion().getId())
                .toList();
    }

    private Product getActiveProduct(Long productId) {
        return productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    private Region getRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "지역을 찾을 수 없습니다."));
    }

    private void validateSeller(Product product, Long requesterId) {
        if (!product.isSeller(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "상품 판매자만 수정하거나 삭제할 수 있습니다.");
        }
    }
}
