package com.target.retail.cart.controller;

import com.target.retail.cart.controller.dto.AddItemRequest;
import com.target.retail.cart.controller.dto.CartResponse;
import com.target.retail.cart.controller.dto.UpdateItemRequest;
import com.target.retail.cart.model.Cart;
import com.target.retail.cart.model.CartLineItem;
import com.target.retail.cart.service.CartService;
import com.target.retail.cart.service.behavior.Behaviors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cart/v1")
public class CartController {

    private CartService cartService;

    private Behaviors behaviors;


    public CartController(CartService cartService, Behaviors behaviors) {
        this.cartService = cartService;
        this.behaviors = behaviors;
    }

    @Operation(summary = "Create a new cart", description = "Creates a new cart with the provided items and returns the cart details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created cart",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request due to duplicate TCINs",
                    content = @Content)
    })
    @PostMapping("/carts")
    public ResponseEntity<CartResponse> createCart(@RequestBody List<AddItemRequest> addItems) {

        if (addItems.stream().map(AddItemRequest::tcin).distinct().count() != addItems.size()) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Integer> itemsInCart = addItems.stream()
                .collect(Collectors.toMap(AddItemRequest::tcin, AddItemRequest::quantity));

        String cartId = cartService.createCart(itemsInCart);

        return getCart(cartId);
    }

    @Operation(summary = "Get cart by ID", description = "Returns the cart details based on the cart identifier provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cart",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content)
    })
    @GetMapping("/carts/{id}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String id) {

        Optional<Cart> cart = cartService.getCart(id);
        return behaviors.getConfiguredBehavior().execute( ()-> cart.map(value -> ResponseEntity.ok(CartResponse.from(value))).orElseGet(() -> ResponseEntity.notFound().build()));

    }

    @Operation(summary = "Remove item from cart using product id", description = "Removes the item from the cart and returns the cart details after the removal. Removing the last item from a cart will also remove the cart.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully removed item from cart",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartResponse.class)) }),
            @ApiResponse(responseCode = "204", description = "Successfully removed last item from cart, removing the cart as well",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content)
    })
    @DeleteMapping("/carts/{id}/items/{tcin}")
    public ResponseEntity<CartResponse> removeItemFromCart(@PathVariable String id, @PathVariable String tcin) {
        if(cartService.getCart(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        cartService.removeItem(id, tcin);
        if (cartService.getCart(id).isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return getCart(id);
        }
    }

    @Operation(summary = "Add an item to the cart", description = "Adds an item to the cart and returns the updated cart details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added item to cart",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content)
    })
    @PostMapping("/carts/{id}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable String id, @RequestBody AddItemRequest addItemRequest) {
        if(cartService.getCart(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        cartService.addItem(id, addItemRequest.tcin(), addItemRequest.quantity());
        return getCart(id);
    }

    @Operation(summary = "Update an item in the cart", description = "Updates the quantity of an item in the cart and returns the updated cart details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated item in cart",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartResponse.class)) }),
            @ApiResponse(responseCode = "404", description = "Cart or item not found",
                    content = @Content)
    })
    @PatchMapping("/carts/{id}/items/{tcin}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable String id, @PathVariable String tcin, @RequestBody UpdateItemRequest updateItemRequest) {

        Optional<CartLineItem> cartLineItem = cartService.getCart(id)
                .flatMap( it -> it.findByTcin(tcin));
        if(cartLineItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        cartService.updateCartItem(id, tcin, updateItemRequest.quantity());
        return getCart(id);

    }

}
