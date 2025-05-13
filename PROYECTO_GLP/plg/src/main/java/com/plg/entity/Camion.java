package com.plg.entity;

import lombok.Getter;


import java.util.Set;

import com.plg.utils.Gen;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Camion extends Nodo {

    private String codigo;
    private TipoCamion tipo; // TA, TB, TC, TD

    // GLP
    private double capacidadMaximaGLP;              // Capacidad en m3 de GLP
    private double capacidadActualGLP;    // Capacidad disponible actual (m3)

    private double tara;                   // Peso del camión vacío en toneladas
    private double pesoCarga;              // Peso actual de la carga en toneladas
    private double pesoCombinado;          // Peso total (tara + carga)

    private EstadoCamion estado;

    // Combustible
    private double combustibleMaximo;   // Capacidad del tanque en galones
    private double combustibleActual;        // Combustible actual en galones
    private double velocidadPromedio; // Velocidad promedio en km/h

    // Comsumo de combustible
    private double distanciaMaxima;   

    // Gen
    private Gen gen;

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
            capacidadActualGLP,
            capacidadMaximaGLP,
            tara,
            pesoCarga,
            combustibleActual,
            combustibleMaximo,
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
        // Actualizamos el peso de la carga
        double pesoGLPPedido = volumenGLP * 0.5; 
        if (pesoGLPPedido > this.pesoCarga) {
            throw new IllegalArgumentException("No se puede descargar más GLP del que tiene el camión.");
        }
        pesoCarga -= pesoGLPPedido;

        // Actualizamos la capacidad de GLP
        if (capacidadActualGLP < volumenGLP) {
            throw new IllegalArgumentException("No se puede descargar más GLP del que tiene el camión.");
        }
        capacidadActualGLP -= volumenGLP;
    }

    public void actualizarEstado(int intervaloTiempo, Set<Pedido> pedidosPorAtender, Set<Pedido> pedidosPlanificados, Set<Pedido> pedidosEntregados) {
        int cantNodos = (int)(intervaloTiempo * velocidadPromedio / 60);
        int antiguo = gen.getPosNodo();
        gen.setPosNodo(antiguo + cantNodos);
        int distanciaRecorrida = gen.getPosNodo() - antiguo;
        actualizarCombustible(distanciaRecorrida);
        for(int i=0; i<=gen.getPosNodo(); i++){
            Nodo nodo = gen.getNodos().get(i);
            if(nodo.getTipoNodo() == TipoNodo.PEDIDO){
                pedidosEntregados.add((Pedido) nodo);
                pedidosPorAtender.remove(nodo);
            }
        }
        for (int i=gen.getPosNodo(); i<gen.getNodos().size(); i++){
            Nodo nodo = gen.getNodos().get(i);
            if(nodo.getTipoNodo() == TipoNodo.PEDIDO){
                pedidosPlanificados.add((Pedido) nodo);
                pedidosPorAtender.remove(nodo);
            }
        }
    }   

    public Camion clone() {
        return Camion.builder()
                .codigo(this.codigo)
                .tipo(this.tipo)
                .capacidadMaximaGLP(this.capacidadMaximaGLP)
                .capacidadActualGLP(this.capacidadActualGLP)
                .tara(this.tara)
                .pesoCarga(this.pesoCarga)
                .pesoCombinado(this.pesoCombinado)
                .estado(this.estado)
                .combustibleMaximo(this.combustibleMaximo)
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
