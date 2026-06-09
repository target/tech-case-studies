package com.target.retail.data.services.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.equalTo;

public class PriceIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testGetPrice() throws Exception {
        getResponse("/prices/" + testProductId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item_id").value(equalTo(testProductId)));
    }

    @Test
    public void testGetPrice_notfound() throws Exception {
        getResponse("/prices/" + invalidProductId)
                .andExpect(status().isNotFound());
    }
}
