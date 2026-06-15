package com.target.retail.product.controllers;

import com.target.retail.product.controller.AvailabilityController;
import com.target.retail.product.dto.AvailabilityResponse;
import com.target.retail.product.exception.AvailabilityNotFoundException;
import com.target.retail.product.model.ItemAvailability;
import com.target.retail.product.service.AvailabilityService;
import com.target.retail.product.service.behavior.Behaviors;
import com.target.retail.product.service.behavior.InducedBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AvailabilityControllerTest {

    private AvailabilityController availabilityController;
    private AvailabilityService availabilityService;
    private Behaviors behaviors;

    @BeforeEach
    void setUp() {
        availabilityService = mock(AvailabilityService.class);
        behaviors = mock(Behaviors.class);
        when(behaviors.getConfiguredBehavior()).thenReturn(new InducedBehavior() {
            @Override
            public <T> T execute(Supplier<T> supplier) {
                return supplier.get();
            }
        });
        availabilityController = new AvailabilityController(availabilityService, behaviors);
    }

    @Test
    void shouldReturnAvailabilityResponse() {
        String productId = "12345";
        ItemAvailability mockItemAvailability = new ItemAvailability(productId, 20, 2);

        when(availabilityService.getItemAvailability(productId)).thenReturn(Optional.of(mockItemAvailability));

        ResponseEntity<AvailabilityResponse> response = availabilityController.getAvailability(productId);

        assertEquals(productId, Objects.requireNonNull(response.getBody()).itemId(), "Product ID does not match.");

        AvailabilityResponse responseBody = response.getBody();
        assertNotNull(responseBody);

        assertEquals(20, responseBody.availableUnits(), "Available Units does not match.");
        assertEquals(2, responseBody.limitedQuantityThreshold(), "Limited Quantity Threshold does not match.");
    }

    @Test
    void shouldReturn404WhenAvailabilityNotFound() {
        String productId = "11111";
        when(availabilityService.getItemAvailability(productId)).thenReturn(Optional.empty());
        assertThrows(AvailabilityNotFoundException.class, () -> availabilityController.getAvailability(productId));
    }

}
