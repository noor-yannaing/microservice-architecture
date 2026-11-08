package com.wynh.microservices.order.client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {
    private final RestClient client;
    public InventoryClient(RestClient.Builder builder, @Value("${services.inventory-url}") String url,
            @Value("${services.internal-token}") String token) {
        client = builder.baseUrl(url).defaultHeader("X-Internal-Token", token).build();
    }
    public boolean reserve(String orderNumber, String skuCode, int quantity) {
        Boolean accepted = client.post().uri("/internal/inventory/reservations")
                .body(new Reservation(orderNumber, skuCode, quantity)).retrieve().body(Boolean.class);
        if (accepted == null) throw new IllegalStateException("Inventory returned an empty reservation response");
        return accepted;
    }
    private record Reservation(String orderNumber, String skuCode, int quantity) { }
}