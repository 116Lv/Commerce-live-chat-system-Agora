// 관심 지역 수정 비즈니스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.region.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private UserRegionRepository userRegionRepository;

    private RegionService createService() {
        return new RegionService(userRepository, regionRepository, userRegionRepository);
    }

    private User user() {
        User user = User.create("user@test.com", "encoded", "동네유저");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Region region(long id, String name) {
        Region region = Region.create(name, "R-%d".formatted(id), "서울", "강남구", name);
        ReflectionTestUtils.setField(region, "id", id);
        return region;
    }

    @Test
    void findRegions_returnsAllRegionsWhenKeywordIsBlank() {
        // given
        RegionService regionService = createService();
        Region first = region(1L, "서울 강남구 역삼동");
        Region second = region(2L, "서울 강남구 삼성동");
        when(regionRepository.findAll()).thenReturn(List.of(first, second));

        // when
        List<RegionResponse> responses = regionService.findRegions(" ");

        // then
        assertThat(responses)
                .extracting(RegionResponse::name)
                .containsExactly("서울 강남구 역삼동", "서울 강남구 삼성동");
    }

    @Test
    void findRegions_filtersRegionsByKeyword() {
        // given
        RegionService regionService = createService();
        Region region = region(1L, "서울 강남구 역삼동");
        when(regionRepository.findByNameContaining("역삼")).thenReturn(List.of(region));

        // when
        List<RegionResponse> responses = regionService.findRegions("역삼");

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).regionId()).isEqualTo(1L);
        assertThat(responses.get(0).name()).isEqualTo("서울 강남구 역삼동");
    }

    @Test
    void findSidoList_returnsDistinctSidoNames() {
        // given
        RegionService regionService = createService();
        when(regionRepository.findDistinctSidoOrderBySido()).thenReturn(List.of("경기도", "서울특별시"));

        // when
        List<String> sidoList = regionService.findSidoList();

        // then
        assertThat(sidoList).containsExactly("경기도", "서울특별시");
    }

    @Test
    void findSigunguList_returnsDistinctSigunguNamesForSido() {
        // given
        RegionService regionService = createService();
        when(regionRepository.findDistinctSigunguBySidoOrderBySigungu("서울특별시"))
                .thenReturn(List.of("강남구", "송파구"));

        // when
        List<String> sigunguList = regionService.findSigunguList("서울특별시");

        // then
        assertThat(sigunguList).containsExactly("강남구", "송파구");
    }

    @Test
    void findDongList_returnsRegionsForSidoAndSigungu() {
        // given
        RegionService regionService = createService();
        Region region = region(1L, "역삼동");
        when(regionRepository.findBySidoAndSigunguOrderByEupmyeondongAsc("서울", "강남구"))
                .thenReturn(List.of(region));

        // when
        List<RegionResponse> dongList = regionService.findDongList("서울", "강남구");

        // then
        assertThat(dongList).hasSize(1);
        assertThat(dongList.get(0).regionId()).isEqualTo(1L);
        assertThat(dongList.get(0).eupmyeondong()).isEqualTo("역삼동");
    }

    @Test
    void updatePreferredRegions_replacesPreviousRegions() {
        // given
        RegionService regionService = createService();
        User user = user();
        Region first = region(1L, "역삼동");
        Region second = region(2L, "삼성동");
        Region third = region(3L, "논현동");
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(regionRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(first, second, third));

        // when
        regionService.updatePreferredRegions(1L, new PreferredRegionUpdateRequest(List.of(1L, 2L, 3L), 2L));

        // then
        verify(userRegionRepository).deleteByUser(user);
        verify(userRegionRepository).saveAll(argThat(userRegions -> {
            assertThat(userRegions).hasSize(3);
            assertThat(userRegions)
                    .filteredOn(UserRegion::isPrimaryRegion)
                    .singleElement()
                    .extracting(userRegion -> userRegion.getRegion().getId())
                    .isEqualTo(2L);
            return true;
        }));
    }

    @Test
    void updatePreferredRegions_throwsInvalidRequestWhenRegionCountLessThanThree() {
        // given
        RegionService regionService = createService();
        PreferredRegionUpdateRequest request = new PreferredRegionUpdateRequest(List.of(1L, 2L), 1L);

        // when & then
        assertThatThrownBy(() -> regionService.updatePreferredRegions(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void updatePreferredRegions_throwsInvalidRequestWhenRegionDuplicated() {
        // given
        RegionService regionService = createService();
        PreferredRegionUpdateRequest request = new PreferredRegionUpdateRequest(List.of(1L, 1L, 2L), 1L);

        // when & then
        assertThatThrownBy(() -> regionService.updatePreferredRegions(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void updatePreferredRegions_throwsInvalidRequestWhenPrimaryRegionIsNotSelected() {
        // given
        RegionService regionService = createService();
        PreferredRegionUpdateRequest request = new PreferredRegionUpdateRequest(List.of(1L, 2L, 3L), 4L);

        // when & then
        assertThatThrownBy(() -> regionService.updatePreferredRegions(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    void updatePreferredRegions_throwsNotFoundWhenRegionMissing() {
        // given
        RegionService regionService = createService();
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user()));
        when(regionRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(region(1L, "역삼동")));

        // when & then
        assertThatThrownBy(
                () -> regionService.updatePreferredRegions(1L, new PreferredRegionUpdateRequest(List.of(1L, 2L, 3L), 1L))
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void updatePreferredRegions_throwsNotFoundWhenUserIsNotActive() {
        // given
        RegionService regionService = createService();
        PreferredRegionUpdateRequest request = new PreferredRegionUpdateRequest(List.of(1L, 2L, 3L), 1L);
        when(userRepository.findByIdAndStatusAndDeletedAtIsNull(1L, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> regionService.updatePreferredRegions(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
