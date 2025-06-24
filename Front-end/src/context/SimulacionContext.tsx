/**
 * @file SimulacionContext.tsx
 * @description Contexto de React para manejar el estado global de la simulación de rutas de camiones.
 * Este contexto proporciona funcionalidad para controlar el avance de la simulación,
 * el estado de los camiones y sus rutas, y la sincronización con el backend.
 */

import React, { createContext, useContext, useEffect, useState } from 'react';
import { getMejorIndividuo } from '../services/simulacionApiService';
import { getAlmacenes, type Almacen } from '../services/almacenApiService';
import type { Pedido, Coordenada, Individuo, Gen, Nodo, Camion } from '../types';
import { calcularPesoCarga, calcularPesoCombinado, calcularConsumoGalones, calcularDistanciaMaxima } from '../types';

/**
 * Constantes de configuración de la simulación
 */
const HORA_INICIAL = 0;
const HORA_PRIMERA_ACTUALIZACION = 1;
const NODOS_PARA_ACTUALIZACION = 100;
//aca 100 nodos significan 2h de tiempo entonces cada nodo 
// representa 1.2 minutos de tiempo real, lo que es un valor razonable para simular el avance 

const INCREMENTO_PORCENTAJE = 1;

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
  estado: 'En Camino' | 'Entregado' | 'Averiado' | 'En Mantenimiento' | 'Disponible';
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;  
  combustibleActual: number;
  combustibleMaximo: number;
  distanciaMaxima: number; 
  pesoCarga: number;
  pesoCombinado: number;
  tara: number;
  tipo: string;
  velocidadPromedio: number;
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
  fechaHoraSimulacion: string | null; // Fecha y hora de la simulación del backend
  diaSimulacion: number | null; // Día extraído de fechaHoraSimulacion
  avanzarHora: () => void;
  reiniciar: () => void;
  cargando: boolean;
  bloqueos: Bloqueo[];
  marcarCamionAveriado: (camionId: string) => void; // Nueva función para manejar averías
}

export interface Bloqueo {
  coordenadas: Coordenada[];
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
  const [bloqueos, setBloqueos] = useState<Bloqueo[]>([]);
  const [fechaHoraSimulacion, setFechaHoraSimulacion] = useState<string | null>(null);
  const [diaSimulacion, setDiaSimulacion] = useState<number | null>(null);

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
      //console.log('🔄 ALMACENES: Llamando a getAlmacenes...');
      const data = await getAlmacenes();
      //console.log('✅ ALMACENES: Datos recibidos:', data);
      setAlmacenes(data);
      //console.log('💾 ALMACENES: Estado actualizado con', data.length, 'almacenes');
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
      console.log("Iniciando solicitud al servidor...");
      type IndividuoConBloqueos = Individuo & { bloqueos?: Bloqueo[], fechaHoraSimulacion?: string };
      const data = await getMejorIndividuo() as IndividuoConBloqueos;
      console.log("Datos recibidos:", data);

      // Actualizar fecha y hora de la simulación
      if (data.fechaHoraSimulacion) {
        setFechaHoraSimulacion(data.fechaHoraSimulacion);
        // Extraer el día de la fecha
        const fecha = new Date(data.fechaHoraSimulacion);
        setDiaSimulacion(fecha.getDate());
        console.log("Fecha de simulación actualizada:", data.fechaHoraSimulacion, "Día:", fecha.getDate());
      }

      const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen: Gen) => ({
        id: gen.camion.codigo,
        ruta: gen.nodos.map((n: Nodo) => `(${n.coordenada.x},${n.coordenada.y})`),
        puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
        pedidos: gen.pedidos,
      }));

      setRutasCamiones(nuevasRutas);

      const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
        const anterior = camiones.find(c => c.id === ruta.id);
        // Buscar el gen correspondiente para obtener los datos completos del camión
        const gen = data.cromosoma.find((g: Gen) => g.camion.codigo === ruta.id);
        const camion = gen?.camion;
        const ubicacion = anterior?.ubicacion ?? ruta.ruta[0];
        return {
          id: ruta.id,
          ubicacion,
          porcentaje: 0,
          estado: camion?.estado === 'DISPONIBLE' ? 'Disponible' : 'En Camino',
          capacidadActualGLP: camion?.capacidadActualGLP ?? 0,
          capacidadMaximaGLP: camion?.capacidadMaximaGLP ?? 0,
          combustibleActual: camion?.combustibleActual ?? 0,
          combustibleMaximo: camion?.combustibleMaximo ?? 0,
          distanciaMaxima: camion?.distanciaMaxima ?? 0,
          pesoCarga: camion?.pesoCarga ?? 0,
          pesoCombinado: camion?.pesoCombinado ?? 0,
          tara: camion?.tara ?? 0,
          tipo: camion?.tipo ?? '',
          velocidadPromedio: camion?.velocidadPromedio ?? 0,
        };
      });

      setCamiones(nuevosCamiones);
      // Extraer bloqueos si existen
      if (data.bloqueos) {
        setBloqueos(data.bloqueos);
      } else {
        setBloqueos([]);
      }
      if (esInicial) setHoraActual(HORA_PRIMERA_ACTUALIZACION);
      setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
      setEsperandoActualizacion(false);
    } catch (error) {
      console.error("Error al cargar datos de simulación:", error);
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

    const nuevosCamiones = camiones.map((camion) => {
      const ruta = rutasCamiones.find(r => r.id === camion.id);
      if (!ruta) return camion;

      // Si el camión está averiado, no avanza
      if (camion.estado === 'Averiado') {
        return camion;
      }

      const siguientePaso = camion.porcentaje + INCREMENTO_PORCENTAJE;
      const rutaLength = ruta.ruta.length;

      // Si llegó al final de la ruta
      if (siguientePaso >= rutaLength) {
        return { ...camion, estado: 'Entregado' as const, porcentaje: rutaLength - 1 };
      }

      // En un mapa reticular, cada paso entre nodos adyacentes es exactamente 1km
      // No necesitamos calcular distancia euclidiana ya que el camión se mueve nodo por nodo
      const distanciaRecorrida = 1; // 1km por paso/nodo en mapa reticular

      // Obtener coordenadas siguientes para verificar entregas de pedidos
      const coordSiguiente = parseCoord(ruta.ruta[siguientePaso]);

      // Adaptar el camión para usar las funciones de cálculo
      const camionAdaptado = adaptarCamionParaCalculos(camion);
      
      // Calcular consumo de combustible usando la función de utilidad
      const consumoCombustible = calcularConsumoGalones(camionAdaptado, distanciaRecorrida);
      
      // Actualizar combustible actual (no puede ser menor que 0)
      const nuevoCombustible = Math.max(0, camion.combustibleActual - consumoCombustible);
      
      // Verificar si hay pedidos para entregar en esta ubicación
      const pedidosEnEstaUbicacion = ruta.pedidos.filter(pedido => 
        pedido.coordenada.x === coordSiguiente.x && 
        pedido.coordenada.y === coordSiguiente.y
      );
      
      // Verificar si el camión está en una coordenada donde tiene que entregar pedidos
      let nuevoGLP = camion.capacidadActualGLP;
      const estaEntregandoPedido = pedidosEnEstaUbicacion.length > 0;
      
      // Reducir GLP si hay pedidos para entregar en esta ubicación
      if (estaEntregandoPedido) {
        console.log(`Camión ${camion.id} entregando ${pedidosEnEstaUbicacion.length} pedidos en (${coordSiguiente.x},${coordSiguiente.y})`);
        for (const pedido of pedidosEnEstaUbicacion) {
          if (pedido.volumenGLPAsignado) {
            console.log(`Reduciendo ${pedido.volumenGLPAsignado} GLP del camión ${camion.id}`);
            nuevoGLP -= pedido.volumenGLPAsignado;
          }
        }
        // Asegurar que no sea negativo
        nuevoGLP = Math.max(0, nuevoGLP);
      }
      
      // Crear nuevo estado del camión con valores actualizados
      const nuevoCamion = {
        ...camion,
        porcentaje: siguientePaso,
        ubicacion: ruta.ruta[siguientePaso],
        combustibleActual: nuevoCombustible,
        capacidadActualGLP: nuevoGLP
      };
      
      // Adaptar el nuevo estado del camión para los cálculos
      const nuevoCamionAdaptado = adaptarCamionParaCalculos(nuevoCamion);
      
      // Primero actualizar el peso de carga basado en la nueva cantidad de GLP
      nuevoCamion.pesoCarga = calcularPesoCarga(nuevoCamionAdaptado);
      
      // Luego actualizar el peso combinado basado en el nuevo peso de carga
      nuevoCamion.pesoCombinado = calcularPesoCombinado(nuevoCamionAdaptado);
      
      // Finalmente actualizar la distancia máxima basada en el combustible actual y peso combinado
      nuevoCamion.distanciaMaxima = calcularDistanciaMaxima(nuevoCamionAdaptado);
      
      // // Log para depuración
      // console.log(`Camión ${camion.id} - Combustible: ${nuevoCombustible.toFixed(2)}/${camion.combustibleMaximo} - GLP: ${nuevoGLP.toFixed(2)}/${camion.capacidadMaximaGLP} - Distancia máx: ${nuevoCamion.distanciaMaxima.toFixed(2)}`);
      
      // Si el camión se quedó sin combustible, cambiar su estado
      if (nuevoCombustible <= 0) {
        nuevoCamion.estado = 'Averiado';
      }
      
      return nuevoCamion;
    });

    const quedan = nodosRestantesAntesDeActualizar - 1;
    setNodosRestantesAntesDeActualizar(quedan);

    if (quedan <= 0) {
      setEsperandoActualizacion(true);
      setCamiones(nuevosCamiones);
      setHoraActual(prev => prev + 1);
      await cargarDatos(false);
    } else {
      setCamiones(nuevosCamiones);
      setHoraActual(prev => prev + 1);
    }
  };

  /**
   * @function reiniciar
   * @description Reinicia la simulación a su estado inicial
   */
  const reiniciar = () => {
    const nuevosCamiones: CamionEstado[] = rutasCamiones.map((ruta) => {
      // Aquí intentamos mantener los datos previos del camión si existen
      const anterior = camiones.find(c => c.id === ruta.id);
      return {
        id: ruta.id,
        ubicacion: ruta.ruta[0],
        porcentaje: 0,
        estado: anterior?.estado ?? 'En Camino',
        capacidadActualGLP: anterior?.capacidadActualGLP ?? 0,
        capacidadMaximaGLP: anterior?.capacidadMaximaGLP ?? 0,
        combustibleActual: anterior?.combustibleActual ?? 0,
        combustibleMaximo: anterior?.combustibleMaximo ?? 0,
        distanciaMaxima: anterior?.distanciaMaxima ?? 0,
        pesoCarga: anterior?.pesoCarga ?? 0,
        pesoCombinado: anterior?.pesoCombinado ?? 0,
        tara: anterior?.tara ?? 0,
        tipo: anterior?.tipo ?? '',
        velocidadPromedio: anterior?.velocidadPromedio ?? 0,
      };
    });
    setCamiones(nuevosCamiones);
    setHoraActual(HORA_PRIMERA_ACTUALIZACION);
    setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
    setEsperandoActualizacion(false);
  };

  /**
   * @function marcarCamionAveriado
   * @description Marca un camión como averiado, deteniéndolo en su posición actual
   * @param {string} camionId - El ID del camión a averiar
   */
  const marcarCamionAveriado = (camionId: string) => {
    setCamiones(prev => 
      prev.map(camion => 
        camion.id === camionId 
          ? { ...camion, estado: 'Averiado' as const }
          : camion
      )
    );
  };

  return (
    <SimulacionContext.Provider
      value={{ 
        horaActual, 
        camiones, 
        rutasCamiones, 
        almacenes, 
        fechaHoraSimulacion,
        diaSimulacion,
        avanzarHora, 
        reiniciar, 
        cargando, 
        bloqueos,
        marcarCamionAveriado
      }}
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

/**
 * Función para parsear una coordenada en formato "(x,y)" a objeto Coordenada
 */
const parseCoord = (s: string): Coordenada => {
  const match = s.match(/\((\d+),\s*(\d+)\)/);
  if (!match) throw new Error(`Coordenada inválida: ${s}`);
  return { x: parseInt(match[1]), y: parseInt(match[2]) };
};

/**
 * Función adaptadora para convertir un CamionEstado a un objeto compatible con Camion
 * Esta función es esencial para poder usar las funciones de cálculo en types.ts
 */
const adaptarCamionParaCalculos = (camion: CamionEstado): Camion => {
  return {
    codigo: camion.id,
    capacidadActualGLP: camion.capacidadActualGLP,
    capacidadMaximaGLP: camion.capacidadMaximaGLP,
    combustibleActual: camion.combustibleActual,
    combustibleMaximo: camion.combustibleMaximo,
    distanciaMaxima: camion.distanciaMaxima,
    estado: camion.estado,
    pesoCarga: camion.pesoCarga,
    pesoCombinado: camion.pesoCombinado,
    tara: camion.tara,
    tipo: camion.tipo,
    velocidadPromedio: camion.velocidadPromedio
  };
};
