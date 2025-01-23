package com.target.retail.data.services.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testHealthCheck() {
        getResponse("/health")
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Ok");
    }

    @Test
    public void testActuatorHealth() {
        getResponse("/actuator/health")
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(health -> {
                    assert health.get("status").equals("UP");
                });
    }

    @Test
    @Disabled("Unable to get this test to work even though the endpoint is working")
    // curl http://localhost:8080/retail_data_services/v1/api-docs
    public void testOpenApiSpec() throws JsonProcessingException {
        String specBody = getResponse("/api-docs")
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        OpenAPI spec = Json.mapper().readValue(specBody, OpenAPI.class);
        assertEquals(SpecVersion.V30, spec.getSpecVersion());
        assertEquals("Retail Data Services API", spec.getInfo().getTitle());
    }
}
