package com.target.retail.cart.model;

import java.math.BigDecimal;
import java.util.Optional;

public record Price(String itemId, BigDecimal regular, Optional<BigDecimal> sale) {

    public BigDecimal getCurrentPrice(){
        return sale.orElse(regular);
    }
}
