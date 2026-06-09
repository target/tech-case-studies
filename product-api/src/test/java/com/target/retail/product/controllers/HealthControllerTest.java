package com.target.retail.product.controllers;


import com.target.retail.product.controller.HealthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    private HealthController healthController;

    @BeforeEach
    void setUp() {
        healthController = new HealthController();
    }

    @Test
    void shouldReturnHealthyStatus() {
        assertEquals("Ok", healthController.healthCheck(), "Incorrect health status.");
    }
}