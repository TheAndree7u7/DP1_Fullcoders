/**
 * @file index.ts
 * @description Punto de entrada para todos los módulos del contexto de simulación
 */

// Exportar tipos y constantes
export * from './types';

// Exportar utilidades existentes (excepto las que se refactorizaron)
export { 
  parseCoord, 
  adaptarCamionParaCalculos,
  formatearTiempoTranscurrido, 
  calcularTimestampSimulacion,
  pausarSimulacion, 
  reanudarSimulacion, 
  iniciarContadorTiempo,
  capturarEstadoCompleto, 
  generarResumenEstado,
  convertirEstadoParaBackend,
  type EstadoSimulacionCompleto 
} from './utils';

// Exportar nuevos módulos refactorizados
export * from './dataManager';
export * from './camionLogic';
export * from './pollingManager';
export * from './stateManager';
export * from './avanceHora'; 