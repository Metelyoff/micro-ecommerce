package com.ecommerce.inventory.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@EnableWebSecurity
@Configuration
public class WebSecurityConfiguration {

    @Value("${spring.application.security.cors:true}")
    private boolean cors;

    @Value("${spring.application.security.csrf:true}")
    private boolean csrf;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        if (!cors) {
            log.info("CORS disabled");
            http.cors(AbstractHttpConfigurer::disable);
        }

        if (!csrf) {
            log.info("CSRF disabled");
            http.csrf(AbstractHttpConfigurer::disable);
        }

        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

}
