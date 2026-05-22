package com.target.retail.cart.model;


import java.time.ZonedDateTime;

public record CartLineItem (String lineItemId, Item item, Integer quantity, Price price, ZonedDateTime createdOn, ZonedDateTime updatedOn) {}