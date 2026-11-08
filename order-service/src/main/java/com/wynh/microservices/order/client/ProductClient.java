package com.wynh.microservices.order.client;
import com.wynh.microservices.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import java.math.BigDecimal;

@Component
public class ProductClient {
    private final RestClient client;
    public ProductClient(RestClient.Builder builder, @Value("${services.product-url}") String url,
            @Value("${services.internal-token}") String token) {
        client = builder.baseUrl(url).defaultHeader("X-Internal-Token", token).build();
    }
    public BigDecimal price(String sku) {
        try {
            var product = client.get().uri("/api/product/{sku}", sku).retrieve().body(Product.class);
            if (product == null || product.price() == null) throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Product catalogue returned no price");
            return product.price();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Product catalogue is unavailable");
        } catch (RestClientException exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Product catalogue is unavailable");
        }
    }
    public record Product(BigDecimal price) { }
}