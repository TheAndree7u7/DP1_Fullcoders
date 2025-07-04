/**
 * @file SimulacionContext.tsx
 * @description Contexto de React para manejar el estado global de la simulación de rutas de camiones.
 * Este contexto proporciona funcionalidad para controlar el avance de la simulación,
 * el estado de los camiones y sus rutas, y la sincronización con el backend.
 */

import React, { createContext, useContext, useEffect, useState } from "react";
import type {
  Pedido,
  Almacen,
  Coordenada,
} from "../types";

// Importaciones de archivos refactorizados
import {
  HORA_INICIAL_SIMULACION,
  HORA_PRIMERA_ACTUALIZACION_DATOS,
  NODOS_REQUERIDOS_ANTES_ACTUALIZACION,
  PROPORCION_SOLICITUD_ANTICIPADA,
} from "./simulacion/constantes-configuracion-simulacion";

import {
  cargarDatosAlmacenesDesdeBackend,
  cargarSolucionAnticipadaEnBackground,
  cargarYProcesarDatosCompletos,
  procesarDatosSolucionParaSimulacion,
  type IndividuoConDatosComplementarios,
} from "./simulacion/servicios-carga-datos-simulacion";

import {
  parseCoordenadasDeCadena,
  calcularHoraSimulacionDesdeFechaBase,
  extraerDiaDeFecha,
  crearControladorTiempo,
  type ControladorTiempoSimulacion,
} from "./simulacion/utilidades-calculo-simulacion";

import {
  marcarCamionComoAveriado,
  avanzarTodosLosCamiones,
  reiniciarEstadosCamiones,
} from "./simulacion/manejadores-estado-camiones";

import {
  determinarSiDebeEnviarSolicitudAnticipada,
  determinarSiDebeActualizarDatos,
  crearEstadoControlSimulacionInicial,
  reiniciarEstadoControlSimulacion,
  type EstadoControlSimulacion,
} from "./simulacion/controladores-tiempo-simulacion";

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
  estado:
    | "En Camino"
    | "Entregado"
    | "Averiado"
    | "En Mantenimiento"
    | "Disponible";
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
  tiempoRealSimulacion: string; // Tiempo real transcurrido desde el inicio de la simulación
  simulacionActiva: boolean; // Indica si la simulación está activa (contador funcionando)
  horaSimulacion: string; // Hora actual dentro de la simulación (HH:MM:SS)
  avanzarHora: () => void;
  reiniciar: () => void;
  iniciarContadorTiempo: () => void; // Nueva función para iniciar el contador manualmente
  cargando: boolean;
  bloqueos: Bloqueo[];
  marcarCamionAveriado: (camionId: string) => void; // Nueva función para manejar averías
  actualizarAlmacenes: () => Promise<void>; // Nueva función para actualizar almacenes
}

/**
 * @interface Bloqueo
 * @description Representa un bloqueo en la simulación
 * @property {Coordenada[]} coordenadas - Lista de nodos bloqueados
 * @property {string} fechaInicio - Fecha y hora de inicio del bloqueo (ISO)
 * @property {string} fechaFin - Fecha y hora de fin del bloqueo (ISO)
 */
export interface Bloqueo {
  coordenadas: Coordenada[];
  fechaInicio: string; // ISO string
  fechaFin: string;    // ISO string
}

// Creación del contexto con valor inicial undefined
const SimulacionContext = createContext<SimulacionContextType | undefined>(
  undefined,
);

/**
 * @component SimulacionProvider
 * @description Proveedor del contexto de simulación que maneja el estado global
 * @param {Object} props - Propiedades del componente
 * @param {React.ReactNode} props.children - Componentes hijos que tendrán acceso al contexto
 */
export const SimulacionProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  // Estados del contexto refactorizados con nombres más descriptivos
  const [estadoControlSimulacion, setEstadoControlSimulacion] = useState<EstadoControlSimulacion>(
    crearEstadoControlSimulacionInicial()
  );
  const [listaCamionesActual, setListaCamionesActual] = useState<CamionEstado[]>([]);
  const [rutasAsignadasACamiones, setRutasAsignadasACamiones] = useState<RutaCamion[]>([]);
  const [almacenesDisponibles, setAlmacenesDisponibles] = useState<Almacen[]>([]);
  const [estadoCargandoDatos, setEstadoCargandoDatos] = useState<boolean>(true);
  const [solucionAnticipadaPrecargada, setSolucionAnticipadaPrecargada] =
    useState<IndividuoConDatosComplementarios | null>(null);
  const [bloqueosActivos, setBloqueosActivos] = useState<Bloqueo[]>([]);
  const [fechaHoraSimulacionActual, setFechaHoraSimulacionActual] = useState<string | null>(null);
  const [diaExtraidoDeSimulacion, setDiaExtraidoDeSimulacion] = useState<number | null>(null);
  const [tiempoRealTranscurrido, setTiempoRealTranscurrido] = useState<string>("00:00:00");
  const [estadoSimulacionActiva, setEstadoSimulacionActiva] = useState<boolean>(false);
  const [horaCalculadaSimulacion, setHoraCalculadaSimulacion] = useState<string>("00:00:00");
  
  // Controlador de tiempo refactorizado
  const [controladorTiempoSimulacion] = useState<ControladorTiempoSimulacion>(() =>
    crearControladorTiempo(setTiempoRealTranscurrido)
  );

  // Cargar almacenes al inicio
  useEffect(() => {
    console.log("🚀 CONTEXTO: Montando contexto y cargando almacenes...");
    cargarAlmacenesIniciales();
    cargarDatosIniciales();
  }, []);

  // Calcular la hora de simulación basado en fechaHoraSimulacion y horaActual
  useEffect(() => {
    if (fechaHoraSimulacionActual && estadoControlSimulacion.horaActual >= 0) {
      const horaCalculada = calcularHoraSimulacionDesdeFechaBase(
        fechaHoraSimulacionActual,
        estadoControlSimulacion.horaActual
      );
      setHoraCalculadaSimulacion(horaCalculada);
    }
  }, [estadoControlSimulacion.horaActual, fechaHoraSimulacionActual]);

  /**
   * @function cargarAlmacenesIniciales
   * @description Carga los datos iniciales de almacenes desde el backend
   */
  const cargarAlmacenesIniciales = async () => {
    try {
      const datosAlmacenes = await cargarDatosAlmacenesDesdeBackend();
      setAlmacenesDisponibles(datosAlmacenes);
    } catch (error) {
      console.error("❌ CONTEXTO: Error al cargar almacenes iniciales:", error);
    }
  };

  /**
   * @function actualizarAlmacenesDesdeBackend
   * @description Actualiza los datos de almacenes desde el backend
   */
  const actualizarAlmacenesDesdeBackend = async () => {
    try {
      console.log("🔄 CONTEXTO: Actualizando información de almacenes...");
      const datosAlmacenes = await cargarDatosAlmacenesDesdeBackend();
      setAlmacenesDisponibles(datosAlmacenes);
      console.log("✅ CONTEXTO: Información de almacenes actualizada");
    } catch (error) {
      console.error("❌ CONTEXTO: Error al actualizar almacenes:", error);
    }
  };

  /**
   * @function cargarDatosIniciales
   * @description Carga los datos iniciales de simulación
   */
  const cargarDatosIniciales = async () => {
    setEstadoCargandoDatos(true);
    try {
      const datosSimulacion = await cargarYProcesarDatosCompletos(listaCamionesActual, true);
      aplicarDatosSimulacionAlEstado(datosSimulacion);
      
      setEstadoControlSimulacion(prev => ({
        ...prev,
        horaActual: HORA_PRIMERA_ACTUALIZACION_DATOS,
      }));
    } catch (error) {
      console.error("❌ CONTEXTO: Error al cargar datos iniciales:", error);
    } finally {
      setEstadoCargandoDatos(false);
    }
  };

  /**
   * @function cargarSolucionAnticipadaEnBackground
   * @description Carga anticipadamente una solución para transición suave
   */
  const cargarSolucionAnticipadaEnBackgroundLocal = async () => {
    try {
      const solucionAnticipada = await cargarSolucionAnticipadaEnBackground();
      setSolucionAnticipadaPrecargada(solucionAnticipada);
    } catch (error) {
      console.error("❌ CONTEXTO: Error al cargar solución anticipada:", error);
    }
  };

  /**
   * @function aplicarSolucionPrecargada
   * @description Aplica una solución previamente cargada
   */
  const aplicarSolucionPrecargada = async (solucion: IndividuoConDatosComplementarios) => {
    try {
      console.log("⚡ CONTEXTO: Aplicando solución precargada...");
      const datosProcessados = procesarDatosSolucionParaSimulacion(solucion, listaCamionesActual);
      aplicarDatosSimulacionAlEstado(datosProcessados);
      
      setEstadoControlSimulacion(prev => ({
        ...prev,
        nodosRestantesParaActualizacion: NODOS_REQUERIDOS_ANTES_ACTUALIZACION,
        esperandoActualizacion: false,
        solicitudAnticipadaEnviada: false,
      }));
      setSolucionAnticipadaPrecargada(null);
      
      console.log("✅ CONTEXTO: Solución precargada aplicada exitosamente");
    } catch (error) {
      console.error("❌ CONTEXTO: Error al aplicar solución precargada:", error);
    }
  };

  /**
   * @function aplicarDatosSimulacionAlEstado
   * @description Aplica los datos de simulación procesados al estado del contexto
   */
  const aplicarDatosSimulacionAlEstado = (datosSimulacion: any) => {
    setRutasAsignadasACamiones(datosSimulacion.rutasActualizadas);
    setListaCamionesActual(datosSimulacion.camionesActualizados);
    setBloqueosActivos(datosSimulacion.bloqueosActualizados);
    
    if (datosSimulacion.almacenesActualizados.length > 0) {
      setAlmacenesDisponibles(datosSimulacion.almacenesActualizados);
    }
    
    if (datosSimulacion.fechaHoraSimulacion) {
      setFechaHoraSimulacionActual(datosSimulacion.fechaHoraSimulacion);
      setDiaExtraidoDeSimulacion(datosSimulacion.diaSimulacion);
    }
  };

  /**
   * @function avanzarHoraSimulacion
   * @description Avanza la simulación una hora
   */
  const avanzarHoraSimulacion = async () => {
    if (estadoControlSimulacion.esperandoActualizacion) {
      return;
    }

    // Verificar si necesitamos solicitar anticipadamente la próxima solución
    if (determinarSiDebeEnviarSolicitudAnticipada(
      estadoControlSimulacion.nodosRestantesParaActualizacion,
      estadoControlSimulacion.solicitudAnticipadaEnviada
    )) {
      console.log("📅 CONTEXTO: Enviando solicitud anticipada...");
      setEstadoControlSimulacion(prev => ({
        ...prev,
        solicitudAnticipadaEnviada: true,
      }));
      cargarSolucionAnticipadaEnBackgroundLocal();
    }

    // Avanzar camiones
    const camionesActualizados = avanzarTodosLosCamiones(listaCamionesActual, rutasAsignadasACamiones);
    setListaCamionesActual(camionesActualizados);

    // Actualizar estado de control
    const nuevoEstado: EstadoControlSimulacion = {
      ...estadoControlSimulacion,
      horaActual: estadoControlSimulacion.horaActual + 1,
      nodosRestantesParaActualizacion: estadoControlSimulacion.nodosRestantesParaActualizacion - 1,
    };

    // Verificar si necesitamos actualizar datos
    if (determinarSiDebeActualizarDatos(nuevoEstado.nodosRestantesParaActualizacion)) {
      nuevoEstado.esperandoActualizacion = true;
      setEstadoControlSimulacion(nuevoEstado);
      
      // Usar solución precargada si está disponible
      if (solucionAnticipadaPrecargada) {
        await aplicarSolucionPrecargada(solucionAnticipadaPrecargada);
      } else {
        console.log("🔄 CONTEXTO: Cargando datos en tiempo real...");
        const datosSimulacion = await cargarYProcesarDatosCompletos(camionesActualizados, false);
        aplicarDatosSimulacionAlEstado(datosSimulacion);
        
        setEstadoControlSimulacion(prev => ({
          ...prev,
          nodosRestantesParaActualizacion: NODOS_REQUERIDOS_ANTES_ACTUALIZACION,
          esperandoActualizacion: false,
          solicitudAnticipadaEnviada: false,
        }));
      }
    } else {
      setEstadoControlSimulacion(nuevoEstado);
    }
  };

  /**
   * @function reiniciarSimulacion
   * @description Reinicia la simulación a su estado inicial
   */
  const reiniciarSimulacion = () => {
    const camionesReiniciados = reiniciarEstadosCamiones(listaCamionesActual, rutasAsignadasACamiones);
    setListaCamionesActual(camionesReiniciados);
    setEstadoControlSimulacion(reiniciarEstadoControlSimulacion());
    setSolucionAnticipadaPrecargada(null);
    
    // Reiniciar el contador de tiempo
    controladorTiempoSimulacion.reiniciarContador();
    setEstadoSimulacionActiva(false);
    console.log("⏱️ CONTEXTO: Simulación reiniciada completamente");
  };

  /**
   * @function iniciarContadorTiempoSimulacion
   * @description Inicia el contador de tiempo real de la simulación
   */
  const iniciarContadorTiempoSimulacion = () => {
    controladorTiempoSimulacion.iniciarContador();
    setEstadoSimulacionActiva(true);
    console.log("⏱️ CONTEXTO: Contador de tiempo iniciado");
  };

  /**
   * @function marcarCamionComoAveriado
   * @description Marca un camión como averiado
   */
  const marcarCamionComoAveriadoLocal = (camionId: string) => {
    const camionesActualizados = marcarCamionComoAveriado(listaCamionesActual, camionId);
    setListaCamionesActual(camionesActualizados);
  };

  return (
    <SimulacionContext.Provider
      value={{
        horaActual: estadoControlSimulacion.horaActual,
        camiones: listaCamionesActual,
        rutasCamiones: rutasAsignadasACamiones,
        almacenes: almacenesDisponibles,
        fechaHoraSimulacion: fechaHoraSimulacionActual,
        diaSimulacion: diaExtraidoDeSimulacion,
        tiempoRealSimulacion: tiempoRealTranscurrido,
        simulacionActiva: estadoSimulacionActiva,
        horaSimulacion: horaCalculadaSimulacion,
        avanzarHora: avanzarHoraSimulacion,
        reiniciar: reiniciarSimulacion,
        iniciarContadorTiempo: iniciarContadorTiempoSimulacion,
        cargando: estadoCargandoDatos,
        bloqueos: bloqueosActivos,
        marcarCamionAveriado: marcarCamionComoAveriadoLocal,
        actualizarAlmacenes: actualizarAlmacenesDesdeBackend,
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
  if (!context)
    throw new Error("useSimulacion debe usarse dentro de SimulacionProvider");
  return context;
};
