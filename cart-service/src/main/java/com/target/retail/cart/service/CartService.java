package com.target.retail.cart.service;

import com.target.retail.cart.data.CartDatabase;
import com.target.retail.cart.service.client.dto.ItemApiResponse;
import com.target.retail.cart.service.client.dto.PriceApiResponse;
import com.target.retail.cart.model.StoredCartLine;
import com.target.retail.cart.model.Cart;
import com.target.retail.cart.model.CartLineItem;
import com.target.retail.cart.model.Item;
import com.target.retail.cart.model.Price;
import com.target.retail.cart.service.client.ItemApiClient;
import com.target.retail.cart.service.client.PriceApiClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CartService{

    private PriceApiClient priceApiClient;

    private ItemApiClient itemApiClient;

    private TaxCalculator taxCalculator;

    private DeliveryChargeCalculator deliveryChargeCalculator;

    private CartDatabase cartDatabase;



    public CartService(CartDatabase cartDatabase, PriceApiClient priceApiClient, ItemApiClient itemApiClient, TaxCalculator taxCalculator, DeliveryChargeCalculator deliveryChargeCalculator) {
        this.cartDatabase = cartDatabase;
        this.priceApiClient = priceApiClient;
        this.itemApiClient = itemApiClient;
        this.taxCalculator = taxCalculator;
        this.deliveryChargeCalculator = deliveryChargeCalculator;
    }

    public String createCart(Map<String, Integer> itemsInCart) {
        String cartId = cartDatabase.newCartId();
        List<StoredCartLine> storedCartLines = itemsInCart.entrySet()
                .stream()
                .map(entry ->
                        new StoredCartLine(cartId + "-" + entry.getKey(),
                                cartId, entry.getKey(), entry.getValue(), ZonedDateTime.now(),
                                ZonedDateTime.now()))
                .toList();

        cartDatabase.updateCart(cartId, storedCartLines);

        return cartId;
    }

    public Optional<Cart> getCart(String cartId) {
        List<StoredCartLine> storedCartLines = cartDatabase.getCart(cartId);
        if(storedCartLines.isEmpty()) {
            return Optional.empty();
        } else {
            List<CartLineItem> cartLineItemList = storedCartLines.stream()
                    .map(this::assembleCartLineItem)
                    .collect(Collectors.toList());

            BigDecimal totalTax = cartLineItemList.stream()
                    .map(it -> {
                        BigDecimal taxableAmount = it.price().getCurrentPrice().multiply(new BigDecimal(it.quantity()));
                        return taxCalculator.calculateTax(taxableAmount, it.item().category());
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal deliveryCharge = calculateDeliveryCharge(cartLineItemList);

            return Optional.of(new Cart(cartId, totalTax, deliveryCharge, cartLineItemList));
        }


    }

    public void removeItem(String cartId, String tcin) {
        Optional<Cart> cart = getCart(cartId);
        if (cart.isEmpty()) {
            throw new RuntimeException("No cart found with id " + cartId);
        }

        List<CartLineItem> updatedItems = cart.get().cartLineItems().stream()
                .filter(it -> !it.item().tcin().equals(tcin))
                .collect(Collectors.toList());

        List<StoredCartLine> storedCartLines = updatedItems.stream()
                .map(it ->
                        new StoredCartLine(it.lineItemId(),
                                cart.get().id(),
                                it.item().tcin(),
                                it.quantity(),
                                it.createdOn(),
                                it.updatedOn()))
                .toList();
        cartDatabase.updateCart(cartId, storedCartLines);
    }

    public void addItem(String cartId, String tcin, Integer quantity) {
        List<StoredCartLine> storedCartLines = new ArrayList<>(cartDatabase.getCart(cartId));
        if (storedCartLines.isEmpty()) {
            throw new RuntimeException("No cart found with id " + cartId);
        }

        Optional<StoredCartLine> storedCartLineForTcin = storedCartLines.stream().filter( it -> it.tcin().equals(tcin)).findFirst();
        Integer quantityForNewStoredLine = quantity;
        if(storedCartLineForTcin.isPresent()) {
            quantityForNewStoredLine += storedCartLineForTcin.get().quantity() ;
            storedCartLines.remove(storedCartLineForTcin.get());
        }

        StoredCartLine storedCartLine =
                new StoredCartLine(storedCartLineForTcin.map(StoredCartLine::lineId).orElseGet(() -> cartId + "-" + tcin),
                        cartId, tcin, quantityForNewStoredLine, ZonedDateTime.now(), ZonedDateTime.now());
        storedCartLines.add(storedCartLine);
        cartDatabase.updateCart(cartId, storedCartLines);

    }

    public void updateCartItem(String cartId, String tcin, Integer quantity) {
        List<StoredCartLine> storedCartLines = new ArrayList<>(cartDatabase.getCart(cartId));
        Optional<StoredCartLine> storedCartLineForTcin = storedCartLines.stream().filter( it -> it.tcin().equals(tcin)).findFirst();
        if(storedCartLineForTcin.isPresent()) {
            storedCartLines.remove(storedCartLineForTcin.get());
            if(quantity > 0) {
                StoredCartLine storedCartLine =
                        new StoredCartLine(storedCartLineForTcin.get().lineId(),
                                cartId, tcin, quantity, ZonedDateTime.now(), ZonedDateTime.now());
                storedCartLines.add(storedCartLine);
            }
            cartDatabase.updateCart(cartId, storedCartLines);
        } else {
            throw new RuntimeException("No cart line found for tcin "+tcin);
        }

    }

    private BigDecimal calculateDeliveryCharge(List<CartLineItem> cartLineItemList) {
        Map<Item, Integer> itemMap = cartLineItemList.stream().collect(Collectors.toMap(CartLineItem::item, CartLineItem::quantity));
        return deliveryChargeCalculator.calculateDeliveryCharges(itemMap);
    }
    private CartLineItem assembleCartLineItem(StoredCartLine scl) {
        Item item = getItem(scl.tcin());
        Price price = getPriceForItem(scl.tcin());
        
        return new CartLineItem(scl.lineId(), item, scl.quantity(), price, scl.createdOn(), scl.updatedOn());
    }



    private Price getPriceForItem(String tcin) {
        PriceApiResponse priceResponse = priceApiClient.getPricing(tcin);

        if(priceResponse.priceType().equals("SALE")) {
            return new Price(tcin, priceResponse.regular(), Optional.of(priceResponse.sale()));
        } else {
            return new Price(tcin, priceResponse.regular(), Optional.empty());
        }

    }

    private Item getItem(String tcin) {
        ItemApiResponse itemApiResponse = itemApiClient.getItem(tcin);

        return new Item(itemApiResponse.itemId(),
                itemApiResponse.smallDescription(),
                itemApiResponse.longDescription(),
                itemApiResponse.brandName(),
                itemApiResponse.category(),
                itemApiResponse.merchClass().toString(),
                itemApiResponse.imageData().primary(),
                itemApiResponse.imageData().alternate(),
                itemApiResponse.imageData().baseUrl());
    }

}
