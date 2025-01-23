package com.target.retail.data.services.service;

import com.target.retail.data.services.data.CsvData;
import com.target.retail.data.services.model.ItemAvailability;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AvailabilityService {
    @Value("${availability.data-file:/data/item_availability.csv}")
    private String itemAvailabilityDataFile;

    @Value("${availability.fast-change-prefix:9012}")
    private String fastChangingAvailabilityPrefix;

    private CsvData<ItemAvailability> itemAvailabilityData;

    private Random random;

    @PostConstruct
    private void init() {
        itemAvailabilityData = new CsvData<>(itemAvailabilityDataFile, ItemAvailability.class);
        random = new Random(LocalDateTime.now().getMinute() / 5);
    }

    public AvailabilityService() {}

    public AvailabilityService(CsvData<ItemAvailability> itemAvailabilityData, String fastChangingAvailabilityPrefix) {
        this.itemAvailabilityData = itemAvailabilityData;
        this.fastChangingAvailabilityPrefix = fastChangingAvailabilityPrefix;
    }

    public Optional<ItemAvailability> getItemAvailability(String id) {
        Optional<ItemAvailability> itemAvailability = itemAvailabilityData.getById(id);
        if(itemAvailability.isPresent() && itemAvailability.get().itemId().startsWith(fastChangingAvailabilityPrefix)) {
            return Optional.of(itemAvailability.get().updateAvailableUnits(recalculateAvailability(itemAvailability.get())));
        } else {
            return itemAvailability;
        }
    }


    private Integer recalculateAvailability(ItemAvailability itemAvailability) {
        int change =  random.nextInt(10);
        int sign = random.nextBoolean() ? -1 : 1;
        return itemAvailability.availableUnits() + (change * sign);
    }

}
