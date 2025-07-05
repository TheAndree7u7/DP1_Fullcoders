/**
 * @file coordenadas.ts
 * @description Utilidades para el manejo de coordenadas en el componente Mapa
 */

import type { Coordenada } from "../../../types";

/**
 * Función para parsear una coordenada en formato "(x,y)" a objeto Coordenada
 * Incluye validación para manejar valores inválidos de manera segura
 * @param {string} s - Coordenada en formato "(x,y)"
 * @returns {Coordenada} Objeto coordenada con propiedades x, y
 */
export const parseCoord = (s: string): Coordenada => {
  // Validar que el parámetro existe y es un string
  if (!s || typeof s !== 'string') {
    console.warn('🚨 parseCoord: Valor inválido recibido:', s);
    return { x: 0, y: 0 }; // Coordenada por defecto
  }
  
  const match = s.match(/\((\d+),\s*(\d+)\)/);
  if (!match) {
    console.warn('🚨 parseCoord: Formato de coordenada inválido:', s);
    return { x: 0, y: 0 }; // Coordenada por defecto
  }
  
  return { x: parseInt(match[1]), y: parseInt(match[2]) };
}; 