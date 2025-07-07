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

        // 2. Actualizar parámetros globales antes de cargar pedidos
        actualizarParametrosGlobales(startDate);

        // 3. Limpiar datos anteriores
        DataLoader.pedidos.clear();
        com.plg.factory.PedidoFactory.pedidos.clear();
        
        // 4. Inicializar datos
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

        // 5. Filtrar los pedidos que se atenderán en la semana [startDate, startDate+7)
        LocalDateTime fechaFin = startDate.plusDays(7);
        List<Pedido> pedidosSemanal = DataLoader.pedidos.stream()
                .filter(p -> p.getFechaRegistro() != null)
                .filter(p -> !p.getFechaRegistro().isBefore(startDate) && p.getFechaRegistro().isBefore(fechaFin))
                .collect(Collectors.toList());

        Simulacion.setPedidosSemanal(pedidosSemanal);

        // 6. Logs de inicio
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
     * Actualiza los parámetros globales basándose en la fecha de inicio seleccionada.
     * Esto asegura que los pedidos se carguen con las fechas correctas.
     */
    private static void actualizarParametrosGlobales(LocalDateTime fechaInicio) {
        // Extraer año, mes y día de la fecha de inicio
        Parametros.anho = String.valueOf(fechaInicio.getYear());
        Parametros.mes = String.format("%02d", fechaInicio.getMonthValue());
        Parametros.dia = String.format("%02d", fechaInicio.getDayOfMonth());
        
        // Actualizar fecha_inicial en Parametros
        Parametros.fecha_inicial = fechaInicio;
        
        System.out.println("📅 Parámetros actualizados:");
        System.out.println("   • Año: " + Parametros.anho);
        System.out.println("   • Mes: " + Parametros.mes);
        System.out.println("   • Día: " + Parametros.dia);
    }

    /**
     * Imprime un resumen actualizado de las métricas principales de la simulación.
     */
    public static void imprimirDatosSimulacion() {
        //2025-01-01T04:00- 6pm 


        //LISTA DE PEDIDOS SEMANALES 
        List<Pedido> pedidosSemanal = Simulacion.getPedidosSemanal();
        LocalDateTime fechaActual = Simulacion.getFechaActual();

        System.out.println("Datos de la simulación:");
        System.out.println("Fecha inicial (enviada por frontend): " + fechaActual);
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