/**
 * @file index.ts
 * @description Punto de entrada para todas las utilidades del contexto de simulación
 */

// Exportar utilidades de coordenadas
export { parseCoord } from './coordenadas';

// Exportar utilidades de camiones
export { adaptarCamionParaCalculos } from './camiones';

// Exportar utilidades de tiempo
export { formatearTiempoTranscurrido, calcularTimestampSimulacion } from './tiempo';

// Exportar controles de simulación
export { 
  pausarSimulacion, 
  reanudarSimulacion, 
  iniciarContadorTiempo, 
  iniciarPollingPrimerPaquete 
} from './controles';

// Exportar utilidades de estado
export { 
  capturarEstadoCompleto, 
  generarResumenEstado,
  convertirEstadoParaBackend,
  type EstadoSimulacionCompleto 
} from './estado';

// Exportar utilidades de ubicación de camiones
export { 
  determinarUbicacionCamion,
  determinarUbicacionFinalCamion,
  camionConsumioTodosLosNodos
} from './ubicacionCamion';

// Exportar utilidades de velocidad
export {
  calcularVelocidadPromedioCamiones,
  calcularIntervaloDinamico,
  obtenerVelocidadCamionEspecifico,
  formatearVelocidad,
  obtenerRangoVelocidad
} from './velocidad'; 