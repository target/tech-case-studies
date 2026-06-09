package com.target.retail.data.services.controllers;

import com.target.retail.data.services.controller.ItemController;
import com.target.retail.data.services.dto.ItemResponse;
import com.target.retail.data.services.dto.ItemResponse.ImageData;
import com.target.retail.data.services.dto.PaginatedResponse;
import com.target.retail.data.services.model.Item;
import com.target.retail.data.services.service.ItemService;
import com.target.retail.data.services.service.behavior.Behaviors;
import com.target.retail.data.services.service.behavior.InducedBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ItemControllerTest {

    private ItemService itemService;
    private ItemController itemController;

    @BeforeEach
    public void setUp() {
        itemService = mock(ItemService.class);
        Behaviors behaviors = mock(Behaviors.class);
        when(behaviors.getConfiguredBehavior()).thenReturn(new InducedBehavior() {
            @Override
            public <T> T execute(Supplier<T> supplier) {
                return supplier.get();
            }
        });
        itemController = new ItemController(itemService, behaviors);
    }

    @Test
    public void testGetItem_Found() {
        Item item = new Item("901234", "Small Desc", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18,
                "primary", "alternate", "http://target.com");
        when(itemService.getItem("901234")).thenReturn(Optional.of(item));

        ResponseEntity<ItemResponse> response = itemController.getItem("901234");

        assertEquals(200, response.getStatusCode().value());

        ItemResponse responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals("901234", responseBody.itemId());
        assertEquals(12, responseBody.merchClass());
    }

    @Test
    public void testGetItem_NotFound() {
        when(itemService.getItem("999999")).thenReturn(Optional.empty());

        ResponseEntity<ItemResponse> response = itemController.getItem("999999");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void testGetItem_ImageBlock() {
        Item item = new Item("901234", "Small Desc", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18,
                "primary", "alternate", "http://target.com");
        when(itemService.getItem("901234")).thenReturn(Optional.of(item));

        ResponseEntity<ItemResponse> response = itemController.getItem("901234");

        assertEquals(200, response.getStatusCode().value());

        ItemResponse responseBody = response.getBody();
        assertNotNull(responseBody);

        ImageData imageData = responseBody.imageData();
        assertEquals("primary", imageData.primary());
        assertEquals("alternate", imageData.alternate());
        assertEquals("http://target.com", imageData.baseUrl());
    }

    @Test
    public void getAllItems_ReturnsPaginatedResponse_WhenValidParameters() {
        List<Item> items = List.of(
                new Item("901234", "Small Desc", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18,
                        "primary", "alternate", "http://target.com"),
                new Item("901235", "Small Desc 2", "Long Desc 2", "Category 2", 13, "ONLINE", "Barcode 2", "Brand 2",
                        21, "primary_2", "alternate_2", "http://target.com/2"));
        when(itemService.getAllItems(0, 2, null)).thenReturn(items);
        when(itemService.getItemCount(null)).thenReturn(10);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 2);

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(0, responseBody.currentPage());
        assertEquals(2, responseBody.items().size());
        assertEquals(1, responseBody.nextPage());
    }

    @Test
    public void getAllItems_ReturnsBadRequest_WhenInvalidPageOrSize() {
        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(-1, 2);
        assertEquals(400, response.getStatusCode().value());

        response = itemController.getAllItems(0, 0);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void getAllItems_ReturnsEmptyList_WhenNoItemsAvailable() {
        when(itemService.getAllItems(0, 2)).thenReturn(List.of());
        when(itemService.getItemCount()).thenReturn(0);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 2);

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(0, responseBody.currentPage());
        assertTrue(responseBody.items().isEmpty());
        assertNull(responseBody.nextPage());
    }

    @Test
    public void getAllItems_WithSmallDescriptionFilter_ReturnsFilteredResults() {
        List<Item> filteredItems = List.of(
                new Item("901234", "Wireless Headphones", "High-quality wireless Bluetooth headphones", "Electronics",
                        12, "IN-STORE", "Barcode123", "AudioTech", 0, "headphones_main.jpg", "headphones_alt.jpg",
                        "http://target.com"));
        when(itemService.getAllItems(0, 10, "wireless")).thenReturn(filteredItems);
        when(itemService.getItemCount("wireless")).thenReturn(1);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 10, "wireless");

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(0, responseBody.currentPage());
        assertEquals(1, responseBody.items().size());
        assertEquals("Wireless Headphones", responseBody.items().get(0).smallDescription());
    }

    @Test
    public void getAllItems_WithSmallDescriptionFilter_CaseInsensitive() {
        List<Item> filteredItems = List.of(
                new Item("901234", "Wireless Headphones", "Description", "Electronics", 12, "IN-STORE", "Barcode123",
                        "Brand", 0, "img1.jpg", "img2.jpg", "http://target.com"),
                new Item("901235", "WIRELESS Mouse", "Description", "Electronics", 13, "ONLINE", "Barcode456", "Brand",
                        0, "mouse1.jpg", "mouse2.jpg", "http://target.com"));
        when(itemService.getAllItems(0, 10, "WIRELESS")).thenReturn(filteredItems);
        when(itemService.getItemCount("WIRELESS")).thenReturn(2);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 10, "WIRELESS");

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(2, responseBody.items().size());
    }

    @Test
    public void getAllItems_WithEmptySmallDescriptionFilter_ReturnsAllItems() {
        List<Item> allItems = List.of(
                new Item("901234", "Small Desc", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18,
                        "primary", "alternate", "http://target.com"),
                new Item("901235", "Small Desc 2", "Long Desc 2", "Category 2", 13, "ONLINE", "Barcode 2", "Brand 2",
                        21, "primary_2", "alternate_2", "http://target.com/2"));
        when(itemService.getAllItems(0, 2, "")).thenReturn(allItems);
        when(itemService.getItemCount("")).thenReturn(10);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 2, "");

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(2, responseBody.items().size());
    }

    @Test
    public void getAllItems_WithWhitespaceSmallDescriptionFilter_ReturnsAllItems() {
        List<Item> allItems = List.of(
                new Item("901234", "Test Item", "Long Desc", "Category", 12, "IN-STORE", "Barcode", "Brand", 18,
                        "primary", "alternate", "http://target.com"));
        when(itemService.getAllItems(0, 10, "   ")).thenReturn(allItems);
        when(itemService.getItemCount("   ")).thenReturn(1);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 10, "   ");

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(1, responseBody.items().size());
    }

    @Test
    public void getAllItems_WithSmallDescriptionFilter_NoMatches_ReturnsEmptyList() {
        when(itemService.getAllItems(0, 10, "nonexistent")).thenReturn(List.of());
        when(itemService.getItemCount("nonexistent")).thenReturn(0);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 10, "nonexistent");

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertTrue(responseBody.items().isEmpty());
        assertEquals(0, responseBody.currentPage());
        assertNull(responseBody.nextPage());
    }

    @Test
    public void getAllItems_WithSmallDescriptionFilter_PartialMatch() {
        List<Item> filteredItems = List.of(
                new Item("901234", "Bluetooth Speaker", "Portable Bluetooth speaker", "Electronics", 12, "IN-STORE",
                        "Barcode123", "AudioTech", 0, "speaker1.jpg", "speaker2.jpg", "http://target.com"),
                new Item("901235", "Bluetooth Headphones", "Wireless Bluetooth headphones", "Electronics", 13, "ONLINE",
                        "Barcode456", "AudioTech", 0, "headphones1.jpg", "headphones2.jpg", "http://target.com"));
        when(itemService.getAllItems(0, 10, "bluetooth")).thenReturn(filteredItems);
        when(itemService.getItemCount("bluetooth")).thenReturn(2);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 10, "bluetooth");

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(2, responseBody.items().size());
        assertTrue(responseBody.items().stream()
                .allMatch(item -> item.smallDescription().toLowerCase().contains("bluetooth")));
    }

    @Test
    public void getAllItems_WithSmallDescriptionFilter_AndInvalidPagination_ReturnsBadRequest() {
        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(-1, 10, "wireless");
        assertEquals(400, response.getStatusCode().value());

        response = itemController.getAllItems(0, 0, "wireless");
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void getAllItems_WithSmallDescriptionFilter_TestsPagination() {
        List<Item> pageItems = List.of(
                new Item("1", "Wireless Item 1", "Desc", "Cat", 1, "IN-STORE", "BC1", "Brand", 0, "img1", "img2",
                        "url"),
                new Item("2", "Wireless Item 2", "Desc", "Cat", 2, "IN-STORE", "BC2", "Brand", 0, "img1", "img2",
                        "url"));
        when(itemService.getAllItems(0, 10, "wireless")).thenReturn(pageItems);
        when(itemService.getItemCount("wireless")).thenReturn(15);

        ResponseEntity<PaginatedResponse<ItemResponse>> response = itemController.getAllItems(0, 10, "wireless");

        assertEquals(200, response.getStatusCode().value());

        PaginatedResponse<ItemResponse> responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(0, responseBody.currentPage());
        assertEquals(2, responseBody.items().size());
        assertEquals(1, responseBody.nextPage());
    }
}
