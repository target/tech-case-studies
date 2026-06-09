package com.target.retail.cart.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public record Cart(String id, BigDecimal totalTax, BigDecimal deliveryCharges, List<CartLineItem> cartLineItems) {
    public BigDecimal getTotal() {
        return subTotal().add(deliveryCharges()).add(totalTax());
    }

    public BigDecimal subTotal() {
        return cartLineItems().stream()
                .map(item -> item.price().getCurrentPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ZonedDateTime createdOn() {
        return cartLineItems().stream().map(CartLineItem::createdOn).min(ZonedDateTime::compareTo).orElse(ZonedDateTime.now());
    }

    public ZonedDateTime updatedOn() {
        return cartLineItems().stream().map(CartLineItem::updatedOn).max(ZonedDateTime::compareTo).orElse(ZonedDateTime.now());
    }

    public Optional<CartLineItem> findByTcin(String tcin) {
        return cartLineItems.stream().filter(it -> it.item().tcin().equals(tcin)).findFirst();
    }
}
