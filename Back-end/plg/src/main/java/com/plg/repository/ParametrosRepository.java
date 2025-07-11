package com.plg.repository;

import org.springframework.stereotype.Repository;

import com.plg.utils.Parametros;
import com.plg.utils.TipoSimulacion;

/**
 * Repositorio en memoria para parámetros de simulación.
 * Maneja las operaciones de lectura y escritura de parámetros.
 */
@Repository
public class ParametrosRepository {

    /**
     * Obtiene todos los parámetros actuales de la simulación.
     * 
     * @return Objeto Parametros con todos los valores actuales
     */
    public Parametros obtenerParametros() {
        return Parametros.getInstance();
    }

    /**
     * Actualiza los parámetros de simulación con nuevos valores.
     * Solo actualiza los campos que no son null en el request.
     * 
     * @param dia                  Nuevo día (opcional)
     * @param mes                  Nuevo mes (opcional)
     * @param anho                 Nuevo año (opcional)
     * @param fechaInicial         Nueva fecha inicial (opcional)
     * @param fechaFinal           Nueva fecha final (opcional)
     * @param intervaloTiempo      Nuevo intervalo de tiempo (opcional)
     * @param contadorPrueba       Nuevo contador de prueba (opcional)
     * @param kilometrosRecorridos Nuevos kilómetros recorridos (opcional)
     * @param fitnessGlobal        Nuevo fitness global (opcional)
     * @param semillaAleatoria     Nueva semilla aleatoria (opcional)
     * @param primeraLlamada       Nuevo valor de primera llamada (opcional)
     * @param tipoSimulacion       Nuevo tipo de simulación (opcional)
     * @return Los parámetros actualizados
     */
    public Parametros actualizarParametros(String dia, String mes, String anho,
            java.time.LocalDateTime fechaInicial,
            java.time.LocalDateTime fechaFinal,
            Integer intervaloTiempo, Integer contadorPrueba,
            Double kilometrosRecorridos, Double fitnessGlobal,
            Long semillaAleatoria, Boolean primeraLlamada,
            TipoSimulacion tipoSimulacion) {

        // Actualizar solo los campos que no son null
        if (dia != null) {
            Parametros.dia = dia;
        }
        if (mes != null) {
            Parametros.mes = mes;
        }
        if (anho != null) {
            Parametros.anho = anho;
        }
        if (fechaInicial != null) {
            Parametros.fecha_inicial = fechaInicial;
        }
        if (fechaFinal != null) {
            Parametros.fecha_final = fechaFinal;
        }
        if (intervaloTiempo != null) {
            Parametros.intervaloTiempo = intervaloTiempo;
        }
        if (contadorPrueba != null) {
            Parametros.contadorPrueba = contadorPrueba;
        }
        if (kilometrosRecorridos != null) {
            Parametros.kilometrosRecorridos = kilometrosRecorridos;
        }
        if (fitnessGlobal != null) {
            Parametros.fitnessGlobal = fitnessGlobal;
        }
        if (semillaAleatoria != null) {
            Parametros.semillaAleatoria = semillaAleatoria;
        }
        if (primeraLlamada != null) {
            Parametros.primera_llamada = primeraLlamada;
        }
        if (tipoSimulacion != null) {
            Parametros.tipoSimulacion = tipoSimulacion;
        }

        return Parametros.getInstance();
    }

    /**
     * Reinicia todos los parámetros a sus valores por defecto.
     * 
     * @return Los parámetros reiniciados
     */
    public Parametros reiniciarParametros() {
        // Reiniciar a valores por defecto
        Parametros.dia = "01";
        Parametros.mes = "02";
        Parametros.anho = "2025";
        Parametros.fecha_inicial = null;
        Parametros.fecha_final = null;
        Parametros.intervaloTiempo = 200;
        Parametros.contadorPrueba = 0;
        Parametros.kilometrosRecorridos = 0;
        Parametros.fitnessGlobal = 0;
        Parametros.semillaAleatoria = 12345L;
        Parametros.primera_llamada = true;
        Parametros.tipoSimulacion = TipoSimulacion.SEMANAL;

        return Parametros.getInstance();
    }
}