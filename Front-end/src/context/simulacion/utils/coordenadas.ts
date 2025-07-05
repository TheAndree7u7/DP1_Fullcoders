/**
 * @file coordenadas.ts
 * @description Utilidades para el manejo de coordenadas en la simulación
 */

import type { Coordenada } from "../../../types";

/**
 * Función para parsear una coordenada en formato "(x,y)" a objeto Coordenada
 * @param {string} s - Coordenada en formato "(x,y)"
 * @returns {Coordenada} Objeto coordenada con propiedades x, y
 * @throws {Error} Si la coordenada no tiene el formato correcto
 */
export const parseCoord = (s: string): Coordenada => {
  const match = s.match(/\((\d+),\s*(\d+)\)/);
  if (!match) throw new Error(`Coordenada inválida: ${s}`);
  return { x: parseInt(match[1]), y: parseInt(match[2]) };
}; 