package com.plg.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.plg.config.DataLoader;
import com.plg.dto.request.PedidoRequest;
import com.plg.dto.request.PedidosLoteRequest;
import com.plg.entity.Camion;
import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.Pedido;
import com.plg.factory.PedidoFactory;
import com.plg.repository.PedidoRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.InvalidInputException;

/**
 * Servicio para operaciones sobre pedidos.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Retorna la lista de pedidos actuales.
     */
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    /**
     * Lista los pedidos registrados entre dos fechas.
     *
     * @param inicio fecha y hora inicial
     * @param fin    fecha y hora final
     * @return lista de pedidos en el rango
     */
    public List<Pedido> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return pedidoRepository.findAllBetween(inicio, fin);
    }

    /**
     * Calcula un resumen simple por estado de los pedidos.
     */
    public Map<String, Object> resumen() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        Map<String, Object> datos = new HashMap<>();
        datos.put("total", pedidos.size());
        datos.put("porEstado",
                pedidos.stream()
                        .collect(Collectors.groupingBy(p -> p.getEstado().name(), Collectors.counting())));
        return datos;
    }

    /**
     * Crea un nuevo pedido utilizando los datos de la solicitud.
     * Si el pedido es demasiado grande para ser manejado por un solo camión,
     * se dividirá automáticamente en múltiples pedidos.
     */
    public List<Pedido> agregar(PedidoRequest request) {
        try {
            Coordenada coordenada = new Coordenada(request.getY(), request.getX());
            
            // Verificar si el pedido necesita ser dividido
            if (necesitaDivision(request.getVolumenGLP())) {
                return dividirPedido(coordenada, request.getVolumenGLP(), request.getHorasLimite());
            } else {
                // Crear pedido normal
                Pedido pedido = PedidoFactory.crearPedido(coordenada, request.getVolumenGLP(), request.getHorasLimite());
                return List.of(pedidoRepository.save(pedido));
            }
        } catch (Exception e) {
            throw new InvalidInputException("No se pudo crear el pedido", e);
        }
    }

    /**
     * Verifica si un pedido necesita ser dividido en función de las capacidades de los camiones.
     */
    private boolean necesitaDivision(double volumenGLP) {
        if (volumenGLP <= 0) {
            return false;
        }
        double capacidadMaxima = obtenerCapacidadMaximaCamion();
        return volumenGLP > capacidadMaxima;
    }

    /**
     * Obtiene la capacidad máxima de GLP de todos los camiones disponibles.
     */
    private double obtenerCapacidadMaximaCamion() {
        List<Camion> camionesDisponibles = DataLoader.camiones.stream()
                .filter(camion -> camion.getEstado() != EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(Collectors.toList());
        
        if (camionesDisponibles.isEmpty()) {
            camionesDisponibles = DataLoader.camiones;
        }
        
        return camionesDisponibles.stream()
                .mapToDouble(Camion::getCapacidadMaximaGLP)
                .max()
                .orElse(200.0); // Valor por defecto si no hay camiones
    }

    /**
     * Divide un pedido grande en múltiples pedidos más pequeños.
     */
    private List<Pedido> dividirPedido(Coordenada coordenada, double volumenTotal, double horasLimite) {
        List<Pedido> pedidosDivididos = new ArrayList<>();
        
        // Validar entrada
        if (volumenTotal <= 0) {
            throw new InvalidInputException("El volumen total debe ser mayor a cero");
        }
        
        // Obtener capacidades de todos los camiones disponibles
        List<Double> capacidadesCamiones = obtenerCapacidadesCamiones();
        
        if (capacidadesCamiones.isEmpty()) {
            throw new InvalidInputException("No hay camiones disponibles para dividir el pedido");
        }
        
        // Calcular la división óptima
        List<Double> volumenePorPedido = calcularDivisionOptima(volumenTotal, capacidadesCamiones);
        
        // Validar que la división sea exitosa
        double totalDividido = volumenePorPedido.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(totalDividido - volumenTotal) > 0.001) {
            throw new InvalidInputException("Error en la división del pedido: volumen total no coincide");
        }
        
        // Crear pedidos divididos
        for (int i = 0; i < volumenePorPedido.size(); i++) {
            double volumenPedido = volumenePorPedido.get(i);
            
            // Generar código único para cada pedido dividido
            String codigoBase = "PEDIDO-" + coordenada.getFila() + "-" + coordenada.getColumna();
            String codigoCompleto = codigoBase + "-DIV" + (i + 1);
            
            Pedido pedido = Pedido.builder()
                    .coordenada(coordenada)
                    .bloqueado(false)
                    .gScore(0)
                    .fScore(0)
                    .tipoNodo(com.plg.entity.TipoNodo.PEDIDO)
                    .codigo(codigoCompleto)
                    .horasLimite(horasLimite)
                    .volumenGLPAsignado(volumenPedido)
                    .estado(com.plg.entity.EstadoPedido.REGISTRADO)
                    .build();
            
            pedidosDivididos.add(pedidoRepository.save(pedido));
        }
        
        System.out.println("Pedido dividido exitosamente: " + volumenTotal + " m³ en " + pedidosDivididos.size() + " pedidos");
        return pedidosDivididos;
    }

    /**
     * Obtiene las capacidades de todos los camiones disponibles.
     */
    private List<Double> obtenerCapacidadesCamiones() {
        List<Camion> camionesDisponibles = DataLoader.camiones.stream()
                .filter(camion -> camion.getEstado() != EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO)
                .collect(Collectors.toList());
        
        if (camionesDisponibles.isEmpty()) {
            camionesDisponibles = DataLoader.camiones;
        }
        
        return camionesDisponibles.stream()
                .map(Camion::getCapacidadMaximaGLP)
                .sorted((a, b) -> Double.compare(b, a)) // Ordenar de mayor a menor
                .collect(Collectors.toList());
    }

    /**
     * Calcula la división óptima del volumen total entre los camiones disponibles.
     * Utiliza un algoritmo greedy para maximizar la utilización de los camiones.
     */
    private List<Double> calcularDivisionOptima(double volumenTotal, List<Double> capacidadesCamiones) {
        List<Double> volumenPorPedido = new ArrayList<>();
        double volumenRestante = volumenTotal;
        
        // Usar algoritmo greedy: asignar primero a los camiones más grandes
        int indiceCamion = 0;
        
        while (volumenRestante > 0 && indiceCamion < capacidadesCamiones.size()) {
            double capacidadCamion = capacidadesCamiones.get(indiceCamion);
            double volumenAsignado = Math.min(volumenRestante, capacidadCamion);
            
            volumenPorPedido.add(volumenAsignado);
            volumenRestante -= volumenAsignado;
            indiceCamion++;
        }
        
        // Si aún queda volumen después de asignar a todos los camiones,
        // comenzar un nuevo ciclo con los camiones más grandes
        while (volumenRestante > 0) {
            indiceCamion = 0;
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
     * Actualiza solo el estado de un pedido por su código.
     *
     * @param request DTO con el código y el nuevo estado
     * @return El pedido actualizado
     */
    public Pedido actualizarEstado(com.plg.dto.request.PedidoEstadoUpdateRequest request) {
        Pedido pedido = pedidoRepository.findAll().stream()
                .filter(p -> p.getCodigo().equals(request.getCodigo()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + request.getCodigo()));
        pedido.setEstado(request.getEstado());
        return pedido;
    }

    /**
     * Cargar un lote de pedidos desde el frontend.
     * Reemplaza la funcionalidad de carga desde archivos planos.
     * 
     * @param request Lote de pedidos a procesar
     * @return Resultado del procesamiento
     */
    public Map<String, Object> cargarLotePedidos(PedidosLoteRequest request) {
        try {
            System.out.println("🔄 SERVICIO: Iniciando procesamiento de lote de pedidos...");
            System.out.println("📦 Total de pedidos recibidos: " + request.getPedidos().size());
            System.out.println("📅 Fecha de inicio: " + request.getFechaInicio());
            
            List<Pedido> pedidosCreados = new ArrayList<>();
            List<String> errores = new ArrayList<>();
            int pedidosExitosos = 0;
            int pedidosDivididos = 0;
            
            // Actualizar parámetros del sistema con la fecha recibida
            actualizarParametrosSimulacion(request.getFechaInicio());
            
            // Procesar cada pedido del lote
            for (int i = 0; i < request.getPedidos().size(); i++) {
                PedidosLoteRequest.PedidoLoteItem item = request.getPedidos().get(i);
                try {
                    // Crear coordenada
                    Coordenada coordenada = new Coordenada(item.getY(), item.getX());
                    
                    // Generar código único para el pedido
                    String codigoPedido = generarCodigoPedido(item, i);
                    
                    // Verificar si necesita división
                    if (necesitaDivision(item.getVolumenGLP())) {
                        List<Pedido> pedidosDivididos_temp = dividirPedidoLote(
                            coordenada, 
                            item.getVolumenGLP(), 
                            item.getHorasLimite(),
                            item.getFechaPedido(),
                            codigoPedido,
                            item.getCliente()
                        );
                        pedidosCreados.addAll(pedidosDivididos_temp);
                        pedidosDivididos += pedidosDivididos_temp.size() - 1; // -1 porque contamos solo las divisiones adicionales
                        pedidosExitosos++;
                    } else {
                        // Crear pedido normal
                        Pedido pedido = crearPedidoDesdeItem(item, coordenada, codigoPedido);
                        pedidosCreados.add(pedidoRepository.save(pedido));
                        pedidosExitosos++;
                    }
                    
                } catch (Exception e) {
                    String error = "Error en pedido " + (i + 1) + ": " + e.getMessage();
                    errores.add(error);
                    System.err.println("❌ " + error);
                }
            }
            
            // Sincronizar con DataLoader para que la simulación use los nuevos pedidos
            sincronizarConDataLoader(pedidosCreados);
            
            // Preparar respuesta
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("totalRecibidos", request.getPedidos().size());
            resultado.put("pedidosExitosos", pedidosExitosos);
            resultado.put("pedidosDivididos", pedidosDivididos);
            resultado.put("totalPedidosCreados", pedidosCreados.size());
            resultado.put("errores", errores);
            resultado.put("fechaInicio", request.getFechaInicio());
            resultado.put("descripcion", request.getDescripcion());
            
            System.out.println("✅ SERVICIO: Lote procesado exitosamente");
            System.out.println("📊 Resumen: " + pedidosExitosos + " exitosos, " + errores.size() + " errores, " + pedidosCreados.size() + " pedidos creados");
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("💥 Error crítico al procesar lote de pedidos: " + e.getMessage());
            e.printStackTrace();
            throw new InvalidInputException("Error al procesar lote de pedidos", e);
        }
    }

    /**
     * Validar un lote de pedidos sin procesarlos.
     * 
     * @param request Lote de pedidos a validar
     * @return Resultado de la validación
     */
    public Map<String, Object> validarLotePedidos(PedidosLoteRequest request) {
        System.out.println("🔍 SERVICIO: Iniciando validación de lote de pedidos...");
        
        List<String> errores = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        int pedidosValidos = 0;
        int pedidosQueDividirian = 0;
        
        // Validaciones generales del lote
        if (request.getFechaInicio() == null) {
            errores.add("La fecha de inicio es obligatoria");
        }
        
        if (request.getPedidos() == null || request.getPedidos().isEmpty()) {
            errores.add("La lista de pedidos no puede estar vacía");
        } else {
            // Validar cada pedido individualmente
            for (int i = 0; i < request.getPedidos().size(); i++) {
                PedidosLoteRequest.PedidoLoteItem item = request.getPedidos().get(i);
                List<String> erroresPedido = validarPedidoIndividual(item, i + 1);
                errores.addAll(erroresPedido);
                
                if (erroresPedido.isEmpty()) {
                    pedidosValidos++;
                    
                    // Verificar si necesitaría división
                    if (necesitaDivision(item.getVolumenGLP())) {
                        pedidosQueDividirian++;
                        advertencias.add("Pedido " + (i + 1) + " será dividido (volumen: " + item.getVolumenGLP() + " m³)");
                    }
                }
            }
        }
        
        // Preparar respuesta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("valido", errores.isEmpty());
        resultado.put("totalPedidos", request.getPedidos() != null ? request.getPedidos().size() : 0);
        resultado.put("pedidosValidos", pedidosValidos);
        resultado.put("pedidosQueDividirian", pedidosQueDividirian);
        resultado.put("errores", errores);
        resultado.put("advertencias", advertencias);
        
        System.out.println("✅ SERVICIO: Validación completada - " + pedidosValidos + " válidos de " + 
                          (request.getPedidos() != null ? request.getPedidos().size() : 0));
        
        return resultado;
    }

    /**
     * Validar un pedido individual
     */
    private List<String> validarPedidoIndividual(PedidosLoteRequest.PedidoLoteItem item, int numeroPedido) {
        List<String> errores = new ArrayList<>();
        
        if (item.getFechaPedido() == null) {
            errores.add("Pedido " + numeroPedido + ": fecha es obligatoria");
        }
        
        if (item.getX() == null || item.getX() < 0) {
            errores.add("Pedido " + numeroPedido + ": coordenada X inválida");
        }
        
        if (item.getY() == null || item.getY() < 0) {
            errores.add("Pedido " + numeroPedido + ": coordenada Y inválida");
        }
        
        if (item.getVolumenGLP() == null || item.getVolumenGLP() <= 0) {
            errores.add("Pedido " + numeroPedido + ": volumen de GLP debe ser mayor a 0");
        }
        
        if (item.getHorasLimite() == null || item.getHorasLimite() <= 0) {
            errores.add("Pedido " + numeroPedido + ": horas límite debe ser mayor a 0");
        }
        
        return errores;
    }

    /**
     * Crear un pedido desde un item del lote
     */
    private Pedido crearPedidoDesdeItem(PedidosLoteRequest.PedidoLoteItem item, Coordenada coordenada, String codigo) {
        return Pedido.builder()
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(com.plg.entity.TipoNodo.PEDIDO)
                .codigo(codigo)
                .horasLimite(item.getHorasLimite())
                .volumenGLPAsignado(item.getVolumenGLP())
                .estado(com.plg.entity.EstadoPedido.REGISTRADO)
                .fechaRegistro(item.getFechaPedido())
                .build();
    }

    /**
     * Generar código único para un pedido
     */
    private String generarCodigoPedido(PedidosLoteRequest.PedidoLoteItem item, int indice) {
        String codigoCliente = item.getCliente() != null ? item.getCliente() : ("c-" + (indice + 1));
        return "PEDIDO-" + item.getY() + "-" + item.getX() + "-" + codigoCliente;
    }

    /**
     * Dividir pedido grande desde el lote
     */
    private List<Pedido> dividirPedidoLote(Coordenada coordenada, double volumenTotal, double horasLimite,
                                          LocalDateTime fechaPedido, String codigoBase, String cliente) {
        List<Pedido> pedidosDivididos = new ArrayList<>();
        
        // Obtener capacidades de camiones
        List<Double> capacidadesCamiones = obtenerCapacidadesCamiones();
        if (capacidadesCamiones.isEmpty()) {
            throw new InvalidInputException("No hay camiones disponibles para dividir el pedido");
        }
        
        // Calcular división óptima
        List<Double> volumenePorPedido = calcularDivisionOptima(volumenTotal, capacidadesCamiones);
        
        // Crear pedidos divididos
        for (int i = 0; i < volumenePorPedido.size(); i++) {
            double volumenPedido = volumenePorPedido.get(i);
            String codigoCompleto = codigoBase + "-DIV" + (i + 1);
            
            Pedido pedido = Pedido.builder()
                    .coordenada(coordenada)
                    .bloqueado(false)
                    .gScore(0)
                    .fScore(0)
                    .tipoNodo(com.plg.entity.TipoNodo.PEDIDO)
                    .codigo(codigoCompleto)
                    .horasLimite(horasLimite)
                    .volumenGLPAsignado(volumenPedido)
                    .estado(com.plg.entity.EstadoPedido.REGISTRADO)
                    .fechaRegistro(fechaPedido)
                    .build();
            
            pedidosDivididos.add(pedidoRepository.save(pedido));
        }
        
        System.out.println("📦 Pedido dividido: " + volumenTotal + " m³ en " + pedidosDivididos.size() + " pedidos");
        return pedidosDivididos;
    }

    /**
     * Actualizar parámetros de simulación con la nueva fecha
     */
    private void actualizarParametrosSimulacion(LocalDateTime fechaInicio) {
        try {
            // Actualizar los parámetros globales del sistema
            com.plg.utils.Parametros.setFechaInicial(fechaInicio);
            System.out.println("📅 Parámetros de simulación actualizados con fecha: " + fechaInicio);
        } catch (Exception e) {
            System.err.println("⚠️ Error al actualizar parámetros de simulación: " + e.getMessage());
        }
    }

    /**
     * Sincronizar con DataLoader para que la simulación use los nuevos pedidos
     */
    private void sincronizarConDataLoader(List<Pedido> pedidosCreados) {
        try {
            // Actualizar la lista de pedidos en DataLoader para que la simulación los use
            if (DataLoader.pedidos == null) {
                DataLoader.pedidos = new ArrayList<>();
            } else {
                DataLoader.pedidos.clear();
            }
            DataLoader.pedidos.addAll(pedidosCreados);
            
            System.out.println("🔄 DataLoader sincronizado con " + pedidosCreados.size() + " pedidos");
        } catch (Exception e) {
            System.err.println("⚠️ Error al sincronizar con DataLoader: " + e.getMessage());
        }
    }
}
