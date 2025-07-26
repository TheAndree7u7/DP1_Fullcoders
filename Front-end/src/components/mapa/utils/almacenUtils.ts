/**
 * @file almacenUtils.ts
 * @description Utilidades para obtener información de pedidos y camiones asignados a almacenes
 */

import type { Almacen, Pedido, CamionEstado, RutaCamion } from '../../../types';
import type { PedidoConAsignacion } from '../../../context/simulacion/types';

/**
 * @function obtenerPedidosAsignadosAlAlmacen
 * @description Obtiene los pedidos que están asignados a camiones que pasan por el almacén especificado
 * @param almacen - El almacén del cual obtener los pedidos
 * @param rutasCamiones - Array de rutas de camiones
 * @param camiones - Array de camiones
 * @param pedidosNoAsignados - Array de pedidos no asignados
 * @returns Array de pedidos asignados al almacén
 */
export const obtenerPedidosAsignadosAlAlmacen = (
  almacen: Almacen,
  rutasCamiones: RutaCamion[],
  camiones: CamionEstado[],
  pedidosNoAsignados: Pedido[]
): PedidoConAsignacion[] => {
  const pedidosAsignados: PedidoConAsignacion[] = [];
  const coordenadaAlmacen = `(${almacen.coordenada.x},${almacen.coordenada.y})`;

  // Buscar camiones que pasan por este almacén
  const camionesEnAlmacen = rutasCamiones.filter(ruta => {
    return ruta.ruta.includes(coordenadaAlmacen);
  });

  // Obtener pedidos de los camiones que pasan por el almacén
  camionesEnAlmacen.forEach(ruta => {
    const camion = camiones.find(c => c.id === ruta.id);
    const estadoCamion = camion?.estado || 'Desconocido';

    ruta.pedidos.forEach(pedido => {
      // Verificar si el pedido ya está en la lista
      const pedidoExistente = pedidosAsignados.find(p => p.codigo === pedido.codigo);
      
      if (!pedidoExistente) {
        // Crear nuevo pedido asignado
        pedidosAsignados.push({
          ...pedido,
          esNoAsignado: false,
          estadoPedido: determinarEstadoPedido(estadoCamion)
        });
      }
    });
  });

  return pedidosAsignados;
};

/**
 * @function obtenerCamionesAsignadosAlAlmacen
 * @description Obtiene los camiones que están asignados al almacén especificado (pasan por él)
 * @param almacen - El almacén del cual obtener los camiones
 * @param rutasCamiones - Array de rutas de camiones
 * @param camiones - Array de camiones
 * @returns Array de camiones asignados al almacén
 */
export const obtenerCamionesAsignadosAlAlmacen = (
  almacen: Almacen,
  rutasCamiones: RutaCamion[],
  camiones: CamionEstado[]
): CamionEstado[] => {
  const coordenadaAlmacen = `(${almacen.coordenada.x},${almacen.coordenada.y})`;

  // Buscar camiones que pasan por este almacén
  const camionesEnAlmacen = rutasCamiones
    .filter(ruta => ruta.ruta.includes(coordenadaAlmacen))
    .map(ruta => camiones.find(c => c.id === ruta.id))
    .filter((camion): camion is CamionEstado => camion !== undefined);

  return camionesEnAlmacen;
};

/**
 * @function determinarEstadoPedido
 * @description Determina el estado de un pedido basado en el estado del camión
 * @param estadoCamion - Estado del camión asignado
 * @returns Estado del pedido
 */
const determinarEstadoPedido = (estadoCamion: string): string => {
  switch (estadoCamion) {
    case 'Entregado':
      return 'ENTREGADO';
    case 'En Camino':
    case 'Disponible':
      return 'EN_TRANSITO';
    case 'Averiado':
      return 'RETRASO';
    default:
      return 'PENDIENTE';
  }
};

/**
 * @function formatearFecha
 * @description Formatea una fecha para mostrar en la interfaz
 * @param fecha - Fecha a formatear
 * @returns Fecha formateada
 */
export const formatearFecha = (fecha: string | undefined): string => {
  if (!fecha) return 'N/A';
  
  try {
    const fechaObj = new Date(fecha);
    return fechaObj.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return 'Fecha inválida';
  }
}; 