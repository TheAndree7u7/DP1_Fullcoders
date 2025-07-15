/**
 * @file pedidos.ts
 * @description Utilidades para el manejo de pedidos en el componente Mapa
 */

import type { Pedido } from "../../../types";
import type { CamionEstado, RutaCamion } from "../../../context/SimulacionContext";
import { parseCoord } from "./coordenadas";

/**
 * Función para obtener los pedidos pendientes (no entregados) de todas las rutas
 * @param {RutaCamion[]} rutasCamiones - Array de rutas de camiones
 * @param {CamionEstado[]} camiones - Array de estados de camiones
 * @returns {Pedido[]} Array de pedidos pendientes
 */
export const getPedidosPendientes = (
  rutasCamiones: RutaCamion[],
  camiones: CamionEstado[]
): Pedido[] => {
  const pedidosPendientes: Pedido[] = [];
  const pedidosVistos = new Set<string>();
  
  rutasCamiones.forEach(ruta => {
    const camionActual = camiones.find(c => c.id === ruta.id);
    if (!camionActual) {
      // Si no hay estado del camión, mostrar todos los pedidos (sin duplicados)
      ruta.pedidos.forEach(pedido => {
        const identificador = `${pedido.codigo}-${pedido.coordenada.x}-${pedido.coordenada.y}`;
        if (!pedidosVistos.has(identificador)) {
          pedidosVistos.add(identificador);
          pedidosPendientes.push(pedido);
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
    ruta.pedidos.forEach(pedido => {
      // Crear identificador único para evitar duplicados
      const identificador = `${pedido.codigo}-${pedido.coordenada.x}-${pedido.coordenada.y}`;
      
      // Si ya vimos este pedido, no agregarlo de nuevo
      if (pedidosVistos.has(identificador)) {
        return;
      }
      
      // Buscar el índice del nodo que corresponde a este pedido
      const indicePedidoEnRuta = ruta.ruta.findIndex(nodo => {
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

      // Si el pedido está en un nodo que aún no ha sido visitado, mostrarlo
      if (indicePedidoEnRuta === -1 || indicePedidoEnRuta > posicionActual) {
        pedidosVistos.add(identificador);
        pedidosPendientes.push(pedido);
      }
    });
  });

  return pedidosPendientes;
}; 