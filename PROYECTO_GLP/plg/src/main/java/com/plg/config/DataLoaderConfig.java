package com.plg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
public class DataLoaderConfig {

    // Ensure database initialization is complete before data loading begins
    @Bean
    @DependsOn("entityManagerFactory")
    public boolean dataLoadingOrderConfiguration(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        // This bean doesn't do much except ensure the dependency order
        return true;
    }
}