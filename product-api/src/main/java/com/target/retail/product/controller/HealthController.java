package com.target.retail.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class HealthController {

    @Operation(summary = "Get health status", description = "Returns the health status of the application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved health status")
    })
    @GetMapping("/health")
    public String healthCheck() {
        return "Ok";
    }
}