package com.target.retail.data.services.controller;

import com.target.retail.data.services.dto.PriceResponse;
import com.target.retail.data.services.model.ItemPrice;
import com.target.retail.data.services.service.PriceService;
import com.target.retail.data.services.service.behavior.Behaviors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;

@RestController
public class PriceController {

    private final PriceService priceService;

    private final Behaviors behaviors;

    public PriceController(PriceService priceService, Behaviors behaviors) {
        this.priceService = priceService;
        this.behaviors = behaviors;
    }

    @Operation(summary = "Get price by product ID", description = "Retrieve the price details for a specific product by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved price", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PriceResponse.class))}), @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)})
    @GetMapping("/prices/{id}")
    public ResponseEntity<PriceResponse> getPrice(@PathVariable String id) {
        Optional<ItemPrice> itemPrice = priceService.getPrice(id);
        return behaviors.getConfiguredBehavior().execute(() -> itemPrice.map(price -> ResponseEntity.ok(new PriceResponse(price))).orElseGet(() -> ResponseEntity.notFound().build()));
    }

}