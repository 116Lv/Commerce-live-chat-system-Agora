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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * PortOne payment provider client.
 */
@Component
@Profile({"prod", "docker"})
public class PortOnePaymentClient implements PaymentClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;
    private final String apiSecret;

    @Autowired
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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiBaseUrl + "/payments/" + orderId))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        return sendForPaidPayment(request, orderId, amount);
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

    private boolean sendForPaidPayment(HttpRequest request, String orderId, BigDecimal amount) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return false;
            }
            return isPaidPayment(response.body(), orderId, amount);
        } catch (Exception e) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne payment verification failed.");
        }
    }

    private boolean isPaidPayment(String body, String orderId, BigDecimal amount) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String providerPaymentId = text(root, "id", "paymentId");
            String status = text(root, "status");
            BigDecimal paidAmount = amount(root);

            return (providerPaymentId == null || providerPaymentId.equals(orderId))
                && "PAID".equalsIgnoreCase(status)
                && paidAmount != null
                && paidAmount.compareTo(amount) == 0;
        } catch (Exception e) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne payment response parsing failed.");
        }
    }

    private String text(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return null;
    }

    private BigDecimal amount(JsonNode root) {
        JsonNode amountNode = root.get("amount");
        if (amountNode != null) {
            if (amountNode.isObject()) {
                JsonNode total = amountNode.get("total");
                if (total != null && total.isNumber()) {
                    return total.decimalValue();
                }
            }
            if (amountNode.isNumber()) {
                return amountNode.decimalValue();
            }
        }

        JsonNode totalAmount = root.get("totalAmount");
        return totalAmount != null && totalAmount.isNumber() ? totalAmount.decimalValue() : null;
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

}
