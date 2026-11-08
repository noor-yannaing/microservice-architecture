package com.wynh.microservices.gateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class GatewaySecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean JwtDecoder decoder;
    @Test void anonymousRequestsAreUnauthorized() throws Exception {
        mvc.perform(get("/api/product")).andExpect(status().isUnauthorized());
    }
    @Test void customerCannotCreateProducts() throws Exception {
        mvc.perform(post("/api/product").with(jwt()).contentType("application/json").content("{}"))
            .andExpect(status().isForbidden());
    }
    @Test void customerCannotChangeStock() throws Exception {
        mvc.perform(put("/api/inventory/keyboard").with(jwt()).contentType("application/json").content("{\"quantity\":5}"))
            .andExpect(status().isForbidden());
    }
    @Test void reservationEndpointIsNotPubliclyRouted() throws Exception {
        mvc.perform(post("/internal/inventory/reservations").with(jwt())).andExpect(status().isForbidden());
    }
}