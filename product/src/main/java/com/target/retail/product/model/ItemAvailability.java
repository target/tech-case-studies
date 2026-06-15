package com.target.retail.product.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.product.data.Identifiable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({ "item_id", "availableUnits", "limitedQuantityThreshold" })
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemAvailability (String itemId, Integer availableUnits, Integer limitedQuantityThreshold) implements Identifiable {

    @Override
    public String getId() {
        return itemId();
    }


    public ItemAvailability updateAvailableUnits(Integer availableUnits) {
        return new ItemAvailability(this.itemId, availableUnits, this.limitedQuantityThreshold);
    }
}