package com.target.retail.product.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.product.model.Item;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ItemResponse(String itemId, String smallDescription, String longDescription, String category,
                           Integer merchClass, String channelRestriction, String barcode, String brandName,
                           Integer ageRestriction, ImageData imageData) {

    public ItemResponse(Item item) {
        this(item.itemId(), item.smallDescription(), item.longDescription(), item.category(), item.merchClass(),
                item.channelRestriction(), item.barcode(), item.brandName(), item.ageRestriction(),
                new ImageData(item));
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ImageData(String primary, String alternate, String baseUrl) {
        public ImageData(Item item) {
            this(item.primary(), item.alternate(), item.baseUrl());
        }
    }
}