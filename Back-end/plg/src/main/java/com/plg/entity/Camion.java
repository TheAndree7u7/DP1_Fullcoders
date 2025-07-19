package com.plg.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plg.repository.AveriaRepository;
import com.plg.utils.Gen;
import com.plg.utils.Parametros;

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

    public void actualizarEstado(Set<Pedido> pedidosPorAtender, Set<Pedido> pedidosPlanificados,
            Set<Pedido> pedidosEntregados) {
        if (this.gen == null) {
            // Primera vez que se llama no existen pedidos por atender
            return;
        }
        int cantNodos = 0;
        int antiguo = gen.getPosNodo();
        if (this.getEstado() == EstadoCamion.DISPONIBLE) {
            // Si el camión está disponible, se mueve a la siguiente posición
            cantNodos = (int) (Parametros.diferenciaTiempoMinRequest * velocidadPromedio / 60);
        } else{
            AveriaRepository averiaRepo = new AveriaRepository();
            List<Averia> averias = averiaRepo.findByCamion(this);
            // Si alguna de las averias 
            // Comparamos si alguna de las averías tiene un tiempo igual a la fecha actual
            if (averias.stream().anyMatch(a -> a.getFechaHoraReporte().isEqual(Parametros.fecha_inicial))) {
                // Entonces la avería surgio por primera vez
                // Por tanto es necesario realizar el movimiento de los camiones
                cantNodos = (int) (Parametros.diferenciaTiempoMinRequest * velocidadPromedio / 60);
            } else {
                // El camion ya ha sido averiado con anterioridad
                cantNodos = 0;
            }
            
        }
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

        // Actualiza la posición del camión en el mapa solo si está disponible
        if (intermedio < gen.getRutaFinal().size()) {
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

                // Verificar que el pedido pertenece a este camión
                if (gen.getPedidos() == null || !gen.getPedidos().contains(pedido)) {
                    continue; // Si el pedido no pertenece a este camión, no entregar
                }

                // Calcular la cantidad de GLP a entregar basada en la distribución proporcional
                int cantidadPedidosAsignados = gen.getPedidos().size();
                if (cantidadPedidosAsignados == 0) {
                    continue; // No hay pedidos asignados, no debería pasar
                }

                double glpPorPedido = (double) this.capacidadMaximaGLP / cantidadPedidosAsignados;
                double volumenRestante = pedido.getVolumenGLPAsignado() - pedido.getVolumenGLPEntregado();
                double volumenAEntregar = Math.min(glpPorPedido, volumenRestante);
                volumenAEntregar = Math.min(volumenAEntregar, this.capacidadActualGLP);

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

}
