package com.wynh.microservices.product;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean @ServiceConnection
    MongoDBContainer database() { return new MongoDBContainer("mongo:7.0.16"); }
}