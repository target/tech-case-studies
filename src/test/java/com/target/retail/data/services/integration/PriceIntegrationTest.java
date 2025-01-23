package com.target.retail.data.services.integration;

import com.target.retail.data.services.dto.PriceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PriceIntegrationTest extends BaseIntegrationTest {
    
    @Test
    public void testGetPrice() {
        getResponse("/prices/" + testProductId)
                .expectStatus().isOk()
                .expectBody(PriceResponse.class)
                .value(price -> {;
                    assert price.productId().equals(testProductId);
                });
    }

    @Test
    public void testGetPrice_notfound() {
        getResponse("/prices/ " + invalidProductId)
                .expectStatus().isNotFound();
    }

}
