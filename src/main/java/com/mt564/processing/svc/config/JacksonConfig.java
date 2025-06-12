package com.mt564.processing.svc.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Creates a shared ObjectMapper bean with common settings:
     * - Supports Java 8 Date/Time API
     * - Ignores unknown properties during deserialization
     * - Disables timestamps for date serialization
     *
     * @return configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // for LocalDate, LocalDateTime, etc.
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // ISO format
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // ignore extra fields
        return mapper;
    }
}
