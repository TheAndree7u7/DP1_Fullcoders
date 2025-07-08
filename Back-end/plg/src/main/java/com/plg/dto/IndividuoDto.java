package com.plg.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.plg.config.DataLoader;
import com.plg.entity.Almacen;
import com.plg.entity.Bloqueo;
import com.plg.entity.Pedido;
import com.plg.utils.Gen;
import com.plg.utils.Individuo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class IndividuoDto {

    private List<GenDto> cromosoma;
    private List<PedidoDto> pedidos;
    private List<BloqueoDto> bloqueos;
    private List<AlmacenDto> almacenes;
    private LocalDateTime fechaHoraSimulacion;

    public IndividuoDto(Individuo individuo, List<Pedido> pedidos, List<Bloqueo> bloqueos) {
        this(individuo, pedidos, bloqueos, LocalDateTime.now());
    }

    public IndividuoDto(Individuo individuo, List<Pedido> pedidos, List<Bloqueo> bloqueos,
            LocalDateTime fechaSimulacion) {
        this.fechaHoraSimulacion = fechaSimulacion;

        this.cromosoma = new ArrayList<>();
        for (Gen gen : individuo.getCromosoma()) {
            cromosoma.add(new GenDto(gen));
        }

        this.pedidos = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            this.pedidos.add(new PedidoDto(pedido));
        }

        this.bloqueos = new ArrayList<>();
        for (Bloqueo bloqueo : bloqueos) {
            this.bloqueos.add(new BloqueoDto(bloqueo));
        }

        // Agregar almacenes desde DataLoader
        this.almacenes = new ArrayList<>();
        for (Almacen almacen : DataLoader.almacenes) {
            this.almacenes.add(new AlmacenDto(almacen));
        }
    }

    /**
     * Retorna un diccionario ordenado con información de cada camión, incluyendo su
     * posición inicial y final.
     * El diccionario está ordenado por el código del camión.
     * 
     * @return Map con el código del camión como clave y un Map con información de
     *         posiciones como valor
     */
    public Map<String, Map<String, Object>> obtenerDiccionarioCamionesPosiciones() {
        Map<String, Map<String, Object>> diccionarioCamiones = new LinkedHashMap<>();

        if (cromosoma == null || cromosoma.isEmpty()) {
            return diccionarioCamiones;
        }

        // Procesar cada gen (cada gen representa un camión)
        for (int i = 0; i < cromosoma.size(); i++) {
            GenDto gen = cromosoma.get(i);
            CamionDto camion = gen.getCamion();

            if (camion == null) {
                continue;
            }

            Map<String, Object> infoCamion = new HashMap<>();

            // Información básica del camión
            infoCamion.put("codigo", camion.getCodigo());
            infoCamion.put("tipo", camion.getTipo());
            infoCamion.put("estado", camion.getEstado());

            // Posición inicial (posición actual del camión)
            Map<String, Integer> posicionInicial = new HashMap<>();
            posicionInicial.put("fila", camion.getFila());
            posicionInicial.put("columna", camion.getColumna());
            infoCamion.put("posicionInicial", posicionInicial);

            // Posición final (último nodo de la ruta)
            Map<String, Integer> posicionFinal = new HashMap<>();
            if (gen.getNodos() != null && !gen.getNodos().isEmpty()) {
                NodoDto ultimoNodo = gen.getNodos().get(gen.getNodos().size() - 1);
                posicionFinal.put("fila", ultimoNodo.getCoordenada().getY());
                posicionFinal.put("columna", ultimoNodo.getCoordenada().getX());
            } else {
                // Si no hay nodos, la posición final es la misma que la inicial
                posicionFinal.put("fila", camion.getFila());
                posicionFinal.put("columna", camion.getColumna());
            }
            infoCamion.put("posicionFinal", posicionFinal);

            // Información adicional
            infoCamion.put("numeroGen", i);
            infoCamion.put("cantidadNodos", gen.getNodos() != null ? gen.getNodos().size() : 0);
            infoCamion.put("cantidadPedidos", gen.getPedidos() != null ? gen.getPedidos().size() : 0);

            // Agregar al diccionario principal usando el código del camión como clave
            diccionarioCamiones.put(camion.getCodigo(), infoCamion);
        }

        return diccionarioCamiones;
    }

    /**
     * Retorna información resumida de las posiciones de todos los camiones en
     * formato de texto.
     * Útil para logging o debugging.
     * 
     * @return String con información formateada de todos los camiones
     */
    public String obtenerResumenPosicionesCamiones() {
        Map<String, Map<String, Object>> diccionario = obtenerDiccionarioCamionesPosiciones();
        StringBuilder resumen = new StringBuilder();

        resumen.append("=== RESUMEN DE POSICIONES DE CAMIONES ===\n");
        resumen.append("Fecha/Hora: ").append(fechaHoraSimulacion).append("\n");
        resumen.append("Total de camiones: ").append(diccionario.size()).append("\n\n");

        for (Map.Entry<String, Map<String, Object>> entry : diccionario.entrySet()) {
            String codigoCamion = entry.getKey();
            Map<String, Object> info = entry.getValue();

            @SuppressWarnings("unchecked")
            Map<String, Integer> posInicial = (Map<String, Integer>) info.get("posicionInicial");
            @SuppressWarnings("unchecked")
            Map<String, Integer> posFinal = (Map<String, Integer>) info.get("posicionFinal");

            resumen.append(String.format("Camión %s (%s):\n", codigoCamion, info.get("tipo")));
            resumen.append(String.format("  Posición inicial: (%d, %d)\n",
                    posInicial.get("fila"), posInicial.get("columna")));
            resumen.append(String.format("  Posición final: (%d, %d)\n",
                    posFinal.get("fila"), posFinal.get("columna")));
            resumen.append(String.format("  Nodos en ruta: %d\n", info.get("cantidadNodos")));
            resumen.append(String.format("  Pedidos asignados: %d\n", info.get("cantidadPedidos")));
            resumen.append("  Estado: ").append(info.get("estado")).append("\n\n");
        }

        return resumen.toString();
    }

    /**
     * Verifica la continuidad de posiciones entre dos individuos.
     * Compara que las posiciones finales de los camiones del individuo anterior
     * coincidan con las posiciones iniciales del individuo actual.
     * 
     * @param individuoActual   El individuo actual
     * @param individuoAnterior El individuo anterior
     * @return Map con el resumen de la verificación de continuidad
     */
    public static Map<String, Object> verificarContinuidadPosiciones(IndividuoDto individuoActual,
            IndividuoDto individuoAnterior) {
        Map<String, Object> resultado = new HashMap<>();
        Map<String, Map<String, Object>> diccionarioActual = individuoActual.obtenerDiccionarioCamionesPosiciones();
        Map<String, Map<String, Object>> diccionarioAnterior = individuoAnterior.obtenerDiccionarioCamionesPosiciones();

        List<String> camionesConContinuidad = new ArrayList<>();
        List<String> camionesSinContinuidad = new ArrayList<>();
        List<String> camionesSoloEnActual = new ArrayList<>();
        List<String> camionesSoloEnAnterior = new ArrayList<>();

        // Verificar camiones que están en ambos individuos
        for (String codigoCamion : diccionarioActual.keySet()) {
            if (diccionarioAnterior.containsKey(codigoCamion)) {
                Map<String, Object> infoActual = diccionarioActual.get(codigoCamion);
                Map<String, Object> infoAnterior = diccionarioAnterior.get(codigoCamion);

                @SuppressWarnings("unchecked")
                Map<String, Integer> posInicialActual = (Map<String, Integer>) infoActual.get("posicionInicial");
                @SuppressWarnings("unchecked")
                Map<String, Integer> posFinalAnterior = (Map<String, Integer>) infoAnterior.get("posicionFinal");

                boolean continuidad = posInicialActual.get("fila").equals(posFinalAnterior.get("fila")) &&
                        posInicialActual.get("columna").equals(posFinalAnterior.get("columna"));

                if (continuidad) {
                    camionesConContinuidad.add(codigoCamion);
                } else {
                    camionesSinContinuidad.add(codigoCamion);
                }
            } else {
                camionesSoloEnActual.add(codigoCamion);
            }
        }

        // Verificar camiones que solo están en el individuo anterior
        for (String codigoCamion : diccionarioAnterior.keySet()) {
            if (!diccionarioActual.containsKey(codigoCamion)) {
                camionesSoloEnAnterior.add(codigoCamion);
            }
        }

        // Construir el resultado
        resultado.put("totalCamionesActual", diccionarioActual.size());
        resultado.put("totalCamionesAnterior", diccionarioAnterior.size());
        resultado.put("camionesConContinuidad", camionesConContinuidad);
        resultado.put("camionesSinContinuidad", camionesSinContinuidad);
        resultado.put("camionesSoloEnActual", camionesSoloEnActual);
        resultado.put("camionesSoloEnAnterior", camionesSoloEnAnterior);
        resultado.put("continuidadPerfecta", camionesSinContinuidad.isEmpty() &&
                camionesSoloEnActual.isEmpty() &&
                camionesSoloEnAnterior.isEmpty());

        return resultado;
    }

    /**
     * Genera un resumen detallado de la verificación de continuidad entre dos
     * individuos.
     * 
     * @param individuoActual   El individuo actual
     * @param individuoAnterior El individuo anterior
     * @return String con el resumen formateado de la verificación
     */
    public static String generarResumenContinuidad(IndividuoDto individuoActual, IndividuoDto individuoAnterior) {
        Map<String, Object> resultado = verificarContinuidadPosiciones(individuoActual, individuoAnterior);
        Map<String, Map<String, Object>> diccionarioActual = individuoActual.obtenerDiccionarioCamionesPosiciones();
        Map<String, Map<String, Object>> diccionarioAnterior = individuoAnterior.obtenerDiccionarioCamionesPosiciones();

        StringBuilder resumen = new StringBuilder();

        resumen.append("=== VERIFICACIÓN DE CONTINUIDAD ENTRE INDIVIDUOS ===\n");
        resumen.append("Fecha/Hora Actual: ").append(individuoActual.getFechaHoraSimulacion()).append("\n");
        resumen.append("Fecha/Hora Anterior: ").append(individuoAnterior.getFechaHoraSimulacion()).append("\n");
        resumen.append("Total camiones actual: ").append(resultado.get("totalCamionesActual")).append("\n");
        resumen.append("Total camiones anterior: ").append(resultado.get("totalCamionesAnterior")).append("\n");
        resumen.append("Continuidad perfecta: ").append((Boolean) resultado.get("continuidadPerfecta") ? "SÍ" : "NO")
                .append("\n\n");

        @SuppressWarnings("unchecked")
        List<String> camionesConContinuidad = (List<String>) resultado.get("camionesConContinuidad");
        @SuppressWarnings("unchecked")
        List<String> camionesSinContinuidad = (List<String>) resultado.get("camionesSinContinuidad");
        @SuppressWarnings("unchecked")
        List<String> camionesSoloEnActual = (List<String>) resultado.get("camionesSoloEnActual");
        @SuppressWarnings("unchecked")
        List<String> camionesSoloEnAnterior = (List<String>) resultado.get("camionesSoloEnAnterior");

        // Camiones con continuidad
        if (!camionesConContinuidad.isEmpty()) {
            resumen.append("✓ CAMIONES CON CONTINUIDAD (").append(camionesConContinuidad.size()).append("):\n");
            for (String codigo : camionesConContinuidad) {
                Map<String, Object> infoActual = diccionarioActual.get(codigo);
                Map<String, Object> infoAnterior = diccionarioAnterior.get(codigo);

                @SuppressWarnings("unchecked")
                Map<String, Integer> posInicialActual = (Map<String, Integer>) infoActual.get("posicionInicial");
                @SuppressWarnings("unchecked")
                Map<String, Integer> posFinalAnterior = (Map<String, Integer>) infoAnterior.get("posicionFinal");

                resumen.append(String.format("  %s: (%d,%d) → (%d,%d)\n",
                        codigo,
                        posFinalAnterior.get("fila"), posFinalAnterior.get("columna"),
                        posInicialActual.get("fila"), posInicialActual.get("columna")));
            }
            resumen.append("\n");
        }

        // Camiones sin continuidad
        if (!camionesSinContinuidad.isEmpty()) {
            resumen.append("✗ CAMIONES SIN CONTINUIDAD (").append(camionesSinContinuidad.size()).append("):\n");
            for (String codigo : camionesSinContinuidad) {
                Map<String, Object> infoActual = diccionarioActual.get(codigo);
                Map<String, Object> infoAnterior = diccionarioAnterior.get(codigo);

                @SuppressWarnings("unchecked")
                Map<String, Integer> posInicialActual = (Map<String, Integer>) infoActual.get("posicionInicial");
                @SuppressWarnings("unchecked")
                Map<String, Integer> posFinalAnterior = (Map<String, Integer>) infoAnterior.get("posicionFinal");

                resumen.append(String.format("  %s: (%d,%d) ≠ (%d,%d)\n",
                        codigo,
                        posFinalAnterior.get("fila"), posFinalAnterior.get("columna"),
                        posInicialActual.get("fila"), posInicialActual.get("columna")));
            }
            resumen.append("\n");
        }

        // Camiones solo en actual
        if (!camionesSoloEnActual.isEmpty()) {
            resumen.append("+ CAMIONES NUEVOS EN ACTUAL (").append(camionesSoloEnActual.size()).append("):\n");
            for (String codigo : camionesSoloEnActual) {
                resumen.append("  ").append(codigo).append("\n");
            }
            resumen.append("\n");
        }

        // Camiones solo en anterior
        if (!camionesSoloEnAnterior.isEmpty()) {
            resumen.append("- CAMIONES QUE DESAPARECIERON (").append(camionesSoloEnAnterior.size()).append("):\n");
            for (String codigo : camionesSoloEnAnterior) {
                resumen.append("  ").append(codigo).append("\n");
            }
            resumen.append("\n");
        }

        return resumen.toString();
    }
}
