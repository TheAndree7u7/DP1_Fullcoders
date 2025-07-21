/**
 * @file camionLogic.ts
 * @description Lógica para el avance y actualización de camiones en la simulación
 */

import type { CamionEstado, RutaCamion } from "./types";
import type { Pedido, Almacen } from "../../types";
import { 
  parseCoord, 
  adaptarCamionParaCalculos 
} from "./utils";
import { 
  calcularPesoCarga,
  calcularPesoCombinado,
  calcularConsumoGalones,
  calcularDistanciaMaxima,
} from "../../types";
import { INCREMENTO_PORCENTAJE } from "./types";

/**
 * @function obtenerCoordenadaAlmacenCentral
 * @description Obtiene la coordenada del almacén central desde la lista de almacenes
 */
const obtenerCoordenadaAlmacenCentral = (almacenes: Almacen[]): string => {
  const almacenCentral = almacenes.find(almacen => almacen.esCentral || almacen.tipo === 'CENTRAL');
  if (almacenCentral) {
    return `(${almacenCentral.coordenada.x},${almacenCentral.coordenada.y})`;
  }
  // Fallback a la coordenada hardcodeada si no se encuentra el almacén central
  console.warn('⚠️ No se encontró almacén central en los datos, usando coordenada por defecto (8,12)');
  return '(8,12)';
};

/**
 * @function verificarCambioEstadoEnAlmacenCentral
 * @description Verifica si debe cambiar el estado del camión cuando pasa por el almacén central
 */
const verificarCambioEstadoEnAlmacenCentral = (
  camion: CamionEstado,
  ruta: RutaCamion,
  siguientePaso: number,
  almacenes: Almacen[]
): "Disponible" | "Averiado" | "En Mantenimiento" | "En Mantenimiento Preventivo" | "En Mantenimiento por Avería" | "En Ruta" => {
  const almacenCentralCoord = obtenerCoordenadaAlmacenCentral(almacenes);
  
  // Obtener nodo actual y anterior
  const nodoActual = ruta.ruta[siguientePaso];
  const nodoAnterior = siguientePaso > 0 ? ruta.ruta[siguientePaso - 1] : ruta.ruta[0];
  
  // Verificar si ambos nodos (actual y anterior) son el almacén central
  const ambosEnAlmacenCentral = nodoActual === almacenCentralCoord && nodoAnterior === almacenCentralCoord;
  
  // Verificar si ambos nodos son diferentes al almacén central
  const ambosFueraAlmacenCentral = nodoActual !== almacenCentralCoord && nodoAnterior !== almacenCentralCoord;
  
  let nuevoEstado = camion.estado;
  
  // Si está "En Ruta" y ambos nodos son el almacén central, cambiar a "Disponible"
  if (camion.estado === "En Ruta" && ambosEnAlmacenCentral) {
    nuevoEstado = "Disponible";
    console.log(`🔄 ESTADO: Camión ${camion.id} cambió de "En Ruta" a "Disponible" en almacén central (${almacenCentralCoord})`);
  }
  // Si está "Disponible" y ambos nodos son diferentes al almacén central, cambiar a "En Ruta"
  else if (camion.estado === "Disponible" && ambosFueraAlmacenCentral) {
    nuevoEstado = "En Ruta";
    console.log(`🔄 ESTADO: Camión ${camion.id} cambió de "Disponible" a "En Ruta" fuera del almacén central (${almacenCentralCoord})`);
  }
  
  return nuevoEstado;
};

/**
 * @function recargarCamionEnAlmacenCentral
 * @description Recarga el GLP y combustible del camión al máximo cuando está en el almacén central
 */
const recargarCamionEnAlmacenCentral = (
  camion: CamionEstado,
  ruta: RutaCamion,
  siguientePaso: number,
  almacenes: Almacen[]
): { nuevoGLP: number; nuevoCombustible: number; seRecargo: boolean } => {
  const almacenCentralCoord = obtenerCoordenadaAlmacenCentral(almacenes);
  const nodoActual = ruta.ruta[siguientePaso];
  
  // Verificar si el camión está en el almacén central
  if (nodoActual === almacenCentralCoord) {
    // Recargar GLP y combustible al máximo
    const nuevoGLP = camion.capacidadMaximaGLP;
    const nuevoCombustible = camion.combustibleMaximo;
    
    // Solo mostrar log si realmente se recargó algo
    const glpRecargado = nuevoGLP > camion.capacidadActualGLP;
    const combustibleRecargado = nuevoCombustible > camion.combustibleActual;
    
    if (glpRecargado || combustibleRecargado) {
      console.log(`⛽ RECARGA: Camión ${camion.id} recargado en almacén central (${almacenCentralCoord}):`, {
        glp: `${camion.capacidadActualGLP.toFixed(2)} → ${nuevoGLP.toFixed(2)}`,
        combustible: `${camion.combustibleActual.toFixed(2)} → ${nuevoCombustible.toFixed(2)}`
      });
    }
    
    return {
      nuevoGLP,
      nuevoCombustible,
      seRecargo: glpRecargado || combustibleRecargado
    };
  }
  
  // Si no está en el almacén central, mantener valores actuales
  return {
    nuevoGLP: camion.capacidadActualGLP,
    nuevoCombustible: camion.combustibleActual,
    seRecargo: false
  };
};

/**
 * @function avanzarCamion
 * @description Avanza un camión en su ruta y actualiza su estado
 */
export const avanzarCamion = (
  camion: CamionEstado,
  ruta: RutaCamion,
  almacenes: Almacen[]
): CamionEstado => {
  // Si el camión está averiado, no avanza
  if (camion.estado === "Averiado") {
    return camion;
  }

  const rutaLength = ruta.ruta.length;
  
  // Si el camión tiene solo un nodo, debe consumir ese nodo hasta el final
  if (rutaLength === 1) {
    return {
      ...camion,
      porcentaje: 1, // Consumir completamente el único nodo
      ubicacion: ruta.ruta[0], // Mantener en la única posición
    };
  }

  const siguientePaso = camion.porcentaje + INCREMENTO_PORCENTAJE;

  // Mover el camión a la nueva posición
  const nuevaUbicacion = ruta.ruta[siguientePaso];
  
  // Si llegó al final de la ruta, mantener en la posición final
  if (siguientePaso >= rutaLength) {
    return {
      ...camion,
      porcentaje: rutaLength - 1,
      ubicacion: nuevaUbicacion,
    };
  }
  
  // Verificar si el camión realmente se movió a una nueva ubicación
  const ubicacionActual = camion.ubicacion;
  const seMovio = ubicacionActual !== nuevaUbicacion;
  
  // Verificar si el camión debe recargar en el almacén central
  const recarga = recargarCamionEnAlmacenCentral(camion, ruta, siguientePaso, almacenes);
  
  // Solo consumir combustible si el camión se movió y no se recargó
  let nuevoCombustible = recarga.nuevoCombustible;
  if (seMovio && !recarga.seRecargo) {
    // En un mapa reticular, cada paso entre nodos adyacentes es exactamente 1km
    const distanciaRecorrida = 1; // 1km por paso/nodo en mapa reticular

    // Adaptar el camión para usar las funciones de cálculo
    const camionAdaptado = adaptarCamionParaCalculos(camion);

    // Calcular consumo de combustible usando la función de utilidad
    const consumoCombustible = calcularConsumoGalones(
      camionAdaptado,
      distanciaRecorrida,
    );

    // Actualizar combustible actual (no puede ser menor que 0)
    nuevoCombustible = Math.max(
      0,
      camion.combustibleActual - consumoCombustible,
    );
  }

  // Verificar si hay pedidos para entregar en la NUEVA ubicación
  let nuevoGLP = recarga.nuevoGLP;
  const pedidosEntregadosAhora: Pedido[] = [];

  // Solo procesar entrega de pedidos si no se recargó (para evitar entregar GLP recién recargado)
  if (!recarga.seRecargo) {
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
      for (const pedido of pedidosEntregadosAhora) {
        if (pedido.volumenGLPAsignado) {
          nuevoGLP -= pedido.volumenGLPAsignado;
        } else {
          console.log(`⚠️ Pedido sin volumenGLPAsignado:`, pedido);
        }
      }
      // Asegurar que no sea negativo
      nuevoGLP = Math.max(0, nuevoGLP);
    }
  }

  // Crear nuevo estado del camión con valores actualizados
  const nuevoCamion = {
    ...camion,
    porcentaje: siguientePaso,
    ubicacion: nuevaUbicacion,
    combustibleActual: nuevoCombustible,
    capacidadActualGLP: nuevoGLP,
  };

  // Actualizar peso de carga y peso combinado cuando se entregan pedidos O se recarga
  if (pedidosEntregadosAhora.length > 0 || recarga.seRecargo) {
    // Adaptar el nuevo estado del camión para los cálculos
    const nuevoCamionAdaptado = adaptarCamionParaCalculos(nuevoCamion);

    // Actualizar el peso de carga basado en la nueva cantidad de GLP
    nuevoCamion.pesoCarga = calcularPesoCarga(nuevoCamionAdaptado);

    // Actualizar el peso combinado basado en el nuevo peso de carga
    nuevoCamion.pesoCombinado = calcularPesoCombinado(nuevoCamionAdaptado);
  }

  // SIEMPRE actualizar la distancia máxima cuando cambie el combustible
  const nuevoCamionAdaptado = adaptarCamionParaCalculos(nuevoCamion);
  nuevoCamion.distanciaMaxima = calcularDistanciaMaxima(nuevoCamionAdaptado);

  // Si el camión se quedó sin combustible, cambiar su estado
  if (nuevoCombustible <= 0) {
    nuevoCamion.estado = "Averiado";
  } else {
    // Verificar cambio de estado en almacén central
    nuevoCamion.estado = verificarCambioEstadoEnAlmacenCentral(nuevoCamion, ruta, siguientePaso, almacenes);
  }

  return nuevoCamion;
};

/**
 * @function avanzarTodosLosCamiones
 * @description Avanza todos los camiones en sus rutas
 */
export const avanzarTodosLosCamiones = (
  camiones: CamionEstado[],
  rutasCamiones: RutaCamion[],
  almacenes: Almacen[]
): CamionEstado[] => {
  return camiones.map((camion) => {
    const ruta = rutasCamiones.find((r) => r.id === camion.id);
    if (!ruta) return camion;

    return avanzarCamion(camion, ruta, almacenes);
  });
};

/**
 * @function marcarCamionAveriado
 * @description Marca un camión como averiado
 */
export const marcarCamionAveriado = (
  camiones: CamionEstado[],
  camionId: string
): CamionEstado[] => {
  return camiones.map((camion) =>
    camion.id === camionId
      ? { ...camion, estado: "Averiado" as const }
      : camion,
  );
}; 