/**
 * @file index.ts
 * @description Exportaciones centralizadas de las utilidades del componente Mapa
 */

// Utilidades de coordenadas
export { parseCoord } from "./coordenadas";

// Utilidades de camiones
export { calcularRotacion, esCoordenadaValida } from "./camiones";

// Utilidades de pedidos
export { getPedidosPendientes } from "./pedidos";
export type { PedidoConAsignacion } from "./pedidos";

// Utilidades de averías
export { handleAveriar } from "./averias";

/**
 * Devuelve el color del semáforo según el porcentaje de capacidad GLP
 * @param porcentaje número entre 0 y 100
 * @param azulSiLleno si es true y porcentaje >= 100, devuelve azul (para camiones al inicio)
 */
export function colorSemaforoGLP(porcentaje: number, azulSiLleno = false): string {
  if (azulSiLleno && porcentaje >= 100) return '#3b82f6'; // Azul Tailwind
  if (porcentaje > 75) return '#22c55e'; // Verde Tailwind
  if (porcentaje >= 40) return '#eab308'; // Amarillo Tailwind
  return '#f97316'; // Naranja Tailwind
} 