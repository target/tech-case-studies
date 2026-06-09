package com.target.retail.cart.service;

import com.target.retail.cart.data.CartDatabase;
import com.target.retail.cart.exception.CartLineItemNotFoundException;
import com.target.retail.cart.model.Cart;
import com.target.retail.cart.model.CartLineItem;
import com.target.retail.cart.model.Item;
import com.target.retail.cart.model.Price;
import com.target.retail.cart.model.StoredCartLine;
import com.target.retail.cart.service.client.ItemApiClient;
import com.target.retail.cart.service.client.PriceApiClient;
import com.target.retail.cart.service.client.dto.ItemApiResponse;
import com.target.retail.cart.service.client.dto.PriceApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class CartServiceTest {

    private CartService cartService;
    private CartDatabase cartDatabase;
    private PriceApiClient priceApiClient;
    private ItemApiClient itemApiClient;
    private TaxCalculator taxCalculator;
    private DeliveryChargeCalculator deliveryChargeCalculator;

    @BeforeEach
    public void setUp() {
        cartDatabase = Mockito.mock(CartDatabase.class);
        priceApiClient = Mockito.mock(PriceApiClient.class);
        itemApiClient = Mockito.mock(ItemApiClient.class);
        taxCalculator = Mockito.mock(TaxCalculator.class);
        deliveryChargeCalculator = Mockito.mock(DeliveryChargeCalculator.class);

        cartService = new CartService(cartDatabase, priceApiClient, itemApiClient, taxCalculator, deliveryChargeCalculator);
    }

    @Test
    public void testGetCart() {
        // Mock data
        String cartId = "123";
        String itemId = "456";
        String deliveryZip = "78910";
        StoredCartLine storedCartLine = new StoredCartLine("1", cartId, itemId, 2,  ZonedDateTime.now(), ZonedDateTime.now());
        List<StoredCartLine> storedCartLines = List.of(storedCartLine);

        Item item = new Item(itemId, "Short description", "Long description", "Brand", "Category",  12, "PrimaryImage", "AlternateImage", "BaseUrl");
        Price price = new Price(itemId, BigDecimal.valueOf(10.00), Optional.of(BigDecimal.valueOf(8.00)));
        CartLineItem cartLineItem = new CartLineItem("1", item, 2, price, ZonedDateTime.now(), ZonedDateTime.now());

        when(cartDatabase.getCart(cartId)).thenReturn(storedCartLines);
        when(itemApiClient.getItem(anyString())).thenReturn(new ItemApiResponse(itemId, "Short description", "Long description", "Brand", 5, "ONLINE", "BAR12345", "BRAND", 21, new ItemApiResponse.ImageData("PrimaryImage", "AlternateImage", "BaseUrl")));
        when(priceApiClient.getPricing(anyString())).thenReturn(new PriceApiResponse(itemId, BigDecimal.valueOf(10.00), BigDecimal.valueOf(8.00), "SALE"));
        when(taxCalculator.calculateTax(Mockito.any(BigDecimal.class), Mockito.anyString())).thenReturn(BigDecimal.valueOf(1.00));
        when(deliveryChargeCalculator.calculateDeliveryCharges(Mockito.anyMap())).thenReturn(BigDecimal.valueOf(5.00));

        // Call the method
        Optional<Cart> cart = cartService.getCart(cartId);

        // Assertions
        assertEquals(cartId, cart.get().id());
        assertEquals(BigDecimal.valueOf(16.00), cart.get().subTotal());
        assertEquals(BigDecimal.valueOf(1.00), cart.get().totalTax());
        assertEquals(BigDecimal.valueOf(5.00), cart.get().deliveryCharges());
        assertEquals(1, cart.get().cartLineItems().size());
        assertEquals(cartLineItem.item().itemId(), cart.get().cartLineItems().get(0).item().itemId());
    }

    @Test
    public void testRemoveItem() {
        // Mock data
        String cartId = "123";
        String itemId1 = "456";
        String itemId2 = "123";
        String deliveryZip = "78910";
        ZonedDateTime now = ZonedDateTime.now();
        StoredCartLine storedCartLine1 = new StoredCartLine("1", cartId, itemId1, 2, now, now);
        StoredCartLine storedCartLine2 = new StoredCartLine("2", cartId, itemId2, 10, now, now);

        List<StoredCartLine> storedCartLines = List.of(storedCartLine1, storedCartLine2);

        Item item = new Item(itemId1, "Short description", "Long description", "Brand", "Category", 12, "PrimaryImage", "AlternateImage", "BaseUrl");
        Price price = new Price(itemId1, BigDecimal.valueOf(10.00), Optional.of(BigDecimal.valueOf(8.00)));
        CartLineItem cartLineItem = new CartLineItem("1", item, 2, price, ZonedDateTime.now(), ZonedDateTime.now());

        when(cartDatabase.getCart(cartId)).thenReturn(storedCartLines);
        when(itemApiClient.getItem(itemId1)).thenReturn(new ItemApiResponse(itemId1, "Short description1", "Long description1", "Brand1", 5, "ONLINE", "BAR12345", "BRAND", 21, new ItemApiResponse.ImageData("PrimaryImage", "AlternateImage", "BaseUrl")));
        when(itemApiClient.getItem(itemId2)).thenReturn(new ItemApiResponse(itemId2, "Short description2", "Long description2", "Brand2", 5, "ONLINE", "BAR12345", "BRAND", 1, new ItemApiResponse.ImageData("PrimaryImage", "AlternateImage", "BaseUrl")));

        when(priceApiClient.getPricing(anyString())).thenReturn(new PriceApiResponse(itemId1, BigDecimal.valueOf(10.00), BigDecimal.valueOf(8.00), "SALE"));
        when(taxCalculator.calculateTax(Mockito.any(BigDecimal.class), Mockito.anyString())).thenReturn(BigDecimal.valueOf(1.00));
        when(deliveryChargeCalculator.calculateDeliveryCharges(Mockito.anyMap())).thenReturn(BigDecimal.valueOf(5.00));

        // Call the method to add item to cart
        Optional<Cart> cart = cartService.getCart(cartId);
        assertEquals(2, cart.get().cartLineItems().size());

        // Call the method to remove item from cart
        cartService.removeItem(cartId, itemId1);

        // verify the call to updateCart
        Mockito.verify(cartDatabase).updateCart(cartId, List.of( new StoredCartLine("2", cartId, itemId2, 10, now, now)));


    }

    @Test
    public void testAddItem_CartHasOneLineBeforeAddingNewOneAndItemIdIsNew() {
        // Mock data
        String cartId = "123";
        String itemId = "456";
        String existingItemId = "789";
        Integer quantity = 2;
        ZonedDateTime now = ZonedDateTime.now();
        List<StoredCartLine> storedCartLines = List.of(
                new StoredCartLine("1", cartId, existingItemId, 1, now, now)
        );

        when(cartDatabase.getCart(cartId)).thenReturn(storedCartLines);

        // Call the method to add item to cart
        cartService.addItem(cartId, itemId, quantity);

        // Verify the call to updateCart
        Mockito.verify(cartDatabase).updateCart(eq(cartId), argThat(list ->
                list.size() == 2 &&
                        list.get(0).getId().equals("1") &&
                        list.get(0).cartId().equals(cartId) &&
                        list.get(0).itemId().equals(existingItemId) &&
                        list.get(0).quantity() == 1 &&
                        list.get(1).getId().equals(cartId+"-"+itemId) &&
                        list.get(1).cartId().equals(cartId) &&
                        list.get(1).itemId().equals(itemId) &&
                        Objects.equals(list.get(1).quantity(), quantity)
        ));
    }

    @Test
    public void testAddItem_CartHasExistingItemId_QuantitiesUpdated() {
        // Mock data
        String cartId = "123";
        String itemId = "456";
        Integer existingQuantity = 2;
        Integer additionalQuantity = 3;
        ZonedDateTime now = ZonedDateTime.now();
        List<StoredCartLine> storedCartLines = List.of(
                new StoredCartLine("1", cartId, itemId, existingQuantity, now, now)
        );

        when(cartDatabase.getCart(cartId)).thenReturn(storedCartLines);

        // Call the method to add item to cart
        cartService.addItem(cartId, itemId, additionalQuantity);

        // Verify the call to updateCart
        Mockito.verify(cartDatabase).updateCart(eq(cartId), argThat(list ->
                list.size() == 1 &&
                        list.get(0).getId().equals("1") &&
                        list.get(0).cartId().equals(cartId) &&
                        list.get(0).itemId().equals(itemId) &&
                        list.get(0).quantity() == (existingQuantity + additionalQuantity)
        ));
    }


    @Test
    public void testUpdateCartItem_ItemFoundInCart() {
        // Mock data
        String cartId = "123";
        String itemId = "456";
        Integer newQuantity = 5;
        ZonedDateTime now = ZonedDateTime.now();
        StoredCartLine storedCartLine = new StoredCartLine("1", cartId, itemId, 2, now, now);
        List<StoredCartLine> storedCartLines = List.of(storedCartLine);

        when(cartDatabase.getCart(cartId)).thenReturn(storedCartLines);

        // Call the method to update the cart item
        cartService.updateCartItem(cartId, itemId, newQuantity);

        // Verify the call to updateCart
        Mockito.verify(cartDatabase).updateCart(eq(cartId), argThat(list ->
                list.size() == 1 &&
                        list.get(0).getId().equals("1") &&
                        list.get(0).cartId().equals(cartId) &&
                        list.get(0).itemId().equals(itemId) &&
                        Objects.equals(list.get(0).quantity(), newQuantity)
        ));
    }

    @Test
    public void testUpdateCartItem_ItemNotFoundInCart() {
        // Mock data
        String cartId = "123";
        String itemId = "456";
        Integer newQuantity = 5;

        // Mock an empty cart or a cart without the specified TCIN
        when(cartDatabase.getCart(cartId)).thenReturn(List.of());

        // Assert that the method throws a CartLineItemNotFoundException
        CartLineItemNotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(
                CartLineItemNotFoundException.class,
                () -> cartService.updateCartItem(cartId, itemId, newQuantity)
        );

        // Verify the exception message
        assertEquals("No cart line found for item id " + itemId, exception.getMessage());
    }

    @Test
    public void testUpdateCartItem_QuantityZero_ItemRemoved() {
        // Mock data
        String cartId = "123";
        String itemIdToRemove = "456";
        String itemIdToKeep = "789";
        Integer quantityToRemove = 0;
        Integer quantityToKeep = 5;
        ZonedDateTime now = ZonedDateTime.now();

        StoredCartLine lineToRemove = new StoredCartLine("1", cartId, itemIdToRemove, 2, now, now);
        StoredCartLine lineToKeep = new StoredCartLine("2", cartId, itemIdToKeep, quantityToKeep, now, now);
        List<StoredCartLine> storedCartLines = List.of(lineToRemove, lineToKeep);

        when(cartDatabase.getCart(cartId)).thenReturn(storedCartLines);

        // Call the method to update the cart item
        cartService.updateCartItem(cartId, itemIdToRemove, quantityToRemove);

        // Verify the call to updateCart
        Mockito.verify(cartDatabase).updateCart(eq(cartId), argThat(list ->
                list.size() == 1 &&
                        list.get(0).getId().equals("2") &&
                        list.get(0).cartId().equals(cartId) &&
                        list.get(0).itemId().equals(itemIdToKeep) &&
                        Objects.equals(list.get(0).quantity(), quantityToKeep)
        ));
    }

    @Test
    public void testCreateCart() {
        // Mock data
        String cartId = "123";
        String itemId1 = "456";
        String itemId2 = "789";
        Integer quantity1 = 2;
        Integer quantity2 = 3;

        // Mock the new cart ID generation
        when(cartDatabase.newCartId()).thenReturn(cartId);

        // Call the method to create a cart
        cartService.createCart(Map.of(itemId1, quantity1, itemId2, quantity2));

        // Verify the call to updateCart
        Mockito.verify(cartDatabase).updateCart(eq(cartId), argThat(list ->
                list.size() == 2 &&
                        list.stream().anyMatch(line ->
                                line.cartId().equals(cartId) &&
                                        line.itemId().equals(itemId1) &&
                                        Objects.equals(line.quantity(), quantity1)
                        ) &&
                        list.stream().anyMatch(line ->
                                line.cartId().equals(cartId) &&
                                        line.itemId().equals(itemId2) &&
                                        Objects.equals(line.quantity(), quantity2)
                        )
        ));
    }
}
