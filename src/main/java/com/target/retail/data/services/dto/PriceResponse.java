package com.target.retail.data.services.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.data.services.model.ItemPrice;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

public record PriceResponse(String productId, BigDecimal regularPrice, BigDecimal salePrice, String priceType) {
    public PriceResponse(ItemPrice itemPrice) {
        this(itemPrice.itemId(), itemPrice.regularPrice(), itemPrice.salePrice(), itemPrice.type());
    }
}

