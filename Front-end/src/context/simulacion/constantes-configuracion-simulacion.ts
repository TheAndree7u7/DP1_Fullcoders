/**
 * @file constantes-configuracion-simulacion.ts
 * @description Constantes de configuración para la simulación de rutas de camiones
 * Contiene valores fijos que determinan el comportamiento de la simulación
 */

/**
 * Hora inicial de la simulación (punto de partida)
 */
export const HORA_INICIAL_SIMULACION = 0;

/**
 * Hora en la que se realiza la primera actualización de datos
 */
export const HORA_PRIMERA_ACTUALIZACION_DATOS = 1;

/**
 * Cantidad de nodos que deben procesarse antes de solicitar nueva actualización al backend
 * Cada 100 nodos representan aproximadamente 2 horas de simulación
 */
export const NODOS_REQUERIDOS_ANTES_ACTUALIZACION = 100;

/**
 * Porcentaje de incremento en cada paso de la simulación
 * Determina qué tan rápido avanzan los camiones en su ruta
 */
export const INCREMENTO_PORCENTAJE_AVANCE_CAMION = 1;

/**
 * Proporción del ciclo en la que se debe solicitar la siguiente solución de forma anticipada
 * 0.75 significa que se solicita cuando se ha completado el 75% del ciclo actual
 */
export const PROPORCION_SOLICITUD_ANTICIPADA = 0.75;

/**
 * Intervalo en milisegundos para actualizar el contador de tiempo real
 */
export const INTERVALO_ACTUALIZACION_CONTADOR_TIEMPO = 1000;

/**
 * Configuración de tiempo para la simulación
 */
export const CONFIGURACION_TIEMPO_SIMULACION = {
  NODOS_POR_ACTUALIZACION_COMPLETA: 100,
  HORAS_POR_CICLO_ACTUALIZACION: 2,
  SEGUNDOS_POR_HORA: 3600,
  FORMATO_HORA_ESPANOL: 'es-ES',
  FORMATO_HORA_COMPLETA: {
    hour: '2-digit' as const,
    minute: '2-digit' as const,
    second: '2-digit' as const,
  },
};

/**
 * Configuración de distancias y movimiento
 */
export const CONFIGURACION_MOVIMIENTO_CAMIONES = {
  DISTANCIA_POR_NODO_EN_KILOMETROS: 1, // En mapa reticular, cada nodo = 1km
  INTERVALO_LOG_CONTADOR_SEGUNDOS: 10, // Cada cuántos segundos hacer log del contador
}; 