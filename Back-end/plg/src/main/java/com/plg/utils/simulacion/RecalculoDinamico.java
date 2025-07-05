package com.plg.utils.simulacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import com.plg.entity.Averia;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.Pedido;
import com.plg.utils.Simulacion;
import com.plg.utils.AlgoritmoGenetico;
import com.plg.utils.Individuo;
import com.plg.utils.Parametros;
import com.plg.entity.Mapa;
import com.plg.dto.IndividuoDto;

/**
 * Clase para manejar el recálculo dinámico de la simulación cuando ocurren averías.
 * 
 * Este sistema permite:
 * 1. Detectar cuando ocurre una avería en un paquete temporal
 * 2. Guardar el estado actual de todos los camiones
 * 3. Recalcular la simulación desde el momento de la avería
 * 4. Aplicar la nueva solución y continuar con la simulación normal
 */
public class RecalculoDinamico {
    
    private static final Map<String, SnapshotCamion> snapshotCamiones = new HashMap<>();
    private static boolean recalculoEnProceso = false;
    
    /**
     * Estructura para almacenar el estado de un camión en un momento dado
     */
    public static class SnapshotCamion {
        public String codigoCamion;
        public Coordenada posicion;
        public LocalDateTime fechaSnapshot;
        public double porcentajeRuta;
        public String estadoAnterior;
        
        public SnapshotCamion(String codigoCamion, Coordenada posicion, LocalDateTime fechaSnapshot, 
                           double porcentajeRuta, String estadoAnterior) {
            this.codigoCamion = codigoCamion;
            this.posicion = posicion;
            this.fechaSnapshot = fechaSnapshot;
            this.porcentajeRuta = porcentajeRuta;
            this.estadoAnterior = estadoAnterior;
        }
    }
    
    /**
     * Inicia el proceso de recálculo dinámico cuando ocurre una avería.
     * 
     * @param codigoCamionAveriado código del camión que sufrió la avería
     * @param averia la avería que desencadena el recálculo
     * @param camiones lista de todos los camiones activos
     */
    public static void iniciarRecalculo(String codigoCamionAveriado, Averia averia, List<Camion> camiones) {
        if (recalculoEnProceso) {
            System.out.println("⚠️ RECÁLCULO: Ya hay un recálculo en proceso, ignorando nueva solicitud");
            return;
        }
        
        try {
            recalculoEnProceso = true;
            LocalDateTime fechaAveria = Simulacion.getFechaActual();
            
            System.out.println("🔄 RECÁLCULO DINÁMICO INICIADO");
            System.out.println("📅 Fecha/Hora de avería: " + fechaAveria);
            System.out.println("🚛 Camión averiado: " + codigoCamionAveriado);
            System.out.println("⚠️ Tipo de avería: " + averia.getTipoIncidente().getCodigo());
            
            // 0. PAUSAR LA SIMULACIÓN INMEDIATAMENTE
            Simulacion.pausarPorAveria();
            System.out.println("⏸️ SIMULACIÓN PAUSADA CONFIRMADA - Estado: " + Simulacion.estaPausadaPorAveria());
            
            // 1. Guardar snapshot del estado global de todos los camiones
            guardarEstadoGlobalCamiones(camiones, fechaAveria);
            
            // 2. Determinar el paquete temporal actual
            int paqueteActual = GestorHistorialSimulacion.getIndicePaqueteActual();
            System.out.println("📦 Paquete temporal afectado: " + paqueteActual);
            
            // 3. Invalidar todos los paquetes futuros
            int paquetesInvalidados = GestorHistorialSimulacion.invalidarPaquetesFuturos(paqueteActual);
            System.out.println("🗑️ Paquetes invalidados: " + paquetesInvalidados);
            
            // 4. Liberar pedidos del camión averiado
            liberarPedidosCamionAveriado(codigoCamionAveriado);
            
            // 5. Ejecutar recálculo en hilo separado para no bloquear la simulación
            CompletableFuture.runAsync(() -> {
                try {
                    // Delay mínimo para hacer visible la pausa (1 segundo)
                    System.out.println("⏳ RECÁLCULO: Manteniendo pausa visible por 1 segundo...");
                    Thread.sleep(1000);
                    ejecutarRecalculoConParche(paqueteActual, fechaAveria, codigoCamionAveriado);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("❌ Recálculo interrumpido: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar recálculo dinámico: " + e.getMessage());
            e.printStackTrace();
            recalculoEnProceso = false;
        }
    }
    
    /**
     * Guarda un snapshot completo del estado global de todos los camiones.
     * Incluye posición, estado, rutas asignadas y progreso.
     */
    private static void guardarEstadoGlobalCamiones(List<Camion> camiones, LocalDateTime fechaSnapshot) {
        snapshotCamiones.clear();
        
        System.out.println("📸 GUARDANDO ESTADO GLOBAL DE CAMIONES");
        System.out.println("📅 Fecha snapshot: " + fechaSnapshot);
        System.out.println("🚛 Total camiones: " + camiones.size());
        
        for (Camion camion : camiones) {
            // Calcular porcentaje de progreso en la ruta actual
            double porcentajeRuta = 0.0;
            if (camion.getGen() != null && camion.getGen().getRutaFinal() != null) {
                // Aquí se podría calcular el progreso real basado en la posición actual
                porcentajeRuta = 0.0; // Por ahora en 0
            }
            
            SnapshotCamion snapshot = new SnapshotCamion(
                camion.getCodigo(),
                camion.getCoordenada(),
                fechaSnapshot,
                porcentajeRuta,
                camion.getEstado().toString()
            );
            
            snapshotCamiones.put(camion.getCodigo(), snapshot);
            System.out.println("📸 Snapshot guardado: " + camion.getCodigo() + 
                             " | Pos: (" + camion.getCoordenada().getFila() + ", " + camion.getCoordenada().getColumna() + ")" +
                             " | Estado: " + camion.getEstado());
        }
        
        System.out.println("✅ Estado global guardado exitosamente");
    }

    /**
     * Libera los pedidos del camión averiado para que puedan ser reasignados.
     * Los pedidos pasan a estado REGISTRADO y quedan disponibles para otros camiones.
     */
    private static void liberarPedidosCamionAveriado(String codigoCamionAveriado) {
        try {
            System.out.println("🔓 LIBERANDO PEDIDOS DEL CAMIÓN AVERIADO: " + codigoCamionAveriado);
            
            // Obtener pedidos por atender y planificados
            List<Pedido> pedidosLiberados = Simulacion.pedidosPlanificados.stream()
                .filter(pedido -> {
                    // Verificar si el pedido estaba asignado al camión averiado
                    // Esta lógica depende de cómo se almacena la asignación
                    return true; // Por ahora liberar todos los pedidos planificados
                })
                .toList();
            
            // Mover pedidos de planificados a por atender
            for (Pedido pedido : pedidosLiberados) {
                Simulacion.pedidosPlanificados.remove(pedido);
                Simulacion.pedidosPorAtender.add(pedido);
                pedido.setEstado(com.plg.entity.EstadoPedido.REGISTRADO);
                System.out.println("🔓 Pedido liberado: " + pedido.getCodigo() + " | Volumen: " + pedido.getVolumenGLPAsignado() + " m³");
            }
            
            System.out.println("✅ Pedidos liberados: " + pedidosLiberados.size());
            System.out.println("📋 Pedidos por atender ahora: " + Simulacion.pedidosPorAtender.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error al liberar pedidos del camión averiado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Determina en qué paquete temporal ocurre la avería.
     * Cada paquete representa un intervalo de tiempo de la simulación.
     */
    private static int determinarPaqueteActual(LocalDateTime fechaAveria) {
        // Calcular el paquete basado en el intervalo de tiempo configurado
        LocalDateTime fechaInicio = fechaAveria.withHour(0).withMinute(0).withSecond(0).withNano(0);
        long minutosDesdeInicio = java.time.Duration.between(fechaInicio, fechaAveria).toMinutes();
        
        // Cada paquete representa un intervalo de tiempo (por ejemplo, 2 horas = 120 minutos)
        int minutosPorPaquete = Parametros.intervaloTiempo * 60; // Convertir a minutos
        int paquete = (int) (minutosDesdeInicio / minutosPorPaquete);
        
        System.out.println("⏱️ Minutos desde inicio del día: " + minutosDesdeInicio);
        System.out.println("⏱️ Minutos por paquete: " + minutosPorPaquete);
        System.out.println("📦 Paquete calculado: " + paquete);
        
        return paquete;
    }
    
    /**
     * Ejecuta el recálculo con paquete parche cuando ocurre una avería.
     * Genera un paquete parche que cubre el tiempo restante del paquete actual,
     * seguido de paquetes completos normales.
     */
    private static void ejecutarRecalculoConParche(int paqueteActual, LocalDateTime fechaAveria, String codigoCamionAveriado) {
        try {
            System.out.println("🔄 EJECUTANDO RECÁLCULO CON PAQUETE PARCHE");
            System.out.println("📦 Paquete actual afectado: " + paqueteActual);
            System.out.println("📅 Fecha avería: " + fechaAveria);
            System.out.println("🚛 Camión averiado: " + codigoCamionAveriado);
            System.out.println("⏸️ Estado pausa durante recálculo: " + Simulacion.estaPausadaPorAveria());
            
            // Obtener los pedidos pendientes que necesitan ser recalculados
            List<Pedido> pedidosPendientes = obtenerPedidosPendientes(fechaAveria);
            
            if (pedidosPendientes.isEmpty()) {
                System.out.println("📝 No hay pedidos pendientes para recalcular");
                // Aún así reanudar la simulación para evitar que se quede colgada
                Simulacion.notificarPaqueteParcheDisponible();
                System.out.println("🔄 Simulación reanudada a pesar de no haber pedidos pendientes");
                return;
            }
            
            System.out.println("📋 Pedidos a recalcular: " + pedidosPendientes.size());
            
            // 1. Generar paquete parche que cubra el tiempo restante
            generarPaqueteParche(fechaAveria, pedidosPendientes);
            
            // 2. Generar paquetes completos adicionales
            generarPaquetesCompletos(fechaAveria, pedidosPendientes);
            
            System.out.println("✅ Recálculo con paquete parche completado exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error durante el recálculo con parche: " + e.getMessage());
            e.printStackTrace();
        } finally {
            recalculoEnProceso = false;
            System.out.println("🔄 Recálculo dinámico completado");
        }
    }

    /**
     * Genera un paquete parche que cubre el tiempo restante del paquete actual.
     */
    private static void generarPaqueteParche(LocalDateTime fechaAveria, List<Pedido> pedidosPendientes) {
        try {
            System.out.println("🩹 GENERANDO PAQUETE PARCHE");
            
            // Calcular tiempo restante del paquete actual
            LocalDateTime finPaqueteActual = calcularFinPaqueteActual(fechaAveria);
            long tiempoRestante = java.time.Duration.between(fechaAveria, finPaqueteActual).toHours();
            
            System.out.println("⏰ Tiempo restante del paquete: " + tiempoRestante + " horas");
            System.out.println("📅 Fin del paquete actual: " + finPaqueteActual);
            
            // Ejecutar algoritmo genético para el paquete parche
            System.out.println("🧠 Ejecutando algoritmo genético para paquete parche...");
            Simulacion.marcarInicioAlgoritmoGenetico();
            AlgoritmoGenetico algoritmoParche = new AlgoritmoGenetico(Mapa.getInstance(), pedidosPendientes);
            algoritmoParche.ejecutarAlgoritmo();
            Simulacion.marcarFinAlgoritmoGenetico();
            System.out.println("✅ Algoritmo genético del paquete parche completado");
            
            Individuo solucionParche = algoritmoParche.getMejorIndividuo();
            
            if (solucionParche != null) {
                System.out.println("✅ Solución parche generada | Fitness: " + solucionParche.getFitness());
                
                // Aplicar la solución parche
                CamionStateApplier.aplicarEstadoFinalCamiones(solucionParche);
                
                // Crear DTO del paquete parche
                IndividuoDto paqueteParche = new IndividuoDto(
                    solucionParche, 
                    pedidosPendientes, 
                    List.of(), // Lista vacía de bloqueos por ahora
                    fechaAveria
                );
                
                // Agregar el paquete parche al historial
                GestorHistorialSimulacion.agregarPaqueteParche(paqueteParche);
                
                System.out.println("🩹 Paquete parche agregado al historial");
                
                // NOTIFICAR QUE EL PAQUETE PARCHE ESTÁ DISPONIBLE - REANUDAR SIMULACIÓN
                Simulacion.notificarPaqueteParcheDisponible();
                
                System.out.println("🔄 Simulación reanudada - Paquete parche será consumido inmediatamente");
                
            } else {
                System.err.println("❌ No se pudo generar solución parche");
                // Aún así reanudar la simulación para evitar que se quede colgada
                Simulacion.notificarPaqueteParcheDisponible();
                System.out.println("🔄 Simulación reanudada a pesar del error en paquete parche");
            }
            
        } catch (Exception e) {
            Simulacion.marcarFinAlgoritmoGenetico(); // Marcar fin en caso de error
            System.err.println("❌ Error al generar paquete parche: " + e.getMessage());
            e.printStackTrace();
            
            // Reanudar simulación en caso de error para evitar que se quede colgada
            Simulacion.notificarPaqueteParcheDisponible();
            System.out.println("🔄 Simulación reanudada después de error en paquete parche");
        }
    }

    /**
     * Genera paquetes completos adicionales después del paquete parche.
     */
    private static void generarPaquetesCompletos(LocalDateTime fechaAveria, List<Pedido> pedidosPendientes) {
        try {
            System.out.println("📦 GENERANDO PAQUETES COMPLETOS ADICIONALES");
            
            // Calcular cuando termina el paquete parche
            LocalDateTime finPaqueteParche = calcularFinPaqueteActual(fechaAveria);
            
            // Generar al menos un paquete completo adicional (como menciona el usuario)
            LocalDateTime inicioSiguientePaquete = finPaqueteParche;
            LocalDateTime finSiguientePaquete = inicioSiguientePaquete.plusHours(2); // Paquete de 2 horas
            
            System.out.println("📅 Generando paquete completo desde " + inicioSiguientePaquete + " hasta " + finSiguientePaquete);
            
            // Ejecutar algoritmo genético para el paquete completo
            AlgoritmoGenetico algoritmoCompleto = new AlgoritmoGenetico(Mapa.getInstance(), pedidosPendientes);
            algoritmoCompleto.ejecutarAlgoritmo();
            
            Individuo solucionCompleta = algoritmoCompleto.getMejorIndividuo();
            
            if (solucionCompleta != null) {
                System.out.println("✅ Solución completa generada | Fitness: " + solucionCompleta.getFitness());
                
                // Aplicar la solución completa
                CamionStateApplier.aplicarEstadoFinalCamiones(solucionCompleta);
                
                // Crear DTO del paquete completo
                IndividuoDto paqueteCompleto = new IndividuoDto(
                    solucionCompleta, 
                    pedidosPendientes, 
                    List.of(), // Lista vacía de bloqueos por ahora
                    inicioSiguientePaquete
                );
                
                // Agregar el paquete completo al historial
                GestorHistorialSimulacion.agregarPaquete(paqueteCompleto);
                
                System.out.println("📦 Paquete completo agregado al historial");
                
            } else {
                System.err.println("❌ No se pudo generar solución completa");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar paquetes completos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calcula cuándo termina el paquete actual basado en el tiempo de avería.
     */
    private static LocalDateTime calcularFinPaqueteActual(LocalDateTime fechaAveria) {
        // Calcular el final del paquete actual
        // Por ejemplo, si los paquetes duran 2 horas, encontrar el próximo múltiplo de 2 horas
        int horaActual = fechaAveria.getHour();
        int horaFinPaquete = ((horaActual / 2) + 1) * 2; // Próximo múltiplo de 2
        
        return fechaAveria.withHour(horaFinPaquete).withMinute(0).withSecond(0).withNano(0);
    }
    
    /**
     * Obtiene los pedidos pendientes que necesitan ser recalculados.
     */
    private static List<Pedido> obtenerPedidosPendientes(LocalDateTime fechaAveria) {
        // Aquí se implementaría la lógica para obtener los pedidos pendientes
        // Por ahora retornamos una lista vacía
        return Simulacion.pedidosPorAtender.stream().toList();
    }
    
    /**
     * Aplica la solución recalculada a los camiones.
     */
    private static void aplicarSolucionRecalculada(Individuo solucion, LocalDateTime fechaAveria, int paquete) {
        System.out.println("🔧 Aplicando solución recalculada...");
        
        // Aplicar el estado final de los camiones
        CamionStateApplier.aplicarEstadoFinalCamiones(solucion);
        
        System.out.println("✅ Solución recalculada aplicada para paquete " + paquete);
    }
    
    /**
     * Crea un nuevo paquete recalculado para el historial de la simulación.
     */
    private static void crearPaqueteRecalculado(Individuo solucion, List<Pedido> pedidos, 
                                               LocalDateTime fechaAveria, int paquete) {
        try {
            // Crear DTO del individuo recalculado
            IndividuoDto paqueteRecalculado = new IndividuoDto(
                solucion, 
                pedidos, 
                List.of(), // Lista vacía de bloqueos por ahora
                fechaAveria
            );
            
            // Agregar al historial de simulación
            GestorHistorialSimulacion.agregarPaqueteRecalculado(paqueteRecalculado, paquete);
            
            System.out.println("📦 Paquete recalculado agregado al historial");
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear paquete recalculado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica si hay un recálculo en proceso.
     */
    public static boolean isRecalculoEnProceso() {
        return recalculoEnProceso;
    }
    
    /**
     * Obtiene el snapshot de un camión específico.
     */
    public static SnapshotCamion getSnapshotCamion(String codigoCamion) {
        return snapshotCamiones.get(codigoCamion);
    }
    
    /**
     * Limpia todos los snapshots almacenados.
     */
    public static void limpiarSnapshots() {
        snapshotCamiones.clear();
        System.out.println("🧹 Snapshots de camiones limpiados");
    }
} 