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