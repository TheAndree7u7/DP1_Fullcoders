import api from '../config/api';

export interface PedidoLoteItem {
  fechaPedido: string;
  x: number;
  y: number;
  volumenGLP: number;
  horasLimite: number;
  cliente?: string;
}

export interface PedidosLoteRequest {
  fechaInicio: string;
  pedidos: PedidoLoteItem[];
  descripcion?: string;
}

export interface PedidosLoteResponse {
  totalRecibidos: number;
  pedidosExitosos: number;
  pedidosDivididos: number;
  totalPedidosCreados: number;
  errores: string[];
  fechaInicio: string;
  descripcion?: string;
}

export interface ValidacionResponse {
  valido: boolean;
  totalPedidos: number;
  pedidosValidos: number;
  pedidosQueDividirian: number;
  errores: string[];
  advertencias: string[];
}

/**
 * Cargar un lote de pedidos al backend
 */
export const cargarLotePedidos = async (request: PedidosLoteRequest): Promise<PedidosLoteResponse> => {
  try {
    console.log('🌐 API: Enviando lote de pedidos al backend...', {
      totalPedidos: request.pedidos.length,
      fechaInicio: request.fechaInicio,
      descripcion: request.descripcion
    });

    const response = await api.post('/pedidos/cargar-lote', request);
    
    console.log('✅ API: Lote de pedidos procesado exitosamente:', response.data);
    return response.data;
    
  } catch (error) {
    console.error('❌ API: Error al cargar lote de pedidos:', error);
    throw error;
  }
};

/**
 * Validar un lote de pedidos sin procesarlos
 */
export const validarLotePedidos = async (request: PedidosLoteRequest): Promise<ValidacionResponse> => {
  try {
    console.log('🔍 API: Validando lote de pedidos...', {
      totalPedidos: request.pedidos.length,
      fechaInicio: request.fechaInicio
    });

    const response = await api.post('/pedidos/validar-lote', request);
    
    console.log('✅ API: Validación completada:', response.data);
    return response.data;
    
  } catch (error) {
    console.error('❌ API: Error al validar lote de pedidos:', error);
    throw error;
  }
};

/**
 * Extraer fecha de inicio desde el nombre del archivo
 * Formato esperado: ventas202501.txt (YYYYMM)
 */
export const extraerFechaDesdeNombreArchivo = (nombreArchivo: string): Date | null => {
  try {
    // Buscar patrón YYYYMM en el nombre del archivo
    const match = nombreArchivo.match(/(\d{6})/);
    if (!match) {
      console.warn('⚠️ No se pudo extraer fecha del archivo:', nombreArchivo);
      return null;
    }
    
    const fechaStr = match[1]; // YYYYMM
    const año = parseInt(fechaStr.substring(0, 4));
    const mes = parseInt(fechaStr.substring(4, 6)) - 1; // JavaScript meses van de 0-11
    
    if (año < 2020 || año > 2030 || mes < 0 || mes > 11) {
      console.warn('⚠️ Fecha inválida extraída del archivo:', nombreArchivo, 'Año:', año, 'Mes:', mes + 1);
      return null;
    }
    
    const fecha = new Date(año, mes, 1, 0, 0, 0, 0);
    console.log('📅 Fecha extraída de', nombreArchivo, ':', fecha.toISOString().split('T')[0]);
    
    return fecha;
  } catch (error) {
    console.error('❌ Error al extraer fecha del archivo:', nombreArchivo, error);
    return null;
  }
};

/**
 * Calcular la fecha de inicio mínima de un conjunto de archivos
 */
export const calcularFechaInicioMinima = (archivos: File[]): Date => {
  const fechas: Date[] = [];
  
  for (const archivo of archivos) {
    const fecha = extraerFechaDesdeNombreArchivo(archivo.name);
    if (fecha) {
      fechas.push(fecha);
    }
  }
  
  if (fechas.length === 0) {
    console.warn('⚠️ No se pudieron extraer fechas de los archivos, usando fecha actual');
    return new Date();
  }
  
  // Encontrar la fecha mínima
  const fechaMinima = new Date(Math.min(...fechas.map(f => f.getTime())));
  console.log('📅 Fecha de inicio calculada (mínima):', fechaMinima.toISOString().split('T')[0]);
  
  return fechaMinima;
};

/**
 * Convertir archivo de texto de pedidos al formato esperado por el backend
 */
export const parsearArchivoPedidos = (contenidoArchivo: string, fechaInicio: Date, nombreArchivo?: string): PedidosLoteRequest => {
  const lineas = contenidoArchivo.split('\n').filter(linea => linea.trim() !== '');
  const pedidos: PedidoLoteItem[] = [];
  
  console.log('📄 Parseando archivo', nombreArchivo || 'desconocido', 'con', lineas.length, 'líneas...');
  console.log('📄 Primeras 3 líneas del archivo:', lineas.slice(0, 3));

  for (let i = 0; i < lineas.length; i++) {
    const linea = lineas[i].trim();
    try {
      // Ignorar líneas vacías
      if (!linea) {
        continue;
      }

      // Verificar formato básico
      if (!linea.includes(':')) {
        console.warn(`⚠️ Línea ${i + 1} no tiene formato esperado:`, linea);
        continue;
      }

      // Formato real: [fecha]:[x],[y],[cliente],[volumen],[horas]
      // Sin número de línea ni →
      const [fechaParte, datosParte] = linea.split(':');
      
      if (!fechaParte || !datosParte) {
        console.warn(`⚠️ Línea ${i + 1} - División por : falló:`, linea);
        continue;
      }

      const datosArray = datosParte.split(',');
      if (datosArray.length < 5) {
        console.warn(`⚠️ Línea ${i + 1} - No tiene suficientes campos:`, datosArray);
        continue;
      }

      const [x, y, cliente, volumenStr, horasStr] = datosArray;

      // Parsear fecha (formato: DDdHHhMMm)
      const fechaPedido = parsearFechaFormato(fechaParte.trim(), fechaInicio);
      
      // Parsear datos numéricos
      const xNum = parseInt(x.trim());
      const yNum = parseInt(y.trim());
      // Manejar volumen con o sin 'm3'
      const volumenGLP = parseFloat(volumenStr.trim().replace('m3', ''));
      // Manejar horas con o sin 'h'
      const horasLimite = parseFloat(horasStr.trim().replace('h', ''));

      // Validar que los datos son números válidos
      if (isNaN(xNum) || isNaN(yNum) || isNaN(volumenGLP) || isNaN(horasLimite)) {
        console.warn(`⚠️ Línea ${i + 1} - Datos numéricos inválidos:`, {
          x: xNum, y: yNum, volumen: volumenGLP, horas: horasLimite
        });
        continue;
      }

      const pedido: PedidoLoteItem = {
        fechaPedido: fechaPedido.toISOString(),
        x: xNum,
        y: yNum,
        volumenGLP,
        horasLimite,
        cliente: cliente?.trim() || `c-${pedidos.length + 1}`
      };

      pedidos.push(pedido);
      
      // Log cada 10 pedidos para debug
      if (pedidos.length % 10 === 0) {
        console.log(`📦 Procesados ${pedidos.length} pedidos...`);
      }

    } catch (error) {
      console.error(`❌ Error al parsear línea ${i + 1}:`, linea, error);
    }
  }

  console.log('✅ Archivo parseado:', pedidos.length, 'pedidos válidos de', lineas.length, 'líneas');
  
  if (pedidos.length === 0) {
    console.error('❌ CRÍTICO: No se parsearon pedidos válidos del archivo');
    console.log('📄 Contenido completo del archivo (primeros 500 chars):', contenidoArchivo.substring(0, 500));
  }
  
  return {
    fechaInicio: fechaInicio.toISOString(),
    pedidos,
    descripcion: nombreArchivo ? `Pedidos desde ${nombreArchivo}` : `Pedidos cargados - ${pedidos.length} items`
  };
};

/**
 * Parsear fecha en formato DDdHHhMMm
 */
const parsearFechaFormato = (fechaStr: string, fechaBase: Date): Date => {
  // Formato: 01d00h24m (día, hora, minuto)
  const regex = /(\d+)d(\d+)h(\d+)m/;
  const match = fechaStr.match(regex);
  
  if (!match) {
    throw new Error(`Formato de fecha inválido: ${fechaStr}`);
  }

  const dia = parseInt(match[1]);
  const hora = parseInt(match[2]);
  const minuto = parseInt(match[3]);

  const fecha = new Date(fechaBase);
  fecha.setDate(dia);
  fecha.setHours(hora, minuto, 0, 0);

  return fecha;
};

/**
 * Crear un archivo de ejemplo para cargar pedidos
 */
export const generarEjemploArchivoPedidos = (): string => {
  return `01d00h24m:16,13,c-198,3m3,4h
01d00h48m:5,18,c-12,9m3,17h
01d01h12m:22,7,c-089,15m3,8h
01d01h36m:11,25,c-456,6m3,12h
01d02h00m:8,15,c-789,12m3,24h
01d02h24m:19,9,c-321,4m3,6h
01d02h48m:14,20,c-654,18m3,10h
01d03h12m:26,12,c-987,7m3,15h
01d03h36m:3,22,c-111,25m3,20h
01d04h00m:17,6,c-222,11m3,18h`;
};