package com.target.retail.data.services.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.data.services.data.Identifiable;

import java.math.BigDecimal;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({ "itemId", "regular_price", "sale_price", "type" })
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemPrice(String itemId, BigDecimal regularPrice, BigDecimal salePrice, String type) implements Identifiable {

    @Override
    public String getId() {
        return itemId;
    }

}
