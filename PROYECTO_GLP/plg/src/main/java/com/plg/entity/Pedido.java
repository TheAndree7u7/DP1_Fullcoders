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
    private int posX; // Coordenada X del cliente
    private int posY; // Coordenada Y del cliente
    //horas limite
    private int horasLimite; // Hora límite para la entrega (en horas)
    //m3
    //fecha creacion
    private LocalDateTime fechaCreacion; // Fecha de creación del pedido
    //fecha entrega
    private LocalDateTime fechaEntrega; // Fecha de entrega del pedido
    //m3
    private double m3; // Volumen total requerido (m3)
    // fecha pedido
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaEntregaRequerida;
    private LocalDateTime fechaEntregaReal;
    
    private double volumenGLP; // Volumen total requerido (m3)
    private double volumenEntregado; // Volumen ya entregado (m3)
    
    private int prioridad; // 1: alta, 2: media, 3: baja
    private int estado; // 0: registrado, 1: asignado, 2: en ruta, 3: entregado, 4: cancelado
    //!m3
    private double m3Asignados; // Volumen asignado al camión (m3)
    private double m3Pendientes; // Volumen restante por asignar
    private double m3Entregados; // Volumen entregado (m3)
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
        if (volumenGLP - volumenEntregado < volumen || !camion.tieneCapacidadPara(volumen)) {
            return false;
        }
        
        // Calcular el porcentaje que representa del total
        double porcentaje = (volumen / volumenGLP) * 100;
        
        // Crear y agregar la asignación
        AsignacionCamion asignacion = new AsignacionCamion(camion, volumen, porcentaje);
        asignacion.setPedido(this);
        
        // Actualizar los volúmenes
        volumenEntregado += volumen;
        
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
        if (volumenEntregado > volumenGLP) {
            return false; // No puede entregar más de lo solicitado
        }
        
        // Actualizar volúmenes
        this.volumenEntregado += volumenEntregado;
        
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
        if (volumenEntregado == 0) {
            estado = 0; // Registrado
        } else if (volumenEntregado < volumenGLP) {
            estado = 1; // Asignado
        } else if (volumenEntregado == volumenGLP) {
            estado = 3; // Entregado
        }
    }
    
    /**
     * Cancela el pedido, liberando capacidad del camión asignado
     */
    public void cancelar() {
        if (camion != null) {
            camion.liberarCapacidad(volumenGLP - volumenEntregado);
        }
        
        estado = 4; // Cancelado
    }
    
    /**
     * Obtiene el porcentaje total entregado del pedido
     */
    public double getPorcentajeEntregado() {
        return (volumenEntregado / volumenGLP) * 100;
    }
    
    /**
     * Verifica si el pedido está completamente entregado
     */
    public boolean isCompletamenteEntregado() {
        return Math.abs(volumenEntregado - volumenGLP) < 0.01; // Comparación con tolerancia
    }
}