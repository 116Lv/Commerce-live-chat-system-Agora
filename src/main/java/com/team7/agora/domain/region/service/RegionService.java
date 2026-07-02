// 관심 지역 수정 비즈니스 로직을 처리하는 서비스
package com.team7.agora.domain.region.service;

import com.team7.agora.domain.region.dto.request.PreferredRegionUpdateRequest;
import com.team7.agora.domain.region.dto.response.RegionResponse;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.region.repository.UserRegionRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.enums.UserStatus;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지역 관련 비즈니스 유스케이스를 처리하는 서비스이다.
 */
@Service
@Transactional(readOnly = true)
public class RegionService {

    private static final int MIN_REGION_COUNT = 3;
    private static final int MAX_REGION_COUNT = 5;

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;

    /**
     * 필요한 의존성을 주입받아 컴포넌트를 생성한다.
     * @param userRepository 데이터를 조회하고 저장하는 리포지토리
     * @param regionRepository 데이터를 조회하고 저장하는 리포지토리
     * @param userRegionRepository 데이터를 조회하고 저장하는 리포지토리
     */
    public RegionService(
            UserRepository userRepository,
            RegionRepository regionRepository,
            UserRegionRepository userRegionRepository
    ) {
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.userRegionRepository = userRegionRepository;
    }

    /**
     * 데이터를 수정한다.
     * @param userId 회원 ID
     * @param request 요청 본문
     */
    @Transactional
    public void updatePreferredRegions(Long userId, PreferredRegionUpdateRequest request) {
        List<Long> regionIds = request.regionIds();
        Long primaryRegionId = request.primaryRegionId();

        validateRegionSelection(regionIds, primaryRegionId);

        User user = userRepository.findByIdAndStatusAndDeletedAtIsNull(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
        List<Region> regions = regionRepository.findAllById(regionIds);
        if (regions.size() != regionIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "선택한 지역 중 존재하지 않는 지역이 있습니다.");
        }

        List<UserRegion> userRegions = regions.stream()
                .map(region -> UserRegion.of(user, region, primaryRegionId.equals(region.getId())))
                .toList();
        userRegionRepository.deleteByUser(user);
        userRegionRepository.saveAll(userRegions);
    }

    /**
     * 'findRegions' 메서드는 필요한 데이터를 조회해 호출한 쪽에 반환한다.
     * @param keyword 검색어
     * @return 클라이언트에 반환할 API 응답
     */
    public List<RegionResponse> findRegions(String keyword) {
        List<Region> regions = (keyword == null || keyword.isBlank())
                ? regionRepository.findAll()
                : regionRepository.findByNameContaining(keyword);

        return regions.stream()
                .map(RegionResponse::from)
                .toList();
    }

    /**
     * 전국 시/도 목록을 조회한다.
     * @return 클라이언트에 반환할 API 응답
     */
    public List<String> findSidoList() {
        return regionRepository.findDistinctSidoOrderBySido();
    }

    /**
     * 특정 시/도에 속한 시/군/구 목록을 조회한다.
     * @param sido 시도 이름
     * @return 클라이언트에 반환할 API 응답
     */
    public List<String> findSigunguList(String sido) {
        return regionRepository.findDistinctSigunguBySidoOrderBySigungu(sido);
    }

    /**
     * 특정 시/도, 시/군/구에 속한 읍/면/동 목록을 조회한다.
     * @param sido 시도 이름
     * @param sigungu 시군구 이름
     * @return 클라이언트에 반환할 API 응답
     */
    public List<RegionResponse> findDongList(String sido, String sigungu) {
        return regionRepository.findBySidoAndSigunguOrderByEupmyeondongAsc(sido, sigungu).stream()
                .map(RegionResponse::from)
                .toList();
    }

    private void validateRegionSelection(List<Long> regionIds, Long primaryRegionId) {
        if (regionIds == null || regionIds.size() < MIN_REGION_COUNT || regionIds.size() > MAX_REGION_COUNT) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "관심 지역은 3개 이상 5개 이하로 선택해야 합니다.");
        }
        if (new HashSet<>(regionIds).size() != regionIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "관심 지역은 중복 선택할 수 없습니다.");
        }
        if (primaryRegionId == null || !regionIds.contains(primaryRegionId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "대표 관심 지역은 선택한 관심 지역에 포함되어야 합니다.");
        }
    }
}
