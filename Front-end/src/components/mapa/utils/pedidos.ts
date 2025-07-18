/**
 * @file pedidos.ts
 * @description Utilidades para el manejo de pedidos en el componente Mapa
 */

import type { CamionEstado, RutaCamion } from "../../../context/SimulacionContext";
import type { Pedido } from "../../../types";
import { parseCoord } from "./coordenadas";

/**
 * Función para obtener los pedidos pendientes (no entregados) de todas las rutas
 * @param {RutaCamion[]} rutasCamiones - Array de rutas de camiones
 * @param {CamionEstado[]} camiones - Array de estados de camiones
 * @returns {Pedido[]} Array de pedidos pendientes con cantidad pendiente calculada
 */
export const getPedidosPendientes = (
  rutasCamiones: RutaCamion[],
  camiones: CamionEstado[]
): Pedido[] => {
  const pedidosMap = new Map<string, Pedido>();
  
  rutasCamiones.forEach(ruta => {
    const camionActual = camiones.find(c => c.id === ruta.id);
    if (!camionActual) {
      // Si no hay estado del camión, mostrar todos los pedidos
      ruta.pedidos.forEach((pedido: Pedido) => {
        if (!pedidosMap.has(pedido.codigo)) {
          pedidosMap.set(pedido.codigo, { ...pedido });
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

      // Si el pedido está en un nodo que aún no ha sido visitado, procesarlo
      if (indicePedidoEnRuta === -1 || indicePedidoEnRuta > posicionActual) {
        if (!pedidosMap.has(pedido.codigo)) {
          // Crear nuevo pedido con la cantidad pendiente inicializada
          pedidosMap.set(pedido.codigo, { 
            ...pedido,
            volumenGLPAsignado: pedido.volumenGLPAsignado // Cantidad total pendiente
          });
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

  return Array.from(pedidosMap.values());
}; 