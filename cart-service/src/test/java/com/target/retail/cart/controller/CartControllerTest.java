package com.target.retail.cart.controller;

import com.target.retail.cart.controller.dto.AddItemRequest;
import com.target.retail.cart.controller.dto.CartResponse;
import com.target.retail.cart.controller.dto.UpdateItemRequest;
import com.target.retail.cart.model.Cart;
import com.target.retail.cart.model.CartLineItem;
import com.target.retail.cart.model.Item;
import com.target.retail.cart.model.Price;
import com.target.retail.cart.service.CartService;
import com.target.retail.cart.service.behavior.Behaviors;
import com.target.retail.cart.service.behavior.InducedBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private Behaviors behaviors;

    @InjectMocks
    private CartController cartController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testGetCartWhenACartExists() {
        // Mock data
        Cart mockCart = new Cart(
                "cart123",
                new BigDecimal("10"),
                new BigDecimal("10"),
                List.of(newCartLineItem())
        );

        // Mock behavior
        when(cartService.getCart("cart123")).thenReturn(Optional.of(mockCart));
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.getCart("cart123");

        // Verify the response
        assertEquals(ResponseEntity.ok(CartResponse.from(mockCart)), response);
    }

    @Test
    public void testGetCartWhenCartDoesnotExist() {
        // Mock behavior
        when(cartService.getCart("cart123")).thenReturn(Optional.empty());
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.getCart("cart123");

        // Verify the response
        assertEquals(ResponseEntity.notFound().build(), response);
    }
    @Test
    public void testRemoveItemFromCart() {
        // Mock data
        Cart mockCartPreDelete = new Cart(
                "cart123",
                new BigDecimal("10"),
                new BigDecimal("10"),
                List.of(newCartLineItem("1"), newCartLineItem("2"))
        );
        Cart mockCartPostDelete = new Cart(
                "cart123",
                new BigDecimal("10"),
                new BigDecimal("10"),
                List.of(newCartLineItem("2"))
        );

        // Mock behavior
        when(cartService.getCart("cart123"))
                .thenReturn(Optional.of(mockCartPreDelete))
                .thenReturn(Optional.of(mockCartPostDelete));
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.removeItemFromCart("cart123", "item1");

        // Verify the response
        assertEquals(ResponseEntity.ok(CartResponse.from(mockCartPostDelete)), response);
    }

    @Test
    public void testRemoveLastItemFromCart() {
        // Mock data
        Cart mockCart = new Cart(
                "cart123",
                new BigDecimal("10"),
                new BigDecimal("10"),
                List.of(newCartLineItem())
        );

        // Mock behavior
        when(cartService.getCart("cart123"))
                .thenReturn(Optional.of(mockCart))
                .thenReturn(Optional.empty());
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.removeItemFromCart("cart123", "item1");

        // Verify the response
        assertEquals(ResponseEntity.noContent().build(), response);
    }

    @Test
    public void testRemoveItemFromCartWhenCartDoesNotExist() {
        // Mock behavior
        when(cartService.getCart("cart123")).thenReturn(Optional.empty());
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.removeItemFromCart("cart123", "item1");

        // Verify the response
        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    public void testUpdateCartItem() {
        // Mock data
        String cartId = "cart123";
        String tcin = "item1";
        int newQuantity = 5;
        Cart mockCart = new Cart(
                cartId,
                new BigDecimal("10"),
                new BigDecimal("10"),
                List.of(newCartLineItem())
        );

        // Mock behavior
        when(cartService.getCart(cartId)).thenReturn(Optional.of(mockCart));
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.updateItem(cartId, tcin, new UpdateItemRequest(newQuantity));

        // Verify the response
        assertEquals(ResponseEntity.ok(CartResponse.from(mockCart)), response);
    }

    @Test
    public void testUpdateCartItemWhenCartDoesNotExist() {
        // Mock data
        String cartId = "cart123";
        String tcin = "item1";
        int newQuantity = 5;

        // Mock behavior
        when(cartService.getCart(cartId)).thenReturn(Optional.empty());
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.updateItem(cartId, tcin, new UpdateItemRequest(newQuantity));

        // Verify the response
        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    public void testUpdateCartItemWhenTcinNotFoundInCart() {
        // Mock data
        String cartId = "cart123";
        String tcin = "missingItemId";
        int newQuantity = 5;
        Cart mockCart = new Cart(
                cartId,
                new BigDecimal("10"),
                new BigDecimal("10"),
                List.of(newCartLineItem())
        );

        // Mock behavior
        when(cartService.getCart(cartId)).thenReturn(Optional.of(mockCart));
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.updateItem(cartId, tcin, new UpdateItemRequest(newQuantity));

        // Verify the response
        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    public void testCreateCart() {
        // Mock data
        List<AddItemRequest> addItems = List.of(
                new AddItemRequest("item1", 2),
                new AddItemRequest("item2", 3)
        );
        String cartId = "cart123";
        Cart mockCart = new Cart(
                cartId,
                new BigDecimal("50"),
                new BigDecimal("50"),
                List.of(newCartLineItem("1"), newCartLineItem("2"))
        );

        // Mock behavior
        when(cartService.createCart(Map.of("item1", 2, "item2", 3))).thenReturn(cartId);
        when(cartService.getCart(cartId)).thenReturn(Optional.of(mockCart));
        when(behaviors.getConfiguredBehavior()).thenReturn(createInducedBehavior());

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.createCart(addItems);

        // Verify the response
        assertEquals(ResponseEntity.ok(CartResponse.from(mockCart)), response);
    }

    @Test
    public void testCreateCartWithDuplicateTcins() {
        // Mock data
        List<AddItemRequest> addItems = List.of(
                new AddItemRequest("item1", 2),
                new AddItemRequest("item1", 3) // Duplicate tcin
        );

        // Execute the method
        ResponseEntity<CartResponse> response = cartController.createCart(addItems);

        // Verify the response
        assertEquals(ResponseEntity.badRequest().build(), response);
    }


    private InducedBehavior createInducedBehavior() {
        return new InducedBehavior() {
            @Override
            public <T> T execute(Supplier<T> supplier) {
                return supplier.get();
            }
        };
    }


    private CartLineItem newCartLineItem(String suffix) {
        String lineId = "line%s".formatted(suffix);
        String itemId = "item%s".formatted(suffix);

        return new CartLineItem(lineId,
                new Item(itemId, "Small Description", "Long Description", "Brand", "Category", "Class", "primaryImage", "alternateImage", "baseUrl"), 1, new Price(itemId, new BigDecimal("10"), Optional.empty()), ZonedDateTime.now(), ZonedDateTime.now());
    }

    private CartLineItem newCartLineItem() {
        return newCartLineItem("1");
    }

}