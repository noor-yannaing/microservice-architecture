package com.wynh.microservices.order;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.wynh.microservices.order.client.InventoryClient;
import org.junit.jupiter.api.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.*;
import java.net.SocketTimeoutException;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class InventoryClientTest {
    private WireMockServer server;
    private InventoryClient client;
    @BeforeEach void start() {
        server = new WireMockServer(0); server.start();
        var factory = new SimpleClientHttpRequestFactory(); factory.setReadTimeout(1000);
        client = new InventoryClient(RestClient.builder().requestFactory(factory), server.baseUrl(), "service-secret");
    }
    @AfterEach void stop() { server.stop(); }
    @Test void sendsReservationIdentityAndServiceCredential() {
        server.stubFor(post(urlEqualTo("/internal/inventory/reservations"))
                .willReturn(okJson("true")));
        assertThat(client.reserve("order-1", "keyboard", 2)).isTrue();
        server.verify(postRequestedFor(urlEqualTo("/internal/inventory/reservations"))
                .withHeader("X-Internal-Token", equalTo("service-secret"))
                .withRequestBody(matchingJsonPath("$.orderNumber", equalTo("order-1")))
                .withRequestBody(matchingJsonPath("$.quantity", equalTo("2"))));
    }
    @Test void timeoutIsNotMistakenForOutOfStock() {
        server.stubFor(post(anyUrl()).willReturn(okJson("true").withFixedDelay(2000)));
        assertThatThrownBy(() -> client.reserve("order-1", "keyboard", 2))
                .isInstanceOf(RestClientException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
    }
}
