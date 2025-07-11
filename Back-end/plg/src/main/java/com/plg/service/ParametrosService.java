package com.plg.service;

import org.springframework.stereotype.Service;

import com.plg.dto.ParametrosDto;
import com.plg.dto.request.ParametrosRequest;
import com.plg.repository.ParametrosRepository;
import com.plg.utils.Parametros;
import com.plg.utils.TipoSimulacion;

/**
 * Servicio para operaciones sobre parámetros de simulación.
 */
@Service
public class ParametrosService {

    private final ParametrosRepository parametrosRepository;

    public ParametrosService(ParametrosRepository parametrosRepository) {
        this.parametrosRepository = parametrosRepository;
    }

    /**
     * Obtiene todos los parámetros actuales de la simulación.
     * 
     * @return DTO con todos los parámetros actuales
     */
    public ParametrosDto obtenerParametros() {
        Parametros parametros = parametrosRepository.obtenerParametros();
        return convertirAParametrosDto(parametros);
    }

    /**
     * Actualiza los parámetros de simulación con los valores proporcionados.
     * Solo actualiza los campos que no son null en el request.
     * 
     * @param request Request con los nuevos valores de parámetros
     * @return DTO con los parámetros actualizados
     */
    public ParametrosDto actualizarParametros(ParametrosRequest request) {
        Parametros parametrosActualizados = parametrosRepository.actualizarParametros(
                request.getDia(),
                request.getMes(),
                request.getAnho(),
                request.getFechaInicial(),
                request.getFechaFinal(),
                request.getIntervaloTiempo(),
                request.getContadorPrueba(),
                request.getKilometrosRecorridos(),
                request.getFitnessGlobal(),
                request.getSemillaAleatoria(),
                request.getPrimeraLlamada(),
                request.getTipoSimulacion());

        System.out.println("⚙️ Parámetros actualizados desde frontend");
        return convertirAParametrosDto(parametrosActualizados);
    }

    /**
     * Reinicia todos los parámetros a sus valores por defecto.
     * 
     * @return DTO con los parámetros reiniciados
     */
    public ParametrosDto reiniciarParametros() {
        Parametros parametrosReiniciados = parametrosRepository.reiniciarParametros();
        System.out.println("🔄 Parámetros reiniciados a valores por defecto");
        return convertirAParametrosDto(parametrosReiniciados);
    }

    /**
     * Obtiene un parámetro específico por su nombre.
     * 
     * @param nombreParametro Nombre del parámetro a obtener
     * @return Valor del parámetro como String
     */
    public String obtenerParametroEspecifico(String nombreParametro) {
        switch (nombreParametro.toLowerCase()) {
            case "dia":
                return Parametros.dia;
            case "mes":
                return Parametros.mes;
            case "anho":
                return Parametros.anho;
            case "intervalotiempo":
                return String.valueOf(Parametros.intervaloTiempo);
            case "contadorprueba":
                return String.valueOf(Parametros.contadorPrueba);
            case "kilometrosrecorridos":
                return String.valueOf(Parametros.kilometrosRecorridos);
            case "fitnessglobal":
                return String.valueOf(Parametros.fitnessGlobal);
            case "semillaaleatoria":
                return String.valueOf(Parametros.semillaAleatoria);
            case "primerallamada":
                return String.valueOf(Parametros.primera_llamada);
            case "tiposimulacion":
                return Parametros.tipoSimulacion.name();
            default:
                throw new IllegalArgumentException("Parámetro no encontrado: " + nombreParametro);
        }
    }

    /**
     * Convierte un objeto Parametros a ParametrosDto.
     * 
     * @param parametros Objeto Parametros a convertir
     * @return DTO con los valores de los parámetros
     */
    private ParametrosDto convertirAParametrosDto(Parametros parametros) {
        return new ParametrosDto(
                Parametros.dia,
                Parametros.mes,
                Parametros.anho,
                Parametros.fecha_inicial,
                Parametros.fecha_final,
                Parametros.intervaloTiempo,
                Parametros.contadorPrueba,
                Parametros.kilometrosRecorridos,
                Parametros.fitnessGlobal,
                Parametros.semillaAleatoria,
                Parametros.primera_llamada,
                Parametros.tipoSimulacion);
    }
}