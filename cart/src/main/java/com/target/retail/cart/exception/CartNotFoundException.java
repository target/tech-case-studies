package com.target.retail.cart.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String cartId) {
        super("No cart found with id " + cartId);
    }
}
