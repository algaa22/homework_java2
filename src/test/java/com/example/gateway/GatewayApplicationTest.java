package com.example.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("gateway")
class GatewayApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }

    @Test
    void applicationStartsAndHealthEndpointWorks() {
        RestClient restClient = RestClient.create();
        String response = restClient.get()
                .uri("http://localhost:" + port + "/actuator/health")
                .retrieve()
                .body(String.class);

        assertThat(response).contains("UP");
    }

    @Test
    void loginEndpointReturnsToken() {
        RestClient restClient = RestClient.create();

        String request = "{\"username\":\"user\",\"password\":\"password\"}";

        String response = restClient.post()
                .uri("http://localhost:" + port + "/api/v1/auth/login")
                .header("Content-Type", "application/json")
                .body(request)
                .retrieve()
                .body(String.class);

        assertThat(response).contains("accessToken");
    }

    @Test
    void unauthorizedAccessReturns401() {
        RestClient restClient = RestClient.create();

        try {
            restClient.get()
                    .uri("http://localhost:" + port + "/api/v1/profile")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("401");
        }
    }
}