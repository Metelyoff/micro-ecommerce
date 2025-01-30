package com.ecommerce.payment_service.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.application.expiration-rules")
public class ExpirationRulePropertyConfig {
    long delayMillis;
    long checkerMillis;
}
