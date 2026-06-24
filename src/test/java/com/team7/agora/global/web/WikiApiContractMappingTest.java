package com.team7.agora.global.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.team7.agora.domain.chat.controller.ChatRoomController;
import com.team7.agora.domain.chat.dto.request.ChatRoomOpenRequest;
import com.team7.agora.domain.nego.controller.NegoChatRoomController;
import com.team7.agora.domain.nego.controller.NegoController;
import com.team7.agora.domain.nego.dto.request.NegoOfferCreateRequest;
import com.team7.agora.domain.payment.controller.PaymentController;
import com.team7.agora.domain.payment.controller.PaymentWebhookController;
import com.team7.agora.domain.payment.dto.request.PaymentConfirmByIdRequest;
import com.team7.agora.domain.payment.dto.request.PaymentPrepareRequest;
import com.team7.agora.domain.payment.dto.request.PaymentWebhookRequest;
import com.team7.agora.domain.region.controller.RegionController;
import com.team7.agora.domain.region.dto.request.PreferredRegionUpdateRequest;
import com.team7.agora.domain.report.controller.ReportController;
import com.team7.agora.domain.report.dto.request.ProductReportCreateByIdRequest;
import com.team7.agora.domain.search.controller.SearchController;
import com.team7.agora.global.auth.AuthUser;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

class WikiApiContractMappingTest {

    @Test
    void regionUpdate_supportsWikiMePath() throws NoSuchMethodException {
        Method method = RegionController.class.getDeclaredMethod(
            "updatePreferredRegions",
            AuthUser.class,
            PreferredRegionUpdateRequest.class
        );

        assertThat(method.getAnnotation(PutMapping.class).value())
            .contains("/api/users/me/regions");
    }

    @Test
    void chatRoomCreate_supportsBodyProductIdPath() throws NoSuchMethodException {
        Method method = ChatRoomController.class.getDeclaredMethod(
            "openRoomByRequest",
            AuthUser.class,
            ChatRoomOpenRequest.class
        );

        assertThat(method.getAnnotation(PostMapping.class).value())
            .contains("/rooms");
    }

    @Test
    void negoCreate_supportsChatRoomScopedWikiPath() throws NoSuchMethodException {
        RequestMapping classMapping = NegoChatRoomController.class.getAnnotation(RequestMapping.class);
        Method method = NegoChatRoomController.class.getDeclaredMethod(
            "createOffer",
            AuthUser.class,
            Long.class,
            NegoOfferCreateRequest.class
        );

        assertThat(classMapping.value()).contains("/api/chat/rooms");
        assertThat(method.getAnnotation(PostMapping.class).value())
            .contains("/{chatRoomId}/nego-offers");
    }

    @Test
    void negoDecisionEndpoints_supportPostMethod() throws NoSuchMethodException {
        Method accept = NegoController.class.getDeclaredMethod("acceptOffer", AuthUser.class, Long.class);
        Method reject = NegoController.class.getDeclaredMethod("rejectOffer", AuthUser.class, Long.class);
        Method extensionRequest = NegoController.class.getDeclaredMethod("requestExtension", AuthUser.class, Long.class);
        Method extensionApprove = NegoController.class.getDeclaredMethod("approveExtension", AuthUser.class, Long.class);
        Method extensionReject = NegoController.class.getDeclaredMethod("rejectExtension", AuthUser.class, Long.class);

        assertThat(methods(accept, reject, extensionRequest, extensionApprove, extensionReject))
            .allSatisfy(method -> assertThat(method.getAnnotation(RequestMapping.class).method())
                .contains(RequestMethod.POST));
    }

    @Test
    void paymentEndpoints_supportWikiBodyBasedPaths() throws NoSuchMethodException {
        Method prepare = PaymentController.class.getDeclaredMethod(
            "prepareByRequest",
            AuthUser.class,
            PaymentPrepareRequest.class
        );
        Method confirm = PaymentController.class.getDeclaredMethod(
            "confirmByRequest",
            AuthUser.class,
            PaymentConfirmByIdRequest.class
        );

        assertThat(prepare.getAnnotation(PostMapping.class).value()).contains("/prepare");
        assertThat(confirm.getAnnotation(PostMapping.class).value()).contains("/confirm");
    }

    @Test
    void paymentWebhook_supportsWikiSingularWebhookPath() throws NoSuchMethodException {
        RequestMapping classMapping = PaymentWebhookController.class.getAnnotation(RequestMapping.class);
        Method method = PaymentWebhookController.class.getDeclaredMethod(
            "portOneWebhook",
            String.class,
            PaymentWebhookRequest.class
        );

        assertThat(classMapping.value()).contains("/api/payments");
        assertThat(method.getAnnotation(PostMapping.class).value()).contains("/webhook");
    }

    @Test
    void productReport_supportsBodyProductIdPath() throws NoSuchMethodException {
        Method method = ReportController.class.getDeclaredMethod(
            "createProductReportByRequest",
            AuthUser.class,
            ProductReportCreateByIdRequest.class
        );

        assertThat(method.getAnnotation(PostMapping.class).value()).contains("/products");
    }

    @Test
    void realtimePopularKeywords_supportsWikiPath() throws NoSuchMethodException {
        Method method = SearchController.class.getDeclaredMethod("popularKeywords", int.class);

        assertThat(method.getAnnotation(org.springframework.web.bind.annotation.GetMapping.class).value())
            .contains("/search/keywords/realtime");
    }

    private Iterable<Method> methods(Method... methods) {
        return Arrays.asList(methods);
    }
}
