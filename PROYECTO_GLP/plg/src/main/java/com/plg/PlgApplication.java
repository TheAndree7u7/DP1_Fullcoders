package com.plg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Habilitar tareas programadas
public class PlgApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlgApplication.class, args);
    }
}
