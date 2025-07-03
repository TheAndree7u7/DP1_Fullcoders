package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.entity.Camion;
import com.plg.entity.Pedido;
import com.plg.utils.Parametros;
import com.plg.utils.Simulacion;

/**
 * Clase utilitaria que centraliza la configuración y los logs de inicio de la simulación.
 */
public class ConfiguracionSimulacion {

    /**
     * Inicializa todos los datos en memoria y calcula la lista de pedidos
     * correspondientes a la semana de la fecha inicial.
     */
    public static void configurarSimulacion(LocalDateTime startDate) {
        // 1. Registrar la fecha actual global en la clase Simulacion
        Simulacion.setFechaActual(startDate);

        // 2. Inicializar datos
        DataLoader.initializeAlmacenes();
        DataLoader.initializeCamiones();
        try {
            DataLoader.initializeMantenimientos();
            DataLoader.initializeAverias();
            DataLoader.initializePedidos();
            DataLoader.initializeBloqueos();
        } catch (java.io.IOException | com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException e) {
            e.printStackTrace();
        }

        // 3. Filtrar los pedidos que se atenderán en la semana [startDate, startDate+7)
        LocalDateTime fechaFin = startDate.plusDays(7);
        List<Pedido> pedidosSemanal = DataLoader.pedidos.stream()
                .filter(p -> p.getFechaRegistro().isAfter(startDate) && p.getFechaRegistro().isBefore(fechaFin))
                .collect(Collectors.toList());

        Simulacion.setPedidosSemanal(pedidosSemanal);

        // 4. Logs de inicio
        System.out.println("\n=== INICIO DE LA SIMULACIÓN ===");
        System.out.println("📊 Estadísticas iniciales:");
        System.out.println("   • Total de pedidos en el sistema: " + DataLoader.pedidos.size());
        System.out.println("   • Pedidos a procesar en esta semana: " + pedidosSemanal.size());
        System.out.println("\n⏰ Configuración temporal:");
        System.out.println("   • Fecha de inicio: " + startDate);
        System.out.println("   • Fecha de finalización: " + startDate.plusDays(7));
        System.out.println("\n=== INICIANDO PROCESO DE SIMULACIÓN ===\n");
    }

    /**
     * Imprime un resumen actualizado de las métricas principales de la simulación.
     */
    public static void imprimirDatosSimulacion() {
        List<Pedido> pedidosSemanal = Simulacion.getPedidosSemanal();
        LocalDateTime fechaActual = Simulacion.getFechaActual();

        System.out.println("Datos de la simulación:");
        System.out.println("Fecha inicial: " + Parametros.fecha_inicial);
        System.out.println("Intervalo de tiempo: " + Parametros.intervaloTiempo + " minutos");
        System.out.println("Cantidad de pedidos semanales: " + pedidosSemanal.size());
        System.out.println("Cantidad de almacenes: " + DataLoader.almacenes.size());
        System.out.println("Cantidad de camiones: " + DataLoader.camiones.size());

        // Solo al inicio del día (00:00) se verifica el mantenimiento programado
        if (fechaActual.getHour() == 0 && fechaActual.getMinute() == 0) {
            System.out.println("🔧 Verificando mantenimientos programados para: " + fechaActual.toLocalDate());
            List<Camion> camiones = DataLoader.camiones != null ? DataLoader.camiones : new ArrayList<>();
            MantenimientoManager.verificarYActualizarMantenimientos(camiones, fechaActual);
        }
    }
} 