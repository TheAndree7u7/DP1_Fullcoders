/**
 * @file SimulacionContext.tsx
 * @description Contexto de React para manejar el estado global de la simulación de rutas de camiones.
 * Este contexto proporciona funcionalidad para controlar el avance de la simulación,
 * el estado de los camiones y sus rutas, y la sincronización con el backend.
 */

import React, { createContext, useContext, useEffect, useState } from "react";
import type { Almacen, Gen, Nodo, Pedido } from "../types";

// ============================
// IMPORTACIONES DE TIPOS Y CONSTANTES
// ============================

// Importar constantes
import {
  HORA_INICIAL,
  HORA_PRIMERA_ACTUALIZACION,
  NODOS_PARA_ACTUALIZACION,
  SEGUNDOS_POR_NODO,
} from "./simulacion";

// Importar tipos
import type {
  SimulacionContextType,
  CamionEstado,
  RutaCamion,
  Bloqueo,
  IndividuoConBloqueos,
} from "./simulacion";

// Re-exportar tipos para uso en otros archivos
export type { CamionEstado, RutaCamion } from "./simulacion";

// ============================
// IMPORTACIONES DE MÓDULOS REFACTORIZADOS
// ============================

// Importar módulos de datos
import {
  cargarDatos,
  cargarSolucionAnticipada,
  reiniciarSimulacionBackend,
} from "./simulacion/dataManager";

// Importar módulos de lógica de camiones
import {
  marcarCamionAveriado as marcarCamionAveriadoUtil,
} from "./simulacion/camionLogic";

// Importar módulos de polling
import {
  ejecutarPollingPrimerPaquete,
} from "./simulacion/pollingManager";

// Importar módulos de gestión de estado
import {
  limpiarEstadoParaNuevaSimulacion as limpiarEstadoParaNuevaSimulacionUtil,
  limpiarSimulacionCompleta as limpiarSimulacionCompletaUtil,
} from "./simulacion/stateManager";

// Importar módulos de avance de hora
import {
  avanzarHora as avanzarHoraUtil,
} from "./simulacion/avanceHora";

// Importar utilidades existentes
import {
  pausarSimulacion as pausarSimulacionUtil,
  reanudarSimulacion as reanudarSimulacionUtil,
  iniciarContadorTiempo as iniciarContadorTiempoUtil,
} from "./simulacion/utils";

// Importar utilidades de validación
import { esValorValido } from "../utils/validacionCamiones";

// ============================
// FUNCIONES AUXILIARES
// ============================

/**
 * @function mapearEstadoBackendAFrontend
 * @description Mapea los estados del backend a los estados del frontend
 */
const mapearEstadoBackendAFrontend = (estadoBackend: string | undefined): "En Camino" | "Disponible" | "Averiado" | "En Mantenimiento" | "En Mantenimiento Preventivo" | "En Mantenimiento por Avería" | "Entregado" => {
  if (estadoBackend === 'DISPONIBLE') {
    return 'Disponible';
  } else if (estadoBackend === 'EN_MANTENIMIENTO_POR_AVERIA') {
    return 'En Mantenimiento por Avería';
  } else if (estadoBackend === 'EN_MANTENIMIENTO_PREVENTIVO') {
    return 'En Mantenimiento Preventivo';
  } else if (estadoBackend === 'INMOVILIZADO_POR_AVERIA') {
    return 'Averiado';
  } else if (estadoBackend === 'EN_MANTENIMIENTO' || estadoBackend === 'EN_MANTENIMIENTO_CORRECTIVO') {
    return 'En Mantenimiento';
  } else {
    return 'En Camino';
  }
};

// ============================
// CREACIÓN DEL CONTEXTO
// ============================

const SimulacionContext = createContext<SimulacionContextType | undefined>(
  undefined,
);

// ============================
// COMPONENTE PROVEEDOR
// ============================

/**
 * @component SimulacionProvider
 * @description Proveedor del contexto de simulación que maneja el estado global
 * @param {Object} props - Propiedades del componente
 * @param {React.ReactNode} props.children - Componentes hijos que tendrán acceso al contexto
 */
export const SimulacionProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  // ============================
  // ESTADOS PRINCIPALES DE LA SIMULACIÓN
  // ============================
  
  // Estados de control de la simulación
  const [horaActual, setHoraActual] = useState<number>(HORA_INICIAL);
  const [simulacionActiva, setSimulacionActiva] = useState<boolean>(false);
  const [cargando, setCargando] = useState<boolean>(true);
  const [pollingActivo, setPollingActivo] = useState<boolean>(false);
  
  // Estados de datos de la simulación
  const [camiones, setCamiones] = useState<CamionEstado[]>([]);
  const [rutasCamiones, setRutasCamiones] = useState<RutaCamion[]>([]);
  const [almacenes, setAlmacenes] = useState<Almacen[]>([]);
  const [bloqueos, setBloqueos] = useState<Bloqueo[]>([]);
  const [pedidosNoAsignados, setPedidosNoAsignados] = useState<Pedido[]>([]);
  
  // Estados de control de actualización
  const [nodosRestantesAntesDeActualizar, setNodosRestantesAntesDeActualizar] =
    useState<number>(NODOS_PARA_ACTUALIZACION);
  const [esperandoActualizacion, setEsperandoActualizacion] =
    useState<boolean>(false);
  const [solicitudAnticipadaEnviada, setSolicitudAnticipadaEnviada] =
    useState<boolean>(false);
  const [proximaSolucionCargada, setProximaSolucionCargada] =
    useState<IndividuoConBloqueos | null>(null);
  const [primerPaqueteCargado, setPrimerPaqueteCargado] = useState<boolean>(false);
  
  // Estados de fechas y tiempo
  const [fechaHoraSimulacion, setFechaHoraSimulacion] = useState<string | null>(null);
  const [fechaInicioSimulacion, setFechaInicioSimulacion] = useState<string | null>(null);
  const [fechaHoraInicioIntervalo, setFechaHoraInicioIntervalo] = useState<string | null>(null);
  const [fechaHoraFinIntervalo, setFechaHoraFinIntervalo] = useState<string | null>(null);
  const [diaSimulacion, setDiaSimulacion] = useState<number | null>(null);
  const [inicioSimulacion, setInicioSimulacion] = useState<Date | null>(null);
  
  // Estados de tiempo y contadores
  const [tiempoRealSimulacion, setTiempoRealSimulacion] = useState<string>("00:00:00");
  const [tiempoTranscurridoSimulado, setTiempoTranscurridoSimulado] = useState<string>("00:00:00");
  const [horaSimulacion, setHoraSimulacion] = useState<string>("00:00:00");
  const [horaSimulacionAcumulada, setHoraSimulacionAcumulada] = useState<string>("00:00:00");
  const [fechaHoraAcumulada, setFechaHoraAcumulada] = useState<string>("");
  const [paqueteActualConsumido, setPaqueteActualConsumido] = useState<number>(0);

  // ============================
  // EFECTOS DE CONTROL DE TIEMPO
  // ============================

  // Contador de tiempo real de la simulación
  useEffect(() => {
    if (!inicioSimulacion || !simulacionActiva) return;

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
  }, [inicioSimulacion, simulacionActiva]);

  // Calcular la hora de simulación basado en fechaHoraSimulacion y horaActual
  useEffect(() => {
    if (fechaHoraSimulacion && horaActual >= 0) {
      let nuevaFecha: Date;
      
      // Si tenemos intervalos específicos, usarlos para un cálculo más preciso
      if (fechaHoraInicioIntervalo && fechaHoraFinIntervalo) {
        const fechaInicio = new Date(fechaHoraInicioIntervalo);
        const fechaFin = new Date(fechaHoraFinIntervalo);
        
        // Calcular la duración total del intervalo en milisegundos
        const duracionTotal = fechaFin.getTime() - fechaInicio.getTime();
        
        // Calcular el progreso dentro del intervalo actual (0-1)
        const progresoEnIntervalo = Math.min(horaActual / NODOS_PARA_ACTUALIZACION, 1);
        const tiempoTranscurrido = duracionTotal * progresoEnIntervalo;
        
        // Calcular la fecha exacta sumando el tiempo transcurrido a la fecha de inicio del intervalo
        nuevaFecha = new Date(fechaInicio.getTime() + tiempoTranscurrido);
        
        // Formatear solo la hora
        const horaFormateada = nuevaFecha.toLocaleTimeString('es-ES', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
        
        setHoraSimulacion(horaFormateada);
      } else {
        // Fallback al método anterior si no hay intervalos específicos
        const fechaBase = new Date(fechaHoraSimulacion);
        
        // Calculamos qué nodo estamos dentro del ciclo actual (0-24)
        const nodoEnCicloActual = horaActual % NODOS_PARA_ACTUALIZACION;
        
        // Calculamos segundos adicionales solo para el incremento local dentro del ciclo actual
        const segundosAdicionales = nodoEnCicloActual * SEGUNDOS_POR_NODO;
        
        // Crea nueva fecha sumando los segundos
        nuevaFecha = new Date(fechaBase.getTime() + segundosAdicionales * 1000);
        
        // Formatear solo la hora
        const horaFormateada = nuevaFecha.toLocaleTimeString('es-ES', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
        
        setHoraSimulacion(horaFormateada);
      }

      // Calcular hora acumulada desde el inicio de la simulación
      if (fechaInicioSimulacion) {
        const fechaInicio = new Date(fechaInicioSimulacion);
        
        // Calcular el tiempo total transcurrido desde el inicio
        const tiempoTotalTranscurrido = nuevaFecha.getTime() - fechaInicio.getTime();
        const fechaAcumulada = new Date(fechaInicio.getTime() + tiempoTotalTranscurrido);
        
        // Formatear hora acumulada
        const horaAcumuladaFormateada = fechaAcumulada.toLocaleTimeString('es-ES', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
        
        // Formatear fecha y hora acumulada completa
        const fechaHoraAcumuladaFormateada = fechaAcumulada.toLocaleString('es-ES', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
        
        setHoraSimulacionAcumulada(horaAcumuladaFormateada);
        setFechaHoraAcumulada(fechaHoraAcumuladaFormateada);
      }
    }
  }, [horaActual, fechaHoraSimulacion, fechaInicioSimulacion, fechaHoraInicioIntervalo, fechaHoraFinIntervalo]);

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

  // ============================
  // EFECTOS DE POLLING
  // ============================

  // Polling automático para obtener el primer paquete después de iniciar la simulación
  useEffect(() => {
    if (!pollingActivo || !simulacionActiva || primerPaqueteCargado) return;

    console.log("🔄 POLLING: Iniciando polling para primer paquete...");
    const cleanup = ejecutarPollingPrimerPaquete(
      fechaInicioSimulacion,
      setPollingActivo,
      setCargando,
      async (data: IndividuoConBloqueos) => {
        await aplicarSolucionPrecargada(data);
        setHoraActual(HORA_PRIMERA_ACTUALIZACION);
        setCargando(false);
        setPrimerPaqueteCargado(true); // Marcar como cargado para evitar polling duplicado
        console.log("🎉 POLLING: Primer paquete aplicado exitosamente al mapa desde la hora", HORA_PRIMERA_ACTUALIZACION);
      }
    );

    return cleanup;
  }, [pollingActivo, simulacionActiva, fechaInicioSimulacion, primerPaqueteCargado]);

  // Monitoreo de cambios en datos de camiones para detectar inconsistencias
  useEffect(() => {
    try {
      // console.log("🔍 MONITOREO: Verificando consistencia de datos de camiones...");
      
      if (!camiones || !Array.isArray(camiones)) {
        console.error("❌ ERROR: camiones no es un array válido en monitoreo:", camiones);
        return;
      }
      
      if (!rutasCamiones || !Array.isArray(rutasCamiones)) {
        console.error("❌ ERROR: rutasCamiones no es un array válido en monitoreo:", rutasCamiones);
        return;
      }
      
      // Verificar consistencia entre camiones y rutas
      const camionesSinRuta = camiones.filter(camion => 
        !rutasCamiones.some(ruta => ruta.id === camion.id)
      );
      
      const rutasSinCamion = rutasCamiones.filter(ruta => 
        !camiones.some(camion => camion.id === ruta.id)
      );
      
      if (camionesSinRuta.length > 0) {
        console.warn("⚠️ ADVERTENCIA: Camiones sin ruta correspondiente:", camionesSinRuta.map(c => c.id));
      }
      
      if (rutasSinCamion.length > 0) {
        console.warn("⚠️ ADVERTENCIA: Rutas sin camión correspondiente:", rutasSinCamion.map(r => r.id));
      }
      
      // Verificar datos nulos o inválidos en camiones
      camiones.forEach((camion, index) => {
        if (!camion.id) {
          console.error(`❌ ERROR: Camión en índice ${index} no tiene ID en monitoreo:`, camion);
        }
        
        if (!esValorValido(camion.capacidadActualGLP)) {
          console.error(`❌ ERROR: Camión ${camion.id} tiene capacidadActualGLP inválida en monitoreo:`, camion.capacidadActualGLP);
        }
        
        if (!esValorValido(camion.combustibleActual)) {
          console.error(`❌ ERROR: Camión ${camion.id} tiene combustibleActual inválido en monitoreo:`, camion.combustibleActual);
        }
      });
      
      // console.log(`✅ MONITOREO COMPLETADO: ${camiones.length} camiones, ${rutasCamiones.length} rutas`);
      
    } catch (error) {
      console.error("❌ ERROR en monitoreo de camiones:", error);
    }
  }, [camiones, rutasCamiones]);

  // ============================
  // FUNCIONES DE GESTIÓN DE DATOS
  // ============================

  /**
   * @function aplicarSolucionPrecargada
   * @description Aplica una solución previamente cargada para transición suave
   */
  const aplicarSolucionPrecargada = async (data: IndividuoConBloqueos) => {
    try {
      console.log("⚡============================= TRANSICIÓN: Aplicando solución precargada======================================");
      
      // Actualizar fechas del paquete actual siendo consumido
      if (data.fechaHoraInicioIntervalo) {
        setFechaHoraInicioIntervalo(data.fechaHoraInicioIntervalo);
      }
      if (data.fechaHoraFinIntervalo) {
        setFechaHoraFinIntervalo(data.fechaHoraFinIntervalo);
      }
      console.log("Fecha de inicio del intervalo", data.fechaHoraInicioIntervalo);
      console.log("Fecha de fin del intervalo", data.fechaHoraFinIntervalo);
      
      // Actualizar fecha y hora de la simulación
      if (data.fechaHoraSimulacion) {
        setFechaHoraSimulacion(data.fechaHoraSimulacion);

        // Establecer fecha de inicio si es la primera vez o si no existe
        if (!fechaInicioSimulacion) {
          setFechaInicioSimulacion(data.fechaHoraSimulacion);
        }

        const fecha = new Date(data.fechaHoraSimulacion);
        setDiaSimulacion(fecha.getDate());
      }

      // Procesar rutas de camiones
      const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen: Gen) => ({
        id: gen.camion.codigo,
        ruta: gen.nodos.map((n: Nodo) => `(${n.coordenada.x},${n.coordenada.y})`),
        puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
        pedidos: gen.pedidos,
      }));

      setRutasCamiones(nuevasRutas);

      // Procesar estado de camiones
      const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
        const gen = data.cromosoma.find((g: Gen) => g.camion.codigo === ruta.id);
        const camion = gen?.camion;
        const anterior = camiones.find(c => c.id === ruta.id);
        
        // NUEVA LÓGICA: Teletransporte para camiones averiados
        let ubicacion: string;
        let porcentaje: number;
        
        if (anterior && anterior.estado === "Averiado") {
          // Si el camión estaba averiado, teletransportarlo a su nueva posición en el cromosoma
          ubicacion = ruta.ruta[0]; // Primera posición de la nueva ruta
          porcentaje = 0; // Reiniciar progreso
          console.log(`🚛💥 TELETRANSPORTE: Camión ${ruta.id} averiado teletransportado de ${anterior.ubicacion} a ${ubicacion}`);
        } else {
          // Para camiones no averiados, mantener lógica anterior
          ubicacion = anterior?.ubicacion ?? ruta.ruta[0];
          porcentaje = 0;
        }
        
        // Verificar si el camión está en el almacén central (posición 0,0)
        const estaEnAlmacenCentral = ubicacion === '(0,0)' || ubicacion === '(0, 0)';
        
        // Mapear estados del backend al frontend
        let estadoFrontend: "En Camino" | "Disponible" | "Averiado" | "En Mantenimiento" | "En Mantenimiento Preventivo" | "Entregado" | "En Mantenimiento por Avería";
        
        if (anterior && anterior.estado === "Averiado") {
          // Si el camión estaba averiado, mantenerlo como averiado pero en nueva posición
          estadoFrontend = "Averiado";
          
          estadoFrontend = mapearEstadoBackendAFrontend(camion?.estado);
          console.log(`🚛💥 ESTADO: Camión ${ruta.id} mantiene estado 'Averiado' en nueva posición ${ubicacion}`);
        } else if (estaEnAlmacenCentral) {
          // Si está en almacén central, mantener estado simple
          estadoFrontend = mapearEstadoBackendAFrontend(camion?.estado);
        } else {
          // Si no está en almacén central, aplicar mapeo completo de estados
          estadoFrontend = mapearEstadoBackendAFrontend(camion?.estado);
        }
        
        return {
          id: ruta.id,
          ubicacion,
          porcentaje,
          estado: estadoFrontend,
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

      // Actualizar bloqueos y almacenes
      if (data.bloqueos) {
        setBloqueos(data.bloqueos);
      } else {
        setBloqueos([]);
      }

      if (data.almacenes && data.almacenes.length > 0) {
        setAlmacenes(data.almacenes);
      }

      // Procesar pedidos no asignados
      if (data.pedidos) {
        setPedidosNoAsignados(data.pedidos);
        console.log(`✅ TRANSICIÓN: ${data.pedidos.length} pedidos no asignados procesados`);
      } else {
        setPedidosNoAsignados([]);
        console.log("⚠️ TRANSICIÓN: No hay pedidos no asignados en la solución precargada");
      }

      // CRÍTICO: Reiniciar el tiempo de simulación para sincronizar con el nuevo intervalo
      // Esto evita que el reloj siga avanzando mientras los camiones empiezan desde 0
      setHoraActual(0); // Reiniciar el nodo actual
      setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
      setEsperandoActualizacion(false);
      setSolicitudAnticipadaEnviada(false);
      setProximaSolucionCargada(null);

      // Asegurar que el estado de carga esté en false después de aplicar datos
      setCargando(false);

      // Incrementar el contador de paquetes consumidos
      setPaqueteActualConsumido(prev => prev + 1);
      
      console.log("✅ TRANSICIÓN: Solución precargada aplicada exitosamente");
    } catch (error) {
      console.error("❌ TRANSICIÓN: Error al aplicar solución precargada:", error);
      throw error;
    }
  };

  /**
   * @function aplicarNuevaSolucionDespuesAveria
   * @description Aplica la nueva solución recalculada después de una avería, manteniendo la continuidad temporal
   */
  const aplicarNuevaSolucionDespuesAveria = async (data: IndividuoConBloqueos) => {
    try {
      console.log("🔄============================= NUEVA SOLUCIÓN: Aplicando solución recalculada después de avería======================================");
      
 
      // Actualizar fechas del paquete actual siendo consumido
      if (data.fechaHoraInicioIntervalo) {
        setFechaHoraInicioIntervalo(data.fechaHoraInicioIntervalo);
      }
      if (data.fechaHoraFinIntervalo) {
        setFechaHoraFinIntervalo(data.fechaHoraFinIntervalo);
      }
      console.log("Fecha de inicio del intervalo", data.fechaHoraInicioIntervalo);
      console.log("Fecha de fin del intervalo", data.fechaHoraFinIntervalo);
      
      // Actualizar fecha y hora de la simulación
      if (data.fechaHoraSimulacion) {
        setFechaHoraSimulacion(data.fechaHoraSimulacion);

        // Establecer fecha de inicio si es la primera vez o si no existe
        if (!fechaInicioSimulacion) {
          setFechaInicioSimulacion(data.fechaHoraSimulacion);
        }

        const fecha = new Date(data.fechaHoraSimulacion);
        setDiaSimulacion(fecha.getDate());
      }

      // Procesar rutas de camiones
      const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen: Gen) => ({
        id: gen.camion.codigo,
        ruta: gen.nodos.map((n: Nodo) => `(${n.coordenada.x},${n.coordenada.y})`),
        puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
        pedidos: gen.pedidos,
      }));

      setRutasCamiones(nuevasRutas);

      // Procesar estado de camiones
      const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
        const gen = data.cromosoma.find((g: Gen) => g.camion.codigo === ruta.id);
        const camion = gen?.camion;
        const anterior = camiones.find(c => c.id === ruta.id);
        
        // NUEVA LÓGICA: Teletransporte para camiones averiados
        let ubicacion: string;
        let porcentaje: number;
        
        if (anterior && anterior.estado === "Averiado") {
          // Si el camión estaba averiado, teletransportarlo a su nueva posición en el cromosoma
          ubicacion = ruta.ruta[0]; // Primera posición de la nueva ruta
          porcentaje = 0; // Reiniciar progreso
          console.log(`🚛💥 TELETRANSPORTE: Camión ${ruta.id} averiado teletransportado de ${anterior.ubicacion} a ${ubicacion}`);
        } else {
          // Para camiones no averiados, mantener lógica anterior
          ubicacion = anterior?.ubicacion ?? ruta.ruta[0];
          porcentaje = 0;
        }
        
        // Verificar si el camión está en el almacén central (posición 0,0)
        const estaEnAlmacenCentral = ubicacion === '(0,0)' || ubicacion === '(0, 0)';
        
        // Mapear estados del backend al frontend
        let estadoFrontend: "En Camino" | "Disponible" | "Averiado" | "En Mantenimiento" | "En Mantenimiento Preventivo" | "Entregado" | "En Mantenimiento por Avería";
        
        if (anterior && anterior.estado === "Averiado") {
          // Si el camión estaba averiado, mantenerlo como averiado pero en nueva posición
          estadoFrontend = "Averiado";
          console.log(`🚛💥 ESTADO: Camión ${ruta.id} mantiene estado 'Averiado' en nueva posición ${ubicacion}`);
        } else if (estaEnAlmacenCentral) {
          // Si está en almacén central, mantener estado simple
          estadoFrontend = camion?.estado === 'DISPONIBLE' ? 'Disponible' : 'En Camino';
        } else {
          // Si no está en almacén central, aplicar mapeo completo de estados
          estadoFrontend = mapearEstadoBackendAFrontend(camion?.estado);
        }
        
        return {
          id: ruta.id,
          ubicacion,
          porcentaje,
          estado: estadoFrontend,
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

      // Actualizar bloqueos y almacenes
      if (data.bloqueos) {
        setBloqueos(data.bloqueos);
      } else {
        setBloqueos([]);
      }

      if (data.almacenes && data.almacenes.length > 0) {
        setAlmacenes(data.almacenes);
      }

      // Procesar pedidos no asignados
      if (data.pedidos) {
        setPedidosNoAsignados(data.pedidos);
        console.log(`✅ TRANSICIÓN: ${data.pedidos.length} pedidos no asignados procesados`);
      } else {
        setPedidosNoAsignados([]);
        console.log("⚠️ TRANSICIÓN: No hay pedidos no asignados en la solución precargada");
      }

      // CRÍTICO: Reiniciar el tiempo de simulación para sincronizar con el nuevo intervalo
      // Esto evita que el reloj siga avanzando mientras los camiones empiezan desde 0
      setHoraActual(0); // Reiniciar el nodo actual
      setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
      setEsperandoActualizacion(false);
      setSolicitudAnticipadaEnviada(false);
      setProximaSolucionCargada(null);

      // Asegurar que el estado de carga esté en false después de aplicar datos
      setCargando(false);

      // Incrementar el contador de paquetes consumidos
      setPaqueteActualConsumido(prev => prev + 1);
      
      console.log("✅ TRANSICIÓN: Solución precargada aplicada exitosamente");
    } catch (error) {
      console.error("❌ TRANSICIÓN: Error al aplicar solución precargada:", error);
      throw error;
    }
  };

  /**
   * @function cargarDatosSimulacion
   * @description Carga los datos de simulación desde el backend
   */
  const cargarDatosSimulacion = async () => {
    try {
      console.log("🔄 CARGANDO: Iniciando carga de datos de simulación...");
      const datos = await cargarDatos(fechaInicioSimulacion);
      
      // Validación de datos recibidos
      // console.log("🔍 VALIDACIÓN: Verificando datos recibidos del backend...");
      
      if (!datos.nuevasRutas || !Array.isArray(datos.nuevasRutas)) {
        console.error("❌ ERROR: nuevasRutas no es un array válido:", datos.nuevasRutas);
        throw new Error("Datos de rutas inválidos recibidos del backend");
      }
      
      if (!datos.nuevosCamiones || !Array.isArray(datos.nuevosCamiones)) {
        console.error("❌ ERROR: nuevosCamiones no es un array válido:", datos.nuevosCamiones);
        throw new Error("Datos de camiones inválidos recibidos del backend");
      }
      
      console.log(`✅ DATOS RECIBIDOS: ${datos.nuevasRutas.length} rutas, ${datos.nuevosCamiones.length} camiones`);
      
      // Actualizar fechas del paquete actual siendo consumido
      if (datos.fechaHoraInicioIntervalo) {
        setFechaHoraInicioIntervalo(datos.fechaHoraInicioIntervalo);
      }
      if (datos.fechaHoraFinIntervalo) {
        setFechaHoraFinIntervalo(datos.fechaHoraFinIntervalo);
      }
      
      // Actualizar fecha y hora de la simulación
      if (datos.fechaHoraSimulacion) {
        setFechaHoraSimulacion(datos.fechaHoraSimulacion);

        // Establecer fecha de inicio si es la primera vez o si no existe
        if (!fechaInicioSimulacion) {
          setFechaInicioSimulacion(datos.fechaHoraSimulacion);
        }

        if (datos.diaSimulacion) {
          setDiaSimulacion(datos.diaSimulacion);
        }
      }

      // Validar y eliminar duplicados antes de establecer las rutas
      // console.log("🔍 VALIDACIÓN: Procesando rutas...");
      const rutasUnicas = datos.nuevasRutas.filter((ruta, index, array) => {
        if (!ruta.id) {
          console.error(`❌ ERROR: Ruta en índice ${index} no tiene ID:`, ruta);
          return false;
        }
        return array.findIndex(r => r.id === ruta.id) === index;
      });
      setRutasCamiones(rutasUnicas);
      console.log(`✅ RUTAS PROCESADAS: ${rutasUnicas.length} rutas únicas`);

      // Validar y eliminar duplicados antes de establecer los camiones
      // console.log("🔍 VALIDACIÓN: Procesando camiones...");
      const camionesUnicos = datos.nuevosCamiones.filter((camion, index, array) => {
        if (!camion.id) {
          console.error(`❌ ERROR: Camión en índice ${index} no tiene ID:`, camion);
          return false;
        }
        
        // Validar propiedades críticas
        if (!esValorValido(camion.capacidadActualGLP)) {
          console.error(`❌ ERROR: Camión ${camion.id} tiene capacidadActualGLP inválida:`, camion.capacidadActualGLP);
        }
        if (!esValorValido(camion.combustibleActual)) {
          console.error(`❌ ERROR: Camión ${camion.id} tiene combustibleActual inválido:`, camion.combustibleActual);
        }
        
        return array.findIndex(c => c.id === camion.id) === index;
      });
      setCamiones(camionesUnicos);
      console.log(`✅ CAMIONES PROCESADOS: ${camionesUnicos.length} camiones únicos`);

      // Validar bloqueos
      if (datos.bloqueos && Array.isArray(datos.bloqueos)) {
        setBloqueos(datos.bloqueos);
        console.log(`✅ BLOQUEOS PROCESADOS: ${datos.bloqueos.length} bloqueos`);
      } else {
        console.warn("⚠️ ADVERTENCIA: No hay bloqueos válidos, estableciendo array vacío");
        setBloqueos([]);
      }

      // Gestionar almacenes: priorizar los que vienen del backend, sino mantener los actuales
      if (datos.almacenes && Array.isArray(datos.almacenes) && datos.almacenes.length > 0) {
        setAlmacenes(datos.almacenes);
        console.log(`✅ ALMACENES PROCESADOS: ${datos.almacenes.length} almacenes`);
      } else {
        console.warn("⚠️ ADVERTENCIA: No hay almacenes válidos del backend, manteniendo almacenes actuales");
      }

      // Procesar pedidos no asignados
      if (datos.pedidosNoAsignados && Array.isArray(datos.pedidosNoAsignados)) {
        setPedidosNoAsignados(datos.pedidosNoAsignados);
        console.log(`✅ PEDIDOS NO ASIGNADOS PROCESADOS: ${datos.pedidosNoAsignados.length} pedidos`);
      } else {
        console.warn("⚠️ ADVERTENCIA: No hay pedidos no asignados válidos, estableciendo array vacío");
        setPedidosNoAsignados([]);
      }

      // CRÍTICO: Reiniciar el tiempo de simulación para sincronizar con el nuevo intervalo
      // Esto evita que el reloj siga avanzando mientras los camiones empiezan desde 0
      setHoraActual(0); // Reiniciar el nodo actual
      setNodosRestantesAntesDeActualizar(NODOS_PARA_ACTUALIZACION);
      setEsperandoActualizacion(false);
      setSolicitudAnticipadaEnviada(false);
      setProximaSolucionCargada(null);

      // Incrementar el contador de paquetes consumidos
      setPaqueteActualConsumido(prev => prev + 1);
      
      console.log("✅ CARGANDO: Tiempo de simulación reiniciado para sincronizar con nuevo intervalo");
      
      console.log("✅ CARGA COMPLETADA: Datos de simulación cargados exitosamente");
    } catch (error) {
      console.error("❌ ERROR CRÍTICO al cargar datos de simulación:", error);
      throw error;
    }
  };

  /**
   * @function cargarSolucionAnticipadaLocal
   * @description Carga anticipadamente la siguiente solución para transición suave
   */
  const cargarSolucionAnticipadaLocal = async () => {
    try {
      
      console.log("Solucion anticipada cargada desde el contexto en cargarSolucionAnticipadaLocal");
      const data = await cargarSolucionAnticipada(fechaHoraFinIntervalo);
      setProximaSolucionCargada(data);
    } catch (error) {
      console.error("⚠️ ANTICIPADA: Error al cargar solución anticipada:", error);
    }
  };

  // ============================
  // FUNCIONES DE CONTROL DE SIMULACIÓN
  // ============================

  /**
   * @function avanzarHora
   * @description Avanza la simulación una hora, actualizando la posición de los camiones
   * y recargando datos del backend cuando sea necesario
   */
  const avanzarHora = async () => {
    await avanzarHoraUtil(
      camiones,
      rutasCamiones,
      esperandoActualizacion,
      simulacionActiva,
      nodosRestantesAntesDeActualizar,
      solicitudAnticipadaEnviada,
      proximaSolucionCargada,
      setCamiones,
      setHoraActual,
      setNodosRestantesAntesDeActualizar,
      setEsperandoActualizacion,
      setSolicitudAnticipadaEnviada,
      cargarSolucionAnticipadaLocal,
      aplicarSolucionPrecargada,
      cargarDatosSimulacion
    );
  };

  /**
   * @function iniciarContadorTiempo
   * @description Inicia el contador de tiempo real de la simulación
   */
  const iniciarContadorTiempo = () => {
    iniciarContadorTiempoUtil(setInicioSimulacion, setTiempoRealSimulacion, setSimulacionActiva);
  };

  /**
   * @function pausarSimulacion
   * @description Pausa la simulación desactivando el contador de tiempo
   */
  const pausarSimulacion = () => {
    pausarSimulacionUtil(setSimulacionActiva);
  };

  /**
   * @function reanudarSimulacion
   * @description Reanuda la simulación activando el contador de tiempo
   */
  const reanudarSimulacion = () => {
    reanudarSimulacionUtil(setSimulacionActiva);
  };

  // ============================
  // FUNCIONES DE GESTIÓN DE ESTADO
  // ============================

  /**
   * @function reiniciar
   * @description Reinicia la simulación a su estado inicial y limpia paquetes del backend
   */
  const reiniciar = async () => {
    console.log("🔄 REINICIO: Iniciando reinicio completo de la simulación...");

    try {
      // Primero reiniciar los paquetes en el backend
      await reiniciarSimulacionBackend();

      // Limpiar estado y cargar nuevos datos
      limpiarEstadoParaNuevaSimulacionUtil(
        setCamiones,
        setRutasCamiones,
        setBloqueos,
        setPedidosNoAsignados,
        setFechaHoraSimulacion,
        setFechaHoraInicioIntervalo,
        setFechaHoraFinIntervalo,
        setDiaSimulacion,
        setTiempoTranscurridoSimulado,
        setHoraSimulacionAcumulada,
        setFechaHoraAcumulada,
        setHoraActual,
        setNodosRestantesAntesDeActualizar,
        setEsperandoActualizacion,
        setSolicitudAnticipadaEnviada,
        setProximaSolucionCargada,
        setPaqueteActualConsumido,
        setInicioSimulacion,
        setTiempoRealSimulacion,
        setSimulacionActiva,
        setPollingActivo,
        setCargando,
        setPrimerPaqueteCargado,
        fechaInicioSimulacion
      );

      console.log("🔄 REINICIO: Reinicio completo finalizado - estado local y backend limpiados");
    } catch (error) {
      console.error("❌ REINICIO: Error al reiniciar simulación:", error);
      throw error;
    }
  };

  /**
   * @function reiniciarYEmpezarNuevo
   * @description Reinicia completamente la simulación y empieza a cargar nuevos paquetes
   */
  const reiniciarYEmpezarNuevo = async () => {
    console.log("🚀 NUEVO INICIO: Reiniciando simulación para empezar con nuevos paquetes...");

    try {
      // Primero reiniciar los paquetes en el backend
      await reiniciarSimulacionBackend();

      // Limpiar completamente el estado local y cargar nuevos datos
      limpiarEstadoParaNuevaSimulacionUtil(
        setCamiones,
        setRutasCamiones,
        setBloqueos,
        setPedidosNoAsignados,
        setFechaHoraSimulacion,
        setFechaHoraInicioIntervalo,
        setFechaHoraFinIntervalo,
        setDiaSimulacion,
        setTiempoTranscurridoSimulado,
        setHoraSimulacionAcumulada,
        setFechaHoraAcumulada,
        setHoraActual,
        setNodosRestantesAntesDeActualizar,
        setEsperandoActualizacion,
        setSolicitudAnticipadaEnviada,
        setProximaSolucionCargada,
        setPaqueteActualConsumido,
        setInicioSimulacion,
        setTiempoRealSimulacion,
        setSimulacionActiva,
        setPollingActivo,
        setCargando,
        setPrimerPaqueteCargado,
        fechaInicioSimulacion
      );

      console.log("🎉 NUEVO INICIO: Simulación reiniciada y nuevos datos cargados exitosamente");
    } catch (error) {
      console.error("❌ NUEVO INICIO: Error al reiniciar e iniciar nueva simulación:", error);
      throw error;
    }
  };

  /**
   * @function limpiarEstadoParaNuevaSimulacion
   * @description Limpia el estado para una nueva simulación y carga los primeros datos
   */
  const limpiarEstadoParaNuevaSimulacion = async () => {
    limpiarEstadoParaNuevaSimulacionUtil(
      setCamiones,
      setRutasCamiones,
      setBloqueos,
      setPedidosNoAsignados,
      setFechaHoraSimulacion,
      setFechaHoraInicioIntervalo,
      setFechaHoraFinIntervalo,
      setDiaSimulacion,
      setTiempoTranscurridoSimulado,
      setHoraSimulacionAcumulada,
      setFechaHoraAcumulada,
      setHoraActual,
      setNodosRestantesAntesDeActualizar,
      setEsperandoActualizacion,
      setSolicitudAnticipadaEnviada,
      setProximaSolucionCargada,
      setPaqueteActualConsumido,
      setInicioSimulacion,
      setTiempoRealSimulacion,
      setSimulacionActiva,
      setPollingActivo,
      setCargando,
      setPrimerPaqueteCargado,
      fechaInicioSimulacion
    );
  };

  /**
   * @function limpiarSimulacionCompleta
   * @description Limpia completamente la simulación incluyendo la fecha de inicio
   * Se usa cuando se inicia una nueva simulación semanal
   */
  const limpiarSimulacionCompleta = () => {
    limpiarSimulacionCompletaUtil(
      setCamiones,
      setRutasCamiones,
      setBloqueos,
      setPedidosNoAsignados,
      setFechaHoraSimulacion,
      setFechaInicioSimulacion,
      setFechaHoraInicioIntervalo,
      setFechaHoraFinIntervalo,
      setDiaSimulacion,
      setTiempoTranscurridoSimulado,
      setHoraSimulacionAcumulada,
      setFechaHoraAcumulada,
      setHoraActual,
      setNodosRestantesAntesDeActualizar,
      setEsperandoActualizacion,
      setSolicitudAnticipadaEnviada,
      setProximaSolucionCargada,
      setPaqueteActualConsumido,
      setSimulacionActiva,
      setPollingActivo,
      setCargando,
      setPrimerPaqueteCargado
    );
  };

  // ============================
  // FUNCIONES DE CONTROL DE POLLING
  // ============================

  /**
   * @function iniciarPollingPrimerPaquete
   * @description Inicia el polling para obtener el primer paquete disponible
   */
  const iniciarPollingPrimerPaquete = () => {
    setPollingActivo(true);
  };

  // ============================
  // FUNCIONES DE GESTIÓN DE CAMIONES
  // ============================

  /**
   * @function marcarCamionAveriado
   * @description Marca un camión como averiado, deteniéndolo en su posición actual
   * @param {string} camionId - El ID del camión a averiar
   */
  const marcarCamionAveriado = (camionId: string) => {
    const nuevosCamiones = marcarCamionAveriadoUtil(camiones, camionId);
    setCamiones(nuevosCamiones);
  };

  // ============================
  // FUNCIONES DE INFORMACIÓN
  // ============================

  /**
   * @function obtenerInfoPaqueteActual
   * @description Obtiene información del paquete que se está consumiendo actualmente en el mapa
   * @returns {Object} Información del paquete actual: inicio, fin y número
   */
  const obtenerInfoPaqueteActual = () => {
    return {
      inicio: fechaHoraInicioIntervalo,
      fin: fechaHoraFinIntervalo,
      numero: paqueteActualConsumido
    };
  };

  // ============================
  // VALOR DEL CONTEXTO
  // ============================

  const contextValue: SimulacionContextType = {
    // Estados de datos
    horaActual,
    camiones,
    rutasCamiones,
    almacenes,
    pedidosNoAsignados,
    bloqueos,
    cargando,
    
    // Estados de fechas y tiempo
    fechaHoraSimulacion,
    fechaInicioSimulacion,
    fechaHoraInicioIntervalo,
    fechaHoraFinIntervalo,
    diaSimulacion,
    tiempoRealSimulacion,
    tiempoTranscurridoSimulado,
    horaSimulacion,
    horaSimulacionAcumulada,
    fechaHoraAcumulada,
    paqueteActualConsumido,
    
    // Estados de control
    simulacionActiva,
    
    // Funciones de control de simulación
    avanzarHora,
    reiniciar,
    iniciarContadorTiempo,
    reiniciarYEmpezarNuevo,
    pausarSimulacion,
    reanudarSimulacion,
    
    // Funciones de gestión de estado
    limpiarEstadoParaNuevaSimulacion,
    limpiarSimulacionCompleta,
    
    // Funciones de control de polling
    iniciarPollingPrimerPaquete,
    
    // Funciones de gestión de camiones
    marcarCamionAveriado,
    
    // Funciones de información
    obtenerInfoPaqueteActual,
    
    // Funciones de recálculo después de avería
    aplicarNuevaSolucionDespuesAveria,
    
    // Setters
    setSimulacionActiva,
    setPollingActivo,
    setFechaInicioSimulacion,
  };

  return (
    <SimulacionContext.Provider value={contextValue}>
      {children}
    </SimulacionContext.Provider>
  );
};

// ============================
// HOOK PERSONALIZADO
// ============================

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






