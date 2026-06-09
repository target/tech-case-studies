package com.target.retail.cart.controller.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.target.retail.cart.model.Cart;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CartResponse(String id, List<ItemResponse> items, BigDecimal subtotal, BigDecimal delivery, BigDecimal tax, BigDecimal total, @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") ZonedDateTime created, @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") ZonedDateTime updated) {

    public String getSubtotalFormatted() {
        return formatNumber(subtotal());
    }

    public String getTaxFormatted() {
        return formatNumber(tax());
    }

    public String getDeliveryFormatted() {
        return formatNumber(delivery());
    }

    public String getTotalFormatted() {
        return formatNumber(total());
    }

    private String formatNumber(BigDecimal bigDecimal) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US); 

        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        
        return formatter.format(bigDecimal);
    }


    public record ItemResponse(String tcin, String title, String description, String brand, String merchClass, Integer quantity, PriceResponse price, ImageResponse imageData) {

        public record PriceResponse(BigDecimal regular, BigDecimal sale) {}

        public record ImageResponse(String primary, String alternate, String baseUrl) {}
    }

    public static CartResponse from(Cart cart) {

        List<ItemResponse> items = cart.cartLineItems().stream().map(it -> new ItemResponse(it.item().tcin(),
                it.item().title(), it.item().description(), it.item().brand(), it.item().merchClass(),
                it.quantity(), new ItemResponse.PriceResponse(it.price().regular(), it.price().sale().orElse(null)),                 new ItemResponse.ImageResponse(it.item().primary(), it.item().alternate(), it.item().baseUrl()))).toList();

        return new CartResponse(cart.id(), items, cart.subTotal(), cart.deliveryCharges(), cart.totalTax(), cart.getTotal(), cart.createdOn(), cart.updatedOn());
    }
}
