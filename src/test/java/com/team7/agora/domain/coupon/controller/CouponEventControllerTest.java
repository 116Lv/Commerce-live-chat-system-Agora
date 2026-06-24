package com.team7.agora.domain.coupon.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team7.agora.domain.coupon.service.CouponQueryService;
import com.team7.agora.global.response.ApiResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponEventControllerTest {

    @Mock
    private CouponQueryService couponQueryService;

    @Test
    void listReturnsSuccessEnvelope() {
        when(couponQueryService.listActiveEvents()).thenReturn(List.of());
        CouponEventController controller = new CouponEventController(couponQueryService);

        ApiResponse<?> response = controller.list();

        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.data()).isEqualTo(List.of());
    }
}
