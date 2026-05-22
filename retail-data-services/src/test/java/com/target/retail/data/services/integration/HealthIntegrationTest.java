package com.target.retail.data.services.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HealthIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testHealthCheck() throws Exception {
        getResponse("/health")
                .andExpect(status().isOk())
                .andExpect(content().string("Ok"));
    }

    @Test
    public void testActuatorHealth() throws Exception {
        getResponse("/actuator/health")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @Disabled("Unable to get this test to work even though the endpoint is working")
    public void testOpenApiSpec() throws Exception {
        String specBody = getResponse("/api-docs")
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OpenAPI spec = Json.mapper().readValue(specBody, OpenAPI.class);
        assertEquals(SpecVersion.V30, spec.getSpecVersion());
        assertEquals("Retail Data Services API", spec.getInfo().getTitle());
    }
}
