package com.target.retail.data.services.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.equalTo;

public class AvailabilityIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testGetAvailability() throws Exception {
        getResponse("/availability/" + testProductId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product_id").value(equalTo(testProductId)));
    }

    @Test
    public void testGetAvailability_notfound() throws Exception {
        getResponse("/availability/" + invalidProductId)
                .andExpect(status().isNotFound());
    }
}
