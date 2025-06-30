package com.plg;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plg.utils.Parametros;
import com.plg.utils.Simulacion;

@SpringBootApplication
@EnableAsync
public class PlgApplication implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(PlgApplication.class);

    public static void main(String[] args) {
        logger.info("🚀 Iniciando aplicación PLG...");
        SpringApplication.run(PlgApplication.class, args);
        logger.info("✅ Servidor iniciado correctamente en puerto 8085");
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("📊 Configurando simulación...");
        // Configurar la simulación de forma síncrona
        LocalDateTime fechaActual = Parametros.fecha_inicial;
        Simulacion.configurarSimulacion(fechaActual);
        
        // Ejecutar la simulación de forma asíncrona
        ejecutarSimulacionAsync();
        
        logger.info("🔄 Iniciando simulación en segundo plano...");
        logger.info("📋 Puedes consultar el estado en: http://localhost:8085/api/simulacion/status");
        logger.info("📋 Puedes obtener el mejor individuo en: http://localhost:8085/api/simulacion/mejor");
    }
    
    @Async
    public void ejecutarSimulacionAsync() {
        try {
            logger.info("🔄 Ejecutando simulación en segundo plano...");
            Simulacion.ejecutarSimulacion();
            logger.info("✅ Simulación completada");
        } catch (Exception e) {
            logger.error("❌ Error en la simulación: {}", e.getMessage(), e);
        }
    }
}
