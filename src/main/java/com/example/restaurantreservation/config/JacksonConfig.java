package com.example.restaurantreservation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();

        // Configure LocalTime serializer globally
        module.addSerializer(LocalTime.class, new LocalTimeSerializer(
                DateTimeFormatter.ofPattern("HH:mm")
        ));

        // Configure LocalTime deserializer globally (accepts HH:mm)
        module.addDeserializer(LocalTime.class, new LocalTimeDeserializer(
                DateTimeFormatter.ofPattern("HH:mm")
        ));

        mapper.registerModule(module);
        return mapper;
    }
}