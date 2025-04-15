package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo;
    private String fechaHora; // formato: 11d13h31m
    private int posX;
    private int posY;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    private int m3; // Volumen total de GLP solicitado
    private double m3Pendientes; // Volumen pendiente de asignar
    private double m3Asignados; // Volumen asignado a camiones
    private double m3Entregados; // Volumen ya entregado
    
    private int horasLimite;
    private int estado; // 0: pendiente, 1: parcialmente asignado, 2: totalmente asignado, 3: en ruta, 4: parcialmente entregado, 5: entregado, 6: cancelado
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    @JsonBackReference(value="camion-pedido")
    private Camion camion; // Para compatibilidad con código anterior (ahora puede ser null si hay múltiples camiones)
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEntrega; // Fecha de entrega completa
    
    // Lista de asignaciones a camiones
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<AsignacionCamion> asignaciones;
    
    /**
     * Asigna una parte del pedido a un camión
     * @param camion Camión al que se asigna
     * @param volumen Volumen asignado en m3
     * @return true si se pudo asignar, false si no hay volumen pendiente suficiente
     */
    public boolean asignarACamion(Camion camion, double volumen) {
        // Validar que haya volumen pendiente y que el camión tenga capacidad
        if (m3Pendientes < volumen || !camion.tieneCapacidadPara(volumen)) {
            return false;
        }
        
        // Calcular el porcentaje que representa del total
        double porcentaje = (volumen / m3) * 100;
        
        // Inicializar la lista de asignaciones si es necesario
        if (asignaciones == null) {
            asignaciones = new ArrayList<>();
        }
        
        // Crear y agregar la asignación
        AsignacionCamion asignacion = new AsignacionCamion(camion, volumen, porcentaje);
        asignacion.setPedido(this);
        asignaciones.add(asignacion);
        
        // Actualizar los volúmenes
        m3Pendientes -= volumen;
        m3Asignados += volumen;
        
        // Asignar el volumen al camión
        camion.asignarPedidoParcial(this, volumen, porcentaje);
        
        // Actualizar estado del pedido
        actualizarEstado();
        
        return true;
    }
    
    /**
     * Registra la entrega de una parte del pedido por un camión
     */
    public boolean registrarEntregaParcial(String codigoCamion, double volumenEntregado, LocalDateTime fechaEntrega) {
        if (asignaciones == null || asignaciones.isEmpty()) {
            return false;
        }
        
        // Buscar la asignación correspondiente
        for (AsignacionCamion asignacion : asignaciones) {
            if (asignacion.getCamion().getCodigo().equals(codigoCamion) && !asignacion.isEntregado()) {
                // Validar volumen
                if (volumenEntregado > asignacion.getVolumenAsignado()) {
                    return false; // No puede entregar más de lo asignado
                }
                
                // Marcar como entregado
                asignacion.setEntregado(true);
                asignacion.setFechaEntregaParcial(fechaEntrega);
                
                // Actualizar volúmenes
                m3Entregados += volumenEntregado;
                
                // Si entregó menos de lo asignado, ajustar
                if (volumenEntregado < asignacion.getVolumenAsignado()) {
                    double diferencia = asignacion.getVolumenAsignado() - volumenEntregado;
                    m3Asignados -= diferencia;
                    m3Pendientes += diferencia; // Devolver al pendiente
                    
                    // Actualizar la asignación
                    asignacion.setVolumenAsignado(volumenEntregado);
                    asignacion.setPorcentajeAsignado((volumenEntregado / m3) * 100);
                }
                
                // Liberar capacidad del camión
                asignacion.getCamion().liberarCapacidad(volumenEntregado);
                
                // Actualizar estado del pedido
                actualizarEstado();
                
                // Si el pedido está completamente entregado, actualizar la fecha de entrega
                if (estado == 5) {
                    this.fechaEntrega = fechaEntrega;
                }
                
                return true;
            }
        }
        
        return false; // No se encontró la asignación
    }
    
    /**
     * Actualiza el estado del pedido según las asignaciones y entregas
     */
    private void actualizarEstado() {
        if (m3Pendientes == m3) {
            estado = 0; // Pendiente
        } else if (m3Pendientes > 0 && m3Asignados > 0) {
            estado = 1; // Parcialmente asignado
        } else if (m3Pendientes == 0 && m3Entregados == 0) {
            estado = 2; // Totalmente asignado
            
            // Verificar si alguna ruta está en curso
            if (asignaciones != null) {
                for (AsignacionCamion asignacion : asignaciones) {
                    if (asignacion.getRuta() != null && asignacion.getRuta().getEstado() == 1) {
                        estado = 3; // En ruta
                        break;
                    }
                }
            }
        } else if (m3Entregados > 0 && m3Entregados < m3) {
            estado = 4; // Parcialmente entregado
        } else if (Math.abs(m3Entregados - m3) < 0.01) { // Comparación con tolerancia para evitar problemas de precisión
            estado = 5; // Entregado
        }
    }
    
    /**
     * Cancela el pedido, liberando capacidad de los camiones asignados
     */
    public void cancelar() {
        if (asignaciones != null) {
            for (AsignacionCamion asignacion : asignaciones) {
                if (!asignacion.isEntregado()) {
                    asignacion.getCamion().liberarCapacidad(asignacion.getVolumenAsignado());
                }
            }
        }
        
        estado = 6; // Cancelado
    }
    
    /**
     * Obtiene el porcentaje total asignado del pedido
     */
    public double getPorcentajeAsignado() {
        return (m3Asignados / m3) * 100;
    }
    
    /**
     * Obtiene el porcentaje total entregado del pedido
     */
    public double getPorcentajeEntregado() {
        return (m3Entregados / m3) * 100;
    }
    
    /**
     * Verifica si el pedido está completamente asignado
     */
    public boolean isCompletamenteAsignado() {
        return m3Pendientes < 0.01; // Casi cero, con tolerancia
    }
    
    /**
     * Verifica si el pedido está completamente entregado
     */
    public boolean isCompletamenteEntregado() {
        return Math.abs(m3Entregados - m3) < 0.01; // Comparación con tolerancia
    }
    
    /**
     * Obtiene una lista de los códigos de camiones asignados
     */
    public List<String> getCodigosCamionesAsignados() {
        if (asignaciones == null) {
            return new ArrayList<>();
        }
        
        return asignaciones.stream()
            .map(a -> a.getCamion().getCodigo())
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene un resumen de las asignaciones para APIs
     */
    public List<Object> getResumenAsignaciones() {
        if (asignaciones == null) {
            return new ArrayList<>();
        }
        
        return asignaciones.stream().map(a -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("camionCodigo", a.getCamion().getCodigo());
            map.put("volumenAsignado", a.getVolumenAsignado());
            map.put("porcentajeAsignado", a.getPorcentajeAsignado());
            map.put("entregado", a.isEntregado());
            if (a.getFechaEntregaParcial() != null) {
                map.put("fechaEntrega", a.getFechaEntregaParcial().toString());
            }
            if (a.getRuta() != null) {
                map.put("rutaCodigo", a.getRuta().getCodigo());
            }
            return map;
        }).collect(Collectors.toList());
    }
}