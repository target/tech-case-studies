package com.target.retail.data.services.integration;

import com.target.retail.data.services.dto.AvailabilityResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AvailabilityIntegrationTest extends BaseIntegrationTest {
    
    @Test
    public void testGetAvailability() {
        getResponse("/availability/" + testProductId)
                .expectStatus().isOk()
                .expectBody(AvailabilityResponse.class)
                .value(availability -> {;
                    assert availability.productId().equals(testProductId);
                });
    }

    @Test
    public void testGetAvailability_notfound() {
        getResponse("/availability/ " + invalidProductId)
                .expectStatus().isNotFound();
    }
}
