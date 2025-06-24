/**
 * @file colors.ts
 * @description Configuración centralizada de colores para los camiones
 * 
 * IMPORTANTE: 
 * - Rojo está reservado para camiones averiados
 * - Negro está reservado para camiones en mantenimiento
 * - Los colores en CAMION_COLORS no incluyen rojo ni negro para evitar confusiones
 * 
 * Cambios realizados:
 * - Eliminado '#ef4444' (rojo claro) y '#f43f5e' (rojo rosa) del array de colores disponibles
 * - Agregado manejo específico de colores para estados de avería y mantenimiento
 */

/**
 * Colores disponibles para camiones en estado normal
 * Excluye rojo (#ef4444, #dc2626, #f43f5e) y negro (#000000) 
 * que están reservados para averías y mantenimiento
 */
export const CAMION_COLORS = [
  '#3b82f6', // Azul
  '#10b981', // Verde esmeralda
  '#f59e0b', // Ámbar
  '#8b5cf6', // Violeta
  '#ec4899', // Rosa
  '#22d3ee', // Cian
  '#a3e635', // Lima
  '#eab308', // Amarillo
  '#06b6d4', // Cian oscuro
  '#84cc16', // Lima oscuro
  '#e879f9', // Fucsia
  '#4ade80', // Verde
  '#f97316', // Naranja
  '#c084fc', // Púrpura claro
  '#2dd4bf', // Turquesa
  '#fde047', // Amarillo claro
  '#facc15', // Amarillo dorado
  '#7dd3fc'  // Azul cielo
];

/**
 * Colores reservados para estados especiales
 */
export const ESTADO_COLORS = {
  AVERIADO: '#dc2626',      // Rojo para averías
  MANTENIMIENTO: '#000000', // Negro para mantenimiento
  DEFAULT: '#3b82f6'        // Azul por defecto
} as const;

/**
 * Función para obtener el color de un camión basado en su estado e índice
 * @param estado - Estado actual del camión
 * @param index - Índice del camión en la lista de rutas (para colores normales)
 * @returns Color hexadecimal correspondiente
 */
export const getCamionColorByState = (estado: string, index: number): string => {
  switch (estado) {
    case 'Averiado':
      return ESTADO_COLORS.AVERIADO;
    case 'En Mantenimiento':
      return ESTADO_COLORS.MANTENIMIENTO;
    default:
      return index >= 0 
        ? CAMION_COLORS[index % CAMION_COLORS.length] 
        : ESTADO_COLORS.DEFAULT;
  }
};
