package com.plg.dto;

import java.util.ArrayList;
import java.util.List;

import com.plg.config.DataLoader;
import com.plg.entity.Nodo;
import com.plg.entity.Pedido;
import com.plg.utils.Gen;

import lombok.Data;

@Data
public class GenDto {
    private CamionDto camion;
    private List<NodoDto> nodos;
    private CoordenadaDto destino;
    private List<PedidoDto> pedidos;
    // Almacenes
    private List<AlmacenDto> almacenes;

    public GenDto(Gen gen) {
        this.camion = new CamionDto(gen.getCamion());
        this.nodos = new ArrayList<>();
        for (Nodo nodo : gen.getRutaFinal()) {
            this.nodos.add(new NodoDto(nodo));
        }
        if (gen.getPedidos().isEmpty()) {
            this.destino = new CoordenadaDto(DataLoader.almacenes.get(0).getCoordenada());
        } else {
            this.destino = new CoordenadaDto(gen.getPedidos().getLast().getCoordenada());
        }
        this.pedidos = new ArrayList<>();
        for (Pedido pedido : gen.getPedidos()) {
            this.pedidos.add(new PedidoDto(pedido));
        }

    }

    /*
     * Cortar un los nodos de este gen que no pueda recorrer el camion/segun una
     * cantidad de tiempo por ejemplo 30 minutos puede recorrer 25 nodos
     */
    public void cortarNodos(int tiempoMinutosIntervalo) {
        int numero_de_nodos_que_puede_recorrer_el_camion = (int) (tiempoMinutosIntervalo * 100 / 120);
        System.out.println(
                "numero_de_nodos_que_puede_recorrer_el_camion: " + numero_de_nodos_que_puede_recorrer_el_camion);

        // Verificar que la lista de nodos no sea null y tenga elementos
        if (this.nodos == null) {
            // System.out.println("⚠️ ADVERTENCIA: Lista de nodos es null, inicializando
            // lista vacía");
            this.nodos = new ArrayList<>();
            return;
        }

        if (this.nodos.isEmpty()) {
            // System.out.println("⚠️ ADVERTENCIA: Lista de nodos vacía, no se puede
            // cortar");
            return;
        }

        // Verificar que no intentemos cortar más nodos de los que tenemos
        int nodosDisponibles = this.nodos.size();
        int nodosACortar = Math.min(numero_de_nodos_que_puede_recorrer_el_camion, nodosDisponibles);

        System.out.println("nodos disponibles: " + nodosDisponibles);
        System.out.println("nodos a cortar: " + nodosACortar);

        // Cortar los nodos de la ruta final solo si hay nodos para cortar
        if (nodosACortar > 0) {
            try {
                this.nodos = this.nodos.subList(0, nodosACortar);

                // Verificar que la lista no esté vacía después del corte
                if (!this.nodos.isEmpty()) {
                    this.destino = this.nodos.get(this.nodos.size() - 1).getCoordenada();
                    // // !añadir 10 nodos repitiendo el ultimo nodo
                    // for (int i = 0; i < 50; i++) {
                    // this.nodos.add(this.nodos.get(this.nodos.size() - 1));
                    // }
                } else {
                    // System.out.println(
                    // "⚠️ ADVERTENCIA: Lista quedó vacía después del corte, usando coordenada del
                    // camión");
                    // Si la lista quedó vacía, usar la coordenada del camión como destino
                    if (this.camion != null) {
                        this.destino = new CoordenadaDto(this.camion.getColumna(), this.camion.getFila());
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                // System.err.println("❌ ERROR: IndexOutOfBoundsException al cortar nodos: " +
                // e.getMessage());
                // System.err.println("nodos.size(): " + this.nodos.size() + ", nodosACortar: "
                // + nodosACortar);
                // En caso de error, mantener la lista original
                return;
            }
        } else {
            // System.out.println("⚠️ ADVERTENCIA: No hay nodos para cortar (nodosACortar =
            // 0)");
            return;
        }

        // System.out.println("tamaño de ruta cortada: " + this.nodos.size());
    }

    /**
     * Verifica que entre cada nodo consecutivo en la ruta haya exactamente una
     * unidad de diferencia.
     * Utiliza la distancia Manhattan (|x1-x2| + |y1-y2|) para calcular la
     * diferencia entre coordenadas.
     * 
     * @return true si todos los nodos consecutivos están separados por exactamente
     *         una unidad, false en caso contrario
     */
    public boolean verificarDiferenciaUnitariaEntreNodos() {
        // Si hay menos de 2 nodos, no hay pares consecutivos para verificar
        if (nodos == null || nodos.size() < 2) {
            return true;
        }

        // Verificar cada par de nodos consecutivos
        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoDto nodoActual = nodos.get(i);
            NodoDto nodoSiguiente = nodos.get(i + 1);

            // Calcular la distancia Manhattan entre las coordenadas
            int diferenciaX = Math.abs(nodoActual.getCoordenada().getX() - nodoSiguiente.getCoordenada().getX());
            int diferenciaY = Math.abs(nodoActual.getCoordenada().getY() - nodoSiguiente.getCoordenada().getY());
            int distanciaManhattan = diferenciaX + diferenciaY;

            // Verificar que la distancia sea exactamente 1
            if (distanciaManhattan != 1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Obtiene información detallada sobre las diferencias entre nodos consecutivos.
     * Útil para debugging cuando la verificación falla.
     * 
     * @return Lista de strings con información sobre cada par de nodos consecutivos
     */
    public List<String> obtenerInformacionDiferenciasNodos() {
        List<String> informacion = new ArrayList<>();

        if (nodos == null || nodos.size() < 2) {
            informacion.add("No hay suficientes nodos para verificar diferencias");
            return informacion;
        }

        for (int i = 0; i < nodos.size() - 1; i++) {
            NodoDto nodoActual = nodos.get(i);
            NodoDto nodoSiguiente = nodos.get(i + 1);

            int diferenciaX = Math.abs(nodoActual.getCoordenada().getX() - nodoSiguiente.getCoordenada().getX());
            int diferenciaY = Math.abs(nodoActual.getCoordenada().getY() - nodoSiguiente.getCoordenada().getY());
            int distanciaManhattan = diferenciaX + diferenciaY;

            String info = String.format("Nodo %d (%d,%d) -> Nodo %d (%d,%d): distancia=%d %s",
                    i,
                    nodoActual.getCoordenada().getX(), nodoActual.getCoordenada().getY(),
                    i + 1,
                    nodoSiguiente.getCoordenada().getX(), nodoSiguiente.getCoordenada().getY(),
                    distanciaManhattan,
                    distanciaManhattan == 1 ? "✓" : "✗ (debería ser 1)");

            informacion.add(info);
        }

        return informacion;
    }
}
