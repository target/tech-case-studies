package com.target.retail.product.service;

import com.target.retail.product.data.CsvData;
import com.target.retail.product.model.ItemAvailability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class AvailabilityServiceTest {

    private AvailabilityService testInstance;

    private String testFile = "item_availability.csv";

    private String testData = """
    item_id,available_units,limited_quantity_threshold
    12345,20,10
    12346,3000,15
    12347,3,10
    """;

    @BeforeEach
    void setUp() throws Exception{
        writeTestAvailabilityFile(testFile);
        CsvData<ItemAvailability> itemAvailabilityCsvData = new CsvData<>(testFile, ItemAvailability.class);
        testInstance = new AvailabilityService(itemAvailabilityCsvData, "9012");
    }

    @AfterEach
    void tearDown() throws Exception {
        deleteTestAvailabilityFile(testFile);
    }

    @Test
    void shouldReturnResponse() {
        Optional<ItemAvailability> itemAvailability = testInstance.getItemAvailability("12345");

        assertTrue(itemAvailability.isPresent(), "Item not found in availability file");
        assertEquals(20, itemAvailability.get().availableUnits(), "The available units is not as expected (20):"+itemAvailability.get().availableUnits());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<ItemAvailability> itemAvailability = testInstance.getItemAvailability("0001");
        assertTrue(itemAvailability.isEmpty(), "Unexpected availability for item - 0001");
    }


    private void writeTestAvailabilityFile(String fileName) throws IOException {
        File testFile = new File(fileName);
        FileWriter writer = new FileWriter(testFile);
        try(writer) {
            writer.write(testData);
        }
    }

    private void deleteTestAvailabilityFile(String fileName) throws IOException {
        File testFile = new File(fileName);
        if(!testFile.delete()) {
            throw new IOException("Could not delete test file : "+fileName);
        }
    }
}
