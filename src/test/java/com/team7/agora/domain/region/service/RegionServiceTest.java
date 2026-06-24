// 관심 지역 수정 비즈니스 규칙을 검증하는 단위 테스트
package com.team7.agora.domain.region.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.region.dto.request.PreferredRegionUpdateRequest;
import com.team7.agora.domain.region.entity.Region;
import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.region.repository.RegionRepository;
import com.team7.agora.domain.region.repository.UserRegionRepository;
import com.team7.agora.domain.user.entity.User;
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
    void updatePreferredRegions_replacesPreviousRegions() {
        // given
        RegionService regionService = createService();
        User user = user();
        Region first = region(1L, "역삼동");
        Region second = region(2L, "삼성동");
        Region third = region(3L, "논현동");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(user()));
        when(regionRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(region(1L, "역삼동")));

        // when & then
        assertThatThrownBy(
                () -> regionService.updatePreferredRegions(1L, new PreferredRegionUpdateRequest(List.of(1L, 2L, 3L), 1L))
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
