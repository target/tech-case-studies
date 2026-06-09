package com.target.retail.cart.data;

import com.target.retail.cart.model.StoredCartLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartDatabaseTest {

    private static final String[] testCarts = { "cart-1.csv", "cart-2.csv"};

    private static final String cartsDataPath = "./carts-test/";

    private static final String testDataForCart1 = """
   lineId,cartId,tcin,quantity,createdOn,updatedOn
   10001,cart-1,987612,10,2025-03-10T00:14:45.73Z,2025-03-16T00:15:45.73Z
   10002,cart-1,789123,12,2025-03-11T00:11:45.73Z,2025-03-14T00:15:45.73Z
   10003,cart-1,456788,1,2025-03-09T00:15:45.73Z,2025-03-15T00:15:45.73Z
   """ ;

    private static final String testDataForCart2 = """
   lineId,cartId,tcin,quantity,createdOn,updatedOn
   10004,cart-2,987612,1,2025-03-16T00:14:45.73Z,2025-03-16T00:15:45.73Z
   10005,cart-2,789123,2,2025-03-14T00:11:45.73Z,2025-03-14T00:15:45.73Z
   10006,cart-2,456788,3,2025-03-13T00:15:45.73Z,2025-03-15T00:15:45.73Z
   """ ;

    private CartDatabase testInstance;

    @BeforeEach
    void setUp() throws Exception {
        writeTestData();
        testInstance = new CartDatabase("./carts-test/");
    }

    @AfterEach
    void tearDown() throws IOException {
        clearTestData();
    }

    @Test
    void testGetCart() {
        assertFalse(testInstance.getCart("cart-1").isEmpty());
        assertEquals(3, testInstance.getCart("cart-1").size(), "unexpected number of lines in cart-1");
    }

    @Test
    void testGetCart_TimestampsAreParsedCorrectly() {
        List<StoredCartLine> lines = testInstance.getCart("cart-1");
        StoredCartLine first = lines.get(0);
        assertEquals(ZonedDateTime.parse("2025-03-10T00:14:45.73Z"), first.createdOn());
        assertEquals(ZonedDateTime.parse("2025-03-16T00:15:45.73Z"), first.updatedOn());
    }

    @Test
    void testNonExistentCart() {
        assertTrue(testInstance.getCart("cart-99").isEmpty());
    }



    @Test
    void testUpdateCart() {
        String cartId = "cart-1";
        // Prepare new data for cart-1
        List<StoredCartLine> updatedCartLines = List.of(
                new StoredCartLine("10001", cartId, "55401",  5, ZonedDateTime.parse("2025-03-10T00:14:45.73Z"), ZonedDateTime.now()),
                new StoredCartLine("10002", cartId, "55401", 8, ZonedDateTime.parse("2025-03-11T00:11:45.73Z"), ZonedDateTime.now())
        );

        // Update the cart
        testInstance.updateCart(cartId, updatedCartLines);

        // Verify the update
        List<StoredCartLine> result = testInstance.getCart("cart-1");
        assertEquals(2, result.size(), "unexpected number of lines in updated cart-1");
        assertEquals(5, result.get(0).quantity(), "unexpected quantity for line 10001 in updated cart-1");
        assertEquals(8, result.get(1).quantity(), "unexpected quantity for line 10002 in updated cart-1");
    }

    @Test
    void testUpdateCartWithNonMatchingCartIds() {
        String cartId = "cart-1";
        // Prepare new data with non-matching cartIds
        List<StoredCartLine> updatedCartLines = List.of(
                new StoredCartLine("10001", "cart-1", "55401",5, ZonedDateTime.parse("2025-03-10T00:14:45.73Z"), ZonedDateTime.now()),
                new StoredCartLine("10002", "non-matching-cart", "55401",  8, ZonedDateTime.parse("2025-03-11T00:11:45.73Z"), ZonedDateTime.now())
        );

        // Verify that a RuntimeException is thrown
        assertThrows(RuntimeException.class, () -> testInstance.updateCart(cartId, updatedCartLines), "Expected RuntimeException for non-matching cartIds");
    }

    private void writeTestData() throws IOException {

        File directory = new File(cartsDataPath);
        directory.mkdirs();
        File testFile = new File("./carts-test/"+testCarts[0]);
        BufferedWriter writer1 = new BufferedWriter(new FileWriter(testFile));
        try (writer1) {
            writer1.write(testDataForCart1);
            writer1.flush();
        }

        testFile = new File(cartsDataPath+testCarts[1]);
        BufferedWriter writer2 = new BufferedWriter(new FileWriter(testFile));
        try (writer2) {
            writer2.write(testDataForCart2);
            writer2.flush();
        }

    }

    private void clearTestData() {
        for(String testFile : testCarts) {
            File file = new File(cartsDataPath+testFile);
            file.delete();
        }
        File directory = new File(cartsDataPath);
        directory.delete();
    }
}

