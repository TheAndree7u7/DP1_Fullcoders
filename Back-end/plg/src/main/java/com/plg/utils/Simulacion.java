package com.plg.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.LinkedHashSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.plg.config.DataLoader;
import com.plg.dto.IndividuoDto;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Camion;
import com.plg.entity.Mapa;
import com.plg.entity.Pedido;
import com.plg.entity.TipoAlmacen;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

public class Simulacion {

    private static List<Pedido> pedidosSemanal;
    private static Mapa mapa = Mapa.getInstance();
    private static LocalDateTime fechaActual;
    public static Set<Pedido> pedidosPorAtender = new LinkedHashSet<>();
    public static Set<Pedido> pedidosPlanificados = new LinkedHashSet<>();
    public static Set<Pedido> pedidosEntregados = new LinkedHashSet<>();
    public static Individuo mejorIndividuo = null;
    
    // Contador para control de frecuencia del algoritmo genético
    private static int contadorEjecuciones = 0;
    private static final int INTERVALO_ALGORITMO_GENETICO = 4; // Ejecutar cada 4 intervalos (2 horas)
    

    // Colas para simulación - Usar cola con capacidad limitada
    public static BlockingQueue<Object> gaTriggerQueue = new java.util.concurrent.LinkedBlockingQueue<>(1);
    public static BlockingQueue<IndividuoDto> gaResultQueue = new java.util.concurrent.LinkedBlockingQueue<>(1);
    public static Semaphore iniciar = new Semaphore(0);
    public static Semaphore continuar = new Semaphore(0);

    public static void configurarSimulacion(LocalDateTime startDate) {
        fechaActual = startDate;
        DataLoader.initializeAlmacenes();
        DataLoader.initializeCamiones();
        try {
            DataLoader.initializeMantenimientos();
            DataLoader.initializeAverias();
            DataLoader.initializePedidos();
            DataLoader.initializeBloqueos();
        } catch (java.io.IOException | ExcepcionesPerzonalizadas.InvalidDataFormatException e) {
            e.printStackTrace();
        }
        LocalDateTime fechaFin = fechaActual.plusDays(7);
        pedidosSemanal = DataLoader.pedidos.stream()
                .filter(pedido -> pedido.getFechaRegistro().isAfter(fechaActual)
                        && pedido.getFechaRegistro().isBefore(fechaFin))
                .collect(Collectors.toList());
        System.out.println("\n=== INICIO DE LA SIMULACIÓN ===");
        System.out.println("📊 Estadísticas iniciales:");
        System.out.println("   • Total de pedidos en el sistema: " + DataLoader.pedidos.size());
        System.out.println("   • Pedidos a procesar en esta semana: " + pedidosSemanal.size());
        System.out.println("\n⏰ Configuración temporal:");
        System.out.println("   • Fecha de inicio: " + fechaActual);
        System.out.println("   • Fecha de finalización: " + fechaActual.plusDays(3));
        System.out.println("\n=== INICIANDO PROCESO DE SIMULACIÓN ===\n");
    }

    public static void ejecutarSimulacion() {


        imprimirDatosSimulacion();
        LocalDateTime fechaLimite = Parametros.fecha_inicial.plusDays(7);
        while (!pedidosSemanal.isEmpty()) {
            Pedido pedido = pedidosSemanal.get(0);
            // Voy agregando pedidos a la lista de pedidos
            if (pedidoConFechaMenorAFechaActual(pedido, fechaActual)) {
                pedidosSemanal.remove(0);
                pedidosPorAtender.add(pedido);
            } else {
                List<Bloqueo> bloqueosActivos = actualizarBloqueos(fechaActual);
                actualizarEstadoGlobal(fechaActual);
                if (!pedidosPorAtender.isEmpty()) {
                    System.out.println("------------------------");
                    System.out.println("⏰ Tiempo actual: " + fechaActual);
                    System.out.println("📦 Pedidos por atender: " + pedidosPorAtender.size());
                    System.out.println("📋 Pedidos planificados: " + pedidosPlanificados.size());
                    
                    contadorEjecuciones++;
                    
                    // Solo ejecutar algoritmo genético cada INTERVALO_ALGORITMO_GENETICO veces
                    if (contadorEjecuciones % INTERVALO_ALGORITMO_GENETICO == 0) {
                        System.out.println("🧬 Ejecutando algoritmo genético (iteración " + contadorEjecuciones + ")");
                        
                        List<Pedido> pedidosEnviar = unirPedidosSinRepetidos(pedidosPlanificados, pedidosPorAtender);
                        System.out.println("🚚 Total pedidos a procesar: " + pedidosEnviar.size());
                        
                        try {
                            // Esperar con timeout más corto para evitar bloqueos
                            boolean adquirido = iniciar.tryAcquire(2, java.util.concurrent.TimeUnit.SECONDS);
                            
                            if (!adquirido) {
                                System.out.println("⏳ Timeout esperando señal de inicio, continuando automáticamente...");
                            }
                            
                            AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidosEnviar);
                            algoritmoGenetico.ejecutarAlgoritmo();            
                            IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo(), pedidosEnviar, bloqueosActivos);
                            
                            // Intentar ofrecer datos con timeout
                            boolean ofrecido = gaResultQueue.offer(mejorIndividuoDto, 1, java.util.concurrent.TimeUnit.SECONDS);
                            if (!ofrecido) {
                                System.out.println("⚠️  Cola llena, datos no enviados");
                            }
                            
                            // Esperar señal de continuar con timeout
                            boolean continuarAdquirido = continuar.tryAcquire(3, java.util.concurrent.TimeUnit.SECONDS);
                            if (!continuarAdquirido) {
                                System.out.println("⏳ Timeout esperando señal de continuar, avanzando automáticamente...");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.err.println("❌ Error al esperar el disparador del algoritmo genético: " + e.getMessage());
                            e.printStackTrace();
                            
                            // Crear una solución de emergencia si hay error con los semáforos
                            try {
                                AlgoritmoGenetico algoritmoGenetico = new AlgoritmoGenetico(mapa, pedidosEnviar);
                                algoritmoGenetico.ejecutarAlgoritmo();
                                IndividuoDto mejorIndividuoDto = new IndividuoDto(algoritmoGenetico.getMejorIndividuo(), pedidosEnviar, bloqueosActivos);
                                gaResultQueue.offer(mejorIndividuoDto);
                            } catch (Exception ex) {
                                System.err.println("❌ Error crítico en algoritmo genético: " + ex.getMessage());
                                ex.printStackTrace();
                            }
                        } catch (Exception e) {
                            System.err.println("❌ Error inesperado en simulación: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("⏭️ Saltando ejecución de algoritmo genético (" + contadorEjecuciones + "/" + INTERVALO_ALGORITMO_GENETICO + ")");
                        System.out.println("🚚 Usando rutas existentes para movimiento de camiones");
                    }
                } else {
                    System.out.println("⏰ Tiempo actual: " + fechaActual + " - Sin pedidos por procesar");
                }
                fechaActual = fechaActual.plusMinutes(Parametros.intervaloTiempo);
                if (fechaActual.isEqual(fechaLimite) || fechaActual.isAfter(fechaLimite)) {
                    break;
                }
            }
        }
        System.out.println("=== REPORTE FINAL DE LA SIMULACIÓN ===");
        System.out.println("⏱️  Duración total: " + Parametros.fecha_inicial + " hasta " + fechaActual);
        System.out.println("📊 Estadísticas finales:");
        System.out.println("   • Kilómetros recorridos: " + Parametros.kilometrosRecorridos);
        System.out.println("   • Fitness global: " + Parametros.fitnessGlobal);
        System.out.println("   • Pedidos entregados: " + pedidosEntregados.size());
        System.out.println("   • Pedidos planificados: " + pedidosPlanificados.size());
        System.out.println("   • Pedidos pendientes: " + pedidosPorAtender.size());
        System.out.println("   • Total pedidos procesados: " + (pedidosEntregados.size() + pedidosPlanificados.size()));
        System.out.println("✅ Simulación completada exitosamente");
        System.out.println("================================================");
    }


    public static List<Pedido> unirPedidosSinRepetidos(Set<Pedido> set1, Set<Pedido> set2) {
        List<Pedido> listaUnida = new ArrayList<>(set1);
        for (Pedido pedido : set2) {
            if (!listaUnida.contains(pedido)) {
                listaUnida.add(pedido);
            }
        }
        return listaUnida;
    }

    public static void actualizarEstadoGlobal(LocalDateTime fechaActual) {
        actualizarRepositorios(fechaActual);
        actualizarCamiones(fechaActual);
    }

    private static List<Bloqueo> actualizarBloqueos(LocalDateTime fechaActual) {
        List<Bloqueo> bloqueos = DataLoader.bloqueos;
        List<Bloqueo> bloqueosActivos = new ArrayList<>();
        for (Bloqueo bloqueo : bloqueos) {
            if (bloqueo.getFechaInicio().isBefore(fechaActual) && bloqueo.getFechaFin().isAfter(fechaActual)) {
                bloqueo.activarBloqueo();
                bloqueosActivos.add(bloqueo);
            } else {
                bloqueo.desactivarBloqueo();
            }
        }
        return bloqueosActivos;
    }

    private static void actualizarRepositorios(LocalDateTime fechaActual) {
        List<Almacen> almacenes = DataLoader.almacenes;
        if (fechaActual.getHour() == 0 && fechaActual.getMinute() == 0) {
            for (Almacen almacen : almacenes) {
                if (almacen.getTipo() == TipoAlmacen.SECUNDARIO) {
                    almacen.setCapacidadActualGLP(almacen.getCapacidadMaximaGLP());
                    almacen.setCapacidadCombustible(almacen.getCapacidadMaximaCombustible());
                }
            }
        }
    }

    private static void actualizarCamiones(LocalDateTime fechaActual) {
        List<Camion> camiones = DataLoader.camiones;
        for (Camion camion : camiones) {
            camion.actualizarEstado(Parametros.intervaloTiempo, pedidosPorAtender, pedidosPlanificados,
                    pedidosEntregados, fechaActual);
        }
    }

    private static void imprimirDatosSimulacion() {
        System.out.println("Datos de la simulación:");
        System.out.println("Fecha inicial: " + Parametros.fecha_inicial);
        System.out.println("Intervalo de tiempo: " + Parametros.intervaloTiempo + " minutos");
        System.out.println("Cantidad de pedidos semanales: " + pedidosSemanal.size());
        System.out.println("Cantidad de almacenes: " + DataLoader.almacenes.size());
        System.out.println("Cantidad de camiones: " + DataLoader.camiones.size());

    }

    private static boolean pedidoConFechaMenorAFechaActual(Pedido pedido, LocalDateTime fechaActual) {
        return pedido.getFechaRegistro().isBefore(fechaActual) || pedido.getFechaRegistro().isEqual(fechaActual);
    }

}
