package com.target.retail.cart.exception;

public class CartLineItemNotFoundException extends RuntimeException {
    public CartLineItemNotFoundException(String itemId) {
        super("No cart line found for item id " + itemId);
    }
}
