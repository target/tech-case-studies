package com.target.retail.cart.service.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ItemApiResponse(String itemId,
                              String smallDescription,
                              String longDescription,
                              String category,
                              Integer merchClass,
                              String channelRestriction,
                              String barcode,
                              String brandName,
                              Integer ageRestriction,
                              ImageData imageData) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ImageData(String primary, String alternate, String baseUrl) {

    }
}

