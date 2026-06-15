package com.target.retail.product.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.product.model.ItemAvailability;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AvailabilityResponse(String itemId, Integer availableUnits, Integer limitedQuantityThreshold) {

    public AvailabilityResponse(ItemAvailability itemAvailability) {
        this(itemAvailability.itemId(), itemAvailability.availableUnits(), itemAvailability.limitedQuantityThreshold());
    }
}
