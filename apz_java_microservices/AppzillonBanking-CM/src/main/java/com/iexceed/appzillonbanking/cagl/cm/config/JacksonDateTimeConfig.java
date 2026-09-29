package com.iexceed.appzillonbanking.cagl.cm.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Configuration
public class JacksonDateTimeConfig {

    private static final DateTimeFormatter CUSTOM_LDT_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsr310Customizer() {
        return builder -> builder
                .modules(new JavaTimeModule())
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(CUSTOM_LDT_FORMAT))
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
