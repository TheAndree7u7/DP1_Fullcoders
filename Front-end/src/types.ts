// ============================
// TIPOS BASE Y ENUMS
// ============================

// Enums basados en las entidades del backend
export enum TipoAlmacen {
  CENTRAL = 'CENTRAL',
  SECUNDARIO = 'SECUNDARIO'
}

export enum TipoCamion {
  TA = 'TA',
  TB = 'TB', 
  TC = 'TC',
  TD = 'TD'
}

export enum TipoNodo {
  ALMACEN = 'ALMACEN',
  INTERMEDIO = 'INTERMEDIO',
  NORMAL = 'NORMAL',
  CAMION_AVERIADO = 'CAMION_AVERIADO',
  PEDIDO = 'PEDIDO'
}

export enum EstadoCamion {
  DISPONIBLE = 'DISPONIBLE',
  NO_DISPONIBLE = 'NO_DISPONIBLE',
  EN_RUTA = 'EN_RUTA',
  ENTREGANDO_GLP_A_CLIENTE = 'ENTREGANDO_GLP_A_CLIENTE',
  EN_MANTENIMIENTO = 'EN_MANTENIMIENTO',
  EN_MANTENIMIENTO_PREVENTIVO = 'EN_MANTENIMIENTO_PREVENTIVO',
  EN_MANTENIMIENTO_CORRECTIVO = 'EN_MANTENIMIENTO_CORRECTIVO',
  EN_MANTENIMIENTO_POR_AVERIA = 'EN_MANTENIMIENTO_POR_AVERIA',
  INMOVILIZADO_POR_AVERIA = 'INMOVILIZADO_POR_AVERIA',
  SIN_COMBUSTIBLE = 'SIN_COMBUSTIBLE',
  RECIBIENDO_COMBUSTIBLE = 'RECIBIENDO_COMBUSTIBLE',
  ENTREGANDO_COMBUSTIBLE_A_CAMION = 'ENTREGANDO_COMBUSTIBLE_A_CAMION',
  RECIBIENDO_GLP = 'RECIBIENDO_GLP',
  ENTREGANDO_GLP_A_CAMION = 'ENTREGANDO_GLP_A_CAMION',
  ALMACEN_TEMPORAL = 'ALMACEN_TEMPORAL'
}

export enum EstadoPedido {
  REGISTRADO = 'REGISTRADO',
  ENTREGADO = 'ENTREGADO',
  PLANIFICADO = 'PLANIFICADO'
}

// Estados adicionales para compatibilidad con el frontend
export type EstadoPedidoFrontend = 
  | EstadoPedido 
  | 'PENDIENTE' 
  | 'EN_TRANSITO' 
  | 'CANCELADO' 
  | 'RETRASO';

// ============================
// INTERFACES BASE
// ============================

// Interfaz para coordenadas en el frontend (mantener compatibilidad)
export interface Coordenada {
  x: number;
  y: number;
}

// Interfaz para coordenadas del backend
export interface CoordenadaBackend {
  fila: number;
  columna: number;
}

// Alias para compatibilidad
export interface CoordenadaFrontend {
  x: number;
  y: number;
}

export interface Nodo {
  coordenada: Coordenada;
  bloqueado: boolean;
  gScore: number;
  fScore: number;
  tipoNodo: TipoNodo;
}

// ============================
// ENTIDADES PRINCIPALES
// ============================

export interface Almacen {
  id: string;
  nombre: string;
  coordenada: Coordenada;
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
  capacidadCombustible: number;
  capacidadActualCombustible: number;
  capacidadMaximaCombustible: number;
  esCentral: boolean;
  permiteCamionesEstacionados: boolean;
  tipo: TipoAlmacen;
  activo: boolean;
  // Propiedades opcionales del Nodo para compatibilidad con backend
  bloqueado?: boolean;
  gScore?: number;
  fScore?: number;
  tipoNodo?: TipoNodo;
}

export interface Camion {
  codigo: string;
  coordenada: Coordenada;
  tipo: TipoCamion | string; // Permitir strings para compatibilidad
  capacidadMaximaGLP: number;
  capacidadActualGLP: number;
  tara: number;
  pesoCarga: number;
  pesoCombinado: number;
  estado: EstadoCamion | string; // Permitir strings para compatibilidad
  combustibleMaximo: number;
  combustibleActual: number;
  velocidadPromedio: number;
  distanciaMaxima: number;
  tiempoParadaRestante: number;
  // Propiedades opcionales del Nodo para compatibilidad con backend
  bloqueado?: boolean;
  gScore?: number;
  fScore?: number;
  tipoNodo?: TipoNodo;
}

export interface Pedido {
  codigo: string;
  coordenada: Coordenada;
  horasLimite: number;
  fechaRegistro: string; // LocalDateTime como string ISO
  volumenGLPAsignado: number;
  estado: EstadoPedidoFrontend | string; // Permitir strings para compatibilidad
  fechaLimite: string; // LocalDateTime como string ISO
  // Propiedades opcionales del Nodo para compatibilidad con backend
  bloqueado?: boolean;
  gScore?: number;
  fScore?: number;
  tipoNodo?: TipoNodo;
}

export interface TipoIncidente {
  codigo: string;
  descripcion: string;
  horasReparacionRuta: number;
  horasEsperaEnRuta: number;
  horasReparacionTaller: number;
  nTurnosEnReparacion: number;
  importaTurnoDiaAveria: boolean;
  requiereTraslado: boolean;
  colorHex: string;
}

export interface Averia {
  camion: Camion;
  tipoIncidente: TipoIncidente;
  fechaHoraReporte: string; // LocalDateTime como string ISO
  fechaHoraReparacion: string; // LocalDateTime como string ISO
  fechaHoraDisponible: string; // LocalDateTime como string ISO
  fechaHoraFinEsperaEnRuta: string; // LocalDateTime como string ISO
  turnoOcurrencia: number;
  tiempoReparacionEstimado: number;
  estado: boolean;
  coordenada: Coordenada;
}

export interface Bloqueo {
  fechaInicio: string; // LocalDateTime como string ISO
  fechaFin: string; // LocalDateTime como string ISO
  nodosBloqueados: Nodo[];
  activo: boolean;
}

export interface Mantenimiento {
  dia: number;
  mes: number;
  camion: Camion;
}

// ============================
// TIPOS DE ALGORITMOS Y SIMULACIÓN
// ============================

export interface Gen {
  camion: Camion;
  nodos: Nodo[];
  destino: Coordenada;
  pedidos: Pedido[];
  rutaFinal: Nodo[];
  posNodo: number;
}

export interface Individuo {
  cromosoma: Gen[]; //!LIsta de genes
  fechaHoraSimulacion: string; // LocalDateTime como string ISO
  fechaHoraInicioIntervalo?: string;
  fechaHoraFinIntervalo?: string;
  tipoIndividuo: TipoIndividuo;
  fitness?: number;
}

export enum TipoIndividuo {
  NORMAL = 'NORMAL',
  PARCHE = 'PARCHE',
  AVERIA = 'EMERGENCIA'
}

export interface Simulacion {
  fechaHoraSimulacion: string; // LocalDateTime como string ISO
  fechaHoraInicioIntervalo?: string;
  individuos: Individuo[];
  mejorIndividuo?: Individuo;
  generacion: number;
  terminada: boolean;
}

// ============================
// TIPOS PARA EL FRONTEND
// ============================

// Tipos para el contexto de simulación (manteniendo compatibilidad)
export interface CamionEstado {
  id: string; // Corresponde a camion.codigo
  ubicacion: string; // Ejemplo: "(12,8)"
  porcentaje: number; // Avance de la ruta
  estado: EstadoCamion | string; // Permitir strings para compatibilidad
  // Propiedades adicionales del camión para compatibilidad
  capacidadActualGLP?: number;
  capacidadMaximaGLP?: number;
  combustibleActual?: number;
  combustibleMaximo?: number;
  distanciaMaxima?: number;
  pesoCarga?: number;
  pesoCombinado?: number;
  tara?: number;
  tipo?: string;
  velocidadPromedio?: number;
}

export interface RutaCamion {
  id: string; // Corresponde a camion.codigo
  ruta: string[]; // Ejemplo: ["(12,8)", "(13,8)"]
  puntoDestino: string; // Ejemplo: "(57,17)"
  pedidos: Pedido[];
}

// Tipos para visualización en mapa
export interface ElementoMapa {
  id: string;
  tipo: 'camion' | 'pedido' | 'almacen' | 'bloqueo' | 'averia';
  coordenada: Coordenada;
  estado?: string;
  datos?: Record<string, unknown>;
}

// Tipos para métricas y rendimiento
export interface MetricasSimulacion {
  tiempoTotal: number;
  distanciaTotal: number;
  combustibleConsumido: number;
  pedidosEntregados: number;
  pedidosPendientes: number;
  camionesActivos: number;
  camionesInactivos: number;
  eficienciaPromedio: number;
}

// ============================
// TIPOS PARA API Y REQUESTS
// ============================

export interface AlmacenRequest {
  nombre: string;
  coordenada: Coordenada;
  tipo: TipoAlmacen;
  capacidadMaximaGLP: number;
  capacidadMaximaCombustible: number;
  activo: boolean;
}

export interface CamionRequest {
  codigo: string;
  tipo: TipoCamion;
  coordenada: Coordenada;
  capacidadMaximaGLP: number;
  combustibleMaximo: number;
  velocidadPromedio: number;
}

export interface PedidoRequest {
  codigo: string;
  coordenada: Coordenada;
  volumenGLPAsignado: number;
  horasLimite: number;
}

export interface AveriaRequest {
  turno: number;
  codigoCamion: string;
  tipoIncidente: string;
  fechaHoraReporte: string;
}

export interface BloqueoRequest {
  fechaInicio: string;
  fechaFin: string;
  coordenadas: Coordenada[];
}

export interface MantenimientoRequest {
  dia: number;
  mes: number;
  codigoCamion: string;
}

export interface SimulacionRequest {
  fechaInicio: string;
  parametros?: {
    poblacionInicial?: number;
    generacionesMaximas?: number;
    tasaMutacion?: number;
    tasaCruce?: number;
  };
}

// ============================
// TIPOS PARA RESPUESTAS DE API
// ============================

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// ============================
// FUNCIONES UTILITARIAS
// ============================

// Conversión entre tipos de coordenadas
export function coordenadaToBackend(coordenada: Coordenada): CoordenadaBackend {
  return {
    fila: coordenada.y,
    columna: coordenada.x
  };
}

export function coordenadaFromBackend(coordenada: CoordenadaBackend): Coordenada {
  return {
    x: coordenada.columna,
    y: coordenada.fila
  };
}

// Funciones de cálculo para camiones
export function calcularPesoCarga(camion: Camion): number {
  if (camion.capacidadMaximaGLP === 0) return 0;
  return (camion.capacidadActualGLP / camion.capacidadMaximaGLP) * camion.pesoCarga;
}

export function calcularPesoCombinado(camion: Camion): number {
  const pesoCargaActual = calcularPesoCarga(camion);
  return camion.tara + pesoCargaActual;
}

export function calcularConsumoGalones(camion: Camion, distanciaKm: number): number {
  const pesoCombinado = calcularPesoCombinado(camion);
  return (distanciaKm * pesoCombinado) / 180;
}

export function calcularDistanciaMaxima(camion: Camion): number {
  const pesoCombinado = calcularPesoCombinado(camion);
  if (pesoCombinado === 0) return 0;
  return (camion.combustibleActual * 180) / pesoCombinado;
}

export function actualizarCamion(camion: Camion): void {
  camion.pesoCarga = calcularPesoCarga(camion);
  camion.pesoCombinado = camion.tara + camion.pesoCarga;
  camion.distanciaMaxima = calcularDistanciaMaxima(camion);
}

// Funciones de validación
export function validarCoordenada(coordenada: Coordenada): boolean {
  return coordenada.x >= 0 && coordenada.y >= 0;
}

export function validarCamion(camion: Camion): boolean {
  return camion.codigo.length > 0 && 
         camion.capacidadMaximaGLP > 0 && 
         camion.combustibleMaximo > 0 && 
         camion.velocidadPromedio > 0;
}

export function validarPedido(pedido: Pedido): boolean {
  return pedido.codigo.length > 0 && 
         pedido.volumenGLPAsignado > 0 && 
         pedido.horasLimite > 0;
}

// Funciones de formato
export function formatearCoordenada(coordenada: Coordenada): string {
  return `(${coordenada.x},${coordenada.y})`;
}

export function formatearCoordenadaFrontend(coordenada: CoordenadaFrontend): string {
  return `(${coordenada.x},${coordenada.y})`;
}

export function formatearFecha(fecha: string): string {
  try {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return 'N/A';
  }
}

// Funciones de estado
export function getEstadoCamionColor(estado: EstadoCamion): string {
  switch (estado) {
    case EstadoCamion.DISPONIBLE:
      return '#00FF00';
    case EstadoCamion.NO_DISPONIBLE:
      return '#FF0000';
    case EstadoCamion.EN_RUTA:
      return '#0000FF';
    case EstadoCamion.ENTREGANDO_GLP_A_CLIENTE:
      return '#0066CC';
    case EstadoCamion.EN_MANTENIMIENTO:
      return '#000000';
    case EstadoCamion.EN_MANTENIMIENTO_PREVENTIVO:
      return '#FFCC00';
    case EstadoCamion.EN_MANTENIMIENTO_CORRECTIVO:
      return '#FF9900';
    case EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA:
      return '#990000';
    case EstadoCamion.INMOVILIZADO_POR_AVERIA:
      return '#CC3300';
    case EstadoCamion.SIN_COMBUSTIBLE:
      return '#808080';
    case EstadoCamion.RECIBIENDO_COMBUSTIBLE:
      return '#6666FF';
    case EstadoCamion.ENTREGANDO_COMBUSTIBLE_A_CAMION:
      return '#6666FF';
    case EstadoCamion.RECIBIENDO_GLP:
      return '#66CC00';
    case EstadoCamion.ENTREGANDO_GLP_A_CAMION:
      return '#33CCCC';
    case EstadoCamion.ALMACEN_TEMPORAL:
      return '#9933CC';
    default:
      return '#808080';
  }
}

export function getEstadoPedidoColor(estado: EstadoPedido): string {
  switch (estado) {
    case EstadoPedido.REGISTRADO:
      return '#FFCC00';
    case EstadoPedido.PLANIFICADO:
      return '#0066CC';
    case EstadoPedido.ENTREGADO:
      return '#00FF00';
    default:
      return '#808080';
  }
}

// ============================
// TIPOS LEGACY PARA COMPATIBILIDAD
// ============================

// Mantener algunos tipos legacy para compatibilidad con código existente
export interface AlmacenBackend {
  coordenada: { fila: number; columna: number };
  nombre: string;
  tipo: 'CENTRAL' | 'SECUNDARIO';
  activo: boolean;
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
  capacidadCombustible: number;
  capacidadMaximaCombustible: number;
  esCentral: boolean;
}

// Alias para tipos que mantienen compatibilidad
export type AlmacenLegacy = {
  id: string;
  nombre: string;
  tipo: 'CENTRAL' | 'INTERMEDIO';
  coordenada: Coordenada;
  activo: boolean;
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
  capacidadCombustible: number;
  capacidadMaximaCombustible: number;
  esCentral: boolean;
};

export type CamionLegacy = {
  codigo: string;
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
  combustibleActual: number;
  combustibleMaximo: number;
  distanciaMaxima: number;
  estado: string;
  pesoCarga: number;
  pesoCombinado: number;
  tara: number;
  tipo: string;
  velocidadPromedio: number;
};

export type PedidoLegacy = {
  codigo: string;
  coordenada: Coordenada;
  horasLimite?: number;
  volumenGLPAsignado?: number;
  estado?: string;
  fechaRegistro?: string;
  fechaLimite?: string;
};

// ============================
// TIPOS PARA CARGA DE ARCHIVOS DE SIMULACIÓN
// ============================

export interface ArchivoCarga {
  nombre: string;
  contenido: string;
  tipo: 'ventas' | 'bloqueos' | 'camiones';
  fechaCreacion: Date;
  tamano: number;
}

export interface DatosVentas {
  fechaHora: string; // Formato: "01d00h24m"
  coordenadaX: number;
  coordenadaY: number;
  codigoCliente: string; // Formato: "c-198"
  volumenGLP: number; // Formato: "3m3"
  horasLimite: number; // Formato: "4h"
}

export interface DatosBloqueo {
  fechaInicio: string; // Formato: "01d00h31m"
  fechaFin: string; // Formato: "01d21h35m"
  coordenadas: Array<{x: number, y: number}>; // Formato: "15,10,30,10,30,18"
}

export interface DatosCamion {
  codigo: string;
  tipo: TipoCamion;
  coordenadaX: number;
  coordenadaY: number;
  capacidadMaximaGLP: number;
  combustibleMaximo: number;
  velocidadPromedio: number;
}

export interface EstadoCargaArchivos {
  ventas: {
    cargado: boolean;
    archivo?: ArchivoCarga;
    errores: string[];
  };
  bloqueos: {
    cargado: boolean;
    archivo?: ArchivoCarga;
    errores: string[];
  };
  camiones: {
    cargado: boolean;
    archivo?: ArchivoCarga;
    errores: string[];
  };
}

export interface EjemploArchivo {
  nombre: string;
  descripcion: string;
  contenido: string;
  tipo: 'ventas' | 'bloqueos' | 'camiones';
  formato: string;
}

export interface ValidacionArchivo {
  esValido: boolean;
  errores: string[];
  advertencias: string[];
  datosParseados?: DatosVentas[] | DatosBloqueo[] | DatosCamion[];
}