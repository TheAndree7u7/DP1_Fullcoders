/**
 * @file pedidos.ts
 * @description Utilidades para el manejo de pedidos en el componente Mapa
 */

import type { CamionEstado, RutaCamion } from "../../../context/SimulacionContext";
import type { Pedido } from "../../../types";
import { parseCoord } from "./coordenadas";

/**
 * Interfaz extendida para pedidos con información de asignación y estado
 */
export interface PedidoConAsignacion extends Pedido {
  esNoAsignado: boolean;
  estadoPedido: string; // 'NO_ASIGNADO', 'PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'RETRASO'
}

/**
 * Función para obtener los pedidos pendientes (no entregados) de todas las rutas
 * @param {RutaCamion[]} rutasCamiones - Array de rutas de camiones
 * @param {CamionEstado[]} camiones - Array de estados de camiones
 * @param {Pedido[]} pedidosNoAsignados - Array de pedidos no asignados a ningún camión
 * @returns {PedidoConAsignacion[]} Array de pedidos pendientes con información de asignación y estado
 */
export const getPedidosPendientes = (
  rutasCamiones: RutaCamion[],
  camiones: CamionEstado[],
  pedidosNoAsignados: Pedido[] = []
): PedidoConAsignacion[] => {
  const pedidosMap = new Map<string, PedidoConAsignacion>();
  
  // Procesar pedidos asignados a camiones
  rutasCamiones.forEach(ruta => {
    const camionActual = camiones.find(c => c.id === ruta.id);
    if (!camionActual) {
      // Si no hay estado del camión, mostrar todos los pedidos como pendientes
      ruta.pedidos.forEach((pedido: Pedido) => {
        if (!pedidosMap.has(pedido.codigo)) {
          pedidosMap.set(pedido.codigo, { 
            ...pedido,
            esNoAsignado: false,
            estadoPedido: 'PENDIENTE'
          });
        }
      });
      return;
    }

    // Obtener la posición actual del camión en la ruta
    const posicionActual = camionActual.porcentaje;
    
    // Si el camión está entregado, no mostrar ningún pedido de esta ruta
    if (camionActual.estado === 'Entregado') {
      return;
    }

    // Para cada pedido de esta ruta, verificar si ya fue visitado
    ruta.pedidos.forEach((pedido: Pedido) => {
      // Buscar el índice del nodo que corresponde a este pedido
      const indicePedidoEnRuta = ruta.ruta.findIndex((nodo: string) => {
        // Validar que el nodo existe y es un string
        if (!nodo || typeof nodo !== 'string') {
          return false;
        }
        
        try {
          const coordNodo = parseCoord(nodo);
          return coordNodo.x === pedido.coordenada.x && coordNodo.y === pedido.coordenada.y;
        } catch {
          console.warn('🚨 Error al parsear coordenada del nodo:', nodo);
          return false;
        }
      });

      // Determinar el estado del pedido basado en la posición del camión
      let estadoPedido = 'PENDIENTE';
      if (camionActual.estado === 'En Camino' || camionActual.estado === 'Disponible') {
        estadoPedido = 'EN_TRANSITO';
      } else if (camionActual.estado === 'Averiado') {
        estadoPedido = 'RETRASO';
      }

      // Si el pedido está en un nodo que aún no ha sido visitado, procesarlo
      if (indicePedidoEnRuta === -1 || indicePedidoEnRuta > posicionActual) {
        if (!pedidosMap.has(pedido.codigo)) {
          // Crear nuevo pedido con la cantidad pendiente inicializada
          pedidosMap.set(pedido.codigo, { 
            ...pedido,
            volumenGLPAsignado: pedido.volumenGLPAsignado, // Cantidad total pendiente
            esNoAsignado: false,
            estadoPedido: estadoPedido
          });
        } else {
          // Actualizar el estado del pedido existente si es más prioritario
          const pedidoExistente = pedidosMap.get(pedido.codigo)!;
          if (estadoPedido === 'EN_TRANSITO' && pedidoExistente.estadoPedido === 'PENDIENTE') {
            pedidoExistente.estadoPedido = 'EN_TRANSITO';
          } else if (estadoPedido === 'RETRASO') {
            pedidoExistente.estadoPedido = 'RETRASO';
          }
        }
      } else {
        // El camión ya visitó este pedido, reducir la cantidad pendiente
        const pedidoExistente = pedidosMap.get(pedido.codigo);
        if (pedidoExistente) {
          // Calcular cuánto entregó este camión (capacidad actual del camión al momento de entrega)
          const cantidadEntregada = Math.min(
            pedido.volumenGLPAsignado, 
            camionActual.capacidadActualGLP || 0
          );
          pedidoExistente.volumenGLPAsignado = Math.max(0, pedidoExistente.volumenGLPAsignado - cantidadEntregada);
          
          // Si la cantidad pendiente es 0, remover el pedido
          if (pedidoExistente.volumenGLPAsignado <= 0) {
            pedidosMap.delete(pedido.codigo);
          }
        }
      }
    });
  });

  // Agregar pedidos no asignados
  pedidosNoAsignados.forEach(pedido => {
    if (!pedidosMap.has(pedido.codigo)) {
      pedidosMap.set(pedido.codigo, { 
        ...pedido,
        esNoAsignado: true,
        estadoPedido: 'NO_ASIGNADO'
      });
    }
  });

  return Array.from(pedidosMap.values());
}; 