package com.target.retail.data.services.controller;

import com.target.retail.data.services.dto.ItemResponse;
import com.target.retail.data.services.dto.PaginatedResponse;
import com.target.retail.data.services.exception.ItemNotFoundException;
import com.target.retail.data.services.model.Item;
import com.target.retail.data.services.service.ItemService;
import com.target.retail.data.services.service.behavior.Behaviors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ItemController {

    private final ItemService itemService;

    private final Behaviors behaviors;

    public ItemController(ItemService itemService, Behaviors behaviors) {
        this.itemService = itemService;
        this.behaviors = behaviors;
    }

    @Operation(summary = "Get the product details by ID", description = "Returns the information about the product for the provided item-id. Like description, barcode, etc.")

    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved items", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ItemResponse.class))}), @ApiResponse(responseCode = "404", description = "Item information is not available", content = @Content)})
    @GetMapping("/items/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable String id) {
        Optional<Item> item = itemService.getItem(id);
        return behaviors.getConfiguredBehavior().execute(() -> item
                .map(it -> ResponseEntity.ok(new ItemResponse(it)))
                .orElseThrow(() -> new ItemNotFoundException(id)));
    }

    @Operation(summary = "Get all products with pagination", description = "Returns a paginated list of products with optional filtering.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved products", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ItemResponse.class))}), @ApiResponse(responseCode = "400", description = "Invalid pagination parameters", content = @Content)})
    @GetMapping("/items")
    public ResponseEntity<PaginatedResponse<ItemResponse>> getAllItems(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "2")
            @RequestParam(defaultValue = "2") int size,
            
            @Parameter(description = "Filter products by small description", example = "wireless")
            @RequestParam(name = "small_description", required = false) String smallDescription) {
        
        return behaviors.getConfiguredBehavior().execute(() -> {
            if (page < 0 || size <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Item> items = itemService.getAllItems(page, size, smallDescription);
            int totalItems = itemService.getItemCount(smallDescription);
            
            List<ItemResponse> responses = items.stream()
                    .map(ItemResponse::new)
                    .toList();
            
            return ResponseEntity.ok(new PaginatedResponse<>(page, responses, totalItems, size));
        });
    }

    public ResponseEntity<PaginatedResponse<ItemResponse>> getAllItems(int page, int size) {
        return getAllItems(page, size, null);
    }
}
