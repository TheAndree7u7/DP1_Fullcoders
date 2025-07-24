/**
 * @file camiones.ts
 * @description Utilidades para el manejo de camiones en el componente Mapa
 */

import type { Coordenada } from "../../../types";

/**
 * Función para validar si una coordenada es válida
 * @param {Coordenada | undefined | null} coord - Coordenada a validar
 * @returns {boolean} true si la coordenada es válida
 */
export const esCoordenadaValida = (coord: Coordenada | undefined | null): coord is Coordenada => {
  return coord !== null && 
         coord !== undefined && 
         typeof coord === 'object' &&
         typeof coord.x === 'number' && 
         typeof coord.y === 'number' &&
         !isNaN(coord.x) && 
         !isNaN(coord.y);
};

/**
 * Función para calcular la rotación de un camión basada en su movimiento
 * @param {Coordenada} from - Coordenada de origen
 * @param {Coordenada} to - Coordenada de destino
 * @returns {number} Ángulo de rotación en grados
 */
export const calcularRotacion = (from: Coordenada | undefined | null, to: Coordenada | undefined | null): number => {
  // Validar que ambas coordenadas existan y tengan las propiedades x, y
  if (!esCoordenadaValida(from) || !esCoordenadaValida(to)) {
    console.warn('🚨 calcularRotacion: Coordenadas inválidas recibidas:', { from, to });
    return 0; // Rotación por defecto (hacia la derecha)
  }
  
  const dx = to.x - from.x;
  const dy = to.y - from.y;
  
  // Si no hay movimiento, mantener la rotación actual (hacia la derecha por defecto)
  if (dx === 0 && dy === 0) return 0;
  
  // Determinar la dirección basada en el movimiento
  // En SVG, y+ es hacia abajo, y- es hacia arriba
  if (Math.abs(dx) > Math.abs(dy)) {
    // Movimiento principalmente horizontal
    return dx > 0 ? 0 : 180; // Derecha : Izquierda
  } else {
    // Movimiento principalmente vertical  
    return dy > 0 ? 90 : 270; // Abajo : Arriba
  }
};

/**
 * Función para calcular la rotación mejorada considerando el siguiente nodo
 * @param {Coordenada} currentPos - Posición actual interpolada del camión
 * @param {Coordenada} nextPos - Siguiente nodo en la ruta
 * @param {Coordenada} prevPos - Nodo anterior en la ruta (opcional)
 * @returns {number} Ángulo de rotación en grados
 */
export const calcularRotacionMejorada = (
  currentPos: Coordenada | undefined | null,
  nextPos: Coordenada | undefined | null,
  prevPos?: Coordenada | undefined | null
): number => {
  // Si tenemos posición actual y siguiente, calcular dirección hacia el siguiente
  if (esCoordenadaValida(currentPos) && esCoordenadaValida(nextPos)) {
    return calcularRotacion(currentPos, nextPos);
  }
  
  // Si no hay siguiente pero hay anterior, usar la dirección del movimiento anterior
  if (esCoordenadaValida(prevPos) && esCoordenadaValida(currentPos)) {
    return calcularRotacion(prevPos, currentPos);
  }
  
  // Si no hay información suficiente, mantener rotación por defecto
  return 0;
};

/**
 * Función para interpolar la posición del camión entre dos nodos
 * @param {Coordenada} from - Nodo de origen
 * @param {Coordenada} to - Nodo de destino
 * @param {number} factor - Factor de interpolación (0.0 a 1.0)
 * @returns {Coordenada} Posición interpolada
 */
export const interpolarPosicion = (
  from: Coordenada | undefined | null,
  to: Coordenada | undefined | null,
  factor: number
): Coordenada => {
  // Validar coordenadas
  if (!esCoordenadaValida(from) || !esCoordenadaValida(to)) {
    console.warn('🚨 interpolarPosicion: Coordenadas inválidas:', { from, to, factor });
    return { x: 0, y: 0 };
  }
  
  // Limitar factor entre 0 y 1
  const factorClamped = Math.max(0, Math.min(1, factor));
  
  // Interpolación lineal
  const x = from.x + (to.x - from.x) * factorClamped;
  const y = from.y + (to.y - from.y) * factorClamped;
  
  return { x, y };
};

/**
 * Función para calcular la rotación consistente del camión
 * @param {Coordenada[]} rutaCoords - Array de coordenadas de la ruta
 * @param {number} porcentaje - Porcentaje de progreso en la ruta
 * @returns {number} Ángulo de rotación en grados
 */
export const calcularRotacionConsistente = (
  rutaCoords: Coordenada[],
  porcentaje: number
): number => {
  if (!rutaCoords || rutaCoords.length < 2) {
    return 0;
  }
  
  const indiceActual = Math.floor(porcentaje);
  const indiceSiguiente = Math.min(indiceActual + 1, rutaCoords.length - 1);
  const indiceAnterior = Math.max(indiceActual - 1, 0);
  
  const nodoActual = rutaCoords[indiceActual];
  const nodoSiguiente = rutaCoords[indiceSiguiente];
  const nodoAnterior = rutaCoords[indiceAnterior];
  
  // Si estamos en el último nodo, usar la dirección del movimiento anterior
  if (indiceActual === rutaCoords.length - 1) {
    return calcularRotacion(nodoAnterior, nodoActual);
  }
  
  // Si estamos en el primer nodo, usar la dirección hacia el siguiente
  if (indiceActual === 0) {
    return calcularRotacion(nodoActual, nodoSiguiente);
  }
  
  // Para nodos intermedios, siempre usar la dirección hacia el siguiente nodo
  // Esto asegura consistencia independientemente de la posición interpolada
  return calcularRotacion(nodoActual, nodoSiguiente);
};



// Constantes para las direcciones cardinales
const HEADING: Record<'E'|'S'|'W'|'N', number> = { 
  E: 0,   // Este (derecha)
  S: 90,  // Sur (abajo) 
  W: 180, // Oeste (izquierda)
  N: 270  // Norte (arriba)
};

/**
 * Función para determinar la dirección cardinal entre dos puntos
 * @param from - Punto de origen
 * @param to - Punto de destino
 * @returns Ángulo de dirección en grados
 */
export const direction = (from: Coordenada, to: Coordenada): number => {
  if (to.x > from.x) return HEADING.E;  // Este
  if (to.x < from.x) return HEADING.W;  // Oeste
  if (to.y > from.y) return HEADING.S;  // Sur
  return HEADING.N;                     // Norte
};

/**
 * Función para calcular el delta más corto entre dos ángulos
 * @param angleA - Ángulo inicial
 * @param angleB - Ángulo final
 * @returns Delta más corto entre -180 y +180
 */
export const shortestDelta = (angleA: number, angleB: number): number => {
  // Resultado entre -180 y +180
  return ((((angleB - angleA) % 360) + 540) % 360) - 180;
};

/**
 * Función para construir los pasos de la ruta con ángulos absolutos
 * @param path - Array de coordenadas de la ruta
 * @returns Array de pasos con coordenadas y ángulos
 */
export const buildSteps = (path: Coordenada[]) => {
  const steps = [];
  for (let i = 1; i < path.length; i++) {
    steps.push({ 
      to: path[i], 
      angle: direction(path[i-1], path[i]) 
    });
  }
  return steps;
};

/**
 * Función para calcular la rotación optimizada usando el algoritmo de camino más corto
 * @param {Coordenada[]} rutaCoords - Array de coordenadas de la ruta
 * @param {number} porcentaje - Porcentaje de progreso en la ruta
 * @param {number} anguloActual - Ángulo actual del camión
 * @returns {number} Ángulo de rotación optimizado
 */
export const calcularRotacionOptimizada = (
  rutaCoords: Coordenada[],
  porcentaje: number,
  anguloActual: number = 0
): number => {
  if (!rutaCoords || rutaCoords.length < 2) {
    return anguloActual;
  }
  
  const indiceActual = Math.floor(porcentaje);
  const indiceSiguiente = Math.min(indiceActual + 1, rutaCoords.length - 1);
  
  // Si estamos en el último nodo, mantener la dirección actual
  if (indiceActual === rutaCoords.length - 1) {
    return anguloActual;
  }
  
  const nodoActual = rutaCoords[indiceActual];
  const nodoSiguiente = rutaCoords[indiceSiguiente];
  
  // Calcular el ángulo objetivo para el siguiente segmento
  const anguloObjetivo = direction(nodoActual, nodoSiguiente);
  
  // Si ya estamos en el ángulo correcto, mantenerlo
  if (anguloActual === anguloObjetivo) {
    return anguloActual;
  }
  
  // Calcular el delta más corto
  const delta = shortestDelta(anguloActual, anguloObjetivo);
  
  // Aplicar el delta para obtener el nuevo ángulo
  const nuevoAngulo = anguloActual + delta;
  
  // Normalizar a 0-359 grados
  return ((nuevoAngulo % 360) + 360) % 360;
};

/**
 * Función para calcular el ángulo inicial del camión basado en su ruta
 * @param {Coordenada[]} rutaCoords - Array de coordenadas de la ruta
 * @param {number} anguloInicialForzado - Ángulo inicial forzado (opcional)
 * @returns {number} Ángulo inicial en grados
 */
export const calcularAnguloInicial = (
  rutaCoords: Coordenada[],
  anguloInicialForzado?: number
): number => {
  // Si se proporciona un ángulo inicial forzado, usarlo
  if (anguloInicialForzado !== undefined) {
    return anguloInicialForzado;
  }
  
  // Si la ruta tiene al menos 2 puntos, inferir la dirección inicial
  if (rutaCoords && rutaCoords.length >= 2) {
    return direction(rutaCoords[0], rutaCoords[1]);
  }
  
  // Por defecto, mirar al Este (0°)
  return 0;
};

/**
 * Función para calcular la rotación con look-ahead (inicia el giro antes de la esquina)
 * @param {Coordenada[]} rutaCoords - Array de coordenadas de la ruta
 * @param {number} porcentaje - Porcentaje de progreso en la ruta
 * @param {number} anguloActual - Ángulo actual del camión
 * @param {number} anguloInicialForzado - Ángulo inicial forzado (opcional)
 * @returns {number} Ángulo de rotación con look-ahead
 */
export const calcularRotacionConLookAhead = (
  rutaCoords: Coordenada[],
  porcentaje: number,
  anguloActual: number = 0,
  anguloInicialForzado?: number
): number => {
  if (!rutaCoords || rutaCoords.length < 2) {
    return anguloActual;
  }
  
  const indiceActual = Math.floor(porcentaje);
  const factorInterpolacion = porcentaje - indiceActual;
  
  // Si estamos en el primer nodo (porcentaje < 1), usar el ángulo inicial
  if (indiceActual === 0 && factorInterpolacion < 0.1) {
    return calcularAnguloInicial(rutaCoords, anguloInicialForzado);
  }
  
  // Si estamos a más de la mitad del segmento actual, empezar a girar hacia el siguiente
  if (factorInterpolacion >= 0.5 && indiceActual + 1 < rutaCoords.length) {
    const nodoActual = rutaCoords[indiceActual];
    const nodoSiguiente = rutaCoords[indiceActual + 1];
    const anguloObjetivo = direction(nodoActual, nodoSiguiente);
    
    // Calcular el delta más corto
    const delta = shortestDelta(anguloActual, anguloObjetivo);
    
    // Interpolar la rotación basándose en qué tan cerca estamos del siguiente nodo
    const factorRotacion = (factorInterpolacion - 0.5) * 2; // 0 a 1 en la segunda mitad
    const deltaInterpolado = delta * factorRotacion;
    
    const nuevoAngulo = anguloActual + deltaInterpolado;
    return ((nuevoAngulo % 360) + 360) % 360;
  }
  
  // Si estamos en la primera mitad, mantener el ángulo actual
  return anguloActual;
};

/**
 * Función para calcular la posición interpolada mejorada con suavizado
 * @param {Coordenada[]} rutaCoords - Array de coordenadas de la ruta
 * @param {number} porcentaje - Porcentaje de progreso en la ruta (puede ser decimal)
 * @returns {Coordenada} Posición interpolada del camión
 */
export const calcularPosicionInterpoladaMejorada = (
  rutaCoords: Coordenada[],
  porcentaje: number
): Coordenada => {
  // Validar que tengamos coordenadas
  if (!rutaCoords || rutaCoords.length === 0) {
    console.warn('🚨 calcularPosicionInterpoladaMejorada: Ruta vacía');
    return { x: 0, y: 0 };
  }
  
  // Si solo hay un nodo, devolver esa posición
  if (rutaCoords.length === 1) {
    return rutaCoords[0];
  }
  
  // Calcular índices de nodos
  const indiceActual = Math.floor(porcentaje);
  const indiceSiguiente = Math.min(indiceActual + 1, rutaCoords.length - 1);
  
  // Obtener coordenadas de los nodos
  const nodoActual = rutaCoords[indiceActual];
  const nodoSiguiente = rutaCoords[indiceSiguiente];
  
  // Calcular factor de interpolación (parte decimal del porcentaje)
  const factorInterpolacion = porcentaje - indiceActual;
  
  // Si estamos exactamente en un nodo (factor = 0), devolver esa posición
  if (factorInterpolacion === 0) {
    return nodoActual;
  }
  
  // MEJORA: Aplicar suavizado para evitar movimientos bruscos
  // Usar una función de suavizado (easing) para hacer el movimiento más natural
  const factorSuavizado = factorInterpolacion < 0.5 
    ? 2 * factorInterpolacion * factorInterpolacion 
    : 1 - Math.pow(-2 * factorInterpolacion + 2, 2) / 2;
  
  // Interpolar entre los dos nodos con suavizado
  return interpolarPosicion(nodoActual, nodoSiguiente, factorSuavizado);
};

/**
 * Función para calcular la posición interpolada del camión basada en su porcentaje de progreso
 * @param {Coordenada[]} rutaCoords - Array de coordenadas de la ruta
 * @param {number} porcentaje - Porcentaje de progreso en la ruta (puede ser decimal)
 * @returns {Coordenada} Posición interpolada del camión
 */
export const calcularPosicionInterpolada = (
  rutaCoords: Coordenada[],
  porcentaje: number
): Coordenada => {
  // Validar que tengamos coordenadas
  if (!rutaCoords || rutaCoords.length === 0) {
    console.warn('🚨 calcularPosicionInterpolada: Ruta vacía');
    return { x: 0, y: 0 };
  }
  
  // Si solo hay un nodo, devolver esa posición
  if (rutaCoords.length === 1) {
    return rutaCoords[0];
  }
  
  // Calcular índices de nodos
  const indiceActual = Math.floor(porcentaje);
  const indiceSiguiente = Math.min(indiceActual + 1, rutaCoords.length - 1);
  
  // Obtener coordenadas de los nodos
  const nodoActual = rutaCoords[indiceActual];
  const nodoSiguiente = rutaCoords[indiceSiguiente];
  
  // Calcular factor de interpolación (parte decimal del porcentaje)
  const factorInterpolacion = porcentaje - indiceActual;
  
  // Si estamos exactamente en un nodo (factor = 0), devolver esa posición
  if (factorInterpolacion === 0) {
    return nodoActual;
  }
  
  // Interpolar entre los dos nodos
  return interpolarPosicion(nodoActual, nodoSiguiente, factorInterpolacion);
};

/**
 * Función para calcular la rotación suavizada del camión
 * @param {Coordenada[]} rutaCoords - Array de coordenadas de la ruta
 * @param {number} porcentaje - Porcentaje de progreso en la ruta
 * @returns {number} Ángulo de rotación en grados
 */
export const calcularRotacionSuavizada = (
  rutaCoords: Coordenada[],
  porcentaje: number
): number => {
  if (!rutaCoords || rutaCoords.length < 2) {
    return 0;
  }
  
  const indiceActual = Math.floor(porcentaje);
  const indiceSiguiente = Math.min(indiceActual + 1, rutaCoords.length - 1);
  const indiceAnterior = Math.max(indiceActual - 1, 0);
  
  const nodoActual = rutaCoords[indiceActual];
  const nodoSiguiente = rutaCoords[indiceSiguiente];
  const nodoAnterior = rutaCoords[indiceAnterior];
  
  // Si estamos en el último nodo, usar la dirección del movimiento anterior
  if (indiceActual === rutaCoords.length - 1) {
    return calcularRotacion(nodoAnterior, nodoActual);
  }
  
  // Si estamos en el primer nodo, usar la dirección hacia el siguiente
  if (indiceActual === 0) {
    return calcularRotacion(nodoActual, nodoSiguiente);
  }
  
  // MEJORA: Para nodos intermedios, usar siempre la dirección entre nodos
  // en lugar de usar la posición interpolada, para mayor consistencia
  const dx = nodoSiguiente.x - nodoActual.x;
  const dy = nodoSiguiente.y - nodoActual.y;
  
  // Si no hay movimiento, mantener la dirección anterior
  if (dx === 0 && dy === 0) {
    return calcularRotacion(nodoAnterior, nodoActual);
  }
  
  // Determinar la dirección basada en el movimiento entre nodos
  // En SVG, y+ es hacia abajo, y- es hacia arriba
  if (Math.abs(dx) > Math.abs(dy)) {
    // Movimiento principalmente horizontal
    return dx > 0 ? 0 : 180; // Derecha : Izquierda
  } else {
    // Movimiento principalmente vertical  
    return dy > 0 ? 90 : 270; // Abajo : Arriba
  }
}; 