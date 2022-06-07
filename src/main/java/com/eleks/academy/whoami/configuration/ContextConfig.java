package com.eleks.academy.whoami.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.IdGenerator;
import org.springframework.util.SimpleIdGenerator;

@Configuration
public class ContextConfig {

    @Bean
    IdGenerator idGenerator() {
        return new SimpleIdGenerator();
    }
}
