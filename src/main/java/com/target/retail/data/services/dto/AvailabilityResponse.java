package com.target.retail.data.services.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.data.services.model.ItemAvailability;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AvailabilityResponse(String productId, Integer availableUnits, Integer limitedQuantityThreshold) {

    public AvailabilityResponse(ItemAvailability itemAvailability) {
        this(itemAvailability.itemId(), itemAvailability.availableUnits(), itemAvailability.limitedQuantityThreshold());
    }
}
