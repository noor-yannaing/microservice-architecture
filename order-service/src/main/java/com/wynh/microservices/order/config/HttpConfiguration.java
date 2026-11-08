package com.wynh.microservices.order.config;
import org.springframework.context.annotation.*;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
public class HttpConfiguration {
    @Bean
    RestClientCustomizer timeouts() {
        return builder -> {
            var factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(2000);
            factory.setReadTimeout(3000);
            builder.requestFactory(factory);
        };
    }
}