package com.plg;

import com.plg.config.DataLoader;
import com.plg.entity.Pedido;
import com.plg.entity.Bloqueo;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class DataLoaderTest {

    public static void main(String[] args) {
        System.out.println("🧪 Iniciando prueba de DataLoader...");

        try {
            // Probar carga de pedidos
            System.out.println("\n=== PRUEBA DE CARGA DE PEDIDOS ===");
            List<Pedido> pedidos = DataLoader.initializePedidos();
            System.out.println("✅ Pedidos cargados exitosamente: " + pedidos.size());

            // Mostrar algunos ejemplos de pedidos
            System.out.println("\n📦 Ejemplos de pedidos cargados:");
            pedidos.stream().limit(5).forEach(pedido -> {
                System.out.println("   • " + pedido.getCodigo() + " - Fecha: " + pedido.getFechaRegistro() +
                        " - Volumen: " + pedido.getVolumenGLPAsignado() + " m³");
            });

            // Probar carga de bloqueos
            System.out.println("\n=== PRUEBA DE CARGA DE BLOQUEOS ===");
            DataLoader.initializeBloqueos();
            System.out.println("✅ Bloqueos cargados exitosamente: " + DataLoader.bloqueos.size());

            // Mostrar algunos ejemplos de bloqueos
            System.out.println("\n🚧 Ejemplos de bloqueos cargados:");
            DataLoader.bloqueos.stream().limit(5).forEach(bloqueo -> {
                Duration duracion = Duration.between(bloqueo.getFechaInicio(), bloqueo.getFechaFin());
                System.out.println("   • Bloqueo - Fecha inicio: " + bloqueo.getFechaInicio() +
                        " - Fecha fin: " + bloqueo.getFechaFin() +
                        " - Duración: " + duracion.toHours() + " horas" +
                        " - Nodos bloqueados: " + bloqueo.getNodosBloqueados().size());
            });

            System.out.println("\n🎉 ¡Todas las pruebas completadas exitosamente!");

        } catch (InvalidDataFormatException e) {
            System.err.println("❌ Error de formato en los datos: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("❌ Error de lectura de archivos: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}