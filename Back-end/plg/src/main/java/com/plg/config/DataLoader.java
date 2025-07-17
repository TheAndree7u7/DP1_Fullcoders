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
    public static volatile boolean inicializando = false;

    public static List<Almacen> initializeAlmacenes() {
        // Limpiar la lista de almacenes antes de crear nuevos
        AlmacenFactory.almacenes.clear();
        almacenes.clear();

        AlmacenFactory.crearAlmacen(TipoAlmacen.CENTRAL, coordenadaCentral, 1_000_000_000,
                1_000_000_000);
        AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(42, 42), 160.0, 50);
        AlmacenFactory.crearAlmacen(TipoAlmacen.SECUNDARIO, new Coordenada(3, 63), 160.0, 50);
        almacenes = AlmacenFactory.almacenes;

        System.out.println("🏭 Almacenes inicializados: " + almacenes.size() + " almacenes creados");
        return almacenes;
    }

    public static List<Camion> initializeCamiones() {
        // Limpiar la lista de camiones antes de crear nuevos
        CamionFactory.camiones.clear();
        camiones.clear();

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

        System.out.println("🚛 Camiones inicializados: " + camiones.size() + " camiones creados");
        return camiones;
    }

    public static List<Averia> initializeAverias() throws InvalidDataFormatException, IOException {
        List<String> lines = Herramientas.readAllLines(pathAverias);
        for (String line : lines) {
            Averia averia = new Averia(line);

        }
        return averias;
    }

    /**
     * Detecta automáticamente todos los archivos de pedidos disponibles en la
     * carpeta data/pedidos.
     * Solo lee archivos del año 2025.
     * 
     * @return Lista de rutas de archivos de pedidos encontrados
     */
    private static List<String> detectarArchivosPedidos() {
        List<String> archivosEncontrados = new ArrayList<>();

        // Lista de archivos del año 2025 (enero a diciembre)
        String[] archivosConocidos = {
                "data/pedidos/ventas202501.txt", // Enero
                "data/pedidos/ventas202502.txt", // Febrero
                "data/pedidos/ventas202503.txt", // Marzo
                "data/pedidos/ventas202504.txt", // Abril
                "data/pedidos/ventas202505.txt", // Mayo
                "data/pedidos/ventas202506.txt", // Junio
                "data/pedidos/ventas202507.txt", // Julio
                "data/pedidos/ventas202508.txt", // Agosto
                "data/pedidos/ventas202509.txt", // Septiembre
                "data/pedidos/ventas202510.txt", // Octubre
                "data/pedidos/ventas202511.txt", // Noviembre
                "data/pedidos/ventas202512.txt" // Diciembre
        };

        // Verificar qué archivos existen realmente
        for (String archivo : archivosConocidos) {
            try {
                if (Herramientas.class.getClassLoader().getResourceAsStream(archivo) != null) {
                    archivosEncontrados.add(archivo);
                    System.out.println("✅ Archivo de pedidos encontrado: " + archivo);
                } else {
                    System.out.println("❌ Archivo de pedidos no encontrado: " + archivo);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error verificando archivo: " + archivo + " - " + e.getMessage());
            }
        }

        return archivosEncontrados;
    }

    /**
     * Calcula las fechas mínima y máxima de registro de los pedidos cargados.
     * 
     * @param pedidos Lista de pedidos para analizar
     * @return Array con [fechaMinima, fechaMaxima] o [null, null] si no hay pedidos
     */
    private static LocalDateTime[] calcularFechasMinMax(List<Pedido> pedidos) {
        if (pedidos == null || pedidos.isEmpty()) {
            System.out.println("⚠️ No hay pedidos para calcular fechas mínima y máxima");
            return new LocalDateTime[] { null, null };
        }

        LocalDateTime fechaMinima = pedidos.stream()
                .map(Pedido::getFechaRegistro)
                .filter(fecha -> fecha != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime fechaMaxima = pedidos.stream()
                .map(Pedido::getFechaRegistro)
                .filter(fecha -> fecha != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (fechaMinima != null && fechaMaxima != null) {
            System.out.println("📅 Rango de fechas de pedidos:");
            System.out.println("   • Fecha mínima: "
                    + fechaMinima.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            System.out.println("   • Fecha máxima: "
                    + fechaMaxima.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Calcular duración total
            long diasEntreFechas = java.time.Duration.between(fechaMinima, fechaMaxima).toDays();
            System.out.println("   • Duración total: " + diasEntreFechas + " días");
        }

        return new LocalDateTime[] { fechaMinima, fechaMaxima };
    }

    /**
     * Detecta automáticamente todos los archivos de bloqueos disponibles en la
     * carpeta data/bloqueos.
     * 
     * @return Lista de rutas de archivos de bloqueos encontrados
     */
    private static List<String> detectarArchivosBloqueos() {
        List<String> archivosEncontrados = new ArrayList<>();

        // Lista de archivos conocidos basada en la estructura de carpetas
        String[] archivosConocidos = {
                "data/bloqueos/202501.bloqueos.txt",
                "data/bloqueos/202502.bloqueos.txt",
                "data/bloqueos/202503.bloqueos.txt",
                "data/bloqueos/202504.bloqueos.txt",
                "data/bloqueos/202505.bloqueos.txt",
                "data/bloqueos/202506.bloqueos.txt",
                "data/bloqueos/202507.bloqueos.txt",
                "data/bloqueos/202508.bloqueos.txt",
                "data/bloqueos/202509.bloqueos.txt",
                "data/bloqueos/202510.bloqueos.txt",
                "data/bloqueos/202511.bloqueos.txt",
                "data/bloqueos/202512.bloqueos.txt"
        };

        // Verificar qué archivos existen realmente
        for (String archivo : archivosConocidos) {
            try {
                if (Herramientas.class.getClassLoader().getResourceAsStream(archivo) != null) {
                    archivosEncontrados.add(archivo);
                    System.out.println("✅ Archivo de bloqueos encontrado: " + archivo);
                } else {
                    System.out.println("❌ Archivo de bloqueos no encontrado: " + archivo);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error verificando archivo: " + archivo + " - " + e.getMessage());
            }
        }

        return archivosEncontrados;
    }

    public static List<Pedido> initializePedidos() throws InvalidDataFormatException, IOException {
        // Limpiar la lista de pedidos antes de cargar nuevos
        PedidoFactory.pedidos.clear();

        List<Pedido> pedidosOriginales = new ArrayList<>();
        List<String> archivosLeidos = new ArrayList<>();

        // Detectar archivos disponibles
        List<String> archivosDisponibles = detectarArchivosPedidos();

        if (archivosDisponibles.isEmpty()) {
            System.err.println("❌ No se encontraron archivos de pedidos disponibles");
            throw new IOException("No se encontraron archivos de pedidos para cargar");
        }

        // Leer todos los archivos de pedidos disponibles
        try {
            for (String archivo : archivosDisponibles) {
                try {
                    List<String> lines = Herramientas.readAllLines(archivo);
                    System.out.println("📁 Leyendo archivo: " + archivo + " - " + lines.size() + " líneas");

                    // Procesar cada línea del archivo
                    for (String line : lines) {
                        Pedido pedido = PedidoFactory.crearPedido(line, archivo);

                        // Validar y corregir coordenadas del pedido
                        Coordenada coordenadaOriginal = pedido.getCoordenada();
                        Coordenada coordenadaCorregida = validarYCorregirCoordenada(coordenadaOriginal);

                        if (!coordenadaOriginal.equals(coordenadaCorregida)) {
                            System.err.println("⚠️ Pedido " + pedido.getCodigo() + " tenía coordenadas inválidas: " +
                                    coordenadaOriginal + " → Corregidas a: " + coordenadaCorregida);
                            pedido.setCoordenada(coordenadaCorregida);
                        }

                        pedido.setFechaLimite(pedido.getFechaRegistro().plusHours((long) pedido.getHorasLimite()));
                        pedidosOriginales.add(pedido);
                    }

                    archivosLeidos.add(archivo);

                } catch (IOException e) {
                    // Si no se puede leer un archivo, continuar con el siguiente
                    System.err.println("⚠️ No se pudo leer el archivo: " + archivo + " - " + e.getMessage());
                } catch (Exception e) {
                    // Si hay error en el procesamiento, continuar con el siguiente archivo
                    System.err.println("⚠️ Error procesando archivo: " + archivo + " - " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error general al leer archivos de pedidos: " + e.getMessage());
            throw e;
        }

        System.out.println("📊 Total de archivos leídos exitosamente: " + archivosLeidos.size());
        System.out.println("📊 Total de pedidos originales cargados: " + pedidosOriginales.size());

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
        System.out.println("📁 Archivos leídos: " + archivosLeidos.size());
        System.out.println("📊 Pedidos originales leídos: " + totalPedidosOriginales);
        System.out.println("📊 Pedidos que requirieron división: " + pedidosDivididos);
        System.out.println("📊 Total de pedidos finales: " + pedidos.size());

        // Calcular y mostrar fechas mínima y máxima
        LocalDateTime[] fechasMinMax = calcularFechasMinMax(pedidos);

        // Mostrar estadísticas por mes si es posible
        mostrarEstadisticasPorMes(pedidos);

        mostrarEstadisticasCapacidad();

        return pedidos;
    }

    /**
     * Muestra estadísticas de pedidos agrupados por mes.
     * 
     * @param pedidos Lista de pedidos para analizar
     */
    private static void mostrarEstadisticasPorMes(List<Pedido> pedidos) {
        if (pedidos == null || pedidos.isEmpty()) {
            return;
        }

        System.out.println("\n📊 ESTADÍSTICAS POR MES:");

        // Agrupar pedidos por mes
        Map<Integer, Long> pedidosPorMes = pedidos.stream()
                .filter(p -> p.getFechaRegistro() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        p -> p.getFechaRegistro().getMonthValue(),
                        java.util.stream.Collectors.counting()));

        // Mostrar estadísticas por mes
        for (int mes = 1; mes <= 12; mes++) {
            Long cantidad = pedidosPorMes.getOrDefault(mes, 0L);
            String nombreMes = obtenerNombreMes(mes);
            System.out.println("   • " + nombreMes + ": " + cantidad + " pedidos");
        }

        // Mostrar total
        long total = pedidosPorMes.values().stream().mapToLong(Long::longValue).sum();
        System.out.println("   • TOTAL: " + total + " pedidos");
    }

    /**
     * Obtiene el nombre del mes en español.
     * 
     * @param mes Número del mes (1-12)
     * @return Nombre del mes en español
     */
    private static String obtenerNombreMes(int mes) {
        String[] nombresMeses = {
                "", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return nombresMeses[mes];
    }

    /**
     * Verifica si un pedido necesita ser dividido en función de las capacidades de
     * los camiones.
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
        List<Double> volumenePorPedido = calcularDivisionOptima(pedidoOriginal.getVolumenGLPAsignado(),
                capacidadesCamiones);

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
                if (volumenRestante <= 0 || entry.getValue() <= 0)
                    continue;

                double capacidad = entry.getKey();
                double volumenAsignado = Math.min(volumenRestante, capacidad);

                volumenPorPedido.add(volumenAsignado);
                volumenRestante -= volumenAsignado;
                entry.setValue(entry.getValue() - 1);
                asignoVolumen = true;

                if (volumenRestante <= 0)
                    break;
            }

            if (!asignoVolumen)
                break; // No hay más camiones disponibles
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
            if (volumenRestante <= 0)
                break;
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
        System.out.println("📈 Ratio demanda/capacidad disponible: "
                + String.format("%.2f", demandaTotalGLP / capacidadTotalDisponible));
        System.out.println(
                "📈 Ratio demanda/capacidad total: " + String.format("%.2f", demandaTotalGLP / capacidadTotalSistema));

        if (demandaTotalGLP > capacidadTotalDisponible) {
            System.out.println("⚠️  ADVERTENCIA: La demanda supera la capacidad disponible!");
        } else {
            System.out.println("✅ La capacidad disponible es suficiente para cubrir la demanda");
        }
    }

    /**
     * Valida y corrige una coordenada para que esté dentro del rango del mapa.
     * 
     * @param coordenada La coordenada a validar
     * @return La coordenada corregida si está fuera del rango, o la original si
     *         está dentro
     */
    private static Coordenada validarYCorregirCoordenada(Coordenada coordenada) {
        if (coordenada == null) {
            System.err.println("⚠️ Coordenada nula encontrada, usando (0,0)");
            return new Coordenada(0, 0);
        }

        // Obtener dimensiones del mapa
        Mapa mapa = Mapa.getInstance();
        if (mapa == null) {
            System.err.println("⚠️ Mapa no inicializado, usando dimensiones por defecto");
            return new Coordenada(0, 0);
        }

        int filasMapa = mapa.getFilas();
        int columnasMapa = mapa.getColumnas();

        int fila = coordenada.getFila();
        int columna = coordenada.getColumna();

        // Ajustar coordenadas al rango válido
        int filaCorregida = Math.max(0, Math.min(fila, filasMapa - 1));
        int columnaCorregida = Math.max(0, Math.min(columna, columnasMapa - 1));

        return new Coordenada(filaCorregida, columnaCorregida);
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
        // Limpiar la lista de bloqueos antes de cargar nuevos
        bloqueos.clear();

        List<String> archivosLeidos = new ArrayList<>();

        // Detectar archivos disponibles
        List<String> archivosDisponibles = detectarArchivosBloqueos();

        if (archivosDisponibles.isEmpty()) {
            System.err.println("❌ No se encontraron archivos de bloqueos disponibles");
            throw new IOException("No se encontraron archivos de bloqueos para cargar");
        }

        // Leer todos los archivos de bloqueos disponibles
        try {
            for (String archivo : archivosDisponibles) {
                try {
                    List<String> lines = Herramientas.readAllLines(archivo);
                    System.out.println("🚧 Leyendo archivo de bloqueos: " + archivo + " - " + lines.size() + " líneas");

                    // Procesar cada línea del archivo
                    for (String line : lines) {
                        Bloqueo bloqueo = new Bloqueo(line);
                        bloqueos.add(bloqueo);
                    }

                    archivosLeidos.add(archivo);

                } catch (IOException e) {
                    // Si no se puede leer un archivo, continuar con el siguiente
                    System.err
                            .println("⚠️ No se pudo leer el archivo de bloqueos: " + archivo + " - " + e.getMessage());
                } catch (Exception e) {
                    // Si hay error en el procesamiento, continuar con el siguiente archivo
                    System.err.println("⚠️ Error procesando archivo de bloqueos: " + archivo + " - " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error general al leer archivos de bloqueos: " + e.getMessage());
            throw e;
        }

        System.out.println("🚧 Total de archivos de bloqueos leídos exitosamente: " + archivosLeidos.size());
        System.out.println("🚧 Total de bloqueos cargados: " + bloqueos.size());
    }
}
