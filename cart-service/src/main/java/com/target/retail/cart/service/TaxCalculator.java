package com.target.retail.cart.service;

import com.target.retail.cart.config.TaxRatesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class TaxCalculator {

    MathContext moneyContext = new MathContext(2, RoundingMode.HALF_EVEN);
    private final TaxRatesConfig taxRates;

    @Autowired
    public TaxCalculator(TaxRatesConfig taxRates) {
        this.taxRates = taxRates;
    }

    public BigDecimal calculateTax(BigDecimal preTaxAmount, String category) {
        BigDecimal taxRate = taxRates.getTaxRate(category);
        BigDecimal taxRatePercentage = taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_EVEN);
        BigDecimal taxes = preTaxAmount.multiply(taxRatePercentage);

        // Round to 2 decimal places
        return taxes.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTaxRate(String category) {
        return taxRates.getTaxRate(category);
    }
}

