package com.plg.entity;

import java.time.LocalDateTime;
import java.util.List;
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
    private double capacidadMaximaGLP; // Capacidad en m3 de GLP
    private double capacidadActualGLP; // Capacidad disponible actual (m3)

    private double tara; // Peso del camión vacío en toneladas
    private double pesoCarga; // Peso actual de la carga en toneladas
    private double pesoCombinado; // Peso total (tara + carga)

    private EstadoCamion estado;

    // Combustible
    private double combustibleMaximo; // Capacidad del tanque en galones
    private double combustibleActual; // Combustible actual en galones
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
                "Camión %s [%s] - %s%n"
                        + "  - Coordenada:    %s%n"
                        + "  - GLP (m3):       %.2f / %.2f%n"
                        + "  - Carga (t):      %.2f (tara) + %.2f (carga)%n"
                        + "  - Combustible:    %.2f / %.2f galones%n"
                        + "  - Distancia máx.: %.2f km%n",
                codigo,
                tipo,
                estado != null ? estado.name() : "N/A",
                getCoordenada() != null ? getCoordenada() : "N/A",
                capacidadActualGLP,
                capacidadMaximaGLP,
                tara,
                pesoCarga,
                combustibleActual,
                combustibleMaximo,
                distanciaMaxima);
    }

    public double calcularDistanciaMaxima() {
        // Prevenir división por cero y valores negativos
        double pesoTotal = tara + pesoCarga;

        // Validaciones de seguridad
        if (combustibleActual <= 0) {
            this.distanciaMaxima = 0.0;
            return this.distanciaMaxima;
        }

        if (pesoTotal <= 0) {
            System.err.println("⚠️ ADVERTENCIA: Peso total del camión " + codigo + " es <= 0. Tara: " + tara
                    + ", Carga: " + pesoCarga);
            this.distanciaMaxima = 50.0; // Valor mínimo de seguridad
            return this.distanciaMaxima;
        }

        // Fórmula corregida para distancia máxima (rendimiento mejorado)
        // Rendimiento base: 15 km/galón, ajustado por peso
        double rendimientoBase = 15.0; // km por galón
        double factorPeso = Math.max(0.3, 10.0 / pesoTotal); // Factor que reduce el rendimiento con más peso
        double rendimientoReal = rendimientoBase * factorPeso;

        this.distanciaMaxima = combustibleActual * rendimientoReal;

        // Asegurar un mínimo razonable
        this.distanciaMaxima = Math.max(this.distanciaMaxima, 10.0);

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
        // Verificar que la ruta final no esté vacía
        if (gen.getRutaFinal().isEmpty()) {
            System.out.println("⚠️ ADVERTENCIA: Camión " + codigo + " tiene ruta final vacía");
            return;
        }

        int intermedio = Math.min(gen.getPosNodo(), gen.getRutaFinal().size() - 1);

        // Asegurar que el índice sea válido
        if (intermedio < 0) {
            intermedio = 0;
        }

        // !Actualiza la posición del camión en el mapa solo si está disponible
        if (this.estado == EstadoCamion.DISPONIBLE && intermedio < gen.getRutaFinal().size()) {
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
                pedidosEntregados.add(pedido);
                // Voy al GLP del camión y reduzco la carga porque lo entrego
                entregarVolumenGLP(pedido.getVolumenGLPAsignado());
                // Marcamos el pedido como entregado para no considerarlo en la siguiente
                // iteración
                pedido.setEstado(EstadoPedido.ENTREGADO);
                pedidosPorAtender.remove(nodo);
                pedidosPlanificados.remove(nodo);

                // El tiempo de parada ya está representado en la rutaFinal como nodos
                // duplicados
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
        if (intermedio >= 0 && intermedio < gen.getRutaFinal().size() &&
                gen.getRutaFinal().get(intermedio).getTipoNodo() == TipoNodo.ALMACEN) {
            Almacen almacen = (Almacen) gen.getRutaFinal().get(intermedio);
            if (almacen.getTipo() == TipoAlmacen.CENTRAL) {
                this.combustibleActual = this.combustibleMaximo;
                this.capacidadActualGLP = this.capacidadMaximaGLP;
            }
        }

        // Quitamos todos los pedidos entregados del mapa y reemplazamos por un nodo
        // normal
        for (Pedido pedido : pedidosEntregados) {
            Mapa.getInstance().setNodo(pedido.getCoordenada(),
                    new Nodo(pedido.getCoordenada(), false, 0, 0, TipoNodo.NORMAL));
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

    // FUNCIOn para imprimir los datos de una lista de camiones de manera resumida
    // de forma cosecutiva separada por |
    public static void imprimirDatosCamiones(List<Camion> camiones) {
        for (Camion camion : camiones) {
            System.out.println(camion.toString());
        }
    }
}
