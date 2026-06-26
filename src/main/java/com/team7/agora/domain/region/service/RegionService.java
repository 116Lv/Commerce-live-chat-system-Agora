// 관심 지역 수정 비즈니스 로직을 처리하는 서비스
package com.team7.agora.domain.region.service;

import com.team7.agora.domain.region.dto.request.PreferredRegionUpdateRequest;
import com.team7.agora.domain.region.dto.response.RegionResponse;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.region.repository.UserRegionRepository;
import com.team7.agora.domain.user.entity.User;
import com.team7.agora.domain.user.repository.UserRepository;
import com.team7.agora.global.exception.BusinessException;
import com.team7.agora.global.exception.ErrorCode;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 유스케이스를 조정하는 서비스이다.
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
     * 의존성을 주입받아 인스턴스를 생성한다.
     * @param userRepository 입력 값
     * @param regionRepository 입력 값
     * @param userRegionRepository 입력 값
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
     * @param userId 입력 값
     * @param request 입력 값
     */
    @Transactional
    public void updatePreferredRegions(Long userId, PreferredRegionUpdateRequest request) {
        List<Long> regionIds = request.regionIds();
        Long primaryRegionId = request.primaryRegionId();

        validateRegionSelection(regionIds, primaryRegionId);

        User user = userRepository.findById(userId)
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
     * 데이터를 반환한다.
     * @param keyword 입력 값
     * @return 처리 결과
     */
    public List<RegionResponse> findRegions(String keyword) {
        List<Region> regions = (keyword == null || keyword.isBlank())
                ? regionRepository.findAll()
                : regionRepository.findByNameContaining(keyword);

        return regions.stream()
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
