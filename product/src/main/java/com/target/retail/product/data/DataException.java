package com.target.retail.product.data;

public class DataException extends RuntimeException {

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable th) {
        super(message, th);
    }
}
