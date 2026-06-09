package com.target.retail.data.services.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.data.services.data.Identifiable;

@JsonPropertyOrder({"itemId", "smallDescription", "longDescription", "category", "merchClass", "channelRestriction", "barcode", "brandName", "ageRestriction", "primary", "alternate", "baseUrl"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Item(String itemId, String smallDescription, String longDescription, String category, Integer merchClass,
                   String channelRestriction, String barcode, String brandName, Integer ageRestriction,
                   String primary, String alternate, String baseUrl) implements Identifiable {

    @Override
    public String getId() {
        return itemId;
    }

}
