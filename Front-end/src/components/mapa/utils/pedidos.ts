/**
 * @file pedidos.ts
 * @description Utilidades para el manejo de pedidos en el componente Mapa
 */

import type { Pedido } from "../../../types";
import type { CamionEstado, RutaCamion } from "../../../context/simulacion/types";
import { parseCoord } from "./coordenadas";

// Constante para comparación de números decimales
const NUM_PEQUEÑO = 0.001;

/**
 * Función para obtener los pedidos pendientes considerando tanto las rutas como el array de pedidos del individuo
 * @param {RutaCamion[]} rutasCamiones - Array de rutas de camiones
 * @param {CamionEstado[]} camiones - Array de estados de camiones
 * @param {Pedido[]} pedidosIndividuo - Array de pedidos del individuo (opcional)
 * @returns {Pedido[]} Array de pedidos pendientes con cantidad pendiente calculada
 */
export const getPedidosPendientes = (
  rutasCamiones: RutaCamion[],
  camiones: CamionEstado[],
  pedidosIndividuo?: Pedido[]
): Pedido[] => {
  const pedidosMap = new Map<string, Pedido>();
  
  // 1. Primero, agregar todos los pedidos del individuo (si existen)
  if (pedidosIndividuo && Array.isArray(pedidosIndividuo)) {
    pedidosIndividuo.forEach((pedido: Pedido) => {
      if (!pedidosMap.has(pedido.codigo)) {
        // Inicializar el pedido con volumenGLPEntregado si no está definido
        const pedidoConEntregado = {
          ...pedido,
          volumenGLPEntregado: pedido.volumenGLPEntregado || 0
        };
        pedidosMap.set(pedido.codigo, pedidoConEntregado);
      }
    });
  }
  
  // 2. Procesar los pedidos de las rutas para actualizar el estado de entrega
  rutasCamiones.forEach((ruta: RutaCamion) => {
    const camionActual = camiones.find((c: CamionEstado) => c.id === ruta.id);
    if (!camionActual) {
      // Si no hay estado del camión, agregar todos los pedidos de la ruta si no existen ya
      ruta.pedidos.forEach((pedido: Pedido) => {
        if (!pedidosMap.has(pedido.codigo)) {
          const pedidoConEntregado = {
            ...pedido,
            volumenGLPEntregado: pedido.volumenGLPEntregado || 0
          };
          pedidosMap.set(pedido.codigo, pedidoConEntregado);
        }
      });
      return;
    }

    // Obtener la posición actual del camión en la ruta
    const posicionActual = camionActual.porcentaje;

    // Para cada pedido de esta ruta, verificar si ya fue visitado y actualizar entrega
    ruta.pedidos.forEach((pedido: Pedido) => {
      // Asegurar que el pedido existe en el mapa
      if (!pedidosMap.has(pedido.codigo)) {
        const pedidoConEntregado = {
          ...pedido,
          volumenGLPEntregado: pedido.volumenGLPEntregado || 0
        };
        pedidosMap.set(pedido.codigo, pedidoConEntregado);
      }

      const pedidoExistente = pedidosMap.get(pedido.codigo)!;

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

      // Si el camión ya visitó este pedido, calcular la entrega
      if (indicePedidoEnRuta !== -1 && indicePedidoEnRuta <= posicionActual) {
        // Calcular cuánto puede entregar este camión
        const pedidosAsignadosAlCamion = ruta.pedidos.length;
        const capacidadDisponiblePorPedido = pedidosAsignadosAlCamion > 0 
          ? (camionActual.capacidadActualGLP || 0) / pedidosAsignadosAlCamion 
          : 0;
        
        const volumenPendiente = pedidoExistente.volumenGLPAsignado - pedidoExistente.volumenGLPEntregado;
        const cantidadAEntregar = Math.min(capacidadDisponiblePorPedido, volumenPendiente);
        
        // Actualizar el volumen entregado
        pedidoExistente.volumenGLPEntregado += cantidadAEntregar;
        
        // Asegurar que no se entregue más de lo asignado
        pedidoExistente.volumenGLPEntregado = Math.min(
          pedidoExistente.volumenGLPEntregado, 
          pedidoExistente.volumenGLPAsignado
        );
      }
    });
  });

  // 3. Filtrar pedidos que están completamente entregados
  const pedidosPendientes = Array.from(pedidosMap.values()).filter((pedido: Pedido) => {
    const volumenPendiente = pedido.volumenGLPAsignado - pedido.volumenGLPEntregado;
    return Math.abs(volumenPendiente) > NUM_PEQUEÑO; // Solo mostrar si aún hay volumen pendiente
  });

  return pedidosPendientes;
}; 