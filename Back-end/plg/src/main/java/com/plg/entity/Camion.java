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

        this.distanciaMaxima = (combustibleActual * 250) / (tara + pesoCarga);
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

    public void actualizarEstado(Set<Pedido> pedidosPlanificados,
            Set<Pedido> pedidosEntregados) {
        if (this.gen == null) {
            // Primera vez que se llama no existen pedidos por atender
            return;
        }
        int cantNodos = 0;
        int antiguo = gen.getPosNodo();
        if (this.getEstado() == EstadoCamion.DISPONIBLE) {
            // Si el camión está disponible, se mueve a la siguiente posición
            cantNodos = calcularCantidadDeNodos();
        } else {
            AveriaRepository averiaRepo = new AveriaRepository();
            List<Averia> averias = averiaRepo.findByCamion(this);
            // Si alguna de las averias
            // Comparamos si alguna de las averías tiene un tiempo igual a la fecha actual
            if (averias.stream().anyMatch(a -> a.getFechaHoraReporte().isEqual(Parametros.fecha_inicial))) {

                cantNodos = calcularCantidadDeNodos();
            } else {
                // El camion ya ha sido averiado con anterioridad
                cantNodos = 0;
            }

        }
        gen.setPosNodo(antiguo + cantNodos);
        int distanciaRecorrida = gen.getPosNodo() - antiguo;
        actualizarCombustible(distanciaRecorrida);
        
        if(Parametros.dataLoader.camionesAveriados.stream()
                .anyMatch(c -> c.getCodigo().equals(this.codigo))) {
            this.setEstado(EstadoCamion.INMOVILIZADO_POR_AVERIA);
        }

        // En el tiempo transcurrido donde se puede encontrar el camión
        // Verificar que la ruta final no esté vacía
        if (gen.getRutaFinal().isEmpty()) {
            System.out.println("⚠️ ADVERTENCIA: Camión " + codigo + " tiene ruta final vacía");
            return;
        }

        int intermedio = Math.min(gen.getPosNodo(), gen.getRutaFinal().size() - 1);


        // Actualiza la posición del camión en el mapa solo si está disponible
        if (intermedio < gen.getRutaFinal().size()) {
            Coordenada nuevaCoordenada = gen.getRutaFinal().get(intermedio).getCoordenada();
            setCoordenada(nuevaCoordenada);
    
        }

        


        // RECORRER RUTA HASTA PUNTO INTERMEDIO
        for (int i = 0; i <= intermedio; i++) {
            Nodo nodo = gen.getRutaFinal().get(i);
            if (nodo.getTipoNodo() == TipoNodo.PEDIDO) {
                Pedido pedido = (Pedido) nodo;
                entregarPedido(pedido, pedidosPlanificados, pedidosEntregados);
            }else if(nodo instanceof Almacen && gen.getAlmacenesIntermedios().contains(nodo)) {
                Almacen almacen = (Almacen) nodo;
                almacen.recargarGlPCamion(this);
                almacen.recargarCombustible(this);
            }else {
                if (nodo instanceof Camion && gen.getCamionesAveriados().contains(nodo)) {
                    Camion camionRecarga = (Camion) nodo;
                    camionRecarga.recargarGlPSiAveriado(this);
                }
            }
        }

        // RECORRER RUTA DESDE PUNTO INTERMEDIO HASTA FINAL
        for (int i = intermedio + 1; i < gen.getRutaFinal().size(); i++) {
            Nodo nodo = gen.getRutaFinal().get(i);
            if (nodo.getTipoNodo() == TipoNodo.PEDIDO && gen.getPedidos().contains(nodo)) {
                Pedido pedido = (Pedido) nodo;
                if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
                    continue;
                }
                pedidosPlanificados.add(pedido);
                pedido.setEstado(EstadoPedido.PLANIFICADO);

            }
        }
        // Quitamos todos los pedidos entregados del mapa y reemplazamos por un nodo
        // normal
        for (Pedido pedido : pedidosEntregados) {
            Mapa.getInstance().setNodo(pedido.getCoordenada(),
                    new Nodo(pedido.getCoordenada(), false, 0, 0, TipoNodo.NORMAL));
        }
        calcularDistanciaMaxima();
    }

    private void entregarPedido(Pedido pedido,
            Set<Pedido> pedidosPlanificados, Set<Pedido> pedidosEntregados) {
        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            // Si el pedido ya está entregado, no hacer nada
            return;
        }

        // Verificar que el pedido pertenece a este camión
        if (gen.getPedidos() == null || !gen.getPedidos().contains(pedido)) {
            return; // Si el pedido no pertenece a este camión, no entregar
        }

        // Calcular la cantidad de GLP a entregar basada en la distribución proporcional
        int cantidadPedidosAsignados = gen.getPedidos().size();
        if (cantidadPedidosAsignados == 0) {
            return; // No hay pedidos asignados, no debería pasar
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
        if (Math.abs(pedido.getVolumenGLPEntregado() - pedido.getVolumenGLPAsignado()) < Parametros.diferenciaParaPedidoEntregado) {
            pedido.setEstado(EstadoPedido.ENTREGADO);
            pedidosEntregados.add(pedido);
            pedidosPlanificados.remove(pedido);
        }
    }

    public int calcularCantidadDeNodos() {
        double diferenciaTiempo = Parametros.intervaloTiempo;
        if (Parametros.diferenciaTiempoMinRequest != 0){
            diferenciaTiempo = Parametros.diferenciaTiempoMinRequest;
        }
        int cantNodos = (int) (diferenciaTiempo * velocidadPromedio / 60);
        List<Nodo> rutaApi = gen.construirRutaFinalApi();
        Nodo nodo_final = rutaApi.get(Math.max(cantNodos-1, 0));
        for(int i = 0; i < gen.getRutaFinal().size(); i++) {
            if (gen.getRutaFinal().get(i).equals(nodo_final)) {
                cantNodos = i;
                break;
            }
        }
        return cantNodos;
    }

    public boolean recargarGlPSiAveriado(Camion camion) {
        double glpRequerido = camion.getCapacidadMaximaGLP() - camion.getCapacidadActualGLP();
        double glpDisponible = this.getCapacidadActualGLP();
        if (glpDisponible <= 0) {
            return false; // No hay GLP para recargar o el camión ya está lleno
        }
        double glpRecargar = Math.min(glpRequerido, glpDisponible);
        camion.setCapacidadActualGLP(camion.getCapacidadActualGLP() + glpRecargar);
        this.setCapacidadActualGLP(this.getCapacidadActualGLP() - glpRecargar);
        return true;
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
