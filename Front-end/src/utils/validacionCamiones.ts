/**
 * @file validacionCamiones.ts
 * @description Utilidades para validar y formatear valores de camiones
 */

/**
 * Valida si un valor numérico es válido (no es NaN ni Infinity)
 * @param value - Valor a validar
 * @returns true si el valor es válido, false en caso contrario
 */
export const esValorValido = (value: number): boolean => {
  return typeof value === 'number' && !isNaN(value) && isFinite(value);
};

/**
 * Obtiene un valor seguro para mostrar en la interfaz
 * @param value - Valor original
 * @param valorPorDefecto - Valor a mostrar si el original es inválido (por defecto -1)
 * @returns El valor original si es válido, o el valor por defecto
 */
export const obtenerValorSeguro = (value: number, valorPorDefecto: number = -1): number => {
  return esValorValido(value) ? value : valorPorDefecto;
};

/**
 * Formatea un valor de combustible para mostrar en la interfaz
 * @param combustibleActual - Valor actual de combustible
 * @param combustibleMaximo - Valor máximo de combustible
 * @returns String formateado para mostrar
 */
export const formatearCombustible = (combustibleActual: number, combustibleMaximo: number): string => {
  const actualSeguro = obtenerValorSeguro(combustibleActual, -1);
  const maximoSeguro = obtenerValorSeguro(combustibleMaximo, 0);
  
  if (actualSeguro === -1) {
    return "ERROR / " + (maximoSeguro > 0 ? maximoSeguro.toString() : "0");
  }
  
  return `${actualSeguro.toFixed(2)} / ${maximoSeguro}`;
};

/**
 * Formatea un valor de capacidad GLP para mostrar en la interfaz
 * @param capacidadActual - Valor actual de capacidad
 * @param capacidadMaxima - Valor máximo de capacidad
 * @returns String formateado para mostrar
 */
export const formatearCapacidadGLP = (capacidadActual: number, capacidadMaxima: number): string => {
  const actualSeguro = obtenerValorSeguro(capacidadActual, -1);
  const maximaSegura = obtenerValorSeguro(capacidadMaxima, 0);
  
  if (actualSeguro === -1) {
    return "ERROR / " + (maximaSegura > 0 ? maximaSegura.toString() : "0");
  }
  
  return `${actualSeguro.toFixed(2)} / ${maximaSegura}`;
};

/**
 * Obtiene la clase CSS para el color del valor según si es válido o no
 * @param value - Valor a evaluar
 * @returns Clase CSS para el color
 */
export const obtenerClaseColorValor = (value: number): string => {
  if (esValorValido(value)) {
    return "text-green-700 font-bold";
  } else {
    return "text-red-600 font-bold";
  }
};

/**
 * Calcula la cantidad de GLP que entregará un camión específico
 * @param camionId - ID del camión
 * @param rutasCamiones - Array de rutas de camiones
 * @param camiones - Array de estados de camiones
 * @returns Cantidad total de GLP que entregará el camión
 */
export const calcularGLPEntregaPorCamion = (
  camionId: string,
  rutasCamiones: any[],
  camiones: any[]
): number => {
  const ruta = rutasCamiones.find(r => r.id === camionId);
  const camion = camiones.find(c => c.id === camionId);
  
  if (!ruta || !camion) {
    return 0;
  }

  let totalEntrega = 0;
  
  // Sumar la cantidad que entregará a cada pedido
  ruta.pedidos.forEach((pedido: any) => {
    const cantidadEntrega = Math.min(
      pedido.volumenGLPAsignado,
      camion.capacidadActualGLP || 0
    );
    totalEntrega += cantidadEntrega;
  });

  return totalEntrega;
}; 