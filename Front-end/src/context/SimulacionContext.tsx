/**
 * @file SimulacionContext.tsx
 * @description Contexto de React para manejar el estado global de la simulación de rutas de camiones.
 * Este contexto proporciona funcionalidad para controlar el avance de la simulación,
 * el estado de los camiones y sus rutas, y la sincronización con el backend.
 */

import React, { createContext, useContext, useEffect, useState } from "react";
import { getMejorIndividuo } from "../services/simulacionApiService";
import { getAlmacenes } from "../services/almacenApiService";
import type {
  Pedido,
  Coordenada,
  Individuo,
  Gen,
  Nodo,
  Camion,
  Almacen,
} from "../types";
import {
  calcularPesoCarga,
  calcularPesoCombinado,
  calcularConsumoGalones,
  calcularDistanciaMaxima,
} from "../types";

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
  avanzarHora: () => void;
  reiniciar: () => void;
  iniciarContadorTiempo: () => void; // Nueva función para iniciar el contador manualmente
  cargando: boolean;
  bloqueos: Bloqueo[];
  marcarCamionAveriado: (camionId: string) => void; // Nueva función para manejar averías
  actualizarAlmacenes: () => Promise<void>; // Nueva función para actualizar almacenes
}

export interface Bloqueo {
  coordenadas: Coordenada[];
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
  const [nodosRestantesAntesDeActualizar, setNodosRestantesAntesDeActualizar] =
    useState<number>(NODOS_PARA_ACTUALIZACION);
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
  const [diaSimulacion, setDiaSimulacion] = useState<number | null>(null);
  const [tiempoRealSimulacion, setTiempoRealSimulacion] = useState<string>("00:00:00");
  const [inicioSimulacion, setInicioSimulacion] = useState<Date | null>(null);

  // Cargar almacenes al inicio
  useEffect(() => {
    console.log("🚀 CONTEXTO: Montando contexto y cargando almacenes...");
    cargarAlmacenes();
    cargarDatos(true);
  }, []);

  // Contador de tiempo real de la simulación
  useEffect(() => {
    if (!inicioSimulacion) return;

    const interval = setInterval(() => {
      const ahora = new Date();
      const diferencia = ahora.getTime() - inicioSimulacion.getTime();
      const segundos = Math.floor(diferencia / 1000);
      const horas = Math.floor(segundos / 3600);
      const minutos = Math.floor((segundos % 3600) / 60);
      const segs = segundos % 60;
      
      const tiempoFormateado = `${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${segs.toString().padStart(2, '0')}`;
      setTiempoRealSimulacion(tiempoFormateado);
    }, 1000);

    return () => clearInterval(interval);
  }, [inicioSimulacion]);

  // Función para actualizar almacenes (útil para refrescar capacidades)
  const actualizarAlmacenes = async () => {
    try {
      console.log("🔄 ALMACENES: Actualizando información de almacenes...");
      const data = await getAlmacenes();
      setAlmacenes(data);
      console.log("✅ ALMACENES: Información actualizada");
    } catch (error) {
      console.error("❌ ALMACENES: Error al actualizar almacenes:", error);
    }
  };

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
      console.error("❌ ALMACENES: Error al cargar almacenes:", error);
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
      const data = (await getMejorIndividuo()) as IndividuoConBloqueos;
      console.log(
        "✅ RESPUESTA: Datos de nueva solución recibidos del servidor:",
        data,
      );

      // Actualizar fecha y hora de la simulación
      if (data.fechaHoraSimulacion) {
        setFechaHoraSimulacion(data.fechaHoraSimulacion);
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

      // Actualizar almacenes si vienen en la respuesta
      if (data.almacenes && data.almacenes.length > 0) {
        console.log("🏪 CONTEXTO: Actualizando almacenes desde simulación:", data.almacenes);
        setAlmacenes(data.almacenes);
      }
      if (esInicial) setHoraActual(HORA_PRIMERA_ACTUALIZACION);
      setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
      setEsperandoActualizacion(false);
      setSolicitudAnticipadaEnviada(false);
      setProximaSolucionCargada(null);
    } catch (error) {
      console.error("Error al cargar datos de simulación:", error);
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
      const data = await getMejorIndividuo() as IndividuoConBloqueos;
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
      
      setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
      setEsperandoActualizacion(false);
      setSolicitudAnticipadaEnviada(false);
      setProximaSolucionCargada(null);
      
    } catch (error) {
      console.error("❌ TRANSICIÓN: Error al aplicar solución precargada:", error);
    }
  };

  /**
   * @function avanzarHora
   * @description Avanza la simulación una hora, actualizando la posición de los camiones
   * y recargando datos del backend cuando sea necesario
   */
  const avanzarHora = async () => {
    if (esperandoActualizacion) return;

    // Verificar si necesitamos solicitar anticipadamente la próxima solución
    const nodosTres4 = Math.floor(NODOS_PARA_ACTUALIZACION * 0.75);
    const nodosRestantes = nodosRestantesAntesDeActualizar - 1;
    
    if (nodosRestantes === nodosTres4 && !solicitudAnticipadaEnviada) {
      console.log("📅 ANTICIPADA: Llegamos a 3/4 del ciclo (nodo", NODOS_PARA_ACTUALIZACION - nodosRestantes, "de", NODOS_PARA_ACTUALIZACION, ") - Solicitando próxima solución...");
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
      // No necesitamos calcular distancia euclidiana ya que el camión se mueve nodo por nodo
      const distanciaRecorrida = 1; // 1km por paso/nodo en mapa reticular

      // Adaptar el camión para usar las funciones de cálculo
      const camionAdaptado = adaptarCamionParaCalculos(camion);

      // Calcular consumo de combustible usando la función de utilidad
      const consumoCombustible = calcularConsumoGalones(
        camionAdaptado,
        distanciaRecorrida,
      );

      // Actualizar combustible actual (no puede ser menor que 0)
      const nuevoCombustible = Math.max(
        0,
        camion.combustibleActual - consumoCombustible,
      );

      // PRIMERO: Mover el camión a la nueva posición
      const nuevaUbicacion = ruta.ruta[siguientePaso];
      const coordNuevaUbicacion = parseCoord(nuevaUbicacion);

      // SEGUNDO: Verificar si hay pedidos para entregar en la NUEVA ubicación (donde acaba de llegar)
      // Usar la misma lógica que getPedidosPendientes() para determinar si el pedido debe entregarse
      let nuevoGLP = camion.capacidadActualGLP;
      const pedidosEntregadosAhora: Pedido[] = [];

      ruta.pedidos.forEach((pedido) => {
        // Buscar el índice del nodo que corresponde a este pedido
        const indicePedidoEnRuta = ruta.ruta.findIndex((nodo) => {
          const coordNodo = parseCoord(nodo);
          return (
            coordNodo.x === pedido.coordenada.x &&
            coordNodo.y === pedido.coordenada.y
          );
        });

        // Si el camión llegó exactamente al nodo del pedido
        if (indicePedidoEnRuta === siguientePaso) {
          pedidosEntregadosAhora.push(pedido);
        }
      });

      // Log para debuggear los pedidos que se entregan
      if (pedidosEntregadosAhora.length > 0) {
        console.log(
          `� Camión ${camion.id} llegó a (${coordNuevaUbicacion.x},${coordNuevaUbicacion.y}) - Entregando ${pedidosEntregadosAhora.length} pedidos:`,
          pedidosEntregadosAhora,
        );
        console.log(`⛽ GLP antes de entrega: ${nuevoGLP.toFixed(2)}`);

        for (const pedido of pedidosEntregadosAhora) {
          console.log(`📋 Pedido:`, pedido);
          if (pedido.volumenGLPAsignado) {
            console.log(
              `⬇️ Reduciendo ${pedido.volumenGLPAsignado} GLP del camión ${camion.id}`,
            );
            nuevoGLP -= pedido.volumenGLPAsignado;
          } else {
            console.log(`⚠️ Pedido sin volumenGLPAsignado:`, pedido);
          }
        }
        // Asegurar que no sea negativo
        nuevoGLP = Math.max(0, nuevoGLP);
        console.log(`✅ GLP después de entrega: ${nuevoGLP.toFixed(2)}`);
      }

      // Crear nuevo estado del camión con valores actualizados
      const nuevoCamion = {
        ...camion,
        porcentaje: siguientePaso,
        ubicacion: nuevaUbicacion,
        combustibleActual: nuevoCombustible,
        capacidadActualGLP: nuevoGLP,
      };

      // SOLO actualizar peso de carga y peso combinado cuando se entregan pedidos
      if (pedidosEntregadosAhora.length > 0) {
        // Adaptar el nuevo estado del camión para los cálculos
        const nuevoCamionAdaptado = adaptarCamionParaCalculos(nuevoCamion);

        // Actualizar el peso de carga basado en la nueva cantidad de GLP
        nuevoCamion.pesoCarga = calcularPesoCarga(nuevoCamionAdaptado);

        // Actualizar el peso combinado basado en el nuevo peso de carga
        nuevoCamion.pesoCombinado = calcularPesoCombinado(nuevoCamionAdaptado);

        console.log(`📊 Camión ${camion.id} pesos actualizados:`, {
          pesoCarga: nuevoCamion.pesoCarga.toFixed(2),
          pesoCombinado: nuevoCamion.pesoCombinado.toFixed(2),
        });
      }

      // SIEMPRE actualizar la distancia máxima cuando cambie el combustible
      const nuevoCamionAdaptado = adaptarCamionParaCalculos(nuevoCamion);
      nuevoCamion.distanciaMaxima =
        calcularDistanciaMaxima(nuevoCamionAdaptado);

      // Log para depuración - mostrar solo cuando hay cambios significativos
      if (
        pedidosEntregadosAhora.length > 0 ||
        nuevoCombustible !== camion.combustibleActual
      ) {
        // console.log(`Camión ${camion.id} actualizado:`, {
        //   combustible: `${nuevoCombustible.toFixed(2)}/${camion.combustibleMaximo}`,
        //   glp: `${nuevoGLP.toFixed(2)}/${camion.capacidadMaximaGLP}`,
        //   distanciaMax: nuevoCamion.distanciaMaxima.toFixed(2),
        //   ubicacion: nuevoCamion.ubicacion,
        //   porcentaje: nuevoCamion.porcentaje
        // });
      }

      // Si el camión se quedó sin combustible, cambiar su estado
      if (nuevoCombustible <= 0) {
        nuevoCamion.estado = "Averiado";
      }

      return nuevoCamion;
    });

    const quedan = nodosRestantesAntesDeActualizar - 1;
    setNodosRestantesAntesDeActualizar(quedan);

    if (quedan <= 0) {
      setEsperandoActualizacion(true);
      setCamiones(nuevosCamiones);
      setHoraActual((prev) => prev + 1);
      
      // Si ya tenemos la solución anticipada cargada, usarla directamente
      if (proximaSolucionCargada) {
        console.log("⚡ TRANSICIÓN: Usando solución anticipada precargada para transición suave");
        await aplicarSolucionPrecargada(proximaSolucionCargada);
      } else {
        console.log("🔄 TRANSICIÓN: Solución anticipada no disponible, cargando en tiempo real...");
        await cargarDatos(false);
      }
    } else {
      setCamiones(nuevosCamiones);
      setHoraActual((prev) => prev + 1);
    }
  };

  /**
   * @function reiniciar
   * @description Reinicia la simulación a su estado inicial
   */
  const reiniciar = () => {
    const nuevosCamiones: CamionEstado[] = rutasCamiones.map((ruta) => {
      // Aquí intentamos mantener los datos previos del camión si existen
      const anterior = camiones.find((c) => c.id === ruta.id);
      return {
        id: ruta.id,
        ubicacion: ruta.ruta[0],
        porcentaje: 0,
        estado: anterior?.estado ?? "En Camino",
        capacidadActualGLP: anterior?.capacidadActualGLP ?? 0,
        capacidadMaximaGLP: anterior?.capacidadMaximaGLP ?? 0,
        combustibleActual: anterior?.combustibleActual ?? 0,
        combustibleMaximo: anterior?.combustibleMaximo ?? 0,
        distanciaMaxima: anterior?.distanciaMaxima ?? 0,
        pesoCarga: anterior?.pesoCarga ?? 0,
        pesoCombinado: anterior?.pesoCombinado ?? 0,
        tara: anterior?.tara ?? 0,
        tipo: anterior?.tipo ?? "",
        velocidadPromedio: anterior?.velocidadPromedio ?? 0,
      };
    });
    setCamiones(nuevosCamiones);
    setHoraActual(HORA_PRIMERA_ACTUALIZACION);
    setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
    setEsperandoActualizacion(false);
    setSolicitudAnticipadaEnviada(false);
    setProximaSolucionCargada(null);
    
    // Reiniciar el contador de tiempo real
    setInicioSimulacion(new Date());
    setTiempoRealSimulacion("00:00:00");
    console.log("⏱️ CONTADOR: Reiniciando contador de tiempo real de simulación...");
  };

  /**
   * @function iniciarContadorTiempo
   * @description Inicia el contador de tiempo real de la simulación
   */
  const iniciarContadorTiempo = () => {
    setInicioSimulacion(new Date());
    setTiempoRealSimulacion("00:00:00");
    console.log("⏱️ CONTADOR: Iniciando contador de tiempo real de simulación...");
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

  return (
    <SimulacionContext.Provider
      value={{
        horaActual,
        camiones,
        rutasCamiones,
        almacenes,
        fechaHoraSimulacion,
        diaSimulacion,
        tiempoRealSimulacion,
        avanzarHora,
        reiniciar,
        iniciarContadorTiempo,
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
    velocidadPromedio: camion.velocidadPromedio,
  };
};
