package com.target.retail.cart.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.cart.data.Identifiable;

import java.time.ZonedDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({ "lineId", "cartId", "itemId", "quantity", "createdOn", "updatedOn" })
@JsonIgnoreProperties(ignoreUnknown = true)
public record StoredCartLine(String lineId, String cartId, String itemId, Integer quantity, ZonedDateTime createdOn, ZonedDateTime updatedOn) implements Identifiable {
    public String getId() {
        return lineId();
    }
}
