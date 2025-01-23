package com.target.retail.data.services.service;

import com.target.retail.data.services.data.CsvData;
import com.target.retail.data.services.dto.PriceResponse;
import com.target.retail.data.services.model.ItemPrice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.*;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceServiceTest {

    private PriceService priceService;

    private static final String testFileName = "test.prices";

    private static final String testData = """
item_id,price,type
12345,8.78,7.99,REGULAR
12346,2.10,1.99,REGULAR
12347,3.50,2.99,REGULAR
""";
    @BeforeEach
    void setUp() throws Exception {
        writeTestPriceFile(testFileName);
        CsvData<ItemPrice> itemPriceCsvData = new CsvData<>(testFileName, ItemPrice.class);

        priceService = new PriceService(itemPriceCsvData, "9012");
    }

    @AfterEach
    void tearDown() throws Exception {
        deleteTestPriceFile(testFileName);
    }

    @Test
    void shouldReturnPriceResponse() {
        String productId = "12345";
        PriceResponse expectedResponse = new PriceResponse(productId, BigDecimal.valueOf(8.78), BigDecimal.valueOf(7.99), "REGULAR");

        Optional<ItemPrice> itemPrice = priceService.getPrice(productId);

        assertEquals(expectedResponse.productId(), itemPrice.get().itemId(), "Product ID does not match.");
        assertEquals(expectedResponse.regularPrice(), itemPrice.get().regularPrice(), "Price does not match.");
        assertEquals(expectedResponse.priceType(), itemPrice.get().type(), "Price type does not match.");
    }


    private void writeTestPriceFile(String fileName) throws IOException {
        File testFile = new File(fileName);
        FileWriter writer = new FileWriter(testFile);
        try(writer) {
            writer.write(testData);
        }
    }

    private void deleteTestPriceFile(String fileName) throws IOException {
        File testFile = new File(fileName);
        if(!testFile.delete()) {
            throw new IOException("Could not delete test file : "+fileName);
        }
    }
}