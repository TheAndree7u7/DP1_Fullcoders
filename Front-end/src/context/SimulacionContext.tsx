/**
 * @file SimulacionContext.tsx
 * @description Contexto de React para manejar el estado global de la simulación de rutas de camiones.
 * Este contexto proporciona funcionalidad para controlar el avance de la simulación,
 * el estado de los camiones y sus rutas, y la sincronización con el backend.
 */

import React, { createContext, useContext, useEffect, useState } from 'react';
import { getMejorIndividuo } from '../services/simulacionApiService';
import { getAlmacenes, type Almacen } from '../services/almacenApiService';
import type { Individuo, Pedido } from '../types';

/**
 * Constantes de configuración de la simulación
 */
const HORA_INICIAL = 0;
const HORA_PRIMERA_ACTUALIZACION = 1;
const NODOS_PARA_ACTUALIZACION = 50;
const INCREMENTO_PORCENTAJE = 1;
const INTERVALO_ACTUALIZACION_BACKEND = 120;

/**
 * @interface CamionEstado
 * @description Representa el estado actual de un camión en la simulación
 * @property {string} id - Identificador único del camión
 * @property {string} ubicacion - Coordenadas actuales del camión en formato "(x,y)"
 * @property {number} porcentaje - Progreso de la ruta (0-100)
 * @property {'En Camino' | 'Entregado'} estado - Estado actual del camión
 */
export interface CamionEstado {
  id: string;
  ubicacion: string; // "(x,y)"
  porcentaje: number;
  estado: 'En Camino' | 'Entregado';
}

/**
 * @interface RutaCamion
 * @description Define la ruta completa de un camión y sus pedidos asociados
 * @property {string} id - Identificador del camión
 * @property {string[]} ruta - Array de coordenadas que forman la ruta
 * @property {string} puntoDestino - Coordenadas del punto final
 * @property {Pedido[]} pedidos - Lista de pedidos asignados al camión
 */
export interface RutaCamion {
  id: string; // camion.codigo
  ruta: string[]; // ["(12,8)", "(13,8)", ...]
  puntoDestino: string; // "(x,y)"
  pedidos: Pedido[];
}

/**
 * @interface SimulacionContextType
 * @description Define la interfaz del contexto de simulación
 * @property {number} horaActual - Hora actual de la simulación
 * @property {CamionEstado[]} camiones - Estado actual de todos los camiones
 * @property {RutaCamion[]} rutasCamiones - Rutas asignadas a cada camión
 * @property {Almacen[]} almacenes - Lista de almacenes disponibles
 * @property {() => void} avanzarHora - Función para avanzar la simulación una hora
 * @property {() => void} reiniciar - Función para reiniciar la simulación
 * @property {boolean} cargando - Estado de carga de datos
 */
interface SimulacionContextType {
  horaActual: number;
  camiones: CamionEstado[];
  rutasCamiones: RutaCamion[];
  almacenes: Almacen[];
  avanzarHora: () => void;
  reiniciar: () => void;
  cargando: boolean;
}

// Creación del contexto con valor inicial undefined
const SimulacionContext = createContext<SimulacionContextType | undefined>(undefined);

/**
 * @component SimulacionProvider
 * @description Proveedor del contexto de simulación que maneja el estado global
 * @param {Object} props - Propiedades del componente
 * @param {React.ReactNode} props.children - Componentes hijos que tendrán acceso al contexto
 */
export const SimulacionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Estados del contexto
  const [horaActual, setHoraActual] = useState<number>(HORA_INICIAL);
  const [camiones, setCamiones] = useState<CamionEstado[]>([]);
  const [rutasCamiones, setRutasCamiones] = useState<RutaCamion[]>([]);
  const [almacenes, setAlmacenes] = useState<Almacen[]>([]);
  const [cargando, setCargando] = useState<boolean>(true);
  const [nodosRestantesAntesDeActualizar, setNodosRestantesAntesDeActualizar] = useState<number>(NODOS_PARA_ACTUALIZACION);
  const [esperandoActualizacion, setEsperandoActualizacion] = useState<boolean>(false);
  const [pasosSinceLastBackendUpdate, setPasosSinceLastBackendUpdate] = useState<number>(0);

  // Cargar almacenes al inicio
  useEffect(() => {
    console.log('🚀 CONTEXTO: Montando contexto y cargando almacenes...');
    cargarAlmacenes();
    cargarDatos(true);
  }, []);

  /**
   * @function cargarAlmacenes
   * @description Carga los datos de almacenes desde el backend
   */
  const cargarAlmacenes = async () => {
    try {
      console.log('🔄 ALMACENES: Llamando a getAlmacenes...');
      const data = await getAlmacenes();
      console.log('✅ ALMACENES: Datos recibidos:', data);
      setAlmacenes(data);
      console.log('💾 ALMACENES: Estado actualizado con', data.length, 'almacenes');
    } catch (error) {
      console.error('❌ ALMACENES: Error al cargar almacenes:', error);
    }
  };

  /**
   * @function cargarDatos
   * @description Carga los datos de simulación desde el backend
   * @param {boolean} esInicial - Indica si es la carga inicial
   */
  const cargarDatos = async (esInicial: boolean = false) => {
    if (esInicial) setCargando(true);
    try {
      console.log('🔄 CONTEXTO: Cargando datos desde API...');
      const data: Individuo = await getMejorIndividuo();
      
      console.log('📊 CONTEXTO: Datos completos recibidos:', data);
      console.log('🧬 CONTEXTO: Cromosoma tiene', data.cromosoma?.length || 0, 'genes');
      
      // Debug detallado del cromosoma
      data.cromosoma?.forEach((gen, index) => {
        console.log(`🧬 Gen ${index}:`, {
          camion: gen.camion?.codigo,
          nodos: gen.nodos?.length || 0,
          pedidos: gen.pedidos?.length || 0,
          pedidosDetalles: gen.pedidos
        });
      });
      
      const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen) => ({
        id: gen.camion.codigo,
        ruta: gen.nodos.map(n => `(${n.coordenada.x},${n.coordenada.y})`),
        puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
        pedidos: gen.pedidos,
      }));

      console.log('🚚 CONTEXTO: Rutas generadas:', nuevasRutas);
      console.log('📦 CONTEXTO: Total de pedidos en todas las rutas:', 
                  nuevasRutas.reduce((total, ruta) => total + ruta.pedidos.length, 0));

      const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
        const anterior = camiones.find(c => c.id === ruta.id);
        const ubicacion = anterior?.ubicacion ?? ruta.ruta[0];
        return {
          id: ruta.id,
          ubicacion,
          porcentaje: 0,
          estado: 'En Camino',
        };
      });

      setRutasCamiones(nuevasRutas);
      setCamiones(nuevosCamiones);
      if (esInicial) setHoraActual(HORA_PRIMERA_ACTUALIZACION);
      setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
      setEsperandoActualizacion(false);
      
      console.log('✅ CONTEXTO: Estados actualizados exitosamente');
    } catch (error) {
      console.error("❌ CONTEXTO: Error al cargar datos de simulación:", error);
    } finally {
      if (esInicial) setCargando(false);
    }
  };

  /**
   * @function avanzarHora
   * @description Avanza la simulación una hora, actualizando la posición de los camiones
   * y recargando datos del backend cuando sea necesario
   */
  const avanzarHora = async () => {
    if (esperandoActualizacion) return;

    // Incrementar contador de pasos desde última actualización del backend
    const nuevosPasos = pasosSinceLastBackendUpdate + 1;
    setPasosSinceLastBackendUpdate(nuevosPasos);

    const nuevosCamiones = camiones.map((camion) => {
      const ruta = rutasCamiones.find(r => r.id === camion.id);
      if (!ruta) return camion;

      const siguientePaso = camion.porcentaje + INCREMENTO_PORCENTAJE;
      const rutaLength = ruta.ruta.length;

      if (siguientePaso >= rutaLength) {
        return { ...camion, estado: 'Entregado' as const, porcentaje: rutaLength - 1 };
      }

      return {
        ...camion,
        porcentaje: siguientePaso,
        ubicacion: ruta.ruta[siguientePaso],
      };
    });

    const quedan = nodosRestantesAntesDeActualizar - 1;
    setNodosRestantesAntesDeActualizar(quedan);

    // Decidir si actualizar desde backend basado en tiempo transcurrido
    const deberiaActualizarBackend = quedan <= 0 || nuevosPasos >= INTERVALO_ACTUALIZACION_BACKEND;

    if (deberiaActualizarBackend) {
      console.log(`🔄 Actualizando desde backend después de ${nuevosPasos} pasos`);
      setEsperandoActualizacion(true);
      setCamiones(nuevosCamiones);
      setHoraActual(prev => prev + 1);
      setPasosSinceLastBackendUpdate(0); // Reset contador
      await cargarDatos(false);
    } else {
      // Solo actualización local (interpolación)
      setCamiones(nuevosCamiones);
      setHoraActual(prev => prev + 1);
    }
  };

  /**
   * @function reiniciar
   * @description Reinicia la simulación a su estado inicial
   */
  const reiniciar = () => {
    const nuevosCamiones: CamionEstado[] = rutasCamiones.map((ruta) => ({
      id: ruta.id,
      ubicacion: ruta.ruta[0],
      porcentaje: 0,
      estado: 'En Camino' as const,
    }));
    setCamiones(nuevosCamiones);
    setHoraActual(HORA_PRIMERA_ACTUALIZACION);
    setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
    setEsperandoActualizacion(false);
  };

  return (
    <SimulacionContext.Provider
      value={{ horaActual, camiones, rutasCamiones, almacenes, avanzarHora, reiniciar, cargando }}
    >
      {children}
    </SimulacionContext.Provider>
  );
};

/**
 * @function useSimulacion
 * @description Hook personalizado para acceder al contexto de simulación
 * @returns {SimulacionContextType} El contexto de simulación
 * @throws {Error} Si se usa fuera de un SimulacionProvider
 */
export const useSimulacion = (): SimulacionContextType => {
  const context = useContext(SimulacionContext);
  if (!context) throw new Error('useSimulacion debe usarse dentro de SimulacionProvider');
  return context;
};
