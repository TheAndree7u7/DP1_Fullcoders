/**
 * @file constants.ts
 * @description Constantes de configuración para la aplicación
 */

// ============================
// CONFIGURACIÓN DE AVERÍAS AUTOMÁTICAS POR TIPO DE SIMULACIÓN
// ============================

/**
 * @constant AVERIAS_AUTOMATICAS_POR_TIPO
 * @description Controla si las averías automáticas están activadas según el tipo de simulación
 * 
 * DIARIA: Las averías automáticas están desactivadas (simulación en tiempo real)
 * SEMANAL: Las averías automáticas están activadas (simulación semanal)
 * COLAPSO: Las averías automáticas están activadas (simulación de colapso)
 */
export const AVERIAS_AUTOMATICAS_POR_TIPO = {
  DIARIA: false,    // Desactivadas para simulación diaria
  SEMANAL: false,    // Activadas para simulación semanal
  COLAPSO: false     // Activadas para simulación de colapso
};

/**
 * @function obtenerAveriasAutomaticasActivas
 * @description Obtiene si las averías automáticas están activadas según el tipo de simulación
 * @param {string} tipoSimulacion - Tipo de simulación ('DIARIA', 'SEMANAL', 'COLAPSO')
 * @returns {boolean} true si las averías automáticas están activadas para el tipo de simulación
 */
export const obtenerAveriasAutomaticasActivas = (tipoSimulacion: string): boolean => {
  return AVERIAS_AUTOMATICAS_POR_TIPO[tipoSimulacion as keyof typeof AVERIAS_AUTOMATICAS_POR_TIPO] ?? false;
};

// ============================
// OTRAS CONSTANTES DE CONFIGURACIÓN
// ============================

/**
 * @constant CONTROLES_SIMULACION_HABILITADOS
 * @description Controla si los controles de simulación están habilitados
 */
export const CONTROLES_SIMULACION_HABILITADOS = false; 