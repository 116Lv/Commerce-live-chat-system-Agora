package com.team7.agora.domain.user.dto.response;

import com.team7.agora.domain.region.entity.UserRegion;
import com.team7.agora.domain.user.entity.User;
import java.util.List;

public record UserMeResponse(
        Long id,
        String email,
        String nickname,
        String phone,
        int smileScore,
        String role,
        String status,
        List<PreferredRegionResponse> preferredRegions
) {

    public record PreferredRegionResponse(
            Long regionId,
            String name,
            boolean primaryRegion
    ) {

        public static PreferredRegionResponse from(UserRegion userRegion) {
            return new PreferredRegionResponse(
                    userRegion.getRegion().getId(),
                    userRegion.getRegion().getName(),
                    userRegion.isPrimaryRegion()
            );
        }
    }

    public static UserMeResponse from(User user) {
        return from(user, List.of());
    }

    public static UserMeResponse from(User user, List<UserRegion> preferredRegions) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getPhone(),
                user.getSmileScore(),
                user.getRole().name(),
                user.getStatus().name(),
                preferredRegions.stream()
                        .map(PreferredRegionResponse::from)
                        .toList()
        );
    }
}
