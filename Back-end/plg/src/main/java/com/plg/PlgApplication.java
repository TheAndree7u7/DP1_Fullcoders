package com.plg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlgApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlgApplication.class, args);
    }

    // REMOVIDO: CommandLineRunner que ejecutaba automáticamente la simulación
    // Ahora la simulación solo se ejecuta cuando el frontend llama a /api/simulacion/iniciar
}
