/**
 * @file index.ts
 * @description Punto de entrada para todas las utilidades del contexto de simulación
 */

// Importar tipos necesarios
import type { RutaCamion, DiccionarioRutasCamiones, NodoRutaCompleto } from '../types';

// Importar utilidades de coordenadas
import { parseCoord } from './coordenadas';

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

/**
 * @function generarDiccionarioRutasCamiones
 * @description Genera un diccionario que mapea cada camión con su ruta completa incluyendo tipos de nodos
 * @param rutasCamiones - Array de rutas de camiones del contexto
 * @returns Diccionario con las rutas completas de cada camión
 */
export const generarDiccionarioRutasCamiones = (
  rutasCamiones: RutaCamion[]
): DiccionarioRutasCamiones => {
  const diccionario: DiccionarioRutasCamiones = {};

  rutasCamiones.forEach((ruta) => {
    const nodosCompletos: NodoRutaCompleto[] = ruta.ruta.map((coordStr, indice) => {
      const coordenada = parseCoord(coordStr);
      const tipo = ruta.tiposNodos?.[indice] || 'NORMAL';
      
      return {
        coordenada,
        tipo,
        indice
      };
    });

    diccionario[ruta.id] = {
      idCamion: ruta.id,
      ruta: nodosCompletos,
      puntoDestino: parseCoord(ruta.puntoDestino),
      pedidos: ruta.pedidos
    };
  });

  return diccionario;
};

/**
 * @function verificarCamionEnNodoAveria
 * @description Verifica si un camión está actualmente en un nodo de tipo avería
 * @param diccionarioRutas - Diccionario de rutas de camiones
 * @param idCamion - ID del camión a verificar
 * @param porcentajeAvance - Porcentaje de avance del camión en su ruta
 * @returns true si el camión está en un nodo de avería, false en caso contrario
 */
export const verificarCamionEnNodoAveria = (
  diccionarioRutas: DiccionarioRutasCamiones,
  idCamion: string,
  porcentajeAvance: number
): boolean => {
  const rutaCamion = diccionarioRutas[idCamion];
  if (!rutaCamion) return false;

  const indiceActual = Math.floor(porcentajeAvance);
  const nodoActual = rutaCamion.ruta[indiceActual];
  
  if (!nodoActual) return false;

  // Verificar si el nodo actual es de tipo avería automática
  return nodoActual.tipo === 'AVERIA_AUTOMATICA_T1' || 
         nodoActual.tipo === 'AVERIA_AUTOMATICA_T2' || 
         nodoActual.tipo === 'AVERIA_AUTOMATICA_T3';
};

/**
 * @function obtenerNodosAveriaEnRuta
 * @description Obtiene todos los nodos de avería en la ruta de un camión
 * @param diccionarioRutas - Diccionario de rutas de camiones
 * @param idCamion - ID del camión
 * @returns Array de nodos de avería en la ruta
 */
export const obtenerNodosAveriaEnRuta = (
  diccionarioRutas: DiccionarioRutasCamiones,
  idCamion: string
): NodoRutaCompleto[] => {
  const rutaCamion = diccionarioRutas[idCamion];
  if (!rutaCamion) return [];

  return rutaCamion.ruta.filter(nodo => 
    nodo.tipo === 'AVERIA_AUTOMATICA_T1' || 
    nodo.tipo === 'AVERIA_AUTOMATICA_T2' || 
    nodo.tipo === 'AVERIA_AUTOMATICA_T3'
  );
}; 