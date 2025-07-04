package com.plg.config;

import com.plg.entity.*;
import com.plg.factory.AlmacenFactory;
import com.plg.factory.CamionFactory;
import com.plg.factory.PedidoFactory;
import com.plg.utils.Herramientas;
import com.plg.utils.Parametros;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidDataFormatException;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class DataLoader {

    private static String pathAverias = "data/averias/averias.v1.txt";
    private static String pathMantenimientos = "data/mantenimientos/mantpreventivo.txt";
    
    // Métodos para generar paths dinámicamente basándose en parámetros actuales
    private static String getPathPedidos() {
        return "data/pedidos/ventas" + Parametros.anho + Parametros.mes + ".txt";
    }
    
    private static String getPathBloqueos() {
        return "data/bloqueos/" + Parametros.anho + Parametros.mes + ".bloqueos.txt";
    }

    private static Coordenada coordenadaCentral = new Coordenada(8, 12);

    public static final List<Mantenimiento> mantenimientos = new ArrayList<>();
    public static List<Pedido> pedidos = new ArrayList<>();
    public static List<Almacen> almacenes = new ArrayList<>();
    public static List<Camion> camiones = new ArrayList<>();
    public static List<Averia> averias = new ArrayList<>();
    public static List<Bloqueo> bloqueos = new ArrayList<>();

    public static List<Almacen> initializeAlmacenes() {
        AlmacenFactory.crearAlmacen(TipoAlmacen.CENTRAL, coordenadaCentral, 1_000_000_000,
                1_000_000_000);
        AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(42, 42), 160.0, 50);
        AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(3, 63), 160.0, 50);
        almacenes = AlmacenFactory.almacenes;
        return almacenes;
    }

    public static List<Camion> initializeCamiones() {
        for (int i = 0; i < 2; i++) {
            CamionFactory.crearCamionesPorTipo(TipoCamion.TB, true, coordenadaCentral);
        }
        for (int i = 0; i < 4; i++) {
            CamionFactory.crearCamionesPorTipo(TipoCamion.TB, true, coordenadaCentral);
        }
        for (int i = 0; i < 4; i++) {
            CamionFactory.crearCamionesPorTipo(TipoCamion.TC, true, coordenadaCentral);
        }
        for (int i = 0; i < 10; i++) {
            CamionFactory.crearCamionesPorTipo(TipoCamion.TD, true, coordenadaCentral);
        }
        camiones = CamionFactory.camiones;
        return camiones;
    }

    public static List<Averia> initializeAverias() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(pathAverias);
        for (String line : lines) {
            Averia averia = new Averia(line);
 
        }
        return averias;
    }

    public static List<Pedido> initializePedidos() throws InvalidDataFormatException, IOException {
        // Limpiar la lista de pedidos antes de cargar nuevos
        PedidoFactory.pedidos.clear();
        
        List<String> lines = Herramientas.readAllLines(getPathPedidos());
        List<Pedido> pedidosOriginales = new ArrayList<>();
        
        // Primero crear todos los pedidos originales
        for (String line : lines) {
            Pedido pedido = PedidoFactory.crearPedido(line);
            pedido.setFechaLimite(pedido.getFechaRegistro().plusHours((long) pedido.getHorasLimite()));
            pedidosOriginales.add(pedido);
        }
        
        // Ahora procesar cada pedido para dividirlo si es necesario
        List<Pedido> pedidosFinales = new ArrayList<>();
        int pedidosDivididos = 0;
        int totalPedidosOriginales = pedidosOriginales.size();
        
        for (Pedido pedidoOriginal : pedidosOriginales) {
            if (necesitaDivision(pedidoOriginal.getVolumenGLPAsignado())) {
                // Dividir el pedido
                List<Pedido> pedidosSubdivididos = dividirPedido(pedidoOriginal);
                pedidosFinales.addAll(pedidosSubdivididos);
                pedidosDivididos++;
                System.out.println("Pedido " + pedidoOriginal.getCodigo() + " dividido en " + 
                                 pedidosSubdivididos.size() + " partes (volumen original: " + 
                                 pedidoOriginal.getVolumenGLPAsignado() + " m³)");
            } else {
                pedidosFinales.add(pedidoOriginal);
            }
        }
        
        // Actualizar las listas
        PedidoFactory.pedidos.clear();
        PedidoFactory.pedidos.addAll(pedidosFinales);
        pedidos = PedidoFactory.pedidos;
        
        // Mostrar estadísticas
        System.out.println("\n=== ESTADÍSTICAS DE CARGA DE PEDIDOS ===");
        System.out.println("📊 Pedidos originales leídos: " + totalPedidosOriginales);
        System.out.println("📊 Pedidos que requirieron división: " + pedidosDivididos);
        System.out.println("📊 Total de pedidos finales: " + pedidos.size());
        
        mostrarEstadisticasCapacidad();
        
        return pedidos;
    }

    /**
     * Verifica si un pedido necesita ser dividido en función de las capacidades de los camiones.
     */
    private static boolean necesitaDivision(double volumenGLP) {
        if (volumenGLP <= 0) {
            return false;
        }
        double capacidadMaxima = obtenerCapacidadMaximaCamion();
        return volumenGLP > capacidadMaxima;
    }

    /**
     * Obtiene la capacidad máxima de GLP de todos los camiones disponibles.
     */
    private static double obtenerCapacidadMaximaCamion() {
        List<Camion> camionesDisponibles = camiones.stream()
                .filter(camion -> camion.getEstado() != EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(Collectors.toList());
        
        if (camionesDisponibles.isEmpty()) {
            camionesDisponibles = camiones;
        }
        
        return camionesDisponibles.stream()
                .mapToDouble(Camion::getCapacidadMaximaGLP)
                .max()
                .orElse(200.0); // Valor por defecto si no hay camiones
    }

    /**
     * Divide un pedido grande en múltiples pedidos más pequeños.
     */
    private static List<Pedido> dividirPedido(Pedido pedidoOriginal) {
        List<Pedido> pedidosDivididos = new ArrayList<>();
        
        // Obtener capacidades de todos los camiones disponibles
        List<Double> capacidadesCamiones = obtenerCapacidadesCamiones();
        
        if (capacidadesCamiones.isEmpty()) {
            // Si no hay camiones, devolver el pedido original
            return List.of(pedidoOriginal);
        }
        
        // Calcular la división óptima
        List<Double> volumenePorPedido = calcularDivisionOptima(pedidoOriginal.getVolumenGLPAsignado(), capacidadesCamiones);
        
        // Crear pedidos divididos
        for (int i = 0; i < volumenePorPedido.size(); i++) {
            double volumenPedido = volumenePorPedido.get(i);
            
            // Generar código único para cada pedido dividido
            String codigoCompleto = pedidoOriginal.getCodigo() + "-DIV" + (i + 1);
            
            Pedido pedido = Pedido.builder()
                    .coordenada(pedidoOriginal.getCoordenada())
                    .bloqueado(false)
                    .gScore(0)
                    .fScore(0)
                    .tipoNodo(TipoNodo.PEDIDO)
                    .codigo(codigoCompleto)
                    .horasLimite(pedidoOriginal.getHorasLimite())
                    .volumenGLPAsignado(volumenPedido)
                    .estado(EstadoPedido.REGISTRADO)
                    .fechaRegistro(pedidoOriginal.getFechaRegistro())
                    .build();
            
            pedidosDivididos.add(pedido);
        }
        
        return pedidosDivididos;
    }

    /**
     * Obtiene las capacidades de todos los camiones disponibles.
     */
    private static List<Double> obtenerCapacidadesCamiones() {
        List<Camion> camionesDisponibles = camiones.stream()
                .filter(camion -> camion.getEstado() != EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(Collectors.toList());
        
        if (camionesDisponibles.isEmpty()) {
            camionesDisponibles = camiones;
        }
        
        return camionesDisponibles.stream()
                .map(Camion::getCapacidadMaximaGLP)
                .sorted((a, b) -> Double.compare(b, a)) // Ordenar de mayor a menor
                .collect(Collectors.toList());
    }

    /**
     * Calcula la división óptima del volumen total entre los camiones disponibles.
     * Implementa múltiples estrategias de optimización.
     */
    private static List<Double> calcularDivisionOptima(double volumenTotal, List<Double> capacidadesCamiones) {
        // Evaluar diferentes estrategias y elegir la mejor
        List<Double> estrategia1 = estrategiaGreedySimple(volumenTotal, capacidadesCamiones);
        List<Double> estrategia2 = estrategiaBalanceada(volumenTotal, capacidadesCamiones);
        List<Double> estrategia3 = estrategiaMinimizarCamiones(volumenTotal, capacidadesCamiones);
        
        // Evaluar y seleccionar la mejor estrategia
        AnalisisEstrategia mejor = evaluarEstrategias(volumenTotal, estrategia1, estrategia2, estrategia3);
        
        System.out.println("🎯 Estrategia seleccionada: " + mejor.nombre + 
                          " (Camiones: " + mejor.numCamiones + 
                          ", Eficiencia: " + String.format("%.1f%%", mejor.eficiencia * 100) + ")");
        
        return mejor.division;
    }

    /**
     * Estrategia 1: Greedy Simple - Llenar camiones más grandes primero
     */
    private static List<Double> estrategiaGreedySimple(double volumenTotal, List<Double> capacidadesCamiones) {
        List<Double> volumenPorPedido = new ArrayList<>();
        double volumenRestante = volumenTotal;
        
        while (volumenRestante > 0) {
            int indiceCamion = 0;
            while (volumenRestante > 0 && indiceCamion < capacidadesCamiones.size()) {
                double capacidadCamion = capacidadesCamiones.get(indiceCamion);
                double volumenAsignado = Math.min(volumenRestante, capacidadCamion);
                
                volumenPorPedido.add(volumenAsignado);
                volumenRestante -= volumenAsignado;
                indiceCamion++;
            }
        }
        
        return volumenPorPedido;
    }

    /**
     * Estrategia 2: Balanceada - Distribuir entre diferentes tipos de camiones
     */
    private static List<Double> estrategiaBalanceada(double volumenTotal, List<Double> capacidadesCamiones) {
        List<Double> volumenPorPedido = new ArrayList<>();
        double volumenRestante = volumenTotal;
        
        // Agrupar capacidades por tipo
        Map<Double, Integer> tiposCamiones = new HashMap<>();
        for (Double capacidad : capacidadesCamiones) {
            tiposCamiones.put(capacidad, tiposCamiones.getOrDefault(capacidad, 0) + 1);
        }
        
        // Distribuir proporcionalmente entre tipos
        while (volumenRestante > 0) {
            boolean asignoVolumen = false;
            
            for (Map.Entry<Double, Integer> entry : tiposCamiones.entrySet()) {
                if (volumenRestante <= 0 || entry.getValue() <= 0) continue;
                
                double capacidad = entry.getKey();
                double volumenAsignado = Math.min(volumenRestante, capacidad);
                
                volumenPorPedido.add(volumenAsignado);
                volumenRestante -= volumenAsignado;
                entry.setValue(entry.getValue() - 1);
                asignoVolumen = true;
                
                if (volumenRestante <= 0) break;
            }
            
            if (!asignoVolumen) break; // No hay más camiones disponibles
        }
        
        return volumenPorPedido;
    }

    /**
     * Estrategia 3: Minimizar Camiones - Usar el menor número posible de camiones
     */
    private static List<Double> estrategiaMinimizarCamiones(double volumenTotal, List<Double> capacidadesCamiones) {
        List<Double> volumenPorPedido = new ArrayList<>();
        double volumenRestante = volumenTotal;
        
        // Ordenar capacidades de mayor a menor
        List<Double> capacidadesOrdenadas = new ArrayList<>(capacidadesCamiones);
        capacidadesOrdenadas.sort((a, b) -> Double.compare(b, a));
        
        for (Double capacidad : capacidadesOrdenadas) {
            while (volumenRestante > 0 && volumenRestante >= capacidad * 0.1) { // Usar si al menos 10% de la capacidad
                double volumenAsignado = Math.min(volumenRestante, capacidad);
                volumenPorPedido.add(volumenAsignado);
                volumenRestante -= volumenAsignado;
            }
            if (volumenRestante <= 0) break;
        }
        
        return volumenPorPedido;
    }

    /**
     * Evalúa las diferentes estrategias y selecciona la mejor
     */
    private static AnalisisEstrategia evaluarEstrategias(double volumenTotal, 
                                                       List<Double> estrategia1, 
                                                       List<Double> estrategia2, 
                                                       List<Double> estrategia3) {
        
        AnalisisEstrategia analisis1 = new AnalisisEstrategia("Greedy Simple", estrategia1, volumenTotal);
        AnalisisEstrategia analisis2 = new AnalisisEstrategia("Balanceada", estrategia2, volumenTotal);
        AnalisisEstrategia analisis3 = new AnalisisEstrategia("Minimizar Camiones", estrategia3, volumenTotal);
        
        // Priorizar: 1) Eficiencia, 2) Menor número de camiones
        List<AnalisisEstrategia> estrategias = Arrays.asList(analisis1, analisis2, analisis3);
        
        return estrategias.stream()
                .filter(e -> e.esValida)
                .max(Comparator.comparing((AnalisisEstrategia e) -> e.eficiencia)
                              .thenComparing((AnalisisEstrategia e) -> -e.numCamiones))
                .orElse(analisis1); // Fallback a estrategia 1
    }

    /**
     * Clase para analizar el rendimiento de cada estrategia
     */
    private static class AnalisisEstrategia {
        String nombre;
        List<Double> division;
        int numCamiones;
        double eficiencia;
        boolean esValida;
        
        AnalisisEstrategia(String nombre, List<Double> division, double volumenTotal) {
            this.nombre = nombre;
            this.division = division;
            this.numCamiones = division.size();
            
            double volumenAsignado = division.stream().mapToDouble(Double::doubleValue).sum();
            this.eficiencia = volumenAsignado / volumenTotal;
            this.esValida = Math.abs(volumenAsignado - volumenTotal) < 0.001;
        }
    }

    /**
     * Muestra estadísticas de capacidad de camiones y demanda de pedidos.
     */
    private static void mostrarEstadisticasCapacidad() {
        // Capacidad total disponible
        double capacidadTotalDisponible = camiones.stream()
                .filter(camion -> camion.getEstado() != EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .mapToDouble(Camion::getCapacidadMaximaGLP)
                .sum();
        
        // Capacidad total de todos los camiones (incluyendo los en mantenimiento)
        double capacidadTotalSistema = camiones.stream()
                .mapToDouble(Camion::getCapacidadMaximaGLP)
                .sum();
        
        // Suma total de GLP de todos los pedidos
        double demandaTotalGLP = pedidos.stream()
                .mapToDouble(Pedido::getVolumenGLPAsignado)
                .sum();
        
        // Cantidad de camiones por estado
        long camionesDisponibles = camiones.stream()
                .filter(camion -> camion.getEstado() != EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .count();
        
        long camionesEnMantenimiento = camiones.stream()
                .filter(camion -> camion.getEstado() == EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .count();
        
        System.out.println("\n=== ESTADÍSTICAS DE CAPACIDAD Y DEMANDA ===");
        System.out.println("🚛 Total de camiones: " + camiones.size());
        System.out.println("✅ Camiones disponibles: " + camionesDisponibles);
        System.out.println("🔧 Camiones en mantenimiento: " + camionesEnMantenimiento);
        System.out.println("📦 Capacidad total disponible: " + String.format("%.2f", capacidadTotalDisponible) + " m³");
        System.out.println("📦 Capacidad total del sistema: " + String.format("%.2f", capacidadTotalSistema) + " m³");
        System.out.println("📊 Demanda total de GLP: " + String.format("%.2f", demandaTotalGLP) + " m³");
        System.out.println("📈 Ratio demanda/capacidad disponible: " + String.format("%.2f", demandaTotalGLP / capacidadTotalDisponible));
        System.out.println("📈 Ratio demanda/capacidad total: " + String.format("%.2f", demandaTotalGLP / capacidadTotalSistema));
        
        if (demandaTotalGLP > capacidadTotalDisponible) {
            System.out.println("⚠️  ADVERTENCIA: La demanda supera la capacidad disponible!");
        } else {
            System.out.println("✅ La capacidad disponible es suficiente para cubrir la demanda");
        }
    }

    /**
     * Inicializa la lista de mantenimientos a partir del archivo de mantenimientos.
     *
     * @return Lista de mantenimientos inicializados.
     * @throws IOException                si ocurre un error al leer el archivo.
     * @throws InvalidDataFormatException si el formato de los datos es inválido.
     */
    public static List<Mantenimiento> initializeMantenimientos()
            throws IOException, InvalidDataFormatException {
        List<String> lines = Herramientas.readAllLines(pathMantenimientos); // Propaga IOException si ocurre

        for (String line : lines) {

            String[] partes = line.split(":");
            if (partes.length != 2) {
                throw new InvalidDataFormatException(
                        "Error de formato en línea de mantenimiento: Se esperaban 2 partes separadas por ':'. Línea: "
                                + line);
            }

            String fechaCompleta = partes[0]; // aaaammdd
            String codigoTipoCamion = partes[1]; // TTNN (Tipo y Número, ej. TA01)

            if (fechaCompleta.length() != 8) {
                throw new InvalidDataFormatException(
                        "Error de formato en fecha de mantenimiento: Se esperaba formato aaaammdd. Valor: "
                                + fechaCompleta + ". Línea: " + line);
            }
            if (codigoTipoCamion.length() != 4) { // Asumiendo TTNN donde TT es tipo y NN es número.
                throw new InvalidDataFormatException(
                        "Error de formato en código/tipo de camión de mantenimiento: Se esperaba formato TTNN. Valor: "
                                + codigoTipoCamion + ". Línea: " + line);
            }

            int mes, dia;
            try {
                // anio = Integer.parseInt(fechaCompleta.substring(0, 4)); // Año no se usa
                mes = Integer.parseInt(fechaCompleta.substring(4, 6));
                dia = Integer.parseInt(fechaCompleta.substring(6, 8));
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                throw new InvalidDataFormatException("Error al parsear fecha de mantenimiento: " + fechaCompleta
                        + ". Línea: " + line + ". Detalles: " + e.getMessage());
            }

            Camion camion;
            try {
                camion = CamionFactory.getCamionPorCodigo(codigoTipoCamion);
 
            } catch (NoSuchElementException e) {
                throw new InvalidDataFormatException(
                        "Error en línea de mantenimiento: " + line + ". Detalles: " + e.getMessage());
            }

            int ini, fin;
            if (mes % 2 == 0) {
                ini = mes;
                fin = 12;
            } else {
                ini = mes;
                fin = 11;
            }

            for (int i = ini; i <= fin; i += 2) {
                Mantenimiento mantenimiento = Mantenimiento.builder()
                        .dia(dia)
                        .mes(i)
                        .camion(camion)
                        .build();
                mantenimientos.add(mantenimiento);
            }
        }
        return mantenimientos;
    }

    public static void initializeBloqueos() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(getPathBloqueos());
        for (String line : lines) {
            Bloqueo bloqueo = new Bloqueo(line);
            bloqueos.add(bloqueo);
        }
    }
}
