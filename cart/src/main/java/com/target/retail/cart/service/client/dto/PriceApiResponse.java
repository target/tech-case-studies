package com.target.retail.cart.service.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PriceApiResponse(String itemId, BigDecimal regular, BigDecimal sale, String priceType) {}
