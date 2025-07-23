package com.plg.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.plg.entity.Averia;
import com.plg.entity.Coordenada;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.entity.TipoNodo;
import com.plg.utils.Gen;
import com.plg.utils.Parametros;

import lombok.Data;

@Data
public class GenDto {
    private CamionDto camion;
    private List<NodoDto> nodos;
    private CoordenadaDto destino;
    private List<PedidoDto> pedidos;

    public GenDto(Gen gen) {
        this.camion = new CamionDto(gen.getCamion());
        this.nodos = new ArrayList<>();
        for (Nodo nodo : gen.construirRutaFinalApi()) {
            NodoDto nodoDto = obtenerTipoNodo(nodo, gen);
            this.nodos.add(nodoDto);
        }
        // EL destino siempre el último nodo de la ruta
        this.destino = new CoordenadaDto(gen.getRutaFinal().getLast().getCoordenada());
        this.pedidos = new ArrayList<>();
        for (Pedido pedido : gen.getPedidos()) {
            this.pedidos.add(new PedidoDto(pedido));
        }

    }

    public NodoDto obtenerTipoNodo(Nodo nodo, Gen gen) {
        // Buscamos el nodo en la lista de almacenesIntermedios
        TipoNodo tipopNodo = TipoNodo.NORMAL;
        if (gen.getAlmacenesIntermedios().stream().anyMatch(a -> a.equals(nodo))) {
            tipopNodo = TipoNodo.ALMACEN_RECARGA;
        } else if (gen.getPedidos().stream().anyMatch(p -> p.equals(nodo))) {
            tipopNodo = TipoNodo.PEDIDO;
        } else if (gen.getCamionesAveriados().stream().anyMatch(c -> c.equals(nodo))) {
            tipopNodo = TipoNodo.CAMION_AVERIADO;
        }
        NodoDto nuevo_nodo = new NodoDto(nodo, tipopNodo);
        return nuevo_nodo;
    }

    // !Corta los nodos que van despues del ultimo nodo que puede recorrer el camion
    public void cortarNodosQueVanDespuesDelUltimoNodoQuePuedeRecorrerElCamion() {
        int cantidad_de_nodos_que_puede_recorrer_el_camion = calcularCantidadDeNodosQuePuedeRecorrerElCamion();

        // Si puede recorrer todos los nodos, no se elimina nada
        if (cantidad_de_nodos_que_puede_recorrer_el_camion >= nodos.size()) {
            return;
        }

        // Elimina los nodos desde el final hasta dejar solo los que puede recorrer
        for (int i = nodos.size() - 1; i >= cantidad_de_nodos_que_puede_recorrer_el_camion; i--) {
            nodos.remove(i);
        }
    }

    // ! calcula la cantidad de nodos que puede recorrer como maximo el camion segun
    // su velocidad
    public int calcularCantidadDeNodosQuePuedeRecorrerElCamion() {
        double velocidad_en_km_h = Parametros.velocidadCamion;

        double cantidad_de_horas_intervalo = Parametros.intervaloTiempo / 60.0;

        double cantidad_de_km_que_puede_recorrer_el_camion = velocidad_en_km_h * cantidad_de_horas_intervalo;
        // System.out
        // .println("Cantidad de km que puede recorrer el camion: " +
        // cantidad_de_km_que_puede_recorrer_el_camion);
        return (int) (cantidad_de_km_que_puede_recorrer_el_camion);
    }

    // !Calcula el indice iniial y final de los nodos que estan en el rango de
    // averias automaticas
    public void colocar_nodo_de_averia_automatica() {
        int posicion_inicial = (int) (calcularCantidadDeNodosQuePuedeRecorrerElCamion()
                * (Parametros.rango_inicial_tramo_averia / 100.0));
        int posicion_final = (int) (calcularCantidadDeNodosQuePuedeRecorrerElCamion()
                * (Parametros.rango_final_tramo_averia / 100.0));
        if (posicion_inicial > 1) {
            posicion_inicial = posicion_inicial - 1;
        }
        if (posicion_final > 1) {
            posicion_final = posicion_final - 1;
        }
        // System.out.println("Posicion inicial: " + posicion_inicial);
        // System.out.println("Posicion final: " + posicion_final);
        int cantidad_nodos_que_puede_recorrer_el_camion = calcularCantidadDeNodosQuePuedeRecorrerElCamion();
        // da una lista de pocisiones en numeros enteros de los nodos que estan en el
        // rango de averias automaticas y que son del tipo normal
        List<Integer> posiciones_normales = new ArrayList<>();
        for (int i = posicion_inicial; i < posicion_final - 1; i++) {
            if (nodos.get(i).getTipo().equals(TipoNodo.NORMAL)) {
                posiciones_normales.add(i);
            }
        }
        // elige una posicion aleatoria dentro de los rangos
        int posicion_aleatoria = new Random().nextInt(posiciones_normales.size());

        // coloca el nodo de avería automática en la posición aleatoria
        CoordenadaDto coordDto = nodos.get(posicion_aleatoria).getCoordenada();
        TipoNodo tipo_nodo_averia;
        // ! Busca el tipo de averia en la lista de averias automaticas
        Averia averia = Parametros.dataLoader.averiasAutomaticas.stream()
                .filter(a -> a.getCamion().getCodigo().equals(camion.getCodigo()))
                .findFirst()
                .orElse(null);
        if (averia != null) {
            String tipo_nodo_averia_string = averia.getTipoIncidente().getCodigo();
            if (tipo_nodo_averia_string.equals("TI1")) {
                tipo_nodo_averia = TipoNodo.AVERIA_AUTOMATICA_T1;
            } else if (tipo_nodo_averia_string.equals("TI2")) {
                tipo_nodo_averia = TipoNodo.AVERIA_AUTOMATICA_T2;
            } else {
                tipo_nodo_averia = TipoNodo.AVERIA_AUTOMATICA_T3;
            }
        } else {
            tipo_nodo_averia = TipoNodo.AVERIA_AUTOMATICA_T1;
        }

        NodoDto nodoAveria = new NodoDto(new Nodo(new Coordenada(coordDto.getY(), coordDto.getX())),
                tipo_nodo_averia);
        nodos.set(posicion_aleatoria, nodoAveria);
        // ! A partir de la posicion aleatoria se cambia el tipo de nodo de los nodos
        // que estan en el rango de averias automaticas
        for (int i = posicion_aleatoria; i < cantidad_nodos_que_puede_recorrer_el_camion; i++) {
            nodos.get(i).setTipo(tipo_nodo_averia);
            nodos.get(i).setCoordenada(nodos.get(posicion_aleatoria).getCoordenada());
        }
    }
}
