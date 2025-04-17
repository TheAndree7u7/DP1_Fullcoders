package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.plg.enums.EstadoCamion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "camiones")
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID único del camión

    private String codigo;
    
    private String tipo; // TA, TB, TC, TD, etc.

    //!GLP
    private double capacidad; // Capacidad en m3 de GLP
    private double capacidadDisponible; // Capacidad disponible actual (m3)
    private double tara; // Peso del camión vacío en toneladas
    private double pesoCarga; // Peso actual de la carga en toneladas
    private double pesoCombinado; // Peso total (tara + carga)
    
 
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoCamion estado; 
    
    //!combustible Atributos relacionados con 
    @Column(name = "capacidad_tanque")
    private double capacidadTanque = 25.0; // Capacidad del tanque en galones
    
    @Column(name = "combustible_actual")
    private double combustibleActual; // Combustible actual en galones
    
    @Column(name = "velocidad_promedio")
    private double velocidadPromedio = 50.0; // Velocidad promedio en km/h
    
    // Posición actual del camión (para calcular distancia a recorrer)
    @Column(name = "pos_x")
    private double posX;
    
    @Column(name = "pos_y")
    private double posY;
    
    // Último almacén visitado
    @ManyToOne
    @JoinColumn(name = "ultimo_almacen_id")
    private Almacen ultimoAlmacen;
    
    // Fecha de la última carga de GLP
    @Column(name = "fecha_ultima_carga")
    private LocalDateTime fechaUltimaCarga;
    
    // Porcentaje de uso actual
    @Column(name = "porcentaje_uso")
    private double porcentajeUso;
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-mantenimiento")
    private List<Mantenimiento> mantenimientos = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-averia")
    private List<Averia> averias = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-pedido")
    private List<Pedido> pedidos = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-ruta")
    private List<Ruta> rutas = new ArrayList<>();
    
    @OneToMany(mappedBy = "camion", cascade = CascadeType.ALL)
    @JsonManagedReference(value="camion-entregaparcial")
    private List<EntregaParcial> entregasParciales = new ArrayList<>();
    
    /**
     * Constructor con parámetros básicos
     */
    public Camion(String codigo, String tipo, double capacidad, double tara) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.capacidadDisponible = capacidad;
        this.tara = tara;
        this.setEstado(EstadoCamion.DISPONIBLE); // Disponible por defecto
        this.porcentajeUso = 0.0;
        inicializar();
    }
 
 
    private EstadoCamion mapIntToEstado(int estadoInt) {
        switch (estadoInt) {
            case 0: return EstadoCamion.DISPONIBLE;
            case 1: return EstadoCamion.EN_RUTA;
            case 2: return EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO;
            case 3: return EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA;
            case 4: return EstadoCamion.SIN_COMBUSTIBLE;
            default: return EstadoCamion.DISPONIBLE;
        }
    }
    
    private int mapEstadoToInt(EstadoCamion estado) {
        if (estado == EstadoCamion.DISPONIBLE) return 0;
        if (estado == EstadoCamion.EN_RUTA) return 1;
        if (estado == EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO) return 2;
        if (estado == EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA) return 3;
        if (estado == EstadoCamion.SIN_COMBUSTIBLE) return 4;
        return 0; // Por defecto disponible
    }
    
 

    /**
     * Asigna un volumen parcial de GLP de un pedido a este camión
     * @param pedido Pedido a asignar
     * @param volumen Volumen a entregar (en m3)
     * @param porcentaje Porcentaje del pedido que representa
     * @return true si se pudo asignar, false si no hay capacidad suficiente
     */
    public boolean asignarPedidoParcial(Pedido pedido, double volumen, double porcentaje) {
        // Verificar si hay capacidad disponible
        if (capacidadDisponible < volumen) {
            return false;
        }
        
        // Actualizar capacidad disponible
        capacidadDisponible -= volumen;
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso de carga y combinado
        actualizarPeso();
        
        // Crear nueva entrega parcial
        EntregaParcial entrega = new EntregaParcial();
        entrega.setCamion(this);
        entrega.setPedido(pedido);
        entrega.setVolumenGLP(volumen);
        entrega.setPorcentajePedido(porcentaje);
        entrega.setFechaAsignacion(LocalDateTime.now());
        entrega.setEstado(0); // Asignado
        
        // Agregar a la lista de entregas parciales
        entregasParciales.add(entrega);
        
        return true;
    }
    
    /**
     * Libera capacidad después de una entrega
     * @param volumen Volumen liberado (en m3)
     */
    public void liberarCapacidad(double volumen) {
        capacidadDisponible += volumen;
        if (capacidadDisponible > capacidad) {
            capacidadDisponible = capacidad;
        }
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso después de liberar capacidad
        actualizarPeso();
    }
    
    /**
     * Actualiza el porcentaje de uso
     */
    private void actualizarPorcentajeUso() {
        porcentajeUso = ((capacidad - capacidadDisponible) / capacidad) * 100;
    }
    
    /**
     * Actualiza el peso de carga y combinado
     * El peso del GLP es aproximadamente 0.55 ton/m3
     */
    private void actualizarPeso() {
        this.pesoCarga = (capacidad - capacidadDisponible) * 0.5; // Peso del GLP en toneladas
        this.pesoCombinado = tara + pesoCarga;
    }
    
    /**
     * Realiza una recarga   de GLP
     */
    public void recargarGLP(double volumenGLP) {
        capacidadDisponible += volumenGLP;
        if (capacidadDisponible > capacidad) {
            capacidadDisponible = capacidad;
        }
        
        // Actualizar porcentaje de uso
        actualizarPorcentajeUso();
        
        // Actualizar peso después de recargar
        actualizarPeso();
    }
    
    /**
     * Realiza una recarga de combustible
     * @param cantidadGalones Cantidad a recargar en galones
     */
    public void recargarCombustible(double cantidadGalones) {
        combustibleActual += cantidadGalones;
        if (combustibleActual > capacidadTanque) {
            combustibleActual = capacidadTanque;
        }
        
        // Si estaba sin combustible, actualizar su estado
        if (getEstado() == EstadoCamion.SIN_COMBUSTIBLE) {
            setEstado(EstadoCamion.DISPONIBLE);
        }
    }
    
    /**
     * Consume combustible durante un recorrido
     * @param cantidadGalones Cantidad a consumir en galones
     * @return true si se pudo consumir, false si no hay suficiente
     */
    public boolean consumirCombustible(double cantidadGalones) {
        if (combustibleActual < cantidadGalones) {
            return false;
        }
        
        combustibleActual -= cantidadGalones;
        
        // Si se quedó sin combustible, actualizar su estado
        if (combustibleActual <= 0.1) {
            setEstado(EstadoCamion.SIN_COMBUSTIBLE);
        }
        
        return true;
    }
    
    /**
     * Obtiene las entregas parciales pendientes
     */
    public List<EntregaParcial> getEntregasPendientes() {
        List<EntregaParcial> pendientes = new ArrayList<>();
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() != 2) { // No entregado
                pendientes.add(entrega);
            }
        }
        
        return pendientes;
    }
    
    /**
     * Obtiene el volumen total de GLP asignado actualmente
     */
    public double getVolumenTotalAsignado() {
        double total = 0.0;
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() != 2) { // No entregado
                total += entrega.getVolumenGLP();
            }
        }
        
        return total;
    }
    
    /**
     * Marca una entrega parcial como completada
     */
    public boolean completarEntregaParcial(Long pedidoId) {
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getPedido().getId().equals(pedidoId) && entrega.getEstado() != 2) {
                entrega.setEstado(2); // Entregado
                entrega.setFechaEntrega(LocalDateTime.now());
                liberarCapacidad(entrega.getVolumenGLP());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verifica si el camión tiene capacidad para un volumen adicional
     */
    public boolean tieneCapacidadPara(double volumenAdicional) {
        return capacidadDisponible >= volumenAdicional;
    }
    
    /**
     * Calcula consumo de combustible para una distancia
     * @param distanciaKm Distancia a recorrer en kilómetros
     * @return Consumo en galones
     */
    public double calcularConsumoCombustible(double distanciaKm) {
        return distanciaKm * pesoCombinado / 180.0;
    }
    
    /**
     * Calcula la distancia máxima que puede recorrer con el combustible actual
     * @return Distancia máxima en kilómetros
     */
    public double calcularDistanciaMaxima() {
        if (pesoCombinado <= 0) {
            return 0.0; // Evitar división por cero
        }
        return combustibleActual * 180.0 / pesoCombinado;
    }
    
    /**
     * Inicializa el camión con valores por defecto
     */
    public void inicializar() {
        if (capacidadDisponible <= 0) {
            capacidadDisponible = capacidad;
        }
        
        if (combustibleActual <= 0) {
            combustibleActual = capacidadTanque * 1; // Inicializa con 100% del tanque
        }
        
        actualizarPorcentajeUso();
        actualizarPeso();
    }
    
    /**
     * Reporta una avería y cambia el estado del camión
     */
    public Averia reportarAveria(String descripcion) {
        Averia averia = new Averia();
        averia.setCamion(this);
        averia.setDescripcion(descripcion);
        averia.setFechaHoraReporte(LocalDateTime.now());
        averia.setEstado(0); // Pendiente
        
        this.setEstado(EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA); // Averiado
        
        if (this.averias == null) {
            this.averias = new ArrayList<>();
        }
        this.averias.add(averia);
        
        return averia;
    }
    
 
    /**
     * Mover el camión a nuevas coordenadas
     */
    public void moverA(int nuevoX, int nuevoY) {
        this.posX = nuevoX;
        this.posY = nuevoY;
    }
    
    /**
     * Calcula la distancia desde la posición actual hasta un punto
     * usando distancia Manhattan
     */
    public double calcularDistanciaHasta(int destinoX, int destinoY) {
        return Math.abs(destinoX - this.posX) + Math.abs(destinoY - this.posY);
    }
    
    /**
     * Obtiene el estado del camión como texto
     */
    @Transient
    public String getEstadoTexto() {
        return getEstado().getDescripcion();
    }
    
    /**
     * Actualiza el estado de las entregas parciales cuando la ruta está en curso
     */
    public void actualizarEstadoEntregasARuta() {
        for (EntregaParcial entrega : entregasParciales) {
            if (entrega.getEstado() == 0) { // Si está asignada
                entrega.setEstado(1); // Cambiar a "En ruta"
            }
        }
    }
    
    /**
     * Obtiene información básica del camión para APIs
     */
    @Transient
    public Map<String, Object> getInfoBasica() {
        Map<String, Object> info = new HashMap<>();
        info.put("codigo", this.codigo);
        info.put("tipo", this.tipo);
        info.put("capacidad", this.capacidad);
        info.put("capacidadDisponible", this.capacidadDisponible);
        info.put("porcentajeUso", this.porcentajeUso); 
        info.put("estado", this.estado);
        info.put("estadoTexto", this.getEstadoTexto());
        info.put("posX", this.posX);
        info.put("posY", this.posY);
        info.put("combustibleActual", this.combustibleActual);
        info.put("distanciaMaxima", this.calcularDistanciaMaxima());
        return info;
    }
    
    /**
     * Obtiene información detallada de las entregas parciales para APIs
     */
    @Transient
    public List<Map<String, Object>> getInfoEntregasParciales() {
        List<Map<String, Object>> listaEntregas = new ArrayList<>();
        
        for (EntregaParcial entrega : entregasParciales) {
            Map<String, Object> infoEntrega = new HashMap<>();
            infoEntrega.put("id", entrega.getId());
            infoEntrega.put("pedidoId", entrega.getPedido().getId());
            infoEntrega.put("codigoPedido", entrega.getPedido().getCodigo());
            infoEntrega.put("volumenGLP", entrega.getVolumenGLP());
            infoEntrega.put("porcentaje", entrega.getPorcentajePedido());
            infoEntrega.put("estado", entrega.getEstado());
            
            switch (entrega.getEstado()) {
                case 0:
                    infoEntrega.put("estadoTexto", "Asignado");
                    break;
                case 1:
                    infoEntrega.put("estadoTexto", "En ruta");
                    break;
                case 2:
                    infoEntrega.put("estadoTexto", "Entregado");
                    break;
                case 3:
                    infoEntrega.put("estadoTexto", "Cancelado");
                    break;
                default:
                    infoEntrega.put("estadoTexto", "Desconocido");
            }
            
            if (entrega.getFechaAsignacion() != null) {
                infoEntrega.put("fechaAsignacion", entrega.getFechaAsignacion().toString());
            }
            
            if (entrega.getFechaEntrega() != null) {
                infoEntrega.put("fechaEntrega", entrega.getFechaEntrega().toString());
            }
            
            listaEntregas.add(infoEntrega);
        }
        
        return listaEntregas;
    }
}