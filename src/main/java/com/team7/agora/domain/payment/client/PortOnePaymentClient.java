package com.team7.agora.domain.payment.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team7.agora.domain.payment.exception.PaymentException;
import com.team7.agora.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * PortOne payment provider client.
 */
@Component
@Profile("prod")
public class PortOnePaymentClient implements PaymentClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;
    private final String apiSecret;

    public PortOnePaymentClient(
        @Value("${portone.api-base-url:https://api.portone.io}") String apiBaseUrl,
        @Value("${portone.api-secret}") String apiSecret
    ) {
        this(HttpClient.newHttpClient(), new ObjectMapper(), apiBaseUrl, apiSecret);
    }

    PortOnePaymentClient(HttpClient httpClient, ObjectMapper objectMapper, String apiBaseUrl, String apiSecret) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiBaseUrl = apiBaseUrl;
        this.apiSecret = apiSecret;
    }

    @Override
    public boolean confirm(String paymentKey, String orderId, BigDecimal amount) {
        String accessToken = issueAccessToken();
        String requestBody = writeJson(new ConfirmRequest(paymentKey, orderId, amount));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiBaseUrl + "/payments/" + orderId + "/confirm"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        return sendForSuccess(request);
    }

    private String issueAccessToken() {
        String requestBody = writeJson(new TokenRequest(apiSecret));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiBaseUrl + "/login/api-secret"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PaymentException(ErrorCode.UNAUTHORIZED, "PortOne API token issuance failed.");
            }
            return extractAccessToken(response.body());
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne API communication failed.");
        }
    }

    private boolean sendForSuccess(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne payment confirmation failed.");
        }
    }

    private String extractAccessToken(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode token = root.get("accessToken");
            if (token == null || token.asText().isBlank()) {
                throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne response does not contain accessToken.");
            }
            return token.asText();
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne response parsing failed.");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne request serialization failed.");
        }
    }

    private record TokenRequest(String apiSecret) {
    }

    private record ConfirmRequest(String paymentKey, String orderId, BigDecimal amount) {
    }
}
