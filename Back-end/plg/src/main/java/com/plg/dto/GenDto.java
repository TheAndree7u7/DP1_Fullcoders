package com.plg.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // ! calcula la cantidad de nodos que puede recorrer como maximo el camion segun
    // su velocidad
    public int calcularCantidadDeNodosQuePuedeRecorrerElCamion() {
        return (int) (Parametros.velocidadCamion);
    }
    //!Calcula el indice iniial y final de los nodos que estan en el rango de averias automaticas
    public void colocar_nodo_de_averia_automatica() {
        int posicion_inicial = calcularCantidadDeNodosQuePuedeRecorrerElCamion()  * Parametros.rango_inicial_tramo_averia;
        int posicion_final = calcularCantidadDeNodosQuePuedeRecorrerElCamion()  * Parametros.rango_final_tramo_averia;

      
        // da una lista de pocisiones en numeros enteros de los nodos que estan en el rango de averias automaticas y que son del tipo normal 
        List<Integer> posiciones_normales = new ArrayList<>();
        for(int i = 0; i < nodos.size(); i++){
            if(nodos.get(i).getTipo().equals(TipoNodo.NORMAL)){
                posiciones_normales.add(i);
            }
        }
        //elige una posicion aleatoria dentro de los rangos
        int posicion_aleatoria = new Random().nextInt(posiciones_normales.size());

        //coloca el nodo de avería automática en la posición aleatoria

    }
}
