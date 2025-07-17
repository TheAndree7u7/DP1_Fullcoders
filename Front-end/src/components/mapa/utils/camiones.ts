/**
 * @file camiones.ts
 * @description Utilidades para el manejo de camiones en el componente Mapa
 */

import type { Coordenada } from "../../../types";

/**
 * Función para validar si una coordenada es válida
 * @param {Coordenada | undefined | null} coord - Coordenada a validar
 * @returns {boolean} true si la coordenada es válida
 */
export const esCoordenadaValida = (coord: Coordenada | undefined | null): coord is Coordenada => {
  return coord !== null && 
         coord !== undefined && 
         typeof coord === 'object' &&
         typeof coord.x === 'number' && 
         typeof coord.y === 'number' &&
         !isNaN(coord.x) && 
         !isNaN(coord.y);
};

/**
 * Función para calcular la rotación de un camión basada en su movimiento
 * @param {Coordenada} from - Coordenada de origen
 * @param {Coordenada} to - Coordenada de destino
 * @returns {number} Ángulo de rotación en grados
 */
export const calcularRotacion = (from: Coordenada | undefined | null, to: Coordenada | undefined | null): number => {
  // Validar que ambas coordenadas existan y tengan las propiedades x, y
  if (!esCoordenadaValida(from) || !esCoordenadaValida(to)) {
    console.warn('🚨 calcularRotacion: Coordenadas inválidas recibidas:', { from, to });
    return 0; // Rotación por defecto (hacia la derecha)
  }
  
  const dx = to.x - from.x;
  const dy = to.y - from.y;
  
  // Si no hay movimiento, mantener la rotación actual (hacia la derecha por defecto)
  if (dx === 0 && dy === 0) return 0;
  
  // Determinar la dirección basada en el movimiento
  // En SVG, y+ es hacia abajo, y- es hacia arriba
  if (Math.abs(dx) > Math.abs(dy)) {
    // Movimiento principalmente horizontal
    return dx > 0 ? 0 : 180; // Derecha : Izquierda
  } else {
    // Movimiento principalmente vertical  
    return dy > 0 ? 90 : 270; // Abajo : Arriba
  }
}; 