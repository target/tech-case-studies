package com.target.retail.data.services.service;

import com.target.retail.data.services.data.CsvData;
import com.target.retail.data.services.model.Item;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    @Value("${item.data-file:/data/items.csv}")
    private String itemDataFile;

    private CsvData<Item> itemData;

    public ItemService() {
    }

    public ItemService(CsvData<Item> itemData) {
        this.itemData = itemData;
    }

    @PostConstruct
    private void init() {
        itemData = new CsvData<>(itemDataFile, Item.class);
    }

    public Optional<Item> getItem(String id) {
        return itemData.getById(id);
    }

    public List<Item> getAllItems(int page, int size, String smallDescriptionFilter) {
        List<Item> items = filterBySmallDescription(itemData.getAll(), smallDescriptionFilter);

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, items.size());

        if (startIndex >= items.size()) {
            return List.of();
        }

        return items.subList(startIndex, endIndex);
    }

    public List<Item> getAllItems(int page, int size) {
        return getAllItems(page, size, null);
    }

    public Integer getItemCount(String smallDescriptionFilter) {
        if (smallDescriptionFilter != null && !smallDescriptionFilter.trim().isEmpty()) {
            return filterBySmallDescription(itemData.getAll(), smallDescriptionFilter).size();
        }
        return itemData.getCount();
    }

    public Integer getItemCount() {
        return getItemCount(null);
    }

    /**
     * Filters a list of items by small description.
     * The filtering is case-insensitive and matches partial strings.
     * 
     * @param items  the list of items to filter
     * @param filter the filter string to apply
     * @return the filtered list of items, or the original list if filter is null or
     *         empty
     */
    private List<Item> filterBySmallDescription(List<Item> items, String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return items;
        }

        String normalizedFilter = filter.trim().toLowerCase();
        return items.stream()
                .filter(item -> item.smallDescription() != null &&
                        item.smallDescription().toLowerCase().contains(normalizedFilter))
                .toList();
    }
}
