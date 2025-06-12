package com.plg;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.plg.utils.Parametros;
import com.plg.utils.Simulacion;

@SpringBootApplication
public class PlgApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PlgApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Vamos a probar simular con los datos de enero 2025
        LocalDateTime fechaActual = Parametros.fecha_inicial;
        Simulacion.configurarSimulacion(fechaActual);
        Thread simulacionThread = new Thread(() -> {
            Simulacion.ejecutarSimulacion();
        });
        simulacionThread.setName("SimulacionThread");
        simulacionThread.setDaemon(true); // Permite que la aplicación se cierre sin esperar a que termine la simulación
        simulacionThread.start();
        System.out.println("Simulación iniciada. Puedes consultar el estado de la simulación en /api/simulacion/mejor");
    }
}
