package com.plg.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una ruta en el sistema.
 * Una ruta consiste en una secuencia ordenada de nodos que forman un camino
 * desde un origen hasta un destino, pasando por puntos intermedios.
 * 
 * La ruta permite gestionar entregas parciales de GLP a cada cliente,
 * permitiendo que un pedido pueda ser atendido por diferentes camiones
 * con porcentajes variables de la cantidad total solicitada.
 */
@Entity
@Table(name = "rutas")
@Getter
@Setter
@NoArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true)
    private String codigo;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-ruta")
    private Camion camion;

    @Column(name = "estado")
    private int estado; // 0: Planificada, 1: En curso, 2: Completada, 3: Cancelada

    @Column(name = "distancia_total")
    private double distanciaTotal;
    
    @Column(name = "tiempo_estimado")
    private int tiempoEstimadoMinutos;
    
    @Column(name = "considera_bloqueos")
    private boolean consideraBloqueos;
    
    @Column(name = "volumen_total_glp")
    private double volumenTotalGLP;
    
    @Column(name = "capacidad_utilizada_porcentaje")
    private double capacidadUtilizadaPorcentaje;
    
    @Column(name = "fecha_inicio_ruta")
    private LocalDateTime fechaInicioRuta;
    
    @Column(name = "fecha_fin_ruta")
    private LocalDateTime fechaFinRuta;
    
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "orden")
    @JsonManagedReference(value="ruta-nodo")
    private List<NodoRuta> nodos = new ArrayList<>();
    
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL)
    @JsonManagedReference(value="ruta-asignacion")
    private List<AsignacionCamion> asignaciones = new ArrayList<>();
    
    // Para almacenar IDs de bloqueos que afectan a esta ruta
    @Column(name = "bloqueos_ids", length = 255)
    private String bloqueosIds;
    
    /**
     * Constructor con código
     */
    public Ruta(String codigo) {
        this.codigo = codigo;
        this.fechaCreacion = LocalDateTime.now();
        this.estado = 0; // Planificada por defecto
        this.volumenTotalGLP = 0.0;
        this.capacidadUtilizadaPorcentaje = 0.0;
        this.consideraBloqueos = true; // Por defecto consideramos bloqueos
    }
    
    /**
     * Añade un nodo a la ruta
     */
    public void agregarNodo(int posX, int posY, String tipo) {
        NodoRuta nodo = new NodoRuta(posX, posY, tipo);
        nodo.setRuta(this);
        nodo.setOrden(nodos.size());
        nodos.add(nodo);
    }
    
    /**
     * Añade un nodo a la ruta con un pedido asociado y una entrega parcial
     */
    public void agregarNodoCliente(int posX, int posY, Pedido pedido, double volumenGLP, double porcentajePedido) {
        NodoRuta nodo = new NodoRuta(posX, posY, "CLIENTE");
        nodo.setRuta(this);
        nodo.setOrden(nodos.size());
        nodo.setPedido(pedido);
        nodo.setVolumenGLP(volumenGLP);
        nodo.setPorcentajePedido(porcentajePedido);
        nodos.add(nodo);
        
        // Actualizar el volumen total de GLP de la ruta
        this.volumenTotalGLP += volumenGLP;
        
        // Actualizar porcentaje de capacidad utilizada del camión
        actualizarCapacidadUtilizada();
    }
    
    /**
     * Método sobrecargado para mantener compatibilidad con código existente
     */
    public void agregarNodoCliente(int posX, int posY, Pedido pedido) {
        // Si no especificamos volumen ni porcentaje, asumimos que se entrega el pedido completo (100%)
        agregarNodoCliente(posX, posY, pedido, pedido.getM3(), 100.0);
    }
    
    /**
     * Actualiza la información de entrega de un nodo específico
     */
    public boolean actualizarEntregaPedido(Long pedidoId, double nuevoVolumenGLP, double nuevoPorcentaje) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null && nodo.getPedido().getId().equals(pedidoId)) {
                // Restar el volumen antiguo del total
                this.volumenTotalGLP -= nodo.getVolumenGLP();
                
                // Actualizar nodo
                nodo.setVolumenGLP(nuevoVolumenGLP);
                nodo.setPorcentajePedido(nuevoPorcentaje);
                
                // Actualizar el total
                this.volumenTotalGLP += nuevoVolumenGLP;
                
                // Recalcular el porcentaje de capacidad utilizada
                actualizarCapacidadUtilizada();
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Registra la entrega de un pedido
     */
    public boolean marcarPedidoComoEntregado(Long pedidoId, LocalDateTime fechaEntrega, String observaciones) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null && nodo.getPedido().getId().equals(pedidoId)) {
                nodo.setEntregado(true);
                nodo.setTiempoLlegadaReal(fechaEntrega);
                nodo.setObservaciones(observaciones);
                
                // Si el camión está presente, liberamos su capacidad
                if (camion != null) {
                    camion.liberarCapacidad(nodo.getVolumenGLP());
                    // Actualizar EntregaParcial relacionada
                    camion.completarEntregaParcial(pedidoId);
                }
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Actualiza el porcentaje de capacidad utilizada del camión
     */
    private void actualizarCapacidadUtilizada() {
        if (camion != null && camion.getCapacidad() > 0) {
            this.capacidadUtilizadaPorcentaje = (this.volumenTotalGLP / camion.getCapacidad()) * 100;
        }
    }
    
    /**
     * Verifica si el camión tiene capacidad suficiente para la cantidad de GLP asignada
     */
    public boolean verificarCapacidadSuficiente() {
        if (camion == null) return false;
        return camion.getCapacidad() >= volumenTotalGLP;
    }
    
    /**
     * Obtiene las entregas pendientes de esta ruta
     */
    @Transient
    public List<NodoRuta> getEntregasPendientes() {
        return nodos.stream()
            .filter(n -> n.getPedido() != null && !n.isEntregado())
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene información agrupada por pedido, incluyendo las entregas parciales
     */
    @Transient
    public List<Map<String, Object>> getInfoEntregasPorPedido() {
        Map<Long, Map<String, Object>> infoPorPedido = new HashMap<>();
        
        for (NodoRuta nodo : nodos) {
            if (nodo.getPedido() != null) {
                Long pedidoId = nodo.getPedido().getId();
                
                // Si no existe entrada para este pedido, crear una nueva
                if (!infoPorPedido.containsKey(pedidoId)) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("pedidoId", pedidoId);
                    info.put("codigoPedido", nodo.getPedido().getCodigo());
                    info.put("volumenTotalPedido", nodo.getPedido().getM3());
                    info.put("volumenAsignado", 0.0);
                    info.put("porcentajeAsignado", 0.0);
                    info.put("entregas", new ArrayList<Map<String, Object>>());
                    
                    infoPorPedido.put(pedidoId, info);
                }
                
                // Actualizar información del pedido
                Map<String, Object> info = infoPorPedido.get(pedidoId);
                double volumenAsignado = (double) info.get("volumenAsignado") + nodo.getVolumenGLP();
                double porcentajeAsignado = (double) info.get("porcentajeAsignado") + nodo.getPorcentajePedido();
                
                info.put("volumenAsignado", volumenAsignado);
                info.put("porcentajeAsignado", porcentajeAsignado);
                
                // Agregar información de esta entrega específica
                Map<String, Object> infoEntrega = new HashMap<>();
                infoEntrega.put("nodoId", nodo.getId());
                infoEntrega.put("posX", nodo.getPosX());
                infoEntrega.put("posY", nodo.getPosY());
                infoEntrega.put("volumenGLP", nodo.getVolumenGLP());
                infoEntrega.put("porcentaje", nodo.getPorcentajePedido());
                infoEntrega.put("entregado", nodo.isEntregado());
                
                ((List<Map<String, Object>>) info.get("entregas")).add(infoEntrega);
            }
        }
        
        return new ArrayList<>(infoPorPedido.values());
    }
    
    /**
     * Obtiene la ruta como array bidimensional para algoritmos de navegación
     */
    @Transient
    public int[][] obtenerRutaComoArray() {
        int[][] rutaArray = new int[nodos.size()][2];
        for (int i = 0; i < nodos.size(); i++) {
            NodoRuta nodo = nodos.get(i);
            rutaArray[i][0] = nodo.getPosX();
            rutaArray[i][1] = nodo.getPosY();
        }
        return rutaArray;
    }
    
    /**
     * Calcula la distancia total de la ruta basada en distancia Manhattan
     * (adecuada para un mapa reticular)
     */
    public void calcularDistanciaTotal() {
        if (nodos.size() < 2) {
            this.distanciaTotal = 0;
            return;
        }
        
        double distancia = 0;
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            // Distancia Manhattan: |x2-x1| + |y2-y1|
            distancia += Math.abs(nodoSiguiente.getPosX() - nodoActual.getPosX()) +
                         Math.abs(nodoSiguiente.getPosY() - nodoActual.getPosY());
        }
        
        this.distanciaTotal = distancia;
    }
    
    /**
     * Estima los tiempos de llegada para cada nodo
     * @param velocidadKmPorHora Velocidad promedio del vehículo en km/h
     * @param tiempoInicio Tiempo de inicio del recorrido
     */
    public void estimarTiemposLlegada(double velocidadKmPorHora, LocalDateTime tiempoInicio) {
        if (nodos.isEmpty()) return;
        
        double velocidadKmPorMinuto = velocidadKmPorHora / 60.0;
        LocalDateTime tiempoActual = tiempoInicio;
        
        // Para el primer nodo (origen), el tiempo estimado es el tiempo de inicio
        nodos.get(0).setTiempoLlegadaEstimado(tiempoActual);
        
        // Para los nodos siguientes, calcular en base a la distancia
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            // Calcular distancia Manhattan entre los nodos
            double distanciaTramo = Math.abs(nodoSiguiente.getPosX() - nodoActual.getPosX()) +
                                    Math.abs(nodoSiguiente.getPosY() - nodoActual.getPosY());
            
            // Calcular tiempo en minutos para recorrer el tramo
            double tiempoTramoMinutos = distanciaTramo / velocidadKmPorMinuto;
            
            // Actualizar tiempo actual
            tiempoActual = tiempoActual.plusMinutes((long)tiempoTramoMinutos);
            
            // Establecer tiempo estimado de llegada al nodo siguiente
            nodoSiguiente.setTiempoLlegadaEstimado(tiempoActual);
        }
        
        // Actualizar tiempo estimado total en minutos
        if (nodos.size() > 1) {
            LocalDateTime tiempoFinal = nodos.get(nodos.size() - 1).getTiempoLlegadaEstimado();
            long minutosTotal = java.time.Duration.between(tiempoInicio, tiempoFinal).toMinutes();
            this.tiempoEstimadoMinutos = (int) minutosTotal;
        }
    }
    
    /**
     * Registra el tiempo real de llegada a un nodo
     */
    public void registrarLlegadaReal(int indiceNodo, LocalDateTime tiempoLlegada) {
        if (indiceNodo >= 0 && indiceNodo < nodos.size()) {
            nodos.get(indiceNodo).setTiempoLlegadaReal(tiempoLlegada);
        }
    }
    
    /**
     * Verifica si la ruta pasa por un punto específico
     */
    public boolean pasaPorPunto(int x, int y) {
        for (NodoRuta nodo : nodos) {
            if (nodo.getPosX() == x && nodo.getPosY() == y) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene los pedidos asociados a esta ruta
     */
    @Transient
    public List<Pedido> getPedidosAsociados() {
        return nodos.stream()
            .filter(n -> n.getPedido() != null)
            .map(NodoRuta::getPedido)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Verifica si un tramo de la ruta cruza un bloqueo
     */
    public boolean verificarInterseccionConBloqueos(List<Bloqueo> bloqueos) {
        if (nodos.size() < 2 || bloqueos == null || bloqueos.isEmpty()) {
            return false;
        }
        
        // Lista para almacenar los IDs de los bloqueos que afectan la ruta
        List<Long> idsBloqueos = new ArrayList<>();
        
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoRuta nodoActual = nodos.get(i);
            NodoRuta nodoSiguiente = nodos.get(i + 1);
            
            for (Bloqueo bloqueo : bloqueos) {
                if (bloqueo.isActivo() && intersectaBloqueo(
                        nodoActual.getPosX(), nodoActual.getPosY(),
                        nodoSiguiente.getPosX(), nodoSiguiente.getPosY(), bloqueo)) {
                    idsBloqueos.add(bloqueo.getId());
                }
            }
        }
        
        // Si encontramos bloqueos, almacenar sus IDs
        if (!idsBloqueos.isEmpty()) {
            this.bloqueosIds = idsBloqueos.stream()
                    .distinct()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            return true;
        }
        
        return false;
    }
     
    /**
     * Verifica si un segmento de ruta intersecta con un bloqueo
     */
    private boolean intersectaBloqueo(int x1, int y1, int x2, int y2, Bloqueo bloqueo) {
        try {
            // Primero intentamos usar el método específico del bloqueo
            return bloqueo.intersectaConSegmento(x1, y1, x2, y2);
        } catch (Exception e) {
            // Implementación alternativa si hay error
            // Calculamos los puntos intermedios del segmento
            List<int[]> puntosIntermedios = generarPuntosIntermedios(x1, y1, x2, y2);
            
            // Verificamos si algún punto intermedio está en el bloqueo
            for (int[] punto : puntosIntermedios) {
                if (bloqueo.contienePunto(punto[0], punto[1])) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    /**
     * Genera una lista de puntos intermedios entre dos puntos (incluidos los extremos)
     */
    private List<int[]> generarPuntosIntermedios(int x1, int y1, int x2, int y2) {
        List<int[]> puntos = new ArrayList<>();
        
        // Añadir el punto inicial
        puntos.add(new int[]{x1, y1});
        
        // Si los puntos son iguales, no hay más que añadir
        if (x1 == x2 && y1 == y2) {
            return puntos;
        }
        
        // Calcular dirección del movimiento
        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);
        
        // Generar puntos intermedios
        int x = x1;
        int y = y1;
        
        while (x != x2 || y != y2) {
            // Si aún no hemos llegado al destino en X, movernos
            if (x != x2) {
                x += dx;
            }
            
            // Si aún no hemos llegado al destino en Y, movernos
            if (y != y2) {
                y += dy;
            }
            
            // Añadir el punto generado
            puntos.add(new int[]{x, y});
        }
        
        return puntos;
    }
    
    /**
     * Obtiene la lista de IDs de bloqueos que afectan a esta ruta
     */
    @Transient
    public List<Long> getBloqueoIdsComoLista() {
        if (bloqueosIds == null || bloqueosIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return java.util.Arrays.stream(bloqueosIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte la ruta a una representación para APIs REST
     */
    @Transient
    public List<Map<String, Object>> convertirARutaAPI() {
        List<Map<String, Object>> rutaAPI = new ArrayList<>();
        
        for (NodoRuta nodo : nodos) {
            Map<String, Object> punto = new HashMap<>();
            punto.put("x", nodo.getPosX());
            punto.put("y", nodo.getPosY());
            punto.put("tipo", nodo.getTipo());
            
            if (nodo.getPedido() != null) {
                punto.put("pedidoId", nodo.getPedido().getId());
                punto.put("volumenGLP", nodo.getVolumenGLP());
                punto.put("porcentajePedido", nodo.getPorcentajePedido());
                punto.put("entregado", nodo.isEntregado());
            }
            
            if (nodo.getTiempoLlegadaEstimado() != null) {
                punto.put("tiempoEstimado", nodo.getTiempoLlegadaEstimado().toString());
            }
            
            if (nodo.getTiempoLlegadaReal() != null) {
                punto.put("tiempoReal", nodo.getTiempoLlegadaReal().toString());
            }
            
            if (nodo.getObservaciones() != null && !nodo.getObservaciones().isEmpty()) {
                punto.put("observaciones", nodo.getObservaciones());
            }
            
            rutaAPI.add(punto);
        }
        
        return rutaAPI;
    }
    
    /**
     * Iniciar la ruta, cambiando su estado y registrando la fecha de inicio
     */
    public void iniciarRuta() {
        this.estado = 1; // En curso
        this.fechaInicioRuta = LocalDateTime.now();
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(1); // En ruta
            this.camion.actualizarEstadoEntregasARuta();
        }
    }
    
    /**
     * Completar la ruta, cambiando su estado y registrando la fecha de fin
     */
    public void completarRuta() {
        this.estado = 2; // Completada
        this.fechaFinRuta = LocalDateTime.now();
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(0); // Disponible
        }
    }
    
    /**
     * Cancelar la ruta
     */
    public void cancelarRuta(String motivo) {
        this.estado = 3; // Cancelada
        
        // Si hay camión asignado, actualizar su estado
        if (this.camion != null) {
            this.camion.setEstado(0); // Disponible
        }
    }
    
    /**
     * Obtiene el estado de la ruta como texto
     */
    @Transient
    public String getEstadoTexto() {
        switch (this.estado) {
            case 0: return "Planificada";
            case 1: return "En curso";
            case 2: return "Completada";
            case 3: return "Cancelada";
            default: return "Desconocido";
        }
    }
    
    /**
     * Actualiza el camión asignado a la ruta
     */
    public void setCamion(Camion camion) {
        this.camion = camion;
        actualizarCapacidadUtilizada();
    }
    
    /**
     * Genera información resumida de la ruta para APIs
     */
    @Transient
    public Map<String, Object> getResumenRuta() {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("id", this.id);
        resumen.put("codigo", this.codigo);
        resumen.put("fechaCreacion", this.fechaCreacion);
        resumen.put("estado", this.estado);
        resumen.put("estadoTexto", this.getEstadoTexto());
        resumen.put("distanciaTotal", this.distanciaTotal);
        resumen.put("tiempoEstimadoMinutos", this.tiempoEstimadoMinutos);
        resumen.put("volumenTotalGLP", this.volumenTotalGLP);
        
        if (this.camion != null) {
            resumen.put("camionCodigo", this.camion.getCodigo());
            resumen.put("capacidadUtilizadaPorcentaje", this.capacidadUtilizadaPorcentaje);
        }
        
        resumen.put("cantidadNodos", this.nodos.size());
        resumen.put("cantidadEntregas", this.getEntregasPendientes().size());
        
        if (this.fechaInicioRuta != null) {
            resumen.put("fechaInicioRuta", this.fechaInicioRuta.toString());
        }
        
        if (this.fechaFinRuta != null) {
            resumen.put("fechaFinRuta", this.fechaFinRuta.toString());
        }
        
        if (this.bloqueosIds != null && !this.bloqueosIds.isEmpty()) {
            resumen.put("tieneBloqueos", true);
            resumen.put("bloqueosIds", this.getBloqueoIdsComoLista());
        } else {
            resumen.put("tieneBloqueos", false);
        }
        
        return resumen;
    }
    
    /**
     * Verifica si la ruta tiene bloqueos activos para la hora actual
     */
    public boolean tieneBloqueoActivo(List<Bloqueo> bloqueos) {
        LocalDateTime ahora = LocalDateTime.now();
        
        // Obtener la lista de IDs de bloqueos que afectan a esta ruta
        List<Long> idsBloqueos = getBloqueoIdsComoLista();
        
        if (idsBloqueos.isEmpty() || bloqueos == null) {
            return false;
        }
        
        // Filtrar los bloqueos por ID y verificar si están activos en este momento
        return bloqueos.stream()
            .filter(b -> idsBloqueos.contains(b.getId()))
            .anyMatch(b -> b.isActivo() && 
                    ahora.isAfter(b.getFechaInicio()) && 
                    ahora.isBefore(b.getFechaFin()));
    }
}