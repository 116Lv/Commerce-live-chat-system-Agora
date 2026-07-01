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
    void smileScoreRequiresAuthenticatedUser() throws NoSuchMethodException {
        Method method = SmileScoreController.class.getMethod("getSmileScore", Long.class);

        assertThat(method.getAnnotation(PreAuthorize.class))
                .extracting(PreAuthorize::value)
                .isEqualTo("isAuthenticated()");
    }

    @Test
    void adminUserControllerRequiresUserAdminAuthorization() {
        assertThat(AdminUserController.class.getAnnotation(PreAuthorize.class))
                .extracting(PreAuthorize::value)
                .isEqualTo("hasAuthority('USER_MANAGE')");
    }

    @Test
    void adminReportControllerRequiresReportAdminAuthorization() throws NoSuchMethodException {
        assertThat(preAuthorizeValue("getUserReports", com.team7.agora.global.auth.AdminPrincipal.class,
                String.class, String.class))
                .isEqualTo("hasAuthority('REPORT_MANAGE')");
        assertThat(preAuthorizeValue("resolveUserReport", com.team7.agora.global.auth.AdminPrincipal.class,
                Long.class, com.team7.agora.domain.admin.dto.request.AdminReportResolveRequest.class))
                .isEqualTo("hasAuthority('REPORT_MANAGE')");
        assertThat(preAuthorizeValue("getProductReports", com.team7.agora.global.auth.AdminPrincipal.class,
                String.class, String.class))
                .isEqualTo("hasAuthority('REPORT_MANAGE')");
        assertThat(preAuthorizeValue("resolveProductReport", com.team7.agora.global.auth.AdminPrincipal.class,
                Long.class, com.team7.agora.domain.admin.dto.request.AdminReportResolveRequest.class))
                .isEqualTo("hasAuthority('REPORT_MANAGE')");
    }

    private String preAuthorizeValue(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        return AdminReportController.class.getMethod(methodName, parameterTypes)
                .getAnnotation(PreAuthorize.class)
                .value();
    }
}
