package com.target.retail.cart.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableConfigurationProperties(CorsProperties.class)
class CorsPropertiesTest {

    @Autowired
    private CorsProperties corsProperties;

    @Test
    void shouldUseDefaultsWhenNotConfigured() {
        assertThat(corsProperties.getAllowedOriginPatterns())
            .containsExactly("http://localhost:*");
        assertThat(corsProperties.getAllowedMethods())
            .containsExactly("*");
        assertThat(corsProperties.getAllowedHeaders())
            .containsExactly("*");
        assertThat(corsProperties.isAllowCredentials()).isFalse();
    }
}
