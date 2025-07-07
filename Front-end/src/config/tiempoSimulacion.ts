/**
 * Configuración de tiempos para la simulación
 */

/**
 * Tiempo por defecto en segundos para actualizar paquetes desde el backend
 * Este valor se usa como valor inicial para el intervalo de actualización
 * Puede ser modificado en tiempo real desde la interfaz de usuario
 */
export const TIEMPO_ACTUALIZACION_SEGUNDOS = 30;

/**
 * Rango permitido para el tiempo de actualización (en segundos)
 */
export const TIEMPO_ACTUALIZACION_MIN = 10;
export const TIEMPO_ACTUALIZACION_MAX = 300;
export const TIEMPO_ACTUALIZACION_STEP = 5;
