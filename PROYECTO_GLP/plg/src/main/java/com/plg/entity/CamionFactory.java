package com.plg.entity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Patrón fábrica para crear camiones operativos y averiados.
 */
public class CamionFactory {

    public static final List<Camion> camiones = new ArrayList<>();
    private static final Map<TipoCamion, Integer> contadorCamiones = new HashMap<>();

    static {
        // Inicializamos el contador para cada tipo de camión
        for (TipoCamion tipo : TipoCamion.values()) {
            contadorCamiones.put(tipo, 0);
        }
    }

    /**
     * Crea un camión operativo listo para operar.
     *
     * @param codigo             Código único del camión
     * @param tipo               Tipo de camión (ej. TipoCamion.TA, TipoCamion.TB)
     * @param capacidad          Capacidad de GLP en m³
     * @param tara               Tara en toneladas
     * @param coordenada         Posición inicial del camión en el mapa
     * @param combustibleInicial Combustible inicial en galones
     * @param pesoCarga          Peso de la carga en toneladas
     * @return instancia de Camion con estado DISPONIBLE
     */
    public static Camion crearCamionOperativo(
            String codigo,
            TipoCamion tipo,
            double capacidadMaximaGLP,
            double tara,
            Coordenada coordenada,
            double combustibleInicial,
            double pesoCarga) {

        return Camion.builder()
                .codigo(codigo)
                .tipo(tipo)
                .capacidadMaximaGLP(capacidadMaximaGLP)
                .capacidadActualGLP(capacidadMaximaGLP)
                .tara(tara)
                .pesoCarga(pesoCarga)
                .pesoCombinado(tara + pesoCarga)
                .combustibleActual(combustibleInicial)
                .combustibleMaximo(25)
                .velocidadPromedio(50.0)
                .estado(EstadoCamion.DISPONIBLE)
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.NORMAL)
                .build();
    }

    /**
     * Crea un camión averiado e inmovilizado.
     *
     * @param codigo     Código único del camión
     * @param tipo       Tipo de camión (ej. TipoCamion.TA, TipoCamion.TB)
     * @param capacidadMaximaGLP  capacidadMaximaGLP de GLP en m³
     * @param tara       Tara en toneladas
     * @param coordenada Posición del camión averiado en el mapa
     * @param pesoCarga  Peso de la carga en toneladas
     * @return instancia de Camion con estado INMOVILIZADO_POR_AVERIA
     */
    public static Camion crearCamionAveriado(
            String codigo,
            TipoCamion tipo,
            double capacidadMaximaGLP,
            double tara,
            Coordenada coordenada,
            double pesoCarga) {
        return Camion.builder()
                .codigo(codigo)
                .tipo(tipo)
                .capacidadMaximaGLP(capacidadMaximaGLP)
                .capacidadActualGLP(0)
                .tara(tara)
                .pesoCarga(pesoCarga)
                .pesoCombinado(tara + pesoCarga)
                .combustibleActual(0)
                .velocidadPromedio(50.0)
                .estado(EstadoCamion.INMOVILIZADO_POR_AVERIA)
                .coordenada(coordenada)
                .bloqueado(false)
                .gScore(0)
                .fScore(0)
                .tipoNodo(TipoNodo.CAMION_AVERIADO)
                .build();
    }

    /**
     * Crea un camión de un tipo dado, operativo o averiado.
     *
     * @param tipo        Tipo de camión (TipoCamion.TA, TipoCamion.TB, TipoCamion.TC, TipoCamion.TD).
     * @param operativo   true para camiones operativos, false para averiados.
     * @param coordenada  Posición inicial de los camiones.
     * @return instancia de Camion configurada según el tipo y estado.
     */
    public static Camion crearCamionesPorTipo(TipoCamion tipo, boolean operativo, Coordenada coordenada) {
        double tara, capacidadGLP, pesoCarga = 0.0;
        switch (tipo) {
            case TA: tara = 2.5; capacidadGLP = 25.0; pesoCarga = 12.5; break;
            case TB: tara = 2.0; capacidadGLP = 15.0; pesoCarga = 7.5; break;
            case TC: tara = 1.5; capacidadGLP = 10.0; pesoCarga = 5; break;
            case TD: tara = 1.0; capacidadGLP = 5.0; pesoCarga = 2.5; break;
            default: throw new IllegalArgumentException("Tipo de camión no válido: " + tipo);
        }

        // Actualizamos el contador y generamos el código único
        int numeroCamion = contadorCamiones.get(tipo) + 1;
        contadorCamiones.put(tipo, numeroCamion);
        String codigo = tipo.name() + String.format("%02d", numeroCamion);

        if (operativo) {
            Camion camion = crearCamionOperativo(codigo, tipo, capacidadGLP, tara, coordenada, 25.0, pesoCarga);
            camion.calcularDistanciaMaxima(); // Calculamos la distancia máxima
            camiones.add(camion); // Agregamos el camión operativo a la lista
            return camion;
        } else {
            return crearCamionAveriado(codigo, tipo, capacidadGLP, tara, coordenada, pesoCarga);
        }
    }

    /**
     * Obtiene el número de camiones creados por tipo.
     *
     * @param tipo Tipo de camión
     * @return número de camiones creados de ese tipo
     */
    public static int getCantidadCamionesPorTipo(TipoCamion tipo) {
        return contadorCamiones.getOrDefault(tipo, 0);
    }
}