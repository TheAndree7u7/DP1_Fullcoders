/**
 * @file manejadores-estado-camiones.ts
 * @description Funciones para manejar el estado y comportamiento de los camiones en la simulación
 * Incluye funciones para avanzar camiones, manejar averías, entregas y cálculos de recursos
 */

import type { Pedido } from "../../types";
import type { CamionEstado, RutaCamion } from "../SimulacionContext";
import { 
  parseCoordenadasDeCadena, 
  adaptarCamionEstadoParaCalculos 
} from "./utilidades-calculo-simulacion";
import { 
  INCREMENTO_PORCENTAJE_AVANCE_CAMION,
  CONFIGURACION_MOVIMIENTO_CAMIONES
} from "./constantes-configuracion-simulacion";
import {
  calcularPesoCarga,
  calcularPesoCombinado,
  calcularConsumoGalones,
  calcularDistanciaMaxima,
} from "../../types";

/**
 * @interface ResultadoAvanceCamion
 * @description Resultado del avance de un camión en la simulación
 */
export interface ResultadoAvanceCamion {
  camionActualizado: CamionEstado;
  pedidosEntregados: Pedido[];
  llegadaADestino: boolean;
}

/**
 * @function marcarCamionComoAveriado
 * @description Marca un camión específico como averiado en la lista de camiones
 * @param {CamionEstado[]} listaCamiones - Lista actual de camiones
 * @param {string} idCamionAveriado - ID del camión a marcar como averiado
 * @returns {CamionEstado[]} Lista actualizada con el camión marcado como averiado
 */
export const marcarCamionComoAveriado = (
  listaCamiones: CamionEstado[],
  idCamionAveriado: string
): CamionEstado[] => {
  console.log(`🔧 AVERÍA: Marcando camión ${idCamionAveriado} como averiado`);
  
  return listaCamiones.map((camion) =>
    camion.id === idCamionAveriado
      ? { ...camion, estado: "Averiado" as const }
      : camion
  );
};

/**
 * @function determinarPedidosParaEntregaEnPosicion
 * @description Determina qué pedidos deben entregarse en la posición actual del camión
 * @param {RutaCamion} rutaCamion - Ruta del camión
 * @param {number} indicePosicionActual - Índice de la posición actual en la ruta
 * @returns {Pedido[]} Lista de pedidos a entregar
 */
export const determinarPedidosParaEntregaEnPosicion = (
  rutaCamion: RutaCamion,
  indicePosicionActual: number
): Pedido[] => {
  const posicionActual = rutaCamion.ruta[indicePosicionActual];
  const coordenadaPosicionActual = parseCoordenadasDeCadena(posicionActual);
  
  const pedidosParaEntregar = rutaCamion.pedidos.filter((pedido) => {
    const indicePedidoEnRuta = rutaCamion.ruta.findIndex((nodo) => {
      const coordenadaNodo = parseCoordenadasDeCadena(nodo);
      return (
        coordenadaNodo.x === pedido.coordenada.x &&
        coordenadaNodo.y === pedido.coordenada.y
      );
    });
    
    return indicePedidoEnRuta === indicePosicionActual;
  });
  
  return pedidosParaEntregar;
};

/**
 * @function calcularNuevaCapacidadGLPDespuesEntrega
 * @description Calcula la nueva capacidad de GLP después de entregar pedidos
 * @param {number} capacidadActualGLP - Capacidad actual de GLP
 * @param {Pedido[]} pedidosEntregados - Lista de pedidos entregados
 * @returns {number} Nueva capacidad de GLP
 */
export const calcularNuevaCapacidadGLPDespuesEntrega = (
  capacidadActualGLP: number,
  pedidosEntregados: Pedido[]
): number => {
  let nuevaCapacidadGLP = capacidadActualGLP;
  
  for (const pedido of pedidosEntregados) {
    if (pedido.volumenGLPAsignado) {
      console.log(`⬇️ ENTREGA: Reduciendo ${pedido.volumenGLPAsignado} GLP por entrega del pedido ${pedido.codigo}`);
      nuevaCapacidadGLP -= pedido.volumenGLPAsignado;
    } else {
      console.log(`⚠️ ENTREGA: Pedido ${pedido.codigo} sin volumenGLPAsignado`);
    }
  }
  
  // Asegurar que no sea negativo
  nuevaCapacidadGLP = Math.max(0, nuevaCapacidadGLP);
  
  return nuevaCapacidadGLP;
};

/**
 * @function calcularNuevoCombustibleDespuesMovimiento
 * @description Calcula el nuevo nivel de combustible después del movimiento
 * @param {CamionEstado} camion - Estado actual del camión
 * @param {number} distanciaRecorrida - Distancia recorrida en km
 * @returns {number} Nuevo nivel de combustible
 */
export const calcularNuevoCombustibleDespuesMovimiento = (
  camion: CamionEstado,
  distanciaRecorrida: number
): number => {
  const camionAdaptado = adaptarCamionEstadoParaCalculos(camion);
  const consumoCombustible = calcularConsumoGalones(camionAdaptado, distanciaRecorrida);
  const nuevoCombustible = Math.max(0, camion.combustibleActual - consumoCombustible);
  
  return nuevoCombustible;
};

/**
 * @function actualizarPesosCamionDespuesEntrega
 * @description Actualiza los pesos del camión después de realizar entregas
 * @param {CamionEstado} camion - Estado actual del camión
 * @returns {Pick<CamionEstado, 'pesoCarga' | 'pesoCombinado' | 'distanciaMaxima'>} Nuevos pesos y distancia máxima
 */
export const actualizarPesosCamionDespuesEntrega = (
  camion: CamionEstado
): Pick<CamionEstado, 'pesoCarga' | 'pesoCombinado' | 'distanciaMaxima'> => {
  const camionAdaptado = adaptarCamionEstadoParaCalculos(camion);
  
  const nuevoPesoCarga = calcularPesoCarga(camionAdaptado);
  const nuevoPesoCombinado = calcularPesoCombinado(camionAdaptado);
  const nuevaDistanciaMaxima = calcularDistanciaMaxima(camionAdaptado);
  
  return {
    pesoCarga: nuevoPesoCarga,
    pesoCombinado: nuevoPesoCombinado,
    distanciaMaxima: nuevaDistanciaMaxima,
  };
};

/**
 * @function avanzarCamionUnPaso
 * @description Avanza un camión un paso en su ruta y procesa entregas
 * @param {CamionEstado} camion - Estado actual del camión
 * @param {RutaCamion} rutaCamion - Ruta del camión
 * @returns {ResultadoAvanceCamion} Resultado del avance del camión
 */
export const avanzarCamionUnPaso = (
  camion: CamionEstado,
  rutaCamion: RutaCamion
): ResultadoAvanceCamion => {
  // Si el camión está averiado, no avanza
  if (camion.estado === "Averiado") {
    return {
      camionActualizado: camion,
      pedidosEntregados: [],
      llegadaADestino: false,
    };
  }

  const siguienteIndicePosicion = camion.porcentaje + INCREMENTO_PORCENTAJE_AVANCE_CAMION;
  const longitudRuta = rutaCamion.ruta.length;

  // Verificar si llegó al final de la ruta
  if (siguienteIndicePosicion >= longitudRuta) {
    return {
      camionActualizado: {
        ...camion,
        estado: "Entregado" as const,
        porcentaje: longitudRuta - 1,
      },
      pedidosEntregados: [],
      llegadaADestino: true,
    };
  }

  // Calcular nuevo combustible por el movimiento
  const distanciaRecorrida = CONFIGURACION_MOVIMIENTO_CAMIONES.DISTANCIA_POR_NODO_EN_KILOMETROS;
  const nuevoCombustible = calcularNuevoCombustibleDespuesMovimiento(camion, distanciaRecorrida);

  // Actualizar posición del camión
  const nuevaUbicacion = rutaCamion.ruta[siguienteIndicePosicion];

  // Determinar pedidos a entregar en la nueva posición
  const pedidosParaEntregar = determinarPedidosParaEntregaEnPosicion(
    rutaCamion,
    siguienteIndicePosicion
  );

  // Calcular nueva capacidad de GLP después de entregas
  const nuevaCapacidadGLP = calcularNuevaCapacidadGLPDespuesEntrega(
    camion.capacidadActualGLP,
    pedidosParaEntregar
  );

  // Log de entregas para debugging
  if (pedidosParaEntregar.length > 0) {
    const coordenadas = parseCoordenadasDeCadena(nuevaUbicacion);
    console.log(`📦 ENTREGA: Camión ${camion.id} llegó a (${coordenadas.x},${coordenadas.y}) - Entregando ${pedidosParaEntregar.length} pedidos`);
    console.log(`⛽ ENTREGA: GLP antes: ${camion.capacidadActualGLP.toFixed(2)}, después: ${nuevaCapacidadGLP.toFixed(2)}`);
  }

  // Crear estado actualizado del camión
  let camionActualizado: CamionEstado = {
    ...camion,
    porcentaje: siguienteIndicePosicion,
    ubicacion: nuevaUbicacion,
    combustibleActual: nuevoCombustible,
    capacidadActualGLP: nuevaCapacidadGLP,
  };

  // Actualizar pesos solo si hubo entregas
  if (pedidosParaEntregar.length > 0) {
    const pesosActualizados = actualizarPesosCamionDespuesEntrega(camionActualizado);
    camionActualizado = {
      ...camionActualizado,
      ...pesosActualizados,
    };
    
    console.log(`📊 ENTREGA: Camión ${camion.id} pesos actualizados:`, {
      pesoCarga: pesosActualizados.pesoCarga.toFixed(2),
      pesoCombinado: pesosActualizados.pesoCombinado.toFixed(2),
      distanciaMaxima: pesosActualizados.distanciaMaxima.toFixed(2),
    });
  } else {
    // Siempre actualizar la distancia máxima cuando cambie el combustible
    const camionAdaptado = adaptarCamionEstadoParaCalculos(camionActualizado);
    camionActualizado.distanciaMaxima = calcularDistanciaMaxima(camionAdaptado);
  }

  // Verificar si el camión se quedó sin combustible
  if (nuevoCombustible <= 0) {
    console.log(`⛽ AVERÍA: Camión ${camion.id} se quedó sin combustible`);
    camionActualizado.estado = "Averiado";
  }

  return {
    camionActualizado,
    pedidosEntregados: pedidosParaEntregar,
    llegadaADestino: false,
  };
};

/**
 * @function avanzarTodosLosCamiones
 * @description Avanza todos los camiones un paso en sus rutas respectivas
 * @param {CamionEstado[]} listaCamiones - Lista de camiones
 * @param {RutaCamion[]} rutasCamiones - Rutas de los camiones
 * @returns {CamionEstado[]} Lista de camiones actualizada
 */
export const avanzarTodosLosCamiones = (
  listaCamiones: CamionEstado[],
  rutasCamiones: RutaCamion[]
): CamionEstado[] => {
  return listaCamiones.map((camion) => {
    const rutaCorrespondiente = rutasCamiones.find((r) => r.id === camion.id);
    if (!rutaCorrespondiente) {
      console.warn(`⚠️ AVANCE: No se encontró ruta para el camión ${camion.id}`);
      return camion;
    }

    const resultado = avanzarCamionUnPaso(camion, rutaCorrespondiente);
    return resultado.camionActualizado;
  });
};

/**
 * @function reiniciarEstadosCamiones
 * @description Reinicia el estado de todos los camiones a sus posiciones iniciales
 * @param {CamionEstado[]} listaCamiones - Lista actual de camiones
 * @param {RutaCamion[]} rutasCamiones - Rutas de los camiones
 * @returns {CamionEstado[]} Lista de camiones reiniciada
 */
export const reiniciarEstadosCamiones = (
  listaCamiones: CamionEstado[],
  rutasCamiones: RutaCamion[]
): CamionEstado[] => {
  return rutasCamiones.map((ruta) => {
    const camionAnterior = listaCamiones.find((c) => c.id === ruta.id);
    
    return {
      id: ruta.id,
      ubicacion: ruta.ruta[0],
      porcentaje: 0,
      estado: camionAnterior?.estado ?? "En Camino",
      capacidadActualGLP: camionAnterior?.capacidadActualGLP ?? 0,
      capacidadMaximaGLP: camionAnterior?.capacidadMaximaGLP ?? 0,
      combustibleActual: camionAnterior?.combustibleActual ?? 0,
      combustibleMaximo: camionAnterior?.combustibleMaximo ?? 0,
      distanciaMaxima: camionAnterior?.distanciaMaxima ?? 0,
      pesoCarga: camionAnterior?.pesoCarga ?? 0,
      pesoCombinado: camionAnterior?.pesoCombinado ?? 0,
      tara: camionAnterior?.tara ?? 0,
      tipo: camionAnterior?.tipo ?? "",
      velocidadPromedio: camionAnterior?.velocidadPromedio ?? 0,
    };
  });
}; 