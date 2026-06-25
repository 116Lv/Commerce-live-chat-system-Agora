package com.team7.agora.domain.payment.client;

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

@Component
@Profile("prod")
public class PortOnePaymentClient implements PaymentClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String apiBaseUrl;
    private final String apiSecret;

    public PortOnePaymentClient(
        @Value("${portone.api-base-url:https://api.portone.io}") String apiBaseUrl,
        @Value("${portone.api-secret}") String apiSecret
    ) {
        this.apiBaseUrl = apiBaseUrl;
        this.apiSecret = apiSecret;
    }

    @Override
    public boolean confirm(String paymentKey, String orderId, BigDecimal amount) {
        String accessToken = issueAccessToken();
        String requestBody = """
            {
              "paymentKey": "%s",
              "orderId": "%s",
              "amount": %s
            }
            """.formatted(paymentKey, orderId, amount.toPlainString());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiBaseUrl + "/payments/" + orderId + "/confirm"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        return sendForSuccess(request);
    }

    private String issueAccessToken() {
        String requestBody = """
            {
              "apiSecret": "%s"
            }
            """.formatted(apiSecret);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiBaseUrl + "/login/api-secret"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new PaymentException(ErrorCode.UNAUTHORIZED, "PortOne API 토큰 발급에 실패했습니다.");
            }
            return extractJsonString(response.body(), "accessToken");
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne API 통신에 실패했습니다.");
        }
    }

    private boolean sendForSuccess(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne 결제 승인 통신에 실패했습니다.");
        }
    }

    private String extractJsonString(String json, String fieldName) {
        String pattern = "\"" + fieldName + "\"";
        int fieldIndex = json.indexOf(pattern);
        if (fieldIndex < 0) {
            throw new PaymentException(ErrorCode.INTERNAL_SERVER_ERROR, "PortOne 응답에서 accessToken을 찾을 수 없습니다.");
        }
        int colonIndex = json.indexOf(':', fieldIndex);
        int firstQuote = json.indexOf('"', colonIndex + 1);
        int secondQuote = json.indexOf('"', firstQuote + 1);
        return json.substring(firstQuote + 1, secondQuote);
    }
}
