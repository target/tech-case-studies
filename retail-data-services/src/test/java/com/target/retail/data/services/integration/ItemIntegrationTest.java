package com.target.retail.data.services.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

public class ItemIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testGetItem() throws Exception {
        getResponse("/items/" + testProductId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item_id").value(equalTo(testProductId)));
    }

    @Test
    public void testGetItem_notfound() throws Exception {
        getResponse("/items/" + invalidProductId)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllItems() throws Exception {
        getResponse("/items?page=0&size=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current_page").value(equalTo(0)))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(equalTo(2)));

        getResponse("/items?page=0&size=10&small_description=men")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.current_page").value(equalTo(0)))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].small_description",
                        containsStringIgnoringCase("men")));

        getResponse("/items?page=-1&size=10")
                .andExpect(status().isBadRequest());
    }
}
