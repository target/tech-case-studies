package com.target.retail.cart.controller.dto;

import com.target.retail.cart.model.Cart;
import com.target.retail.cart.model.CartLineItem;
import com.target.retail.cart.model.Item;
import com.target.retail.cart.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartResponseTest {


    @Test
    public void testFrom() {
        // Create a mock Cart object
        Cart cart = new Cart(
                "cart123",
                new BigDecimal("10"),
                new BigDecimal("10"),
                List.of(new CartLineItem("1",new Item("12345", "Item Title", "Item Description", "Brand", "Category", "MerchClass", "primary", "alternate", "baseUrl"), 2, new Price("12345", new BigDecimal("8.00"), Optional.of(new BigDecimal("6.00"))),
                ZonedDateTime.now(),
                ZonedDateTime.now())));

        // Convert Cart to CartResponse
        CartResponse cartResponse = CartResponse.from(cart);

        // Assertions
        assertEquals(cart.id(), cartResponse.id());
        assertEquals(cart.subTotal(), cartResponse.subtotal());
        assertEquals(cart.deliveryCharges(), cartResponse.delivery());
        assertEquals(cart.totalTax(), cartResponse.tax());
        assertEquals(cart.getTotal(), cartResponse.total());
        assertEquals(cart.createdOn(), cartResponse.created());
        assertEquals(cart.updatedOn(), cartResponse.updated());

        // Check items
        assertEquals(cart.cartLineItems().size(), cartResponse.items().size());
        CartLineItem cartLineItem = cart.cartLineItems().get(0);
        CartResponse.ItemResponse itemResponse = cartResponse.items().get(0);
        assertEquals(cartLineItem.item().tcin(), itemResponse.tcin());
        assertEquals(cartLineItem.item().title(), itemResponse.title());
        assertEquals(cartLineItem.item().description(), itemResponse.description());
        assertEquals(cartLineItem.item().brand(), itemResponse.brand());
        assertEquals(cartLineItem.item().merchClass(), itemResponse.merchClass());
        assertEquals(cartLineItem.quantity(), itemResponse.quantity());
        assertEquals(cartLineItem.price().regular(), itemResponse.price().regular());
        assertEquals(cartLineItem.price().sale().orElse(null), itemResponse.price().sale());
        assertEquals(cartLineItem.item().primary(), itemResponse.imageData().primary());
        assertEquals(cartLineItem.item().alternate(), itemResponse.imageData().alternate());
        assertEquals(cartLineItem.item().baseUrl(), itemResponse.imageData().baseUrl());
    }

    @Test
    public void testFormattedPricesInCartResponse() {
        Cart cart = new Cart(
                "cart123",
                new BigDecimal("10.5"),
                new BigDecimal("5.25"),
                List.of(new CartLineItem(
                    "1",
                    new Item("12345", "Item Title", "Item Description", "Brand", "Category", "MerchClass", "primary", "alternate", "baseUrl"),
                    2,
                    new Price("12345", new BigDecimal("8.00"), Optional.of(new BigDecimal("6.50"))),
                    ZonedDateTime.now(),
                    ZonedDateTime.now()
                ))
        );

        CartResponse cartResponse = CartResponse.from(cart);

        assertEquals("$5.25", cartResponse.getDeliveryFormatted());
        assertEquals("$13.00", cartResponse.getSubtotalFormatted());
        assertEquals("$10.50", cartResponse.getTaxFormatted());
        assertEquals("$28.75", cartResponse.getTotalFormatted());
    }
}
