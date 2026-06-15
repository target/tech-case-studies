package com.target.retail.cart.service.client;

import com.target.retail.cart.service.client.dto.ItemApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ItemApiClient {

    private final RestClient restClient;

    public ItemApiClient(@Value("${clients.item-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ItemApiResponse getItem(String itemId) {
        return restClient.get()
                .uri("/items/{itemId}", itemId)
                .retrieve()
                .body(ItemApiResponse.class);
    }
}
