import { useContext } from 'react';
import { MapaContext } from '../context/MapaContextDefinition';
import type { MapaContextType } from '../context/MapaContextDefinition';

// ============================
// HOOK PRINCIPAL
// ============================

export const useMapa = (): MapaContextType => {
  const context = useContext(MapaContext);
  if (!context) {
    throw new Error("useMapa debe usarse dentro de MapaProvider");
  }
  return context;
};

// ============================
// HOOKS ESPECIALIZADOS
// ============================

export const useMapaState = () => {
  const { 
    camiones, 
    pedidos, 
    almacenes, 
    bloqueos, 
    isLoading, 
    error, 
    ultimaActualizacion 
  } = useMapa();
  
  return {
    camiones,
    pedidos,
    almacenes,
    bloqueos,
    isLoading,
    error,
    ultimaActualizacion
  };
};

export const useMapaActions = () => {
  const { 
    actualizarDatosMapa, 
    limpiarMapa, 
    setError, 
    setLoading 
  } = useMapa();
  
  return {
    actualizarDatosMapa,
    limpiarMapa,
    setError,
    setLoading
  };
};

// ============================
// HOOKS CON LÓGICA DE NEGOCIO
// ============================

export const useMapaStats = () => {
  const { camiones, pedidos } = useMapa();
  
  const totalCamiones = camiones.length;
  const camionesPorEstado = camiones.reduce((acc, camion) => {
    acc[camion.estado] = (acc[camion.estado] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);
  
  const totalPedidos = pedidos.length;
  const pedidosPorEstado = pedidos.reduce((acc, pedido) => {
    acc[pedido.estado] = (acc[pedido.estado] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);
  
  const pedidosAsignados = pedidos.filter(p => p.camionAsignado !== null).length;
  const pedidosSinAsignar = totalPedidos - pedidosAsignados;
  
  return {
    totalCamiones,
    camionesPorEstado,
    totalPedidos,
    pedidosPorEstado,
    pedidosAsignados,
    pedidosSinAsignar
  };
};

export const useMapaElementos = () => {
  const { camiones, pedidos, almacenes, bloqueos } = useMapa();
  
  console.log("🎯 useMapaElementos - Estado del mapa:", {
    camiones: camiones.length,
    pedidos: pedidos.length,
    almacenes: almacenes.length,
    bloqueos: bloqueos.length
  });

  // Elementos para el mapa con formato compatible
  const elementosMapa = {
    camiones: camiones.map(camion => ({
      id: camion.id,
      color: camion.color,
      ruta: camion.ruta,
      posicion: camion.posicionInterpolada || camion.posicion,
      rotacion: camion.rotacion
    })),
    
    pedidos: pedidos.map(pedido => ({
      codigo: pedido.codigo,
      coordenada: pedido.coordenada
    })),
    
    almacenes: almacenes.map(almacen => ({
      id: almacen.id,
      nombre: almacen.nombre,
      tipo: almacen.tipo,
      coordenada: almacen.coordenada,
      capacidadActualGLP: almacen.capacidadActualGLP,
      capacidadMaximaGLP: almacen.capacidadMaximaGLP
    })),
    
    bloqueos: bloqueos.filter(b => b.activo).map(bloqueo => ({
      coordenadas: bloqueo.coordenadas
    }))
  };

  console.log("🎯 useMapaElementos - Elementos finales:", {
    camiones: elementosMapa.camiones.length,
    pedidos: elementosMapa.pedidos.length,
    almacenes: elementosMapa.almacenes.length,
    bloqueos: elementosMapa.bloqueos.length,
    samples: {
      camion: elementosMapa.camiones[0],
      pedido: elementosMapa.pedidos[0],
      almacen: elementosMapa.almacenes[0]
    }
  });
  
  return elementosMapa;
};
