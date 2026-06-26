package com.team7.agora.domain.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.admin.controller.AdminReportController;
import com.team7.agora.domain.admin.controller.AdminUserController;
import com.team7.agora.domain.user.controller.SmileScoreController;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class ControllerAuthorizationTest {

    @Test
    void smileScoreRequiresOwnerOrUserAdminAuthorization() throws NoSuchMethodException {
        Method method = SmileScoreController.class.getMethod("getSmileScore", Long.class);

        assertThat(method.getAnnotation(PreAuthorize.class))
                .extracting(PreAuthorize::value)
                .isEqualTo("#userId == authentication.principal.userId or hasAnyAuthority('ROOT_ADMIN', 'USER_ADMIN')");
    }

    @Test
    void adminUserControllerRequiresUserAdminAuthorization() {
        assertThat(AdminUserController.class.getAnnotation(PreAuthorize.class))
                .extracting(PreAuthorize::value)
                .isEqualTo("hasAnyAuthority('ROOT_ADMIN', 'USER_ADMIN')");
    }

    @Test
    void adminReportControllerRequiresReportAdminAuthorization() throws NoSuchMethodException {
        assertThat(preAuthorizeValue("getUserReports", com.team7.agora.global.auth.CustomUserDetails.class))
                .isEqualTo("hasAnyAuthority('ROOT_ADMIN', 'USER_ADMIN')");
        assertThat(preAuthorizeValue("resolveUserReport", com.team7.agora.global.auth.CustomUserDetails.class,
                Long.class, com.team7.agora.domain.admin.dto.request.AdminReportResolveRequest.class))
                .isEqualTo("hasAnyAuthority('ROOT_ADMIN', 'USER_ADMIN')");
        assertThat(preAuthorizeValue("getProductReports", com.team7.agora.global.auth.CustomUserDetails.class))
                .isEqualTo("hasAnyAuthority('ROOT_ADMIN', 'PRODUCT_ADMIN')");
        assertThat(preAuthorizeValue("resolveProductReport", com.team7.agora.global.auth.CustomUserDetails.class,
                Long.class, com.team7.agora.domain.admin.dto.request.AdminReportResolveRequest.class))
                .isEqualTo("hasAnyAuthority('ROOT_ADMIN', 'PRODUCT_ADMIN')");
    }

    private String preAuthorizeValue(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return AdminReportController.class.getMethod(methodName, parameterTypes)
                .getAnnotation(PreAuthorize.class)
                .value();
    }
}
