package com.target.retail.data.services.service;

import com.target.retail.data.services.data.CsvData;
import com.target.retail.data.services.model.ItemPrice;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PriceService {

    @Value("${price.data-file:/data/prices.csv}")
    private String priceDataFile;

    @Value("${price.fast-change-prefix:9012}")
    private String fastChangingPricesPrefix;

    private CsvData<ItemPrice> itemPriceData;

    private Random random;

    public PriceService() {
    }

    public PriceService(CsvData<ItemPrice> itemPriceData, String fastChangingPricesPrefix) {
        this.itemPriceData = itemPriceData;
        this.fastChangingPricesPrefix = fastChangingPricesPrefix;
    }

    @PostConstruct
    private void init() {
        itemPriceData = new CsvData<>(priceDataFile, ItemPrice.class);
        random = new Random(LocalDateTime.now().getMinute() / 5);
    }

    public Optional<ItemPrice> getPrice(String id) {
        Optional<ItemPrice> itemPrice = itemPriceData.getById(id);
        if (itemPrice.isPresent() && itemPrice.get().itemId().startsWith(fastChangingPricesPrefix)) {
            return itemPrice.map(item -> new ItemPrice(item.itemId(), repriceItem(item.regularPrice()), repriceItem(item.salePrice()), item.type()));
        }
        return itemPrice;
    }

    private BigDecimal repriceItem(BigDecimal price) {
        float change = (float) random.nextInt(5) / 100;
        int sign = random.nextBoolean() ? -1 : 1;

        return price.multiply(new BigDecimal(1 + change * sign));
    }
}