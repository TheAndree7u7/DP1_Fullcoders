/**
 * @file simulacion.ts
 * @description Configuración parametrizable para la simulación de rutas
 */

/**
 * Configuración parametrizable del tiempo de simulación
 */
export const CONFIGURACION_TIEMPO = {
  // Duración de cada paquete en minutos (parametrizable)
  DURACION_PAQUETE_MINUTOS: 120, // 2 horas por defecto
  
  // Intervalo entre actualizaciones en milisegundos para la animación
  INTERVALO_ACTUALIZACION_MS: 1000, // 1 segundo por defecto
  
  // Factor de conversión: cuántos minutos de simulación representa cada nodo
  MINUTOS_POR_NODO: function() {
    return this.DURACION_PAQUETE_MINUTOS / 100; // 100 nodos por paquete
  },
  
  // Velocidad de animación (más bajo = más rápido)
  VELOCIDAD_ANIMACION: {
    LENTA: 2000,    // 2 segundos por paso
    NORMAL: 1000,   // 1 segundo por paso
    RAPIDA: 500     // 0.5 segundos por paso
  }
};

/**
 * Función para actualizar la duración de paquetes dinámicamente
 * @param nuevaDuracionMinutos Nueva duración en minutos
 */
export const actualizarDuracionPaquete = (nuevaDuracionMinutos: number): void => {
  if (nuevaDuracionMinutos > 0) {
    CONFIGURACION_TIEMPO.DURACION_PAQUETE_MINUTOS = nuevaDuracionMinutos;
    console.log(`✅ Duración de paquete actualizada a ${nuevaDuracionMinutos} minutos`);
    console.log(`🔄 Cada nodo representa ahora ${CONFIGURACION_TIEMPO.MINUTOS_POR_NODO()} minutos`);
  } else {
    console.warn('⚠️ La duración debe ser mayor a 0 minutos');
  }
};

/**
 * Función para obtener el tiempo total de simulación en horas
 */
export const obtenerTiempoTotalSimulacion = (): number => {
  return CONFIGURACION_TIEMPO.DURACION_PAQUETE_MINUTOS / 60;
};

/**
 * Configuración de estados de simulación
 */
export const ESTADOS_SIMULACION = {
  CARGANDO: 'cargando',
  EJECUTANDO: 'ejecutando',
  PAUSADO: 'pausado',
  COMPLETADO: 'completado',
  ERROR: 'error'
} as const;

export type EstadoSimulacion = typeof ESTADOS_SIMULACION[keyof typeof ESTADOS_SIMULACION];
