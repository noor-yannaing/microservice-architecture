package com.wynh.microservices.product;
import com.wynh.microservices.product.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "management.tracing.enabled=false")
@Import(TestcontainersConfiguration.class)
class ProductIT {
    @Autowired TestRestTemplate http;
    @Autowired ProductRepository products;
    @BeforeEach void clean() { products.deleteAll(); }
    private HttpEntity<Map<String,Object>> request(Map<String,Object> body) {
        var headers = new HttpHeaders(); headers.set("X-Internal-Token", "local-service-token");
        headers.setContentType(MediaType.APPLICATION_JSON); return new HttpEntity<>(body, headers);
    }
    @Test void createsProductAndRejectsDuplicateSku() {
        var body = Map.<String,Object>of("skuCode", "keyboard", "name", "Keyboard", "description", "Mechanical keyboard", "price", 79.90);
        var created = http.postForEntity("/api/product", request(body), Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).containsKey("id").containsEntry("skuCode", "keyboard");
        assertThat(http.postForEntity("/api/product", request(body), Map.class).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
    @Test void invalidPriceDoesNotReachDatabase() {
        var body = Map.<String,Object>of("skuCode", "keyboard", "name", "Keyboard", "description", "Description", "price", -1);
        assertThat(http.postForEntity("/api/product", request(body), Map.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(products.count()).isZero();
    }
    @Test void directAccessRequiresServiceCredential() {
        assertThat(http.getForEntity("/api/product", String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}