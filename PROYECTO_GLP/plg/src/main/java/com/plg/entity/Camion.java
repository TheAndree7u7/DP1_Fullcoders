package com.plg.entity;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import com.plg.entity.TipoCamion;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Camion extends Nodo {

    private String codigo;
    private TipoCamion tipo; // TA, TB, TC, TD

    // GLP
    private double capacidadMaxima;              // Capacidad en m3 de GLP
    private double capacidadActual;    // Capacidad disponible actual (m3)

    private double tara;                   // Peso del camión vacío en toneladas
    private double pesoCarga;              // Peso actual de la carga en toneladas
    private double pesoCombinado;          // Peso total (tara + carga)

    private EstadoCamion estado;

    // Combustible
    private double capacidadTanque;   // Capacidad del tanque en galones
    private double combustibleActual;        // Combustible actual en galones
    private double velocidadPromedio; // Velocidad promedio en km/h

    // Comsumo de combustible
    private double distanciaMaxima;   

    public Camion(Coordenada coordenada, boolean bloqueado, double gScore, TipoNodo tipoNodo, double fScore) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
    }


    @Override
    public String toString() {
        return String.format(
            "Camión %s [%s]%n" +
            "  - Coordenada:    %s%n" +
            "  - GLP (m3):       %.2f / %.2f%n" +
            "  - Carga (t):      %.2f (tara) + %.2f (carga)%n" +
            "  - Combustible:    %.2f / %.2f galones%n" +
            "  - Velocidad:      %.2f km/h%n" +
            "  - Distancia máx.: %.2f km%n" +
            "  - Estado:         %s \n",
            codigo,
            tipo,
            getCoordenada() != null ? getCoordenada() : "N/A",
            capacidadActual,
            capacidadMaxima,
            tara,
            pesoCarga,
            combustibleActual,
            capacidadTanque,
            velocidadPromedio,
            distanciaMaxima,
            estado
        );
    }


    public double calcularDistanciaMaxima() {
        if (tara + pesoCarga <= 0) {
            throw new IllegalArgumentException("No se puede calcular la distancia máxima con un peso total no válido.");
        }
        this.distanciaMaxima = (combustibleActual * 180) / (tara + pesoCarga);
        return this.distanciaMaxima;
    }

    public void actualizarCombustible(double distancia) {
        if (distancia > this.distanciaMaxima) {
            throw new IllegalArgumentException("No se puede recorrer una distancia mayor a la distancia máxima calculada.");
        }
        double combustibleUsado = this.combustibleActual * distancia  / this.distanciaMaxima;
        this.combustibleActual -= combustibleUsado;
    } 


    public void actualizarCargaPedido(double volumenGLP) {
        // Convertimos de volumen a peso (1 m3 de GLP = 0.5 toneladas)
        double pesoGLPPedido = volumenGLP * 0.5; 
        if (pesoGLPPedido > this.pesoCarga) {
            throw new IllegalArgumentException("No se puede descargar más GLP del que tiene el camión.");
        }
        pesoCarga -= pesoGLPPedido;
    }


    public Camion clone() {
        return Camion.builder()
                .codigo(this.codigo)
                .tipo(this.tipo)
                .capacidadMaxima(this.capacidadMaxima)
                .capacidadActual(this.capacidadActual)
                .tara(this.tara)
                .pesoCarga(this.pesoCarga)
                .pesoCombinado(this.pesoCombinado)
                .estado(this.estado)
                .capacidadTanque(this.capacidadTanque)
                .combustibleActual(this.combustibleActual)
                .velocidadPromedio(this.velocidadPromedio)
                .distanciaMaxima(this.distanciaMaxima)
                .coordenada(getCoordenada())
                .bloqueado(isBloqueado())
                .gScore(getGScore())
                .fScore(getFScore())
                .tipoNodo(getTipoNodo())
                .build();
    }

}
