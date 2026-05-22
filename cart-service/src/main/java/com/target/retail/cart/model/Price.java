package com.target.retail.cart.model;

import java.math.BigDecimal;
import java.util.Optional;

public record Price(String tcin, BigDecimal regularPrice, Optional<BigDecimal> salePrice) {

    public BigDecimal getCurrentPrice(){
        return salePrice.orElse(regularPrice);
    }
}
