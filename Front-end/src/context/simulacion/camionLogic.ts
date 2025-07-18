/**
 * @file camionLogic.ts
 * @description Lógica para el avance y actualización de camiones en la simulación
 */

import type { CamionEstado, RutaCamion } from "./types";
import type { Pedido } from "../../types";
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
 * @function avanzarCamion
 * @description Avanza un camión en su ruta y actualiza su estado
 */
export const avanzarCamion = (
  camion: CamionEstado,
  ruta: RutaCamion
): CamionEstado => {
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

  // Mover el camión a la nueva posición
  const nuevaUbicacion = ruta.ruta[siguientePaso];
  
  // Verificar si el camión realmente se movió a una nueva ubicación
  const ubicacionActual = camion.ubicacion;
  const seMovio = ubicacionActual !== nuevaUbicacion;
  
  // Solo consumir combustible si el camión se movió
  let nuevoCombustible = camion.combustibleActual;
  if (seMovio) {
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
  }

  // SIEMPRE actualizar la distancia máxima cuando cambie el combustible
  const nuevoCamionAdaptado = adaptarCamionParaCalculos(nuevoCamion);
  nuevoCamion.distanciaMaxima = calcularDistanciaMaxima(nuevoCamionAdaptado);

  // Si el camión se quedó sin combustible, cambiar su estado
  if (nuevoCombustible <= 0) {
    nuevoCamion.estado = "Averiado";
  }

  return nuevoCamion;
};

/**
 * @function avanzarTodosLosCamiones
 * @description Avanza todos los camiones en sus rutas
 */
export const avanzarTodosLosCamiones = (
  camiones: CamionEstado[],
  rutasCamiones: RutaCamion[]
): CamionEstado[] => {
  return camiones.map((camion) => {
    const ruta = rutasCamiones.find((r) => r.id === camion.id);
    if (!ruta) return camion;

    return avanzarCamion(camion, ruta);
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