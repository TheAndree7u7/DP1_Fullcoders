import axios from 'axios';
import { API_URLS } from '../config/api';
import type { AlmacenBackend, Almacen } from '../types';

export const getAlmacenes = async (): Promise<Almacen[]> => {
  //console.log(`🌐 API: Haciendo petición a ${API_URLS.ALMACENES}`);
  const response = await axios.get<AlmacenBackend[]>(API_URLS.ALMACENES);
  //console.log('📥 API: Respuesta recibida:', response.data);
  const almacenesTransformados = response.data.map(a => ({
    id: a.nombre.replace(/\s+/g, '-').toLowerCase(),
    nombre: a.nombre,
    tipo: (a.tipo === 'CENTRAL' ? 'CENTRAL' : 'INTERMEDIO') as 'CENTRAL' | 'INTERMEDIO',
    coordenada: { x: a.coordenada.columna, y: a.coordenada.fila },
    activo: a.activo,
    // Agregar información de capacidades
    capacidadActualGLP: a.capacidadActualGLP || 0,
    capacidadMaximaGLP: a.capacidadMaximaGLP || 0,
    capacidadCombustible: a.capacidadCombustible || 0,
    capacidadMaximaCombustible: a.capacidadMaximaCombustible || 0,
    esCentral: a.esCentral || false,
  }));
  //console.log('🔄 API: Almacenes transformados:', almacenesTransformados);
  return almacenesTransformados;
};

// Función centralizada para obtener todos los datos iniciales necesarios
type DatosIniciales = {
  almacenes: Almacen[];
  // Agrega aquí más propiedades si necesitas más datos de otros endpoints
};

export const getDatosIniciales = async (): Promise<DatosIniciales> => {
  const almacenes = await getAlmacenes();
  // Si necesitas más datos, agrégalos aquí usando Promise.all
  // const [almacenes, pedidos, camiones] = await Promise.all([
  //   getAlmacenes(),
  //   getPedidos(),
  //   getCamiones()
  // ]);
  return { almacenes };
};