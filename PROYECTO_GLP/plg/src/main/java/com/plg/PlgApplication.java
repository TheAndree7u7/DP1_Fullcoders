package com.plg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableScheduling  // Habilitar tareas programadas
public class PlgApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PlgApplication.class, args);
        
        // Registrar un hook para el cierre de la aplicación
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // Esperar a que todas las tareas asíncronas terminen (máximo 30 segundos)
                Thread.sleep(TimeUnit.SECONDS.toMillis(30));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
    
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        // En versiones anteriores de Spring Boot, no está disponible setGracefulShutdownTimeout
        // Configuramos otras propiedades útiles en su lugar
        factory.setContextPath("");
        factory.setPort(8085);
        return factory;
    }
}
