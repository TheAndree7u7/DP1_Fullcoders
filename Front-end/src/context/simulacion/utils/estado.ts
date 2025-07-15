/**
 * @file estado.ts
 * @description Utilidades para capturar y gestionar el estado completo de la simulación
 */

import type { RutaCamion } from '../../../types';
import type { Almacen } from '../../../types';
import type { CamionEstado, Bloqueo } from '../../SimulacionContext';

/**
 * @interface EstadoSimulacionCompleto
 * @description Representa el estado completo de la simulación en un momento dado
 */
export interface EstadoSimulacionCompleto {
  timestamp: string;
  horaActual: number;
  horaSimulacion: string;
  fechaHoraSimulacion: string | null;
  fechaInicioSimulacion: string | null;
  diaSimulacion: number | null;
  tiempoRealSimulacion: string;
  tiempoTranscurridoSimulado: string;
  camiones: CamionEstado[];
  rutasCamiones: RutaCamion[];
  almacenes: Almacen[];
  bloqueos: Bloqueo[];
}

/**
 * Captura el estado completo actual de la simulación
 * @param {Object} estadoActual - Estado actual de la simulación desde el contexto
 * @returns {EstadoSimulacionCompleto} Estado completo capturado
 */
export const capturarEstadoCompleto = (estadoActual: {
  horaActual: number;
  horaSimulacion: string;
  fechaHoraSimulacion: string | null;
  fechaInicioSimulacion: string | null;
  diaSimulacion: number | null;
  tiempoRealSimulacion: string;
  tiempoTranscurridoSimulado: string;
  camiones: CamionEstado[];
  rutasCamiones: RutaCamion[];
  almacenes: Almacen[];
  bloqueos: Bloqueo[];
}): EstadoSimulacionCompleto => {
  const timestamp = new Date().toISOString();
  
  console.log("📸 ESTADO: Capturando estado completo de la simulación...");
  console.log("📊 ESTADO: Datos capturados:", {
    timestamp,
    horaActual: estadoActual.horaActual,
    camiones: estadoActual.camiones.length,
    rutas: estadoActual.rutasCamiones.length,
    almacenes: estadoActual.almacenes.length,
    bloqueos: estadoActual.bloqueos.length
  });

  return {
    timestamp,
    horaActual: estadoActual.horaActual,
    horaSimulacion: estadoActual.horaSimulacion,
    fechaHoraSimulacion: estadoActual.fechaHoraSimulacion,
    fechaInicioSimulacion: estadoActual.fechaInicioSimulacion,
    diaSimulacion: estadoActual.diaSimulacion,
    tiempoRealSimulacion: estadoActual.tiempoRealSimulacion,
    tiempoTranscurridoSimulado: estadoActual.tiempoTranscurridoSimulado,
    // Crear copias profundas para evitar referencias mutables
    camiones: JSON.parse(JSON.stringify(estadoActual.camiones)),
    rutasCamiones: JSON.parse(JSON.stringify(estadoActual.rutasCamiones)),
    almacenes: JSON.parse(JSON.stringify(estadoActual.almacenes)),
    bloqueos: JSON.parse(JSON.stringify(estadoActual.bloqueos))
  };
};

/**
 * Genera un resumen del estado para logging
 * @param {EstadoSimulacionCompleto} estado - Estado completo a resumir
 * @returns {string} Resumen legible del estado
 */
export const generarResumenEstado = (estado: EstadoSimulacionCompleto): string => {
  const camionesEnCamino = estado.camiones.filter(c => c.estado === "En Camino").length;
  const camionesAveriados = estado.camiones.filter(c => c.estado === "Averiado").length;
  const camionesEntregados = estado.camiones.filter(c => c.estado === "Entregado").length;
  
  const totalPedidos = estado.rutasCamiones.reduce((total, ruta) => total + ruta.pedidos.length, 0);
  
  return `Estado al ${estado.timestamp}:
  🕐 Hora actual: ${estado.horaActual} | Hora simulación: ${estado.horaSimulacion}
  🚛 Camiones: ${estado.camiones.length} total (${camionesEnCamino} en camino, ${camionesAveriados} averiados, ${camionesEntregados} entregados)
  📦 Pedidos: ${totalPedidos} total
  🏪 Almacenes: ${estado.almacenes.length}
  🚧 Bloqueos: ${estado.bloqueos.length}`;
};

/**
 * Convierte el estado completo del frontend al formato simplificado que espera el backend
 * @param {EstadoSimulacionCompleto} estado - Estado completo del frontend
 * @returns {object} Estado simplificado para el backend
 */
export const convertirEstadoParaBackend = (estado: EstadoSimulacionCompleto): object => {
  console.log("🔄 ESTADO: Convirtiendo estado para backend...");
  
  return {
    timestamp: estado.timestamp,
    horaActual: estado.horaActual,
    horaSimulacion: estado.horaSimulacion,
    fechaHoraSimulacion: estado.fechaHoraSimulacion,
    fechaInicioSimulacion: estado.fechaInicioSimulacion,
    diaSimulacion: estado.diaSimulacion,
    tiempoRealSimulacion: estado.tiempoRealSimulacion,
    tiempoTranscurridoSimulado: estado.tiempoTranscurridoSimulado,
    
    // Convertir camiones
    camiones: estado.camiones.map(camion => ({
      id: camion.id,
      ubicacion: camion.ubicacion,
      porcentaje: camion.porcentaje,
      estado: camion.estado,
      capacidadActualGLP: camion.capacidadActualGLP,
      capacidadMaximaGLP: camion.capacidadMaximaGLP,
      combustibleActual: camion.combustibleActual,
      combustibleMaximo: camion.combustibleMaximo,
      distanciaMaxima: camion.distanciaMaxima,
      pesoCarga: camion.pesoCarga,
      pesoCombinado: camion.pesoCombinado,
      tara: camion.tara,
      tipo: camion.tipo,
      velocidadPromedio: camion.velocidadPromedio
    })),
    
    // Convertir rutas de camiones
    rutasCamiones: estado.rutasCamiones.map(ruta => ({
      id: ruta.id,
      ruta: ruta.ruta,
      puntoDestino: ruta.puntoDestino,
      pedidos: ruta.pedidos.map(pedido => ({
        codigo: pedido.codigo,
        coordenadaX: pedido.coordenada?.x || 0,
        coordenadaY: pedido.coordenada?.y || 0,
        horasLimite: pedido.horasLimite,
        volumenGLPAsignado: pedido.volumenGLPAsignado,
        estado: pedido.estado,
        fechaRegistro: pedido.fechaRegistro,
        fechaLimite: pedido.fechaLimite
      }))
    })),
    
    // Convertir almacenes
    almacenes: estado.almacenes.map(almacen => ({
      coordenadaX: almacen.coordenada?.x || 0,
      coordenadaY: almacen.coordenada?.y || 0,
      nombre: almacen.nombre,
      capacidadActualGLP: almacen.capacidadActualGLP,
      capacidadMaximaGLP: almacen.capacidadMaximaGLP,
      capacidadCombustible: almacen.capacidadCombustible || 0,
      capacidadActualCombustible: almacen.capacidadCombustible || 0,
      capacidadMaximaCombustible: almacen.capacidadMaximaCombustible || 0,
      esCentral: almacen.tipo === 'CENTRAL',
      permiteCamionesEstacionados: true, // Valor por defecto
      tipo: almacen.tipo,
      activo: almacen.activo
    })),
    
    // Convertir bloqueos
    bloqueos: estado.bloqueos.map(bloqueo => ({
      coordenadas: bloqueo.coordenadas.map(coord => ({
        x: coord.x,
        y: coord.y
      })),
      fechaInicio: bloqueo.fechaInicio,
      fechaFin: bloqueo.fechaFin
    }))
  };
}; 