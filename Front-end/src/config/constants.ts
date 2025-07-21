/**
 * @file constants.ts
 * @description Constantes de configuración para la funcionalidad de averías automáticas
 */

/**
 * Configuración de averías automáticas
 */
export const AVERIAS_AUTOMATICAS_CONFIG = {
  // Activar/desactivar la funcionalidad
  ACTIVADO: true,
  
  // Cada cuántos paquetes debe ocurrir una avería automática
  PAQUETES_PARA_AVERIA: 2,
  
  // Porcentaje del tiempo del intervalo donde debe ocurrir la avería (entre 5% y 35%)
  PORCENTAJE_MINIMO_TIEMPO: 0.05, // 5%
  PORCENTAJE_MAXIMO_TIEMPO: 0.35, // 35%
  
  // Tipos de avería disponibles (1, 2, 3)
  TIPOS_AVERIA_DISPONIBLES: [1, 2, 3],
  
  // Prioridad para seleccionar camiones (menor capacidad primero)
  PRIORIDAD_CAPACIDAD_MINIMA: true,
  
  // Estados de camión válidos para averías automáticas
  ESTADOS_VALIDOS: ['En Ruta'],
  
  // Tipos de nodo válidos para averías (excluir PEDIDO)
  TIPOS_NODO_VALIDOS: ['NORMAL', 'ALMACEN', 'INTERMEDIO', 'ALMACEN_RECARGA']
} as const;

/**
 * Tipos de avería disponibles
 */
export const TIPOS_AVERIA = {
  TIPO_1: 1,
  TIPO_2: 2,
  TIPO_3: 3
} as const; 