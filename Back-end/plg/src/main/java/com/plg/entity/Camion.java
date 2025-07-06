package com.plg.entity;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plg.utils.Gen;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
    
    // Tiempo de parada para despacho (en minutos)
    @lombok.Builder.Default
    private int tiempoParadaRestante = 0;

    // Gen
    @JsonIgnore
    private Gen gen;
    @JsonIgnore
    private Camion camionCopia;

    public Camion(Coordenada coordenada, boolean bloqueado, double gScore, TipoNodo tipoNodo, double fScore) {
        super(coordenada, bloqueado, gScore, fScore, tipoNodo);
    }

    @Override
    public String toString() {
        return String.format(
                "Camión %s [%s]%n"
                + "  - Coordenada:    %s%n"
                + "  - GLP (m3):       %.2f / %.2f%n"
                + "  - Carga (t):      %.2f (tara) + %.2f (carga)%n"
                + "  - Combustible:    %.2f / %.2f galones%n"
                + "  - Distancia máx.: %.2f km%n",
                codigo,
                tipo,
                getCoordenada() != null ? getCoordenada() : "N/A",
                capacidadActualGLP,
                capacidadMaximaGLP,
                tara,
                pesoCarga,
                combustibleActual,
                combustibleMaximo,
                distanciaMaxima
        );
    }

    public double calcularDistanciaMaxima() {
        this.distanciaMaxima = (combustibleActual * 180) / (tara + pesoCarga);
        return this.distanciaMaxima;
    }

    public void actualizarCombustible(double distancia) {

        double combustibleUsado = this.combustibleActual * distancia / this.distanciaMaxima;
        this.combustibleActual -= combustibleUsado;
    }

    public void entregarVolumenGLP(double volumenGLP) {
        // double pesoGLPPedido = volumenGLP * 0.5;  
        // pesoCarga -= pesoGLPPedido;
        capacidadActualGLP -= volumenGLP;
    }

    public void actualizarEstado(int intervaloTiempo, Set<Pedido> pedidosPorAtender, Set<Pedido> pedidosPlanificados,
            Set<Pedido> pedidosEntregados, LocalDateTime fechaActual) {
        if (this.gen == null) {
            // Primera vez que se llama no existen pedidos por atender
            return;
        }

        // El tiempo de parada está representado en la rutaFinal como nodos duplicados
        // No necesitamos lógica adicional aquí

        // Actualizar el nodo en el que se encuentra el camión
        int cantNodos = (int) (intervaloTiempo * velocidadPromedio / 60);
        int antiguo = gen.getPosNodo();
        gen.setPosNodo(antiguo + cantNodos);
        int distanciaRecorrida = gen.getPosNodo() - antiguo;
        actualizarCombustible(distanciaRecorrida);

        // En el tiempo transcurrido donde se puede encontrar el camión
        // System.out.println("gen.nodos.size() = " + gen.getRutaFinal().size());
        int intermedio = Math.min(gen.getPosNodo(), gen.getRutaFinal().size() - 1);

        // System.out.println("intermedio = " + intermedio);
        // Actualiza la posición del camión en el mapa solo si está disponible
        if (this.estado == EstadoCamion.DISPONIBLE) {
            Coordenada nuevaCoordenada = gen.getRutaFinal().get(intermedio).getCoordenada();
            setCoordenada(nuevaCoordenada);
        }

        // Actualizamos el estado de los pedidos
        for (int i = 0; i <= intermedio; i++) {
            Nodo nodo = gen.getRutaFinal().get(i);
            if (nodo.getTipoNodo() == TipoNodo.PEDIDO) {
                Pedido pedido = (Pedido) nodo;
                if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
                    continue;
                }
                // Calcular cuánto GLP puede entregar este camión en este paso
                double volumenRestante = pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado();
                double volumenAEntregar = Math.min(this.capacidadActualGLP, volumenRestante);
                if (volumenAEntregar > 0) {
                    entregarVolumenGLP(volumenAEntregar);
                    pedido.setVolumenGLPEntregado(pedido.getVolumenGLPEntregado() + volumenAEntregar);
                }
                // Si ya se entregó todo el GLP, marcar como entregado y actualizar sets
                if (Math.abs(pedido.getVolumenGLPEntregado() - pedido.getVolumenGLPAsignado()) < 1e-6) {
                    pedido.setEstado(EstadoPedido.ENTREGADO);
                    pedidosEntregados.add(pedido);
                    pedidosPorAtender.remove(nodo);
                    pedidosPlanificados.remove(nodo);
                }
            }
        }
        for (int i = intermedio + 1; i < gen.getRutaFinal().size(); i++) {
            Nodo nodo = gen.getRutaFinal().get(i);
            if (nodo.getTipoNodo() == TipoNodo.PEDIDO) {
                Pedido pedido = (Pedido) nodo;
                if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
                    continue;
                }
                pedidosPlanificados.add((Pedido) nodo);
                pedido.setEstado(EstadoPedido.PLANIFICADO);
                pedidosPorAtender.remove(nodo);
            }
        }

        // Si ya regresé al almacén central, actualizo el combustible del camión
        // y la carga de GLP
        if (gen.getRutaFinal().get(intermedio).getTipoNodo() == TipoNodo.ALMACEN) {
            Almacen almacen = (Almacen) gen.getRutaFinal().get(intermedio);
            if (almacen.getTipo() == TipoAlmacen.CENTRAL) {
                this.combustibleActual = this.combustibleMaximo;
                this.capacidadActualGLP = this.capacidadMaximaGLP;
            }
        }

        // Quitamos todos los pedidos entregados del mapa y reemplazamos por un nodo normal
        for (Pedido pedido : pedidosEntregados) {
            Mapa.getInstance().setNodo(pedido.getCoordenada(), new Nodo(pedido.getCoordenada(), false, 0, 0, TipoNodo.NORMAL));
        }

        // Calcular la distancia máxima que puede recorrer el camión
        calcularDistanciaMaxima();
    }

    @JsonIgnore
    public Camion getClone() {
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
                .tiempoParadaRestante(this.tiempoParadaRestante)
                .coordenada(getCoordenada())
                .bloqueado(isBloqueado())
                .gScore(getGScore())
                .fScore(getFScore())
                .tipoNodo(getTipoNodo())
                .build();
    }

    public void guardarCopia() {
        this.camionCopia = getClone();
    }

    public void restaurarCopia() {
        if (this.camionCopia != null) {
            this.codigo = camionCopia.getCodigo();
            this.tipo = camionCopia.getTipo();
            this.capacidadMaximaGLP = camionCopia.getCapacidadMaximaGLP();
            this.capacidadActualGLP = camionCopia.getCapacidadActualGLP();
            this.tara = camionCopia.getTara();
            this.pesoCarga = camionCopia.getPesoCarga();
            this.pesoCombinado = camionCopia.getPesoCombinado();
            this.estado = camionCopia.getEstado();
            this.combustibleMaximo = camionCopia.getCombustibleMaximo();
            this.combustibleActual = camionCopia.getCombustibleActual();
            this.velocidadPromedio = camionCopia.getVelocidadPromedio();
            this.distanciaMaxima = camionCopia.calcularDistanciaMaxima();
            this.tiempoParadaRestante = camionCopia.getTiempoParadaRestante();
            setCoordenada(camionCopia.getCoordenada());
            setBloqueado(camionCopia.isBloqueado());
            setGScore(camionCopia.getGScore());
            setFScore(camionCopia.getFScore());
            setTipoNodo(camionCopia.getTipoNodo());
        }
    }

}
