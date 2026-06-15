package com.target.retail.cart.data;

import com.target.retail.cart.model.StoredCartLine;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartDatabase {

    @Value("${cart.path:/data/carts/}")
    private String dataPath;
    private Map<String, List<StoredCartLine>> cartMap;

    public CartDatabase() {
    }

    public CartDatabase(String dataPath) {
        this.dataPath = dataPath;
        init();
    }

    @PostConstruct
    private void init() {
        cartMap = initializeDatabase(dataPath);
    }


    public List<StoredCartLine> getCart(String id) {
        return cartMap.getOrDefault(id, Collections.emptyList());
    }

    public void addLine(StoredCartLine storedCartLine) {
        cartMap.get(storedCartLine.cartId()).add(storedCartLine);
    }

    public void updateCart(String cartId, List<StoredCartLine> cartLines) {
        Set<String> cartIds = cartLines.stream().map(StoredCartLine::cartId).collect(Collectors.toSet());

        boolean hasMismatch = !cartLines.stream()
                .map(StoredCartLine::cartId)
                .filter(id -> !id.equals(cartId))
                .toList().isEmpty();
        if (hasMismatch) {
            throw new RuntimeException("cart lines cartIds that does not equal " + cartId);
        }

        cartMap.put(cartId, Collections.unmodifiableList(cartLines));
    }

    public String newCartId() {
        return String.valueOf(Math.abs(UUID.randomUUID().hashCode()));
    }

    private Map<String, List<StoredCartLine>> loadCart(String cartFile) {
        CsvData<StoredCartLine> csvCartData = new CsvData<>(cartFile, StoredCartLine.class);
        return csvCartData.mapUsingKey(StoredCartLine::cartId);
    }

    private Map<String, List<StoredCartLine>> initializeDatabase(String dataFolder) {
        File dataDirectory = new File(dataFolder);
        Map<String, List<StoredCartLine>> cartMap = new HashMap<>();
        Arrays.asList(Objects.requireNonNull(dataDirectory.listFiles())).forEach(cartFile -> {
            Map<String, List<StoredCartLine>> map = loadCart(cartFile.getAbsolutePath());
            map.forEach((key, value) -> cartMap.merge(key, value, (existing, newValue) -> {
                existing.clear();
                existing.addAll(newValue);
                return existing;
            }));
        });
        return cartMap;
    }

}
