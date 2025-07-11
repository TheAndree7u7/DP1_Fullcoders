/**
 * @file camiones.ts
 * @description Utilidades para el manejo de camiones en el componente Mapa
 */

import type { Coordenada } from "../../../types";

/**
 * Función para calcular la rotación de un camión basada en su movimiento
 * @param {Coordenada} from - Coordenada de origen
 * @param {Coordenada} to - Coordenada de destino
 * @returns {number} Ángulo de rotación en grados
 */
export const calcularRotacion = (from: Coordenada, to: Coordenada): number => {
  // Validar que las coordenadas existan
  if (!from || !to || typeof from.x !== 'number' || typeof from.y !== 'number' || 
      typeof to.x !== 'number' || typeof to.y !== 'number') {
    return 0; // Retornar 0 grados si las coordenadas son inválidas
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