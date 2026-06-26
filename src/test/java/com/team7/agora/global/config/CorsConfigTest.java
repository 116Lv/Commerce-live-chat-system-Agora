package com.team7.agora.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

class CorsConfigTest {

    @Test
    void corsConfigurationAllowsOnlyConfiguredOrigins() {
        CorsConfig config = new CorsConfig(new CorsProperties(List.of("https://app.agora.example")));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        request.addHeader("Origin", "https://app.agora.example");

        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("https://app.agora.example");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PATCH", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).contains("Authorization", "Content-Type");
    }
}
