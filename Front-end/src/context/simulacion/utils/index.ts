/**
 * @file index.ts
 * @description Punto de entrada para todas las utilidades del contexto de simulación
 */

// Exportar utilidades de coordenadas
export { parseCoord } from './coordenadas';

// Exportar utilidades de camiones
export { adaptarCamionParaCalculos } from './camiones';

// Exportar utilidades de tiempo
export { } from './tiempo';

// Exportar controles de simulación
export { 
  pausarSimulacion, 
  reanudarSimulacion, 
  iniciarContadorTiempo, 
  iniciarPollingPrimerPaquete 
} from './controles'; 