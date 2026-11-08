package com.wynh.microservices.gateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.function.*;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration(proxyBeanMethods = false)
public class GatewayRoutes {
    @Bean
    RouterFunction<ServerResponse> serviceRoutes(@Value("${services.product-url}") String product,
            @Value("${services.order-url}") String order, @Value("${services.inventory-url}") String inventory,
            @Value("${services.internal-token}") String token) {
        return route("product").route(path("/api/product").or(path("/api/product/**")), http()).before(uri(product))
                .before(request -> trusted(request, token)).build()
                .and(route("order").route(path("/api/order").or(path("/api/order/**")), http()).before(uri(order))
                        .before(request -> trusted(request, token)).build())
                .and(route("inventory").route(path("/api/inventory").or(path("/api/inventory/**")), http()).before(uri(inventory))
                        .before(request -> trusted(request, token)).build());
    }

    private ServerRequest trusted(ServerRequest request, String token) {
        var principal = (JwtAuthenticationToken) request.principal().orElseThrow();
        return ServerRequest.from(request).headers(headers -> {
            headers.remove("X-Customer-Id");
            headers.remove("X-Internal-Token");
            headers.remove("Authorization");
            headers.set("X-Customer-Id", principal.getToken().getSubject());
            headers.set("X-Internal-Token", token);
        }).build();
    }
}