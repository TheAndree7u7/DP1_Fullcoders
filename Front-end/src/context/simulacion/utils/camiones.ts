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
    capacidadActualGLP: camion.capacidadActualGLP,
    capacidadMaximaGLP: camion.capacidadMaximaGLP,
    combustibleActual: camion.combustibleActual,
    combustibleMaximo: camion.combustibleMaximo,
    distanciaMaxima: camion.distanciaMaxima,
    estado: camion.estado,
    pesoCarga: camion.pesoCarga,
    pesoCombinado: camion.pesoCombinado,
    tara: camion.tara,
    tipo: camion.tipo,
    velocidadPromedio: camion.velocidadPromedio,
  };
}; 