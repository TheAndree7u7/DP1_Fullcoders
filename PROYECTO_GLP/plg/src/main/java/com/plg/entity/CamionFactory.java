package com.plg.entity;

import com.plg.entity.Coordenada;
import com.plg.entity.EstadoCamion;
import com.plg.entity.TipoNodo;
import com.plg.entity.Camion;
import com.plg.entity.TipoCamion;
import java.util.ArrayList;
import java.util.List;

/**
 * Patrón fábrica para crear camiones operativos y averiados.
 */
public class CamionFactory {

    /**
     * Crea un camión operativo listo para operar.
     *
     * @param codigo             Código único del camión
     * @param tipo               Tipo de camión (ej. TipoCamion.TA, TipoCamion.TB)
     * @param capacidad          Capacidad de GLP en m³
     * @param tara               Tara en toneladas
     * @param coordenada         Posición inicial del camión en el mapa
     * @param combustibleInicial Combustible inicial en galones
     * @return instancia de Camion con estado DISPONIBLE
     */
    public static Camion crearCamionOperativo(
            String codigo,
            TipoCamion tipo,
            double capacidad,
            double tara,
            Coordenada coordenada,
            double combustibleInicial) {
        return Camion.builder()
                .codigo(codigo)
                .tipo(tipo)
                .capacidad(capacidad)
                .capacidadDisponible(capacidad)
                .tara(tara)
                .pesoCarga(0)
                .pesoCombinado(tara)
                .combustibleActual(combustibleInicial)
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
     * @param capacidad  Capacidad de GLP en m³
     * @param tara       Tara en toneladas
     * @param coordenada Posición del camión averiado en el mapa
     * @return instancia de Camion con estado INMOVILIZADO_POR_AVERIA
     */
    public static Camion crearCamionAveriado(
            String codigo,
            TipoCamion tipo,
            double capacidad,
            double tara,
            Coordenada coordenada) {
        return Camion.builder()
                .codigo(codigo)
                .tipo(tipo)
                .capacidad(capacidad)
                .capacidadDisponible(0)
                .tara(tara)
                .pesoCarga(0)
                .pesoCombinado(tara)
                .combustibleActual(0)
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
        double tara, capacidadGLP;
        switch (tipo) {
            case TA: tara = 2.5; capacidadGLP = 25.0; break;
            case TB: tara = 2.0; capacidadGLP = 15.0; break;
            case TC: tara = 1.5; capacidadGLP = 10.0; break;
            case TD: tara = 1.0; capacidadGLP = 5.0; break;
            default: throw new IllegalArgumentException("Tipo de camión no válido: " + tipo);
        }
        if (operativo) {
            return crearCamionOperativo(tipo.name() + "01", tipo, capacidadGLP, tara, coordenada, 25.0);
        } else {
            return crearCamionAveriado(tipo.name() + "01", tipo, capacidadGLP, tara, coordenada);
        }
    }
}