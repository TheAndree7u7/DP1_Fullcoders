import type { Camion, Almacen, Coordenada, RutaCamion } from '../../types';
import type { CamionMapa, PedidoMapa, AlmacenMapa, BloqueoMapa } from './types';

const COLORES_CAMIONES = ['#3b82f6', '#ef4444', '#10b981', '#f59e0b', '#8b5cf6'];

const parsearCoordenada = (coordenadaStr: string): Coordenada => {
  const match = coordenadaStr.match(/\((-?\d+),(-?\d+)\)/);
  if (match) {
    return { x: parseInt(match[1]), y: parseInt(match[2]) };
  }
  return { x: 0, y: 0 };
};

interface DatosSimulacion {
  camiones?: Camion[];
  rutasCamiones?: RutaCamion[];
  pedidos?: Array<{
    codigo: string;
    coordenada: Coordenada;
    estado?: string;
    volumenGLPAsignado?: number;
    horasLimite?: number;
    camionAsignado?: string;
  }>;
  almacenes?: Almacen[];
  bloqueos?: Array<{
    coordenadas: Coordenada[];
    activo?: boolean;
    fechaInicio?: string;
    fechaFin?: string;
  }>;
}

export const transformarDatos = (datos: DatosSimulacion) => {
  console.log("🔧 transformarDatos recibió:", {
    camiones: datos.camiones?.length || 0,
    rutasCamiones: datos.rutasCamiones?.length || 0,
    almacenes: datos.almacenes?.length || 0,
    bloqueos: datos.bloqueos?.length || 0
  });

  const camiones: CamionMapa[] = datos.camiones?.map((camion: Camion, index: number) => {
    // Buscar la ruta correspondiente a este camión
    const rutaCamion = datos.rutasCamiones?.find(r => r.id === camion.codigo);
    const rutaParsed = rutaCamion?.ruta.map(parsearCoordenada) || [camion.coordenada];
    const tieneRuta = rutaParsed.length > 1;

    console.log(`🚛 Procesando camión ${camion.codigo}:`, {
      rutaEncontrada: !!rutaCamion,
      rutaLength: rutaParsed.length,
      tieneRuta,
      coordenada: camion.coordenada
    });

    return {
      id: camion.id || camion.codigo,
      codigo: camion.codigo,
      color: COLORES_CAMIONES[index % COLORES_CAMIONES.length],
      ruta: rutaParsed,
      posicion: camion.coordenada,
      rotacion: 0,
      destino: rutaCamion ? parsearCoordenada(rutaCamion.puntoDestino) : null,
      pedidosAsignados: rutaCamion?.pedidos.map(p => p.codigo) || [],
      estado: typeof camion.estado === 'string' ? camion.estado : 'disponible',
      progreso: camion.porcentaje || 0,
      nodoActual: 0,
      posicionInterpolada: camion.coordenada,
      enMovimiento: tieneRuta,
      tiempoInicioMovimiento: tieneRuta ? Date.now() : 0,
    };
  }) || [];

  console.log("🚛 Camiones transformados:", camiones.length);

  // Extraer pedidos de las rutas de camiones
  const pedidosExtraidos = datos.rutasCamiones?.flatMap(ruta => 
    ruta.pedidos.map(pedido => ({
      codigo: pedido.codigo,
      coordenada: pedido.coordenada,
      estado: 'asignado',
      volumenGLP: pedido.volumenGLPAsignado || 0,
      horasLimite: pedido.horasLimite || 24,
      camionAsignado: ruta.id,
    }))
  ) || [];

  const pedidos: PedidoMapa[] = datos.pedidos?.map((pedido) => ({
    codigo: pedido.codigo,
    coordenada: pedido.coordenada,
    estado: pedido.estado || 'pendiente',
    volumenGLP: pedido.volumenGLPAsignado || 0,
    horasLimite: pedido.horasLimite || 24,
    camionAsignado: pedido.camionAsignado || null,
  })) || pedidosExtraidos;

  const almacenes: AlmacenMapa[] = datos.almacenes?.map((almacen: Almacen) => ({
    id: almacen.id || almacen.nombre,
    nombre: almacen.nombre,
    tipo: almacen.tipo,
    coordenada: almacen.coordenada,
    capacidadActualGLP: almacen.capacidadActualGLP,
    capacidadMaximaGLP: almacen.capacidadMaximaGLP,
    capacidadCombustible: almacen.capacidadCombustible || 0,
    capacidadMaximaCombustible: almacen.capacidadMaximaCombustible || 0,
    esCentral: almacen.tipo === 'CENTRAL',
  })) || [];

  const bloqueos: BloqueoMapa[] = datos.bloqueos?.map((bloqueo) => ({
    coordenadas: bloqueo.coordenadas || [],
    activo: bloqueo.activo !== false,
    fechaInicio: bloqueo.fechaInicio || '',
    fechaFin: bloqueo.fechaFin || '',
  })) || [];

  console.log("📊 Resultado final transformarDatos:", {
    camiones: camiones.length,
    pedidos: pedidos.length,
    almacenes: almacenes.length,
    bloqueos: bloqueos.length,
    sample: {
      camion: camiones[0],
      pedido: pedidos[0],
      almacen: almacenes[0]
    }
  });

  return { camiones, pedidos, almacenes, bloqueos };
};
