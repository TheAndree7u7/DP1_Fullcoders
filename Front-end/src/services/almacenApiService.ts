import axios from 'axios';

export interface AlmacenBackend {
  coordenada: { fila: number; columna: number };
  nombre: string;
  tipo: 'CENTRAL' | 'SECUNDARIO';
  activo: boolean;
}

export interface Almacen {
  id: string;
  nombre: string;
  tipo: 'CENTRAL' | 'INTERMEDIO';
  coordenada: { x: number; y: number };
  activo: boolean;
}

export const getAlmacenes = async (): Promise<Almacen[]> => {
  //log('🌐 API: Haciendo petición a http://localhost:8085/api/almacenes');
  const response = await axios.get<AlmacenBackend[]>('http://localhost:8085/api/almacenes');
  //log('📥 API: Respuesta recibida:', response.data);
  const almacenesTransformados = response.data.map(a => ({
    id: a.nombre.replace(/\s+/g, '-').toLowerCase(),
    nombre: a.nombre,
    tipo: (a.tipo === 'CENTRAL' ? 'CENTRAL' : 'INTERMEDIO') as 'CENTRAL' | 'INTERMEDIO',
    coordenada: { x: a.coordenada.columna, y: a.coordenada.fila },
    activo: a.activo,
  }));
  //log('🔄 API: Almacenes transformados:', almacenesTransformados);
  return almacenesTransformados;
};