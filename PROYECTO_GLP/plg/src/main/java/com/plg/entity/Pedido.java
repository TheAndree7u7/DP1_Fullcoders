package com.plg.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String codigo;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonBackReference(value="cliente-pedido")
    private Cliente cliente;
    private double posX; // Coordenada X del cliente
    private double posY; // Coordenada Y del cliente
    //horas limite
    private double horasLimite; // Hora límite para la entrega (en horas)
    //m3
    //fecha creacion
    // private LocalDateTime fechaCreacion; // Fecha de creación del pedido
    //fecha entrega
    private LocalDateTime fechaRegistro; // Fecha de entrega del pedido
    //m3
     
    // fecha pedido
    // private LocalDateTime fechaPedido;
    private LocalDateTime fechaEntregaRequerida;
    private LocalDateTime fechaEntregaReal;
    
    private double volumenGLPAsignado; // Volumen total requerido (m3)
    private double volumenGLPEntregado; // Volumen ya entregado (m3)
    private double volumenGLPPendiente; // Volumen restante por asignar (m3) 
    private int prioridad; // 1: alta, 2: media, 3: baja
    private int estado; // 0: registrado, 1: asignado, 2: en ruta, 3: entregado, 4: cancelado
 

    private String fechaHora; //formato "ddmmyyyy hh:mm:ss"
    private String fechaAsignaciones; //formato "ddmmyyyy hh:mm:ss" 
     
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-entregaparcial")
    private List<EntregaParcial> entregasParciales = new ArrayList<>();
    

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-nodo")
    private List<NodoRuta> nodos = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "camion_id")
    @JsonBackReference(value="camion-pedido")
    private Camion camion;
    //Asignaciones
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @JsonManagedReference(value="pedido-asignacion")
    private List<AsignacionCamion> asignaciones = new ArrayList<>();

    /**
     * Asigna una parte del pedido a un camión
     * @param camion Camión al que se asigna
     * @param volumen Volumen asignado en m3
     * @return true si se pudo asignar, false si no hay volumen pendiente suficiente
     */
    public boolean asignarACamion(Camion camion, double volumen) {
        // Validar que haya volumen pendiente y que el camión tenga capacidad
        if (volumenGLPAsignado - volumenGLPEntregado < volumen || !camion.tieneCapacidadPara(volumen)) {
            return false;
        }
        
        // Calcular el porcentaje que representa del total
        double porcentaje = (volumen / volumenGLPAsignado) * 100;
        
        // Crear y agregar la asignación
        AsignacionCamion asignacion = new AsignacionCamion(camion, volumen, porcentaje);
        asignacion.setPedido(this);
        
        // Actualizar los volúmenes
        volumenGLPEntregado += volumen;
        
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
        if (volumenEntregado > volumenGLPAsignado) {
            return false; // No puede entregar más de lo solicitado
        }
        
        // Actualizar volúmenes
        this.volumenGLPEntregado += volumenEntregado;
        
        // Liberar capacidad del camión
        camion.liberarCapacidad(volumenEntregado);
        
        // Actualizar estado del pedido
        actualizarEstado();
        
        // Si el pedido está completamente entregado, actualizar la fecha de entrega
        if (estado == 3) {
            this.fechaEntregaReal = fechaEntrega;
        }
        
        return true;
    }
    
    /**
     * Actualiza el estado del pedido según las entregas
     */
    private void actualizarEstado() {
        if (volumenGLPEntregado == 0) {
            estado = 0; // Registrado
        } else if (volumenGLPEntregado < volumenGLPAsignado) {
            estado = 1; // Asignado
        } else if (volumenGLPEntregado == volumenGLPAsignado) {
            estado = 3; // Entregado
        }
    }
    
    /**
     * Cancela el pedido, liberando capacidad del camión asignado
     */
    public void cancelar() {
        if (camion != null) {
            camion.liberarCapacidad(volumenGLPAsignado - volumenGLPEntregado);
        }
        
        estado = 4; // Cancelado
    }
    
    /**
     * Obtiene el porcentaje total entregado del pedido
     */
    public double getPorcentajeEntregado() {
        return (volumenGLPEntregado / volumenGLPAsignado) * 100;
    }
    
    /**
     * Verifica si el pedido está completamente entregado
     */
    public boolean isCompletamenteEntregado() {
        return Math.abs(volumenGLPEntregado - volumenGLPAsignado) < 0.01; // Comparación con tolerancia
    }
}