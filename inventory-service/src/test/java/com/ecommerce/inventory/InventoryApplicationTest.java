package com.ecommerce.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryApplicationTest extends AbstractIntegrationTest {

    @Autowired
    private Environment environment;

    @Test
    void main_shouldStartApplicationWithoutErrors() {
        assertThat(environment.getActiveProfiles()).contains("test");
    }

}
