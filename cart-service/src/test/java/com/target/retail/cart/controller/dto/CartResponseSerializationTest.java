package com.target.retail.cart.controller.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.target.retail.cart.model.Cart;
import com.target.retail.cart.model.CartLineItem;
import com.target.retail.cart.model.Item;
import com.target.retail.cart.model.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CartResponseSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void timestampsSerializeAsIso8601Strings() throws Exception {
        ZonedDateTime created = ZonedDateTime.parse("2025-03-10T00:14:45.730Z");
        ZonedDateTime updated = ZonedDateTime.parse("2025-03-16T00:15:45.730Z");

        Cart cart = new Cart(
                "cart123",
                new BigDecimal("1.00"),
                new BigDecimal("5.00"),
                List.of(new CartLineItem(
                        "1",
                        new Item("12345", "Title", "Description", "Brand", "APPAREL", "MerchClass",
                                "primary", "alternate", "baseUrl"),
                        1,
                        new Price("12345", new BigDecimal("10.00"), Optional.empty()),
                        created,
                        updated
                ))
        );

        CartResponse response = CartResponse.from(cart);
        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("2025-03-10T00:14:45.730");
        assertThat(json).contains("2025-03-16T00:15:45.730");
        assertThat(json).doesNotContain("\"created\":" + created.toInstant().toEpochMilli());
        assertThat(json).doesNotContain("\"updated\":" + updated.toInstant().toEpochMilli());
    }
}
