package com.target.retail.cart.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
  This test is mostly here to make sure the taxes are wired in correctly from the application.yml. Changes
  to that file should be reflected in this test.
 */
@SpringBootTest
public class TaxCalculatorTest {

    @Autowired
    TaxCalculator taxCalculator;

    @Test
    public void testStandardCalculateTax() {
        BigDecimal preTaxAmount = new BigDecimal("100.00");
        BigDecimal expectedTax = new BigDecimal("7.23");
        BigDecimal actualTax = taxCalculator.calculateTax(preTaxAmount, "MISC");
        assertEquals(expectedTax, actualTax);
    }

    @Test
    public void testDairyCalculatedTax() {
        BigDecimal preTaxAmount = new BigDecimal("10.00");
        BigDecimal actualTax = taxCalculator.calculateTax(preTaxAmount, "DAIRY");
        assertEquals(new BigDecimal("0.00"), actualTax);
    }

    @Test
    public void testLuxuryCalculatedTax() {
        BigDecimal preTaxAmount = new BigDecimal("10.00");
        BigDecimal actualTax = taxCalculator.calculateTax(preTaxAmount, "LUXURY");
        assertEquals(new BigDecimal("1.23"), actualTax);
    }

    @Test
    public void getTaxRate() {
        assertEquals(BigDecimal.ZERO, taxCalculator.getTaxRate("DAIRY"), "Food isn't taxed");
        assertEquals(BigDecimal.ZERO, taxCalculator.getTaxRate("APPAREL"), "Apparel isn't taxed");
        assertEquals(new BigDecimal("12.34"), taxCalculator.getTaxRate("LUXURY"), "Luxury items are taxed higher");
        assertEquals(new BigDecimal("7.23"), taxCalculator.getTaxRate("TOYS"), "Toys are taxed at default");
    }
}
