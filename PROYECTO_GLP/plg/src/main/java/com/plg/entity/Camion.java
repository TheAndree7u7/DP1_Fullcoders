package com.plg.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    private String codigo;
    private String tipo; // TA, TB, TC, TD, etc.

    // GLP
    private double capacidad;              // Capacidad en m3 de GLP
    private double capacidadDisponible;    // Capacidad disponible actual (m3)
    private double tara;                   // Peso del camión vacío en toneladas
    private double pesoCarga;              // Peso actual de la carga en toneladas
    private double pesoCombinado;          // Peso total (tara + carga)

    private EstadoCamion estado;

    // Combustible
    private double capacidadTanque = 25.0;   // Capacidad del tanque en galones
    private double combustibleActual;        // Combustible actual en galones
    private double velocidadPromedio = 50.0; // Velocidad promedio en km/h

    // Posición actual del camión (para calcular distancia)
    private double posX;
    private double posY;

    // Último almacén visitado
    private Almacen ultimoAlmacen;

    // Fecha de la última carga de GLP
    private LocalDateTime fechaUltimaCarga;

    // Porcentaje de uso actual
    private double porcentajeUso;

    // Historiales
    private List<Mantenimiento> mantenimientos     = new ArrayList<>();
    private List<Averia>      averias             = new ArrayList<>();
    private List<Pedido>      pedidos             = new ArrayList<>();
    private List<EntregaParcial> entregasParciales = new ArrayList<>();

    /** Constructor con parámetros básicos */
    public Camion(String codigo, String tipo, double capacidad, double tara) {
        this.codigo               = codigo;
        this.tipo                 = tipo;
        this.capacidad            = capacidad;
        this.capacidadDisponible  = capacidad;
        this.tara                 = tara;
        this.estado               = EstadoCamion.DISPONIBLE;
        this.porcentajeUso        = 0.0;
        inicializar();
    }

    /** Asigna un volumen parcial de GLP de un pedido a este camión */
    public boolean asignarPedidoParcial(Pedido pedido, double volumen, double porcentaje) {
        if (capacidadDisponible < volumen) return false;

        capacidadDisponible -= volumen;
        actualizarPorcentajeUso();
        actualizarPeso();

        EntregaParcial entrega = new EntregaParcial();
        entrega.setCamion(this);
        entrega.setPedido(pedido);
        entrega.setVolumenGLP(volumen);
        entrega.setPorcentajePedido(porcentaje);
        entrega.setFechaAsignacion(LocalDateTime.now());
        entrega.setEstado(0); // Asignado

        entregasParciales.add(entrega);
        return true;
    }

    /** Libera capacidad después de una entrega */
    public void liberarCapacidad(double volumen) {
        capacidadDisponible += volumen;
        if (capacidadDisponible > capacidad) {
            capacidadDisponible = capacidad;
        }
        actualizarPorcentajeUso();
        actualizarPeso();
    }

    /** Recarga GLP */
    public void recargarGLP(double volumenGLP) {
        capacidadDisponible += volumenGLP;
        if (capacidadDisponible > capacidad) {
            capacidadDisponible = capacidad;
        }
        actualizarPorcentajeUso();
        actualizarPeso();
    }

    /** Recarga combustible */
    public void recargarCombustible(double cantidadGalones) {
        combustibleActual += cantidadGalones;
        if (combustibleActual > capacidadTanque) {
            combustibleActual = capacidadTanque;
        }
        if (estado == EstadoCamion.SIN_COMBUSTIBLE) {
            estado = EstadoCamion.DISPONIBLE;
        }
    }

    /** Consume combustible */
    public boolean consumirCombustible(double cantidadGalones) {
        if (combustibleActual < cantidadGalones) return false;

        combustibleActual -= cantidadGalones;
        if (combustibleActual <= 0.1) {
            estado = EstadoCamion.SIN_COMBUSTIBLE;
        }
        return true;
    }

    /** Obtiene las entregas parciales pendientes */
    public List<EntregaParcial> getEntregasPendientes() {
        List<EntregaParcial> pendientes = new ArrayList<>();
        for (EntregaParcial e : entregasParciales) {
            if (e.getEstado() != 2) pendientes.add(e); // 2 = entregado
        }
        return pendientes;
    }

    /** Volumen total de GLP actualmente asignado */
    public double getVolumenTotalAsignado() {
        return entregasParciales.stream()
                .filter(e -> e.getEstado() != 2)
                .mapToDouble(EntregaParcial::getVolumenGLP)
                .sum();
    }

    /** Completa una entrega parcial dada */
    public boolean completarEntregaParcial(Long pedidoId) {
        for (EntregaParcial e : entregasParciales) {
            if (e.getPedido().getId().equals(pedidoId) && e.getEstado() != 2) {
                e.setEstado(2); // Entregado
                e.setFechaEntrega(LocalDateTime.now());
                liberarCapacidad(e.getVolumenGLP());
                return true;
            }
        }
        return false;
    }

    /** Verifica capacidad adicional */
    public boolean tieneCapacidadPara(double volumenAdicional) {
        return capacidadDisponible >= volumenAdicional;
    }

    /** Calcula consumo de combustible para una distancia */
    public double calcularConsumoCombustible(double distanciaKm) {
        return distanciaKm * pesoCombinado / 180.0;
    }

    /** Distancia máxima con el combustible actual */
    public double calcularDistanciaMaxima() {
        if (pesoCombinado <= 0) return 0.0;
        return combustibleActual * 180.0 / pesoCombinado;
    }

    /** Inicializa valores por defecto */
    public void inicializar() {
        if (capacidadDisponible <= 0) capacidadDisponible = capacidad;
        if (combustibleActual   <= 0) combustibleActual   = capacidadTanque;
        actualizarPorcentajeUso();
        actualizarPeso();
    }

    /** Reporta una avería */
    public Averia reportarAveria(String descripcion) {
        Averia a = new Averia();
        a.setCamion(this);
        a.setDescripcion(descripcion);
        a.setFechaHoraReporte(LocalDateTime.now());
        a.setEstado(0); // Pendiente
        this.estado = EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA;
        averias.add(a);
        return a;
    }

    /** Mueve el camión a nuevas coordenadas */
    public void moverA(int nuevoX, int nuevoY) {
        posX = nuevoX;
        posY = nuevoY;
    }

    /** Calcula distancia Manhattan hasta un punto */
    public double calcularDistanciaHasta(int destinoX, int destinoY) {
        return Math.abs(destinoX - posX) + Math.abs(destinoY - posY);
    }

    /** Información básica para APIs */
    public Map<String, Object> getInfoBasica() {
        Map<String, Object> info = new HashMap<>();
        info.put("codigo", codigo);
        info.put("tipo", tipo);
        info.put("capacidad", capacidad);
        info.put("capacidadDisponible", capacidadDisponible);
        info.put("porcentajeUso", porcentajeUso);
        info.put("estado", estado);
        info.put("posX", posX);
        info.put("posY", posY);
        info.put("combustibleActual", combustibleActual);
        info.put("distanciaMaxima", calcularDistanciaMaxima());
        return info;
    }

    /** Detalle de entregas parciales para APIs */
    public List<Map<String, Object>> getInfoEntregasParciales() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (EntregaParcial e : entregasParciales) {
            Map<String, Object> m = new HashMap<>();
            m.put("pedidoId",      e.getPedido().getId());
            m.put("codigoPedido",  e.getPedido().getCodigo());
            m.put("volumenGLP",    e.getVolumenGLP());
            m.put("porcentaje",    e.getPorcentajePedido());
            m.put("estado",        e.getEstado());
            m.put("fechaAsignacion", e.getFechaAsignacion());
            m.put("fechaEntrega",    e.getFechaEntrega());
            lista.add(m);
        }
        return lista;
    }

    /** Actualiza el estado de las entregas cuando inicia la ruta */
    public void actualizarEstadoEntregasARuta() {
        for (EntregaParcial e : entregasParciales) {
            if (e.getEstado() == 0) {
                e.setEstado(1); // En ruta
            }
        }
    }

    // Métodos auxiliares privados
    private void actualizarPorcentajeUso() {
        porcentajeUso = ((capacidad - capacidadDisponible) / capacidad) * 100;
    }

    private void actualizarPeso() {
        // Peso del GLP ≈ 0.5 ton/m3
        pesoCarga     = (capacidad - capacidadDisponible) * 0.5;
        pesoCombinado = tara + pesoCarga;
    }
}
