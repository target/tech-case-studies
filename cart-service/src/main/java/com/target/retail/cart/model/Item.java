package com.target.retail.cart.model;

public record Item(
        String tcin,
        String title,
        String description,
        String brand,
        String category,
        String merchClass,
        String primaryImage,
        String alternateImage,
        String baseUrl
) {
}