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
    public boolean colocar_nodo_de_averia_automatica() {
        try {
            // Validación de que la lista de nodos no esté vacía
            if (nodos == null || nodos.isEmpty()) {
                System.out.println("❌ Lista de nodos vacía o nula para camión: " + camion.getCodigo());
                return false;
            }

            int cantidad_nodos_que_puede_recorrer_el_camion = calcularCantidadDeNodosQuePuedeRecorrerElCamion();

            // Validación de que el camión pueda recorrer al menos un nodo
            if (cantidad_nodos_que_puede_recorrer_el_camion <= 0) {
                System.out.println("❌ El camión no puede recorrer ningún nodo: " + camion.getCodigo());
                return false;
            }

            int posicion_inicial = (int) (cantidad_nodos_que_puede_recorrer_el_camion
                    * (Parametros.rango_inicial_tramo_averia / 100.0));
            int posicion_final = (int) (cantidad_nodos_que_puede_recorrer_el_camion
                    * (Parametros.rango_final_tramo_averia / 100.0));

            // Validación de rangos de parámetros
            if (Parametros.rango_inicial_tramo_averia < 0 || Parametros.rango_inicial_tramo_averia > 100 ||
                    Parametros.rango_final_tramo_averia < 0 || Parametros.rango_final_tramo_averia > 100) {
                System.out.println("❌ Rangos de avería automática fuera de rango válido (0-100)");
                return false;
            }

            // Validación de que el rango inicial sea menor que el final
            if (posicion_inicial >= posicion_final) {
                System.out.println("❌ Rango de avería automática inválido: inicial >= final");
                return false;
            }

            if (posicion_inicial > 1) {
                posicion_inicial = posicion_inicial - 1;
            }
            if (posicion_final > 1) {
                posicion_final = posicion_final - 1;
            }

            // Validación de que las posiciones estén dentro del rango de nodos disponibles
            if (posicion_inicial < 0 || posicion_final > nodos.size() || posicion_inicial >= nodos.size()) {
                System.out.println("❌ Posiciones de avería automática fuera de rango de nodos disponibles");
                return false;
            }

            // System.out.println("Posicion inicial: " + posicion_inicial);
            // System.out.println("Posicion final: " + posicion_final);

            // da una lista de pocisiones en numeros enteros de los nodos que estan en el
            // rango de averias automaticas y que son del tipo normal
            List<Integer> posiciones_normales = new ArrayList<>();
            for (int i = posicion_inicial; i < posicion_final && i < nodos.size(); i++) {
                if (nodos.get(i) != null && nodos.get(i).getTipo() != null &&
                        nodos.get(i).getTipo().equals(TipoNodo.NORMAL)) {
                    posiciones_normales.add(i);
                }
            }

            // Validación de que haya nodos normales disponibles para colocar la avería
            if (posiciones_normales.isEmpty()) {
                System.out.println("❌ No hay nodos normales disponibles en el rango de avería automática para camión: "
                        + camion.getCodigo());
                return false;
            }

            // elige una posicion aleatoria dentro de los rangos
            int posicion_aleatoria = new Random().nextInt(posiciones_normales.size());
            int indice_nodo_seleccionado = posiciones_normales.get(posicion_aleatoria);

            // Validación adicional del índice seleccionado
            if (indice_nodo_seleccionado < 0 || indice_nodo_seleccionado >= nodos.size()) {
                System.out.println("❌ Índice de nodo seleccionado fuera de rango");
                return false;
            }

            // coloca el nodo de avería automática en la posición aleatoria
            CoordenadaDto coordDto = nodos.get(indice_nodo_seleccionado).getCoordenada();
            if (coordDto == null) {
                System.out.println("❌ Coordenada del nodo seleccionado es nula");
                return false;
            }

            TipoNodo tipo_nodo_averia;
            // ! Busca el tipo de averia en la lista de averias automaticas
            Averia averia = Parametros.dataLoader.averiasAutomaticas.stream()
                    .filter(a -> a.getCamion() != null && a.getCamion().getCodigo() != null &&
                            a.getCamion().getCodigo().equals(camion.getCodigo()))
                    .findFirst()
                    .orElse(null);
            if (averia != null && averia.getTipoIncidente() != null) {
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
            nodos.set(indice_nodo_seleccionado, nodoAveria);

            // ! A partir de la posicion aleatoria se cambia el tipo de nodo de los nodos
            // que estan en el rango de averias automaticas
            for (int i = indice_nodo_seleccionado; i < nodos.size(); i++) {
                if (nodos.get(i) != null) {
                    nodos.get(i).setTipo(tipo_nodo_averia);
                    nodos.get(i).setCoordenada(nodos.get(indice_nodo_seleccionado).getCoordenada());
                }
            }

            return true;

        } catch (Exception e) {
            System.out.println("❌ Error en colocar_nodo_de_averia_automatica para camión " + camion.getCodigo() + ": "
                    + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
