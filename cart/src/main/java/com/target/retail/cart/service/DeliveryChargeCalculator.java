package com.target.retail.cart.service;

import com.target.retail.cart.model.Item;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class DeliveryChargeCalculator {


    public BigDecimal calculateDeliveryCharges(Map<Item, Integer> itemQuantities) {
        //This is some rudimentary placeholder logic. This will need to be replaced with something
        //more realistic. Since this is only supporting cart , the delivery zip code may not be known. So
        //calculation should be based on item data. This is an estimated delivery charge at best.
        float defaultDeliveryPerUnit = 1.5f;

        return BigDecimal.valueOf(itemQuantities.values().stream().mapToInt(Integer::intValue).sum() * defaultDeliveryPerUnit);

    }
}
