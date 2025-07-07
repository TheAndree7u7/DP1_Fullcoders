/**
 * @file camiones.ts
 * @description Utilidades para el manejo de camiones en la simulación
 */

import type { Camion } from "../../../types";
import type { CamionEstado } from "../../SimulacionContext";

/**
 * Función adaptadora para convertir un CamionEstado a un objeto compatible con Camion
 * Esta función es esencial para poder usar las funciones de cálculo en types.ts
 * @param {CamionEstado} camion - Estado del camión a convertir
 * @returns {Camion} Objeto camión compatible con las funciones de cálculo
 */
export const adaptarCamionParaCalculos = (camion: CamionEstado): Camion => {
  return {
    codigo: camion.id,
    coordenada: { x: 0, y: 0 }, // Coordenada por defecto
    capacidadActualGLP: camion.capacidadActualGLP || 0,
    capacidadMaximaGLP: camion.capacidadMaximaGLP || 0,
    combustibleActual: camion.combustibleActual || 0,
    combustibleMaximo: camion.combustibleMaximo || 0,
    distanciaMaxima: camion.distanciaMaxima || 0,
    estado: camion.estado,
    pesoCarga: camion.pesoCarga || 0,
    pesoCombinado: camion.pesoCombinado || 0,
    tara: camion.tara || 0,
    tipo: camion.tipo || 'TA',
    velocidadPromedio: camion.velocidadPromedio || 0,
    tiempoParadaRestante: 0, // Valor por defecto
  };
}; 