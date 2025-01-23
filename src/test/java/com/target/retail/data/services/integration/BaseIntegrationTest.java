package com.target.retail.data.services.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseIntegrationTest {

    @LocalServerPort
    protected int serverPort;

    protected String testProductId = "54276335";
    protected String invalidProductId = "999999999";

    WebTestClient webClient;

    @BeforeEach
    public void setup() {
        webClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + serverPort).build();
    }

    public WebTestClient.ResponseSpec getResponse(String url) {
        return webClient.get()
                .uri(url)
                .exchange();
    }

    /**
     * Return the baseUrl that the test client will be initialized with.  This should end with a trailing slash
     */
    public String getBaseUrl() {
        return "http://localhost:%d/".formatted(serverPort);
    }

}


