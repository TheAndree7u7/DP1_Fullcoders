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
  
  // PASO 1: Crear un set de códigos de pedidos que están en las rutas de los camiones
  const pedidosEnRutasSet = new Set<string>();
  rutasCamiones.forEach(ruta => {
    ruta.pedidos.forEach(pedido => {
      pedidosEnRutasSet.add(pedido.codigo);
    });
  });
  
  console.log('🔍 DEBUG: Pedidos en rutas de camiones:', Array.from(pedidosEnRutasSet));
  console.log('🔍 DEBUG: Pedidos no asignados del backend:', pedidosNoAsignados.map(p => p.codigo));
  
  // PASO 2: Procesar TODOS los pedidos que están en las rutas de los camiones
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
          console.log('✅ DEBUG: Agregando pedido en ruta:', pedido.codigo, 'estado:', estadoPedido, 'camión:', ruta.id);
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
            console.log('🔄 DEBUG: Actualizando estado a EN_TRANSITO:', pedido.codigo);
          } else if (estadoPedido === 'RETRASO') {
            pedidoExistente.estadoPedido = 'RETRASO';
            console.log('🔄 DEBUG: Actualizando estado a RETRASO:', pedido.codigo);
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

  // PASO 3: Agregar SOLO los pedidos que NO están en las rutas de los camiones
  pedidosNoAsignados.forEach(pedido => {
    // Verificar que el pedido NO esté en las rutas de los camiones
    if (!pedidosEnRutasSet.has(pedido.codigo)) {
      console.log('✅ DEBUG: Agregando pedido NO en rutas (realmente no asignado):', pedido.codigo);
      pedidosMap.set(pedido.codigo, { 
        ...pedido,
        esNoAsignado: true,
        estadoPedido: 'NO_ASIGNADO'
      });
    } else {
      console.log('⚠️ DEBUG: Pedido del array no asignados SÍ está en rutas de camiones:', pedido.codigo);
    }
  });

  const resultado = Array.from(pedidosMap.values());
  console.log('🔍 DEBUG: Pedidos procesados:', resultado.map(p => ({
    codigo: p.codigo,
    estado: p.estadoPedido,
    esNoAsignado: p.esNoAsignado
  })));
  
  return resultado;
}; 