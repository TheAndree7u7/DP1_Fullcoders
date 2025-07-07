/**
 * @file SimulacionContext.tsx
 * @description Contexto de React para manejar el estado global de la simulación de rutas de camiones.
 * Este contexto proporciona funcionalidad para controlar el avance de la simulación,
 * el estado de los camiones y sus rutas, y la sincronización con el backend.
 */

import React, { createContext, useContext, useEffect, useState } from "react";
import { getMejorIndividuo, reiniciarSimulacion, iniciarSimulacion } from "../services/simulacionApiService";
import { getAlmacenes } from "../services/almacenApiService";
import { TIEMPO_ACTUALIZACION_SEGUNDOS } from "../config/tiempoSimulacion";
import type {
  Pedido,
  Individuo,
  Gen,
  Nodo,
  Almacen,
  Coordenada,
} from "../types";
import {
  calcularPesoCarga,
  calcularPesoCombinado,
  calcularConsumoGalones,
  calcularDistanciaMaxima,
} from "../types";
import { 
  parseCoord, 
  adaptarCamionParaCalculos
} from "./simulacion/utils";

/**
 * Constantes de configuración de la simulación
 */
const HORA_INICIAL = 0;
const HORA_PRIMERA_ACTUALIZACION = 0; // Empezar desde el primer paquete
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
  fechaInicioSimulacion: string | null; // Fecha y hora de inicio de la simulación
  diaSimulacion: number | null; // Día extraído de fechaHoraSimulacion
  tiempoRealSimulacion: string; // Tiempo real transcurrido desde el inicio de la simulación
  tiempoTranscurridoSimulado: string; // Tiempo transcurrido dentro de la simulación
  simulacionActiva: boolean; // Indica si la simulación está activa (contador funcionando)
  horaSimulacion: string; // Hora actual dentro de la simulación (HH:MM:SS)
  tiempoActualizacionSegundos: number; // Tiempo parametrizable para actualización
  avanzarHora: () => void;
  reiniciar: () => Promise<void>;
  iniciarContadorTiempo: () => void; // Nueva función para iniciar el contador manualmente
  reiniciarYEmpezarNuevo: () => Promise<void>; // Nueva función para reiniciar y empezar con nuevos paquetes
  limpiarEstadoParaNuevaSimulacion: () => void; // Limpia estado pero no carga datos
  iniciarSimulacion: (fechaInicio: string) => Promise<void>; // Función para iniciar una nueva simulación
  cambiarTiempoActualizacion: (segundos: number) => void; // Función para cambiar el tiempo de actualización
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

// Tipo para la solución precargada
type IndividuoConBloqueos = Individuo & {
  bloqueos?: Bloqueo[];
  almacenes?: Almacen[];
  fechaHoraSimulacion?: string;
};

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
  // Estados del contexto
  const [horaActual, setHoraActual] = useState<number>(HORA_INICIAL);
  const [camiones, setCamiones] = useState<CamionEstado[]>([]);
  const [rutasCamiones, setRutasCamiones] = useState<RutaCamion[]>([]);
  const [almacenes, setAlmacenes] = useState<Almacen[]>([]);
  const [cargando, setCargando] = useState<boolean>(true);
  const [tiempoActualizacionSegundos, setTiempoActualizacionSegundos] = useState<number>(TIEMPO_ACTUALIZACION_SEGUNDOS); // Tiempo parametrizable para actualización
  const [esperandoActualizacion, setEsperandoActualizacion] =
    useState<boolean>(false);
  const [solicitudAnticipadaEnviada, setSolicitudAnticipadaEnviada] =
    useState<boolean>(false);
  const [proximaSolucionCargada, setProximaSolucionCargada] =
    useState<IndividuoConBloqueos | null>(null);
  const [bloqueos, setBloqueos] = useState<Bloqueo[]>([]);
  const [fechaHoraSimulacion, setFechaHoraSimulacion] = useState<string | null>(
    null,
  );
  const [fechaInicioSimulacion, setFechaInicioSimulacion] = useState<string | null>(null);
  const [diaSimulacion, setDiaSimulacion] = useState<number | null>(null);
  const [tiempoRealSimulacion, setTiempoRealSimulacion] = useState<string>("00:00:00");
  const [tiempoTranscurridoSimulado, setTiempoTranscurridoSimulado] = useState<string>("00:00:00");
  const [inicioSimulacion, setInicioSimulacion] = useState<Date | null>(null);
  const [simulacionActiva, setSimulacionActiva] = useState<boolean>(false);
  const [horaSimulacion, setHoraSimulacion] = useState<string>("00:00:00");

  // Cargar almacenes al inicio con reintentos
  useEffect(() => {
    console.log("🚀 CONTEXTO: Montando contexto...");
    cargarDatosIniciales();
  }, []);

  // Sistema de actualización automática cada X segundos cuando la simulación está activa
  useEffect(() => {
    if (!simulacionActiva) return;

    console.log("⏰ INTERVALOS: Iniciando sistema de actualización automática cada", tiempoActualizacionSegundos, "segundos");
    
    const interval = setInterval(async () => {
      console.log("🔄 INTERVALOS: Solicitando nueva actualización de datos...");
      try {
        await cargarDatos(false);
        console.log("✅ INTERVALOS: Datos actualizados exitosamente");
      } catch (error) {
        console.error("❌ INTERVALOS: Error al actualizar datos:", error);
      }
    }, tiempoActualizacionSegundos * 1000);

    return () => {
      console.log("🛑 INTERVALOS: Limpiando intervalo de actualización");
      clearInterval(interval);
    };
  }, [simulacionActiva, tiempoActualizacionSegundos]);

  const cargarDatosIniciales = async () => {
    let intentos = 0;
    const maxIntentos = 10;
    
    while (intentos < maxIntentos) {
      try {
        console.log(`🔄 CONTEXTO: Intento ${intentos + 1}/${maxIntentos} de carga inicial...`);
        
        // Intentar cargar almacenes primero (silencioso en reintentos)
        await cargarAlmacenes(intentos > 0);
        console.log("✅ CONTEXTO: Almacenes cargados exitosamente");
        
        // No intentar cargar datos de simulación automáticamente
        // Los datos se cargarán manualmente cuando se necesiten
        console.log("ℹ️ CONTEXTO: Datos de simulación se cargarán manualmente cuando se requieran");
        
        // Poner cargando en false ya que los almacenes se cargaron exitosamente
        setCargando(false);
        console.log("✅ CONTEXTO: Estado de carga cambiado a false - almacenes listos");
        
        // Si llegamos aquí, al menos los almacenes se cargaron correctamente
        break;
        
      } catch (error) {
        intentos++;
        console.log(`⚠️ CONTEXTO: Intento ${intentos} fallido:`, error);
        
        if (intentos < maxIntentos) {
          // Esperar antes del siguiente intento
          await new Promise(resolve => setTimeout(resolve, 2000));
        } else {
          console.error("❌ CONTEXTO: No se pudieron cargar los datos iniciales después de", maxIntentos, "intentos");
        }
      }
    }
  };

  // Contador de tiempo real de la simulación
  useEffect(() => {
    if (!inicioSimulacion) return;

    console.log("⏱️ CONTADOR: Iniciando useEffect del contador con fecha:", inicioSimulacion);

    const interval = setInterval(() => {
      const ahora = new Date();
      const diferencia = ahora.getTime() - inicioSimulacion.getTime();
      const segundos = Math.floor(diferencia / 1000);
      const horas = Math.floor(segundos / 3600);
      const minutos = Math.floor((segundos % 3600) / 60);
      const segs = segundos % 60;
      
      const tiempoFormateado = `${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${segs.toString().padStart(2, '0')}`;
      setTiempoRealSimulacion(tiempoFormateado);
      
      // Log cada 10 segundos para debuggear
      if (segundos % 10 === 0) {
        // console.log("⏱️ CONTADOR: Tiempo transcurrido:", tiempoFormateado);
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [inicioSimulacion]);

  // Calcular la hora de simulación basado en fechaHoraSimulacion y horaActual
  useEffect(() => {
    if (fechaHoraSimulacion && horaActual >= 0) {
      const fechaBase = new Date(fechaHoraSimulacion);
      
      // Calculamos los segundos transcurridos basados en el progreso actual (0-100%)
      const segundosTranscurridos = (horaActual / 100) * tiempoActualizacionSegundos;
      
      // Crea nueva fecha sumando los segundos transcurridos
      const nuevaFecha = new Date(fechaBase.getTime() + segundosTranscurridos * 1000);
      
      // Formatear solo la hora
      const horaFormateada = nuevaFecha.toLocaleTimeString('es-ES', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
      
      setHoraSimulacion(horaFormateada);
    }
  }, [horaActual, fechaHoraSimulacion, tiempoActualizacionSegundos]);

  // Calcular tiempo transcurrido simulado
  useEffect(() => {
    if (fechaHoraSimulacion && fechaInicioSimulacion) {
      const fechaActual = new Date(fechaHoraSimulacion);
      const fechaInicio = new Date(fechaInicioSimulacion);
      
      // Calcular diferencia en milisegundos
      const diferenciaMilisegundos = fechaActual.getTime() - fechaInicio.getTime();
      
      // Convertir a segundos
      const totalSegundos = Math.floor(diferenciaMilisegundos / 1000);
      
      // Calcular días, horas, minutos y segundos
      const dias = Math.floor(totalSegundos / 86400);
      const horas = Math.floor((totalSegundos % 86400) / 3600);
      const minutos = Math.floor((totalSegundos % 3600) / 60);
      const segundos = totalSegundos % 60;
      
      // Formatear como HH:MM:SS para compatibilidad con la función existente
      const horasFormateadas = (dias * 24 + horas).toString().padStart(2, '0');
      const minutosFormateados = minutos.toString().padStart(2, '0');
      const segundosFormateados = segundos.toString().padStart(2, '0');
      
      const tiempoFormateado = `${horasFormateadas}:${minutosFormateados}:${segundosFormateados}`;
      setTiempoTranscurridoSimulado(tiempoFormateado);
    } else {
      setTiempoTranscurridoSimulado("00:00:00");
    }
  }, [fechaHoraSimulacion, fechaInicioSimulacion]);

  // Función para actualizar almacenes (útil para refrescar capacidades)
  const actualizarAlmacenes = async () => {
    try {
      console.log("🔄 ALMACENES: Actualizando información de almacenes...");
      await cargarAlmacenes(false);
      console.log("✅ ALMACENES: Información actualizada");
    } catch (error) {
      console.error("❌ ALMACENES: Error al actualizar almacenes:", error);
    }
  };

  /**
   * @function cargarAlmacenes
   * @description Carga los datos de almacenes desde el backend
   */
  const cargarAlmacenes = async (silencioso: boolean = false) => {
    try {
      const data = await getAlmacenes();
      setAlmacenes(data);
      if (!silencioso) {
        console.log("✅ ALMACENES: Almacenes cargados:", data.length, "items");
      }
    } catch (error) {
      if (!silencioso) {
        console.error("❌ ALMACENES: Error al cargar almacenes:", error);
      }
      throw error; // Re-lanzar para que el caller pueda manejar el error
    }
  };

  /**
   * @function cargarDatos
   * @description Carga los datos de simulación desde el backend
   * @param {boolean} esInicial - Indica si es la carga inicial
   */
  const cargarDatos = async (esInicial: boolean = false) => {
    if (esInicial) {
      setCargando(true);
    }
    try {
      console.log(
        "🔄 SOLICITUD: Iniciando solicitud de nueva solución al servidor...",
      );
      
      // Usar la fecha actual de la simulación si está disponible, sino usar la fecha actual del sistema
      const fechaRequest = fechaHoraSimulacion || new Date().toISOString();
      console.log("📅 SOLICITUD: Enviando solicitud con fecha:", fechaRequest);
      
      const data = (await getMejorIndividuo(fechaRequest)) as IndividuoConBloqueos;
      console.log(
        "✅ RESPUESTA: Datos de nueva solución recibidos del servidor:",
        data,
      );

      // Actualizar fecha y hora de la simulación
      if (data.fechaHoraSimulacion) {
        setFechaHoraSimulacion(data.fechaHoraSimulacion);
        
        // Establecer fecha de inicio si es la primera vez
        if (!fechaInicioSimulacion) {
          setFechaInicioSimulacion(data.fechaHoraSimulacion);
          console.log("Fecha de inicio de simulación establecida:", data.fechaHoraSimulacion);
        }
        
        // Extraer el día de la fecha
        const fecha = new Date(data.fechaHoraSimulacion);
        setDiaSimulacion(fecha.getDate());
        console.log(
          "Fecha de simulación actualizada:",
          data.fechaHoraSimulacion,
          "Día:",
          fecha.getDate(),
        );
      }

      const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen: Gen) => ({
        id: gen.camion.codigo,
        ruta: gen.nodos.map(
          (n: Nodo) => `(${n.coordenada.x},${n.coordenada.y})`,
        ),
        puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
        pedidos: gen.pedidos,
      }));

      // Log para verificar los pedidos que llegan del backend
      console.log("🔍 Verificando pedidos en las rutas:");
      nuevasRutas.forEach((ruta) => {
        if (ruta.pedidos && ruta.pedidos.length > 0) {
          console.log(
            `Camión ${ruta.id} tiene ${ruta.pedidos.length} pedidos:`,
            ruta.pedidos,
          );
          ruta.pedidos.forEach((pedido, index) => {
            console.log(`  Pedido ${index + 1}:`, {
              codigo: pedido.codigo,
              coordenada: pedido.coordenada,
              volumenGLPAsignado: pedido.volumenGLPAsignado,
              estado: pedido.estado,
            });
          });
        } else {
          console.log(`Camión ${ruta.id} no tiene pedidos asignados`);
        }
      });

      setRutasCamiones(nuevasRutas);
      console.log(
        "📋 ACTUALIZACIÓN: Rutas de camiones actualizadas en el mapa con",
        nuevasRutas.length,
        "camiones",
      );

      const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
        const anterior = camiones.find((c) => c.id === ruta.id);
        // Buscar el gen correspondiente para obtener los datos completos del camión
        const gen = data.cromosoma.find(
          (g: Gen) => g.camion.codigo === ruta.id,
        );
        const camion = gen?.camion;
        const ubicacion = anterior?.ubicacion ?? ruta.ruta[0];
        return {
          id: ruta.id,
          ubicacion,
          porcentaje: 0,
          estado: camion?.estado === "DISPONIBLE" ? "Disponible" : "En Camino",
          capacidadActualGLP: camion?.capacidadActualGLP ?? 0,
          capacidadMaximaGLP: camion?.capacidadMaximaGLP ?? 0,
          combustibleActual: camion?.combustibleActual ?? 0,
          combustibleMaximo: camion?.combustibleMaximo ?? 0,
          distanciaMaxima: camion?.distanciaMaxima ?? 0,
          pesoCarga: camion?.pesoCarga ?? 0,
          pesoCombinado: camion?.pesoCombinado ?? 0,
          tara: camion?.tara ?? 0,
          tipo: camion?.tipo ?? "",
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

      // Gestionar almacenes: priorizar los que vienen del backend, sino mantener los actuales o cargar nuevos
      if (data.almacenes && data.almacenes.length > 0) {
        console.log("🏪 CONTEXTO: Actualizando almacenes desde simulación:", data.almacenes);
        setAlmacenes(data.almacenes);
      } else if (almacenes.length === 0) {
        // Si no vienen almacenes en la respuesta y no tenemos almacenes cargados, cargarlos
        console.log("🏪 CONTEXTO: No hay almacenes en la simulación, cargando desde API...");
        try {
          await cargarAlmacenes(false);
        } catch (error) {
          console.log("⚠️ CONTEXTO: Error al cargar almacenes desde API:", error);
        }
      } else {
        console.log("🏪 CONTEXTO: Manteniendo almacenes existentes (" + almacenes.length + " items)");
      }
      
      if (esInicial) setHoraActual(HORA_PRIMERA_ACTUALIZACION);
      setEsperandoActualizacion(false);
      setSolicitudAnticipadaEnviada(false);
      setProximaSolucionCargada(null);
    } catch (error) {
      console.error("Error al cargar datos de simulación:", error);
      throw error; // Re-lanzar para que el caller pueda manejar el error
    } finally {
      if (esInicial) setCargando(false);
    }
  };

  /**
   * @function cargarSolucionAnticipada
   * @description Carga anticipadamente la siguiente solución para transición suave
   */
  const cargarSolucionAnticipada = async () => {
    try {
      console.log("🚀 ANTICIPADA: Cargando solución anticipada en background...");
      
      // Usar la fecha actual de la simulación si está disponible, sino usar la fecha actual del sistema
      const fechaRequest = fechaHoraSimulacion || new Date().toISOString();
      console.log("📅 ANTICIPADA: Enviando solicitud con fecha:", fechaRequest);
      
      const data = await getMejorIndividuo(fechaRequest) as IndividuoConBloqueos;
      console.log("✨ ANTICIPADA: Solución anticipada cargada y lista:", data);
      setProximaSolucionCargada(data);
    } catch (error) {
      console.error("⚠️ ANTICIPADA: Error al cargar solución anticipada:", error);
    }
  };

  /**
   * @function aplicarSolucionPrecargada
   * @description Aplica una solución previamente cargada para transición suave
   */
  const aplicarSolucionPrecargada = async (data: IndividuoConBloqueos) => {
    try {
      console.log("⚡ TRANSICIÓN: Aplicando solución precargada...");
      
      // Actualizar fecha y hora de la simulación
          if (data.fechaHoraSimulacion) {
      setFechaHoraSimulacion(data.fechaHoraSimulacion);
      
      // Establecer fecha de inicio si es la primera vez
      if (!fechaInicioSimulacion) {
        setFechaInicioSimulacion(data.fechaHoraSimulacion);
        console.log("Fecha de inicio de simulación establecida:", data.fechaHoraSimulacion);
      }
      
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
      console.log("📋 TRANSICIÓN: Rutas aplicadas desde solución precargada con", nuevasRutas.length, "camiones");

      const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
        const anterior = camiones.find(c => c.id === ruta.id);
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
      
      if (data.bloqueos) {
        setBloqueos(data.bloqueos);
      } else {
        setBloqueos([]);
      }

      // Actualizar almacenes si vienen en la respuesta
      if (data.almacenes && data.almacenes.length > 0) {
        console.log("🏪 TRANSICIÓN: Actualizando almacenes desde solución precargada:", data.almacenes);
        setAlmacenes(data.almacenes);
      }
      
      setEsperandoActualizacion(false);
      setSolicitudAnticipadaEnviada(false);
      setProximaSolucionCargada(null);
      
      // Asegurar que el estado de carga esté en false después de aplicar datos
      setCargando(false);
      console.log("✅ TRANSICIÓN: Estado de carga cambiado a false después de aplicar solución");
      
    } catch (error) {
      console.error("❌ TRANSICIÓN: Error al aplicar solución precargada:", error);
    }
  };

  /**
   * @function avanzarHora
   * @description Avanza la simulación incrementando el progreso basado en tiempo,
   * actualizando la posición de los camiones y recargando datos del backend cuando sea necesario
   */
  const avanzarHora = async () => {
    if (esperandoActualizacion) return;

    // Verificar si necesitamos solicitar anticipadamente la próxima solución (al 75% del tiempo)
    const tiempoTresCuartos = 75; // 75% del progreso
    
    if (horaActual >= tiempoTresCuartos && !solicitudAnticipadaEnviada) {
      console.log("📅 ANTICIPADA: Llegamos a 3/4 del tiempo (", horaActual, "% de 100%) - Solicitando próxima solución...");
      setSolicitudAnticipadaEnviada(true);
      cargarSolucionAnticipada();
    }

    const nuevosCamiones = camiones.map((camion) => {
      const ruta = rutasCamiones.find((r) => r.id === camion.id);
      if (!ruta) return camion;

      // Si el camión está averiado, no avanza
      if (camion.estado === "Averiado") {
        return camion;
      }

      const siguientePaso = camion.porcentaje + INCREMENTO_PORCENTAJE;
      const rutaLength = ruta.ruta.length;

      // Si llegó al final de la ruta
      if (siguientePaso >= rutaLength) {
        return {
          ...camion,
          estado: "Entregado" as const,
          porcentaje: rutaLength - 1,
        };
      }

      // En un mapa reticular, cada paso entre nodos adyacentes es exactamente 1km
      const distanciaRecorrida = 1; // 1km por paso/nodo en mapa reticular

      // Adaptar el camión para usar las funciones de cálculo
      const camionAdaptado = adaptarCamionParaCalculos(camion);

      // Calcular consumo de combustible
      const consumoCombustible = calcularConsumoGalones(
        camionAdaptado,
        distanciaRecorrida,
      );

      // Actualizar combustible actual
      const nuevoCombustible = Math.max(
        0,
        camion.combustibleActual - consumoCombustible,
      );

      // Mover el camión a la nueva posición
      const nuevaUbicacion = ruta.ruta[siguientePaso];

      // Verificar si hay pedidos para entregar en la nueva ubicación
      let nuevoGLP = camion.capacidadActualGLP;
      const pedidosEntregadosAhora: Pedido[] = [];

      ruta.pedidos.forEach((pedido) => {
        const indicePedidoEnRuta = ruta.ruta.findIndex((nodo) => {
          const coordNodo = parseCoord(nodo);
          return (
            coordNodo.x === pedido.coordenada.x &&
            coordNodo.y === pedido.coordenada.y
          );
        });

        if (indicePedidoEnRuta === siguientePaso) {
          pedidosEntregadosAhora.push(pedido);
        }
      });

      // Procesar entregas de pedidos
      if (pedidosEntregadosAhora.length > 0) {
        for (const pedido of pedidosEntregadosAhora) {
          if (pedido.volumenGLPAsignado) {
            nuevoGLP -= pedido.volumenGLPAsignado;
          }
        }
        nuevoGLP = Math.max(0, nuevoGLP);
      }

      // Crear nuevo estado del camión
      const nuevoCamion = {
        ...camion,
        porcentaje: siguientePaso,
        ubicacion: nuevaUbicacion,
        combustibleActual: nuevoCombustible,
        capacidadActualGLP: nuevoGLP,
      };

      // Actualizar pesos cuando se entregan pedidos
      if (pedidosEntregadosAhora.length > 0) {
        const nuevoCamionAdaptado = adaptarCamionParaCalculos(nuevoCamion);
        nuevoCamion.pesoCarga = calcularPesoCarga(nuevoCamionAdaptado);
        nuevoCamion.pesoCombinado = calcularPesoCombinado(nuevoCamionAdaptado);
      }

      // Actualizar distancia máxima
      const nuevoCamionAdaptado = adaptarCamionParaCalculos(nuevoCamion);
      nuevoCamion.distanciaMaxima = calcularDistanciaMaxima(nuevoCamionAdaptado);

      // Si se quedó sin combustible, marcar como averiado
      if (nuevoCombustible <= 0) {
        nuevoCamion.estado = "Averiado";
      }

      return nuevoCamion;
    });

    const nuevoProgreso = Math.min(horaActual + 1, 100);
    
    if (nuevoProgreso >= 100) {
      // Ciclo completado, cargar nuevos datos
      setEsperandoActualizacion(true);
      setCamiones(nuevosCamiones);
      setHoraActual(nuevoProgreso);
      
      // Si ya tenemos la solución anticipada cargada, usarla directamente
      if (proximaSolucionCargada) {
        console.log("⚡ TRANSICIÓN: Usando solución anticipada precargada para transición suave");
        await aplicarSolucionPrecargada(proximaSolucionCargada);
        setProximaSolucionCargada(null);
      } else {
        console.log("🔄 TRANSICIÓN: Solución anticipada no disponible, cargando en tiempo real...");
        await cargarDatos(false);
      }
      
      // Resetear para el siguiente ciclo
      setHoraActual(0);
      setSolicitudAnticipadaEnviada(false);
      setEsperandoActualizacion(false);
    } else {
      setCamiones(nuevosCamiones);
      setHoraActual(nuevoProgreso);
    }
  };

  /**
   * @function reiniciar
   * @description Reinicia la simulación a su estado inicial y limpia paquetes del backend
   */
  const reiniciar = async () => {
    console.log("🔄 REINICIO: Iniciando reinicio completo de la simulación...");
    
    try {
      // Primero reiniciar los paquetes en el backend
      await reiniciarSimulacion();
      console.log("✅ REINICIO: Paquetes del backend reiniciados exitosamente");
      
      // Limpiar estado y cargar nuevos datos
      await limpiarEstadoParaNuevaSimulacion();
      
      console.log("🔄 REINICIO: Reinicio completo finalizado - estado local y backend limpiados");
    } catch (error) {
      console.error("❌ REINICIO: Error al reiniciar simulación:", error);
      throw error;
    }
  };

  /**
   * @function iniciarContadorTiempo
   * @description Inicia el contador de tiempo real de la simulación
   */
  const iniciarContadorTiempo = () => {
    setInicioSimulacion(new Date());
    setTiempoRealSimulacion("00:00:00");
    setSimulacionActiva(true);
    console.log("⏱️ CONTADOR: Iniciando contador de tiempo real de simulación...");
  };

  /**
   * @function reiniciarYEmpezarNuevo
   * @description Reinicia completamente la simulación y empieza a cargar nuevos paquetes
   */
  const reiniciarYEmpezarNuevo = async () => {
    console.log("🚀 NUEVO INICIO: Reiniciando simulación para empezar con nuevos paquetes...");
    
    try {
      // Primero reiniciar los paquetes en el backend
      await reiniciarSimulacion();
      console.log("✅ NUEVO INICIO: Paquetes del backend reiniciados exitosamente");
      
      // Limpiar completamente el estado local y cargar nuevos datos
      await limpiarEstadoParaNuevaSimulacion();
      
      console.log("🎉 NUEVO INICIO: Simulación reiniciada y nuevos datos cargados exitosamente");
    } catch (error) {
      console.error("❌ NUEVO INICIO: Error al reiniciar e iniciar nueva simulación:", error);
      throw error;
    }
  };

  /**
   * @function marcarCamionAveriado
   * @description Marca un camión como averiado, deteniéndolo en su posición actual
   * @param {string} camionId - El ID del camión a averiar
   */
  const marcarCamionAveriado = (camionId: string) => {
    setCamiones((prev) =>
      prev.map((camion) =>
        camion.id === camionId
          ? { ...camion, estado: "Averiado" as const }
          : camion,
      ),
    );
  };

  /**
   * @function limpiarEstadoParaNuevaSimulacion
   * @description Limpia el estado para una nueva simulación y carga los primeros datos
   */
  const limpiarEstadoParaNuevaSimulacion = async () => {
    console.log("🧹 LIMPIEZA: Limpiando estado para nueva simulación...");
    
    // Limpiar datos de simulación anterior (pero NO los almacenes)
    setCamiones([]);
    setRutasCamiones([]);
    setBloqueos([]);
    setFechaHoraSimulacion(null);
    setFechaInicioSimulacion(null);
    setDiaSimulacion(null);
    setTiempoTranscurridoSimulado("00:00:00");
    
    // Resetear contadores
    setHoraActual(HORA_INICIAL);
    setEsperandoActualizacion(false);
    setSolicitudAnticipadaEnviada(false);
    setProximaSolucionCargada(null);
    
    // Iniciar contador de tiempo
    setInicioSimulacion(new Date());
    setTiempoRealSimulacion("00:00:00");
    setSimulacionActiva(true);
    
    console.log("✅ LIMPIEZA: Estado limpio, cargando almacenes y datos...");
    
    // Asegurar que los almacenes estén cargados SIEMPRE
    try {
      if (almacenes.length === 0) {
        console.log("🏪 LIMPIEZA: Cargando almacenes...");
        await cargarAlmacenes(false);
        console.log("✅ LIMPIEZA: Almacenes cargados exitosamente");
      }
    } catch (error) {
      console.log("⚠️ LIMPIEZA: Error al cargar almacenes:", error);
    }
    
    // Mientras esperamos el primer paquete, mostrar estado de carga
    setCargando(true);
    console.log("🔄 LIMPIEZA: Configurando estado de carga mientras esperamos primer paquete...");
    
    // Los datos se cargarán manualmente cuando se necesiten
    console.log("🔄 LIMPIEZA: Datos listos para carga manual cuando se requiera...");
  };

  /**
   * @function cambiarTiempoActualizacion
   * @description Cambia el tiempo de actualización en segundos
   * @param {number} segundos - Nuevo tiempo en segundos
   */
  const cambiarTiempoActualizacion = (segundos: number) => {
    setTiempoActualizacionSegundos(segundos);
    console.log("⏱️ TIEMPO: Tiempo de actualización cambiado a", segundos, "segundos");
  };

  /**
   * @function iniciarSimulacionConFecha
   * @description Inicia una nueva simulación con una fecha específica
   * @param {string} fechaInicio - Fecha de inicio en formato ISO
   */
  const iniciarSimulacionConFecha = async (fechaInicio: string) => {
    try {
      console.log("🚀 INICIO: Iniciando nueva simulación con fecha:", fechaInicio);
      
      // Primero limpiar el estado actual
      await limpiarEstadoParaNuevaSimulacion();
      
      // Llamar al servicio para iniciar la simulación en el backend
      const mensaje = await iniciarSimulacion(fechaInicio);
      console.log("✅ INICIO: Simulación iniciada en el backend:", mensaje);
      
      // Activar la simulación para que comience el sistema de actualización automática
      setSimulacionActiva(true);
      
      // Cargar los primeros datos después de un breve delay para que el backend procese
      setTimeout(async () => {
        try {
          await cargarDatos(true);
          console.log("🎉 INICIO: Primeros datos cargados exitosamente");
        } catch (error) {
          console.error("⚠️ INICIO: Error al cargar primeros datos:", error);
        }
      }, 2000);
      
    } catch (error) {
      console.error("❌ INICIO: Error al iniciar simulación:", error);
      throw error;
    }
  };

  return (
    <SimulacionContext.Provider
      value={{
        horaActual,
        camiones,
        rutasCamiones,
        almacenes,
        fechaHoraSimulacion,
        fechaInicioSimulacion,
        diaSimulacion,
        tiempoRealSimulacion,
        tiempoTranscurridoSimulado,
        simulacionActiva,
        horaSimulacion,
        tiempoActualizacionSegundos,
        avanzarHora,
        reiniciar,
        iniciarContadorTiempo,
        reiniciarYEmpezarNuevo,
        limpiarEstadoParaNuevaSimulacion,
        iniciarSimulacion: iniciarSimulacionConFecha,
        cambiarTiempoActualizacion,
        cargando,
        bloqueos,
        marcarCamionAveriado,
        actualizarAlmacenes,
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






