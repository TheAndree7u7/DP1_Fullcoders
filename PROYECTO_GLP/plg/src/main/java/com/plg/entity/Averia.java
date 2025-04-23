package com.plg.entity;

import java.time.LocalDateTime;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Averia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "camion_codigo")
    @JsonBackReference(value="camion-averia")
    private Camion camion;
    
    private LocalDateTime fechaHoraReporte;
    private String descripcion;
    private String turno; // T1, T2, T3
    private String tipoIncidente; // TI1, TI2, TI3
    private double posX;
    private double posY;
    private double kilometroOcurrencia; // Punto del trayecto donde ocurre la avería
    private int estado; // 0: reportada, 1: atendida, 2: reparada
    private boolean conCarga; // Indica si el camión llevaba carga cuando ocurrió la avería
    
    // Campos adicionales según las nuevas especificaciones
    private LocalDateTime tiempoInmovilizacion; // Tiempo que permanece inmovilizado
    private LocalDateTime tiempoFinInoperatividad; // Tiempo en que estará disponible nuevamente
    private boolean requiereTraslado; // Si debe ser trasladado al almacén (tipo 2 y 3)
    private boolean esValida; // Indica si la avería es válida (unidad en operación y con carga)
    
    /**
     * Calcula si una avería es válida según las condiciones:
     * - La unidad debe estar en operación
     * - La avería solo tiene sentido si lleva carga
     * @param estaEnOperacion indica si el camión está en operación
     * @return true si la avería es válida, false en caso contrario
     */
    public boolean calcularValidezAveria(boolean estaEnOperacion) {
        esValida = estaEnOperacion && conCarga;
        return esValida;
    }
    
    /**
     * Calcula el kilómetro de ocurrencia de la avería en el rango de 5% a 35% del tramo total
     * @param distanciaTotal la distancia total del recorrido (ida y vuelta)
     */
    public void calcularKilometroOcurrencia(double distanciaTotal) {
        if (!esValida) return;
        
        Random random = new Random();
        // Calcular kilómetro de ocurrencia entre 5% y 35% del tramo total
        double minKm = distanciaTotal * 0.05;
        double maxKm = distanciaTotal * 0.35;
        kilometroOcurrencia = minKm + (maxKm - minKm) * random.nextDouble();
    }
    
    // Método para generar el formato de registro según especificación
    public String generarRegistro() {
        if (camion == null) return "";
        return String.format("%s_%s_%s", 
                            turno, 
                            camion.getCodigo(), 
                            tipoIncidente);
    }
    
    /**
     * Método para calcular tiempo de inmovilización según tipo de incidente
     * Considera la duración de los turnos (por defecto 8 horas)
     * @param duracionTurnoHoras duración de cada turno en horas (por defecto 8)
     */
    public void calcularTiemposInoperatividad(int duracionTurnoHoras) {
        if (tipoIncidente == null || fechaHoraReporte == null) return;

        LocalDateTime ahora = fechaHoraReporte;

        switch (tipoIncidente) {
            case "TI1" -> {
                // Incidente tipo 1: inmoviliza 2 horas, continúa ruta
                tiempoInmovilizacion = ahora.plusHours(2);
                tiempoFinInoperatividad = tiempoInmovilizacion;
                requiereTraslado = false;
            }
            case "TI2" -> {
                // Incidente tipo 2: inmoviliza 2 horas + un turno completo
                tiempoInmovilizacion = ahora.plusHours(2);
                requiereTraslado = true;
                tiempoFinInoperatividad = ahora.plusHours(duracionTurnoHoras * 2);  
            }
            case "TI3" -> {
                // Incidente tipo 3: inmoviliza 4 horas + tres días completos
                tiempoInmovilizacion = ahora.plusHours(4);
                requiereTraslado = true;
                // Disponible en turno 1 del día A+3
                tiempoFinInoperatividad = ahora.plusDays(3).withHour(0).plusHours(duracionTurnoHoras);
            }
            default -> {
            }
        }
        // Tipo de incidente no reconocido, no se realiza ninguna acción
            }
    
    /**
     * Sobrecarga del método para usar la duración de turno por defecto (8 horas)
     */
    public void calcularTiemposInoperatividad() {
        calcularTiemposInoperatividad(8);
    }
}