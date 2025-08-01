package com.ecommerce.inventory.configs;

import com.ecommerce.inventory.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigurationIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${spring.application.cors.allowed-origins}")
    private String allowedOrigins;

    @Test
    void corsConfiguration_shouldApplyAllowedOrigins() throws Exception {
        String origin = allowedOrigins.split(",")[0];
        mockMvc.perform(options("/test")
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", HttpMethod.GET.name()))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origin))
                .andExpect(header().string("Access-Control-Allow-Methods", "HEAD,GET,POST,PUT,PATCH,DELETE"));
    }

}