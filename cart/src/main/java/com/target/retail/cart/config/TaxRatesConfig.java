package com.target.retail.cart.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "taxes")
public class TaxRatesConfig{
    private BigDecimal defaultRate;
    private Map<String, BigDecimal> ratesByCategory = new HashMap<>();

    public BigDecimal getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(BigDecimal defaultRate) {
        this.defaultRate = defaultRate;
    }

    public Map<String, BigDecimal> getRatesByCategory() {
        return ratesByCategory;
    }

    public void setRatesByCategory(Map<String, BigDecimal> ratesByCategory) {
        this.ratesByCategory = ratesByCategory;
    }


    public BigDecimal getTaxRate(String category) {
        return ratesByCategory.getOrDefault(category, defaultRate);
    }

}
