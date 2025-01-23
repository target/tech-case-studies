package com.target.retail.data.services.service;

import com.target.retail.data.services.data.CsvData;
import com.target.retail.data.services.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ItemServiceTest {

    @Mock
    private CsvData<Item> itemData;

    @InjectMocks
    private ItemService itemService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        itemService = new ItemService(itemData);
    }

    @Test
    public void testGetItem_Found() {
        Item item = new Item("901234", "Small Desc", "Long Desc", "Category", 12 ,"IN-STORE", "Barcode", "Brand", 18, "primary_image", "alternate_image", "http://target.com");
        when(itemData.getById("901234")).thenReturn(Optional.of(item));

        Optional<Item> response = itemService.getItem("901234");

        assertTrue(response.isPresent());
        assertEquals("901234", response.get().itemId());
        assertEquals("Small Desc", response.get().smallDescription());
        assertEquals("Long Desc", response.get().longDescription());
        assertEquals("Category", response.get().category());
        assertEquals("IN-STORE", response.get().channelRestriction());
        assertEquals("Barcode", response.get().barcode());
        assertEquals("Brand", response.get().brandName());
        assertEquals(18, response.get().ageRestriction());
    }

    @Test
    public void testGetItem_NotFound() {
        when(itemData.getById("999999")).thenReturn(Optional.empty());

        Optional<Item> response = itemService.getItem("999999");

        assertTrue(response.isEmpty());
    }

    @Test
    public void testGetItem_ImageDetails() {
        Item item = new Item("901234", "Small Desc", "Long Desc", "Category", 12 ,"IN-STORE", "Barcode", "Brand", 18, "primary_image", "alternate_image", "http://target.com");
        when(itemData.getById("901234")).thenReturn(Optional.of(item));

        Optional<Item> response = itemService.getItem("901234");

        assertTrue(response.isPresent());
        assertEquals("alternate_image", response.get().alternateImage());
        assertEquals("primary_image", response.get().primaryImage());
        assertEquals("http://target.com", response.get().baseUrl());
    }
    @Test
    public void getItemCount_ReturnsPaginatedList_WhenValidPageAndSize() {
        List<Item> items = List.of(
                new Item("901234", "Small Desc", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18, "primary_image", "alternate_image", "http://target.com"),
                new Item("901235", "Small Desc 2", "Long Desc 2", "Category 2", 13, "ONLINE", "Barcode 2", "Brand 2", 21, "primary_image_2", "alternate_image_2", "http://target.com/2")
        );
        when(itemData.getAll()).thenReturn(items);

        List<Item> result = itemService.getAllItems(0, 1);

        assertEquals(1, result.size());
        assertEquals("901234", result.get(0).itemId());
    }

    @Test
    public void getItemCount_ReturnsEmptyList_WhenPageOutOfBounds() {
        List<Item> items = List.of(
                new Item("901234", "Small Desc", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18, "primary_image", "alternate_image", "http://target.com")
        );
        when(itemData.getAll()).thenReturn(items);

        List<Item> result = itemService.getAllItems(2, 1);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getItemCount_ReturnsEmptyList_WhenSizeIsZero() {
        List<Item> items = List.of(
                new Item("901234", "Small Desc", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18, "primary_image", "alternate_image", "http://target.com")
        );
        when(itemData.getAll()).thenReturn(items);

        List<Item> result = itemService.getAllItems(0, 0);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getItemCount_ReturnsTotalItemCount() {
        List<Item> items = List.of(
                new Item("901234", "Small Desc", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18, "primary_image", "alternate_image", "http://target.com"),
                new Item("901235", "Small Desc 2", "Long Desc 2", "Category 2", 13, "ONLINE", "Barcode 2", "Brand 2", 21, "primary_image_2", "alternate_image_2", "http://target.com/2")
        );
        when(itemData.getCount()).thenReturn(items.size());

        Integer totalItems = itemService.getItemCount();

        assertEquals(2, totalItems);
    }
}