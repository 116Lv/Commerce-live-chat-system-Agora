package com.team7.agora.domain.payment.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PortOnePaymentClientTest {

    private HttpServer server;
    private String baseUrl;
    private volatile String tokenRequestBody;
    private volatile String confirmRequestBody;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/login/api-secret", exchange -> {
            tokenRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] response = "{\"accessToken\":\"access-token\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(tokenRequestBody.contains("a\\\"b") ? 200 : 400, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/payments/order-1/confirm", exchange -> {
            confirmRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void confirmSerializesPortOneRequestsAsJson() {
        PortOnePaymentClient client = new PortOnePaymentClient(baseUrl, "a\"b");

        boolean approved = client.confirm("payment-key", "order-1", BigDecimal.valueOf(50000));

        assertThat(approved).isTrue();
        assertThat(tokenRequestBody).contains("a\\\"b");
        assertThat(confirmRequestBody).contains("\"paymentKey\":\"payment-key\"");
    }

    @Test
    void dockerProfileCreatesPortOnePaymentClientBean() {
        new ApplicationContextRunner()
            .withPropertyValues("spring.profiles.active=docker", "portone.api-secret=test-secret")
            .withUserConfiguration(PortOnePaymentClient.class)
            .run(context -> assertThat(context).hasSingleBean(PortOnePaymentClient.class));
    }
}
