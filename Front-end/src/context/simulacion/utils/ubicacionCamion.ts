/**
 * @file ubicacionCamion.ts
 * @description Utilidades para manejar la ubicación de los camiones
 */

import type { CamionEstado, RutaCamion } from "../types";

/**
 * @function determinarUbicacionCamion
 * @description Determina la ubicación de un camión basándose en su estado anterior y ruta actual
 * @param anterior - Estado anterior del camión
 * @param ruta - Ruta actual del camión
 * @returns La ubicación del camión
 */
export const determinarUbicacionCamion = (
  anterior: CamionEstado | undefined,
  ruta: RutaCamion
): string => {
  // Si ya tiene ubicación anterior, mantenerla (esto evita teletransporte)
  if (anterior?.ubicacion) {
    console.log(`🔍 DEBUG: Camión ${ruta.id} - Manteniendo ubicación anterior: "${anterior.ubicacion}"`);
    return anterior.ubicacion;
  }
  
  // Si tiene ruta con nodos, usar la primera posición
  if (ruta.ruta && ruta.ruta.length > 0) {
    const ubicacion = ruta.ruta[0];
    console.log(`🔍 DEBUG: Camión ${ruta.id} - Usando primera posición de ruta: "${ubicacion}"`);
    return ubicacion;
  }
  
  // Si no tiene ruta, usar la coordenada del almacén central
  console.log(`🔍 DEBUG: Camión ${ruta.id} - Sin ruta, usando almacén central: "(8,12)"`);
  return '(8,12)';
};

/**
 * @function determinarUbicacionFinalCamion
 * @description Determina la ubicación final de un camión que ya consumió todos sus nodos
 * @param ruta - Ruta del camión
 * @returns La ubicación final del camión
 */
export const determinarUbicacionFinalCamion = (ruta: RutaCamion): string => {
  if (ruta.ruta && ruta.ruta.length > 0) {
    const ultimaPosicion = ruta.ruta[ruta.ruta.length - 1];
    console.log(`🔍 DEBUG: Camión ${ruta.id} - Ubicación final: "${ultimaPosicion}"`);
    return ultimaPosicion;
  }
  
  // Si no tiene ruta, usar la coordenada del almacén central
  console.log(`🔍 DEBUG: Camión ${ruta.id} - Sin ruta, ubicación final en almacén central: "(8,12)"`);
  return '(8,12)';
};

/**
 * @function camionConsumioTodosLosNodos
 * @description Verifica si un camión ya consumió todos los nodos de su ruta
 * @param camion - Estado del camión
 * @param ruta - Ruta del camión
 * @returns true si el camión consumió todos los nodos
 */
export const camionConsumioTodosLosNodos = (
  camion: CamionEstado,
  ruta: RutaCamion
): boolean => {
  if (!ruta.ruta || ruta.ruta.length === 0) {
    return false;
  }
  
  // Si el porcentaje es mayor o igual a la longitud de la ruta, consumió todos los nodos
  const consumioTodos = camion.porcentaje >= ruta.ruta.length - 1;
  
  if (consumioTodos) {
    console.log(`🔍 DEBUG: Camión ${camion.id} - Ya consumió todos los nodos (${camion.porcentaje}/${ruta.ruta.length - 1})`);
  }
  
  return consumioTodos;
}; 