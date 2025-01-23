package com.target.retail.data.services.integration;

import com.target.retail.data.services.model.Item;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemIntegrationTest extends BaseIntegrationTest {
    @Test
    public void testGetItem() {
        getResponse("/items/" + testProductId)
                .expectStatus().isOk()
                .expectBody(Item.class)
                .value(item -> {
                    ;
                    assert item.getId().equals(testProductId);
                });
    }

    @Test
    public void testGetItem_notfound() {
        getResponse("/items/ " + invalidProductId)
                .expectStatus().isNotFound();
    }

    @Test
    public void testGetAllItems() {
        getResponse("/items?page=0&size=2")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.current_page").isEqualTo(0)
                .jsonPath("$.items").isArray()
                .jsonPath("$.items.length()").isEqualTo(2);

        getResponse("/items?page=0&size=10&small_description=men")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.current_page").isEqualTo(0)
                .jsonPath("$.items").isArray()
                .jsonPath("$.items").isNotEmpty()
                .jsonPath("$.items[0].small_description")
                .value(desc -> assertTrue(desc.toString().toLowerCase().contains("men")));

        getResponse("/items?page=-1&size=10")
                .expectStatus().isBadRequest();
    }
}
