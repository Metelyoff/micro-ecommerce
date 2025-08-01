package com.ecommerce.inventory.configs;

import com.ecommerce.inventory.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import(PersistenceConfiguration.class)
class PersistenceConfigurationIT extends AbstractIntegrationTest {

    @Autowired
    private AuditorAware<String> auditorProvider;

    @Test
    void auditorProvider_shouldReturnSystemUser() {
        Optional<String> auditor = auditorProvider.getCurrentAuditor();
        assertThat(auditor).isPresent().contains("SYSTEM");
    }

}