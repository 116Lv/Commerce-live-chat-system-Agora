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
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;
    private final ProductSearchService productSearchService;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param productRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     * @param regionRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRegionRepository 데이터를 조회하고 저장하는 리포지토리
     * @param productSearchService 해당 기능의 비즈니스 로직을 처리하는 서비스
     */
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

    /**
     * 판매자가 입력한 상품 정보와 거래 지역으로 새 상품을 등록한다.
     * @param sellerId 상품 판매자 ID
     * @param request 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ProductResponse create(Long sellerId, ProductCreateRequest request) {
        User seller = getActiveUser(sellerId);
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

    /**
     * 데이터를 수정한다.
     * @param requesterId 가격 제안을 생성한 구매자 ID
     * @param productId 상품 ID
     * @param request 요청 본문
     * @return 클라이언트에 반환할 API 응답
     */
    @Transactional
    public ProductResponse update(Long requesterId, Long productId, ProductUpdateRequest request) {
        Product product = getActiveProduct(productId);
        validateSeller(product, requesterId);
        validateEditable(product);
        product.update(request.title(), request.description(), request.price(), request.category());
        productSearchService.evictSearchCache();
        return ProductResponse.from(product);
    }

    /**
     * 데이터를 삭제한다.
     * @param requesterId 가격 제안을 생성한 구매자 ID
     * @param productId 상품 ID
     */
    @Transactional
    public void delete(Long requesterId, Long productId) {
        Product product = getActiveProduct(productId);
        validateSeller(product, requesterId);
        product.delete();
        productSearchService.evictSearchCache();
    }

    /**
     * 'getProduct' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param productId 상품 ID
     * @return 클라이언트에 반환할 API 응답
     */
    public ProductResponse getProduct(Long productId) {
        return ProductResponse.from(getActiveProduct(productId));
    }

    /**
     * 'getProducts' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param viewerId 상품을 조회하는 회원 ID
     * @param regionId 지역 ID
     * @param pageable 페이지 요청 정보
     * @return 클라이언트에 반환할 API 응답
     */
    public List<ProductResponse> getProducts(Long viewerId, Long regionId, Pageable pageable) {
        List<Long> regionIds = resolveRegionIds(viewerId, regionId);
        Page<Product> products = (regionIds == null)
            ? productRepository.findAllByDeletedAtIsNullAndStatusNot(ProductStatus.HIDDEN, pageable)
            : productRepository.findAllByRegionIdInAndDeletedAtIsNullAndStatusNot(regionIds, ProductStatus.HIDDEN, pageable);
        return products.stream()
            .map(ProductResponse::from)
            .toList();
    }

    /**
     * 'getMyProducts' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param sellerId 상품 판매자 ID
     * @return 클라이언트에 반환할 API 응답
     */
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

    private User getActiveUser(Long userId) {
        return userRepository.findByIdAndStatusAndDeletedAtIsNull(userId, UserStatus.ACTIVE)
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
    private void validateEditable(Product product) {
        if (product.getStatus() != ProductStatus.SELLING) {
            throw new BusinessException(ErrorCode.CONFLICT, "판매 중인 상품만 수정할 수 있습니다.");
        }
    }
}
