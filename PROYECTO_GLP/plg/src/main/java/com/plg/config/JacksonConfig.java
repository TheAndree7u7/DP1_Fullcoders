package com.plg.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
// import com.fasterxml.jackson.databind.StreamWriteConstraints;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Registrar módulo para manejar tipos de fecha/hora de Java 8+
        objectMapper.registerModule(new JavaTimeModule());
        
        // Configuración para evitar problemas comunes
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Configurar para ignorar propiedades nulas/vacías
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Aumentar el límite de profundidad de anidamiento permitido
        // // Esto soluciona el error: Document nesting depth exceeds the maximum allowed
        // StreamWriteConstraints streamWriteConstraints = StreamWriteConstraints.builder()
        //         .maxNestingDepth(2000) // Aumentar de 1000 (valor por defecto) a 2000
        //         .build();
        // objectMapper.getFactory().setStreamWriteConstraints(streamWriteConstraints);
        
        return objectMapper;
    }
}