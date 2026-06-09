package com.target.retail.data.services.controllers;

import com.target.retail.data.services.dto.PriceResponse;
import com.target.retail.data.services.model.ItemPrice;
import com.target.retail.data.services.service.PriceService;
import com.target.retail.data.services.controller.PriceController;
import com.target.retail.data.services.service.behavior.Behaviors;
import com.target.retail.data.services.service.behavior.InducedBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PriceControllerTest {

    private PriceController priceController;
    private PriceService priceService;
    private Behaviors behaviors;

    @BeforeEach
    void setUp() {
        priceService = mock(PriceService.class);
        behaviors = mock(Behaviors.class);
        when(behaviors.getConfiguredBehavior()).thenReturn(new InducedBehavior() {
            @Override
            public <T> T execute(Supplier<T> supplier) {
                return supplier.get();
            }
        });
        priceController = new PriceController(priceService, behaviors);
    }

    @Test
    void shouldReturnPriceResponse() {
        String productId = "12345";
        // PriceResponse mockResponse = new PriceResponse(productId,
        // BigDecimal.valueOf(19.99), "Regular");

        ItemPrice mockItemPrice = new ItemPrice(productId, BigDecimal.valueOf(19.99), BigDecimal.valueOf(17.99),
                "REGULAR");

        when(priceService.getPrice(productId)).thenReturn(Optional.of(mockItemPrice));

        ResponseEntity<PriceResponse> response = priceController.getPrice(productId);

        assertEquals(productId, Objects.requireNonNull(response.getBody()).itemId(), "Product ID does not match.");

        PriceResponse responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(BigDecimal.valueOf(19.99), responseBody.regular(), "Price does not match.");
        assertEquals("REGULAR", responseBody.priceType(), "Price type does not match.");
    }

}
