package com.target.retail.product.controller;

import com.target.retail.product.dto.AvailabilityResponse;
import com.target.retail.product.exception.AvailabilityNotFoundException;
import com.target.retail.product.model.ItemAvailability;
import com.target.retail.product.service.AvailabilityService;
import com.target.retail.product.service.behavior.Behaviors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    private final Behaviors behaviors;

    public AvailabilityController(AvailabilityService availabilityService, Behaviors behaviors) {
        this.availabilityService = availabilityService;
        this.behaviors = behaviors;
    }

    @Operation(summary = "Get availability by product ID", description = "Returns the available units along with threshold quantities for any provided item-id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved availability",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AvailabilityResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Availability information is not available",
                    content = @Content)
    })
    @GetMapping("/availability/{id}")
    public ResponseEntity<AvailabilityResponse> getAvailability(@PathVariable String id) {
        Optional<ItemAvailability> itemAvailability = availabilityService.getItemAvailability(id);
        return behaviors.getConfiguredBehavior().execute(() ->
                itemAvailability.map(it ->
                                ResponseEntity.ok(new AvailabilityResponse(it)))
                        .orElseThrow(() -> new AvailabilityNotFoundException(id))
        );
    }


}