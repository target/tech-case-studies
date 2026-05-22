package com.target.retail.cart.service.client;

import com.target.retail.cart.service.client.dto.PriceApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PriceApiClient {

    private final RestClient restClient;

    public PriceApiClient(@Value("${clients.price-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PriceApiResponse getPricing(String tcin) {
        return restClient.get()
                .uri("/prices/{item_id}", tcin)
                .retrieve()
                .body(PriceApiResponse.class);
    }
}
