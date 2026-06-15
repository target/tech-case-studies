package com.target.retail.cart.model;

public record Item(
        String itemId,
        String title,
        String description,
        String brand,
        String category,
        Integer merchClass,
        String primary,
        String alternate,
        String baseUrl
) {
}
