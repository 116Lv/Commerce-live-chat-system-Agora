package com.team7.agora.domain.payment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.payment.dto.request.PaymentWebhookRequest;
import com.team7.agora.domain.payment.dto.response.PaymentResponse;
import com.team7.agora.domain.payment.exception.PaymentException;
import com.team7.agora.domain.payment.service.PaymentWebhookService;
import com.team7.agora.domain.payment.service.PaymentWebhookVerifier;
import com.team7.agora.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerTest {

    @Mock
    private PaymentWebhookService paymentWebhookService;

    @Mock
    private PaymentWebhookVerifier paymentWebhookVerifier;

    @Test
    void portOneWebhookRejectsMissingSignature() {
        PaymentWebhookController controller = new PaymentWebhookController(paymentWebhookService, paymentWebhookVerifier, new ObjectMapper());
        String body = "{\"orderId\":\"order-1\",\"paymentKey\":\"payment-key\",\"status\":\"PAID\"}";
        when(paymentWebhookVerifier.isValid(body, null, null)).thenReturn(false);

        assertThatThrownBy(() -> controller.portOneWebhook(null, null, body))
            .isInstanceOf(PaymentException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.UNAUTHORIZED);
        verify(paymentWebhookService, never()).handlePaid("order-1", "payment-key");
    }

    @Test
    void portOneWebhookProcessesPaidEventWithValidSignature() {
        PaymentWebhookController controller = new PaymentWebhookController(paymentWebhookService, paymentWebhookVerifier, new ObjectMapper());
        String body = "{\"orderId\":\"order-1\",\"paymentKey\":\"payment-key\",\"status\":\"PAID\"}";
        PaymentResponse response = new PaymentResponse(
            1L,
            10L,
            2L,
            BigDecimal.valueOf(50000),
            "order-1",
            "payment-key",
            "PAID",
            100L
        );
        when(paymentWebhookVerifier.isValid(body, "100", "signature")).thenReturn(true);
        when(paymentWebhookService.handlePaid("order-1", "payment-key")).thenReturn(response);

        var result = controller.portOneWebhook("100", "signature", body);

        assertThat(result.getBody().data()).isEqualTo(response);
    }
}
