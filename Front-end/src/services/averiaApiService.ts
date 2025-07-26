import { API_CONFIG } from "../config/api";
import type { EstadoSimulacionCompleto } from "../context/simulacion/utils/estado";
import { convertirEstadoParaBackend } from "../context/simulacion/utils/estado";

// Tipo para la respuesta de avería (puedes ajustarlo según lo que devuelva el backend)
export interface AveriaResponse {
  id: number;
  codigoCamion: string;
  tipoIncidente: string;
  fechaHoraOcurrencia: string;
  fechaHoraDisponible: string;
  tiempoReparacionEstimado: number;
  turnoOcurrencia?: number;
  // ...otros campos posibles
}

// Tipo para el estado convertido para el backend
interface EstadoConvertidoParaBackend {
  timestamp: string;
  horaActual: number;
  horaSimulacion: string;
  fechaHoraSimulacion: string | null;
  fechaInicioSimulacion: string | null;
  diaSimulacion: number | null;
  tiempoRealSimulacion: string;
  tiempoTranscurridoSimulado: string;
  camiones: Array<{
    id: string;
    ubicacion: string;
    porcentaje: number;
    estado: string;
    capacidadActualGLP: number;
    capacidadMaximaGLP: number;
    combustibleActual: number;
    combustibleMaximo: number;
    distanciaMaxima: number;
    pesoCarga: number;
    pesoCombinado: number;
    tara: number;
    tipo: string;
    velocidadPromedio: number;
  }>;
  rutasCamiones: Array<{
    id: string;
    ruta: string[];
    puntoDestino: string;
    pedidos: Array<{
      codigo: string;
      coordenadaX: number;
      coordenadaY: number;
      horasLimite: number;
      volumenGLPAsignado: number;
      estado: string;
      fechaRegistro: string;
      fechaLimite: string;
    }>;
  }>;
  almacenes: Array<{
    coordenadaX: number;
    coordenadaY: number;
    nombre: string;
    capacidadActualGLP: number;
    capacidadMaximaGLP: number;
    capacidadCombustible: number;
    capacidadActualCombustible: number;
    capacidadMaximaCombustible: number;
    esCentral: boolean;
    permiteCamionesEstacionados: boolean;
    tipo: string;
    activo: boolean;
  }>;
  bloqueos: Array<{
    coordenadas: Array<{ x: number; y: number }>;
    fechaInicio: string;
    fechaFin: string;
  }>;
}

// Servicio original: POST /api/averias/averiar-camion
export async function averiarCamionTipo(
  codigoCamion: string,
  tipo: number,
  fechaHoraReporte: string
): Promise<AveriaResponse> {
  const response = await fetch(`${API_CONFIG.BASE_URL}/averias/averiar-camion`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      codigoCamion,
      tipoIncidente: `TI${tipo}`,
      fechaHoraReporte
    })
  });
  if (!response.ok) {
    throw new Error("No se pudo averiar el camión");
  }
  return response.json();
}

// 🔧 TEMPORAL: Función para debugging del JSON que se envía al backend
export async function debugAveriaRaw(
  codigoCamion: string,
  tipo: number,
  fechaHoraReporte: string,
  estadoCompleto: EstadoSimulacionCompleto
): Promise<string> {
  console.log("🔍 DEBUG FRONTEND: Enviando JSON raw para debugging...");
  
  const estadoConvertido = convertirEstadoParaBackend(estadoCompleto) as EstadoConvertidoParaBackend;
  
  // Extraer coordenada del camión averiado
  const camionAveriado = estadoCompleto.camiones.find(c => c.id === codigoCamion);
  let coordenadaAveria = null;
  
  if (camionAveriado && camionAveriado.ubicacion) {
    const match = camionAveriado.ubicacion.match(/\((\d+),(\d+)\)/);
    if (match) {
      coordenadaAveria = {
        fila: parseInt(match[2]),
        columna: parseInt(match[1])
      };
    }
  }
  
  if (!coordenadaAveria) {
    coordenadaAveria = { fila: 8, columna: 12 };
  }
  
  const datosEnvio = {
    codigoCamion,
    tipoIncidente: `TI${tipo}`,
    fechaHoraReporte,
    coordenada: coordenadaAveria,
    estadoSimulacion: estadoConvertido
  };

  console.log("🔍 DEBUG FRONTEND: JSON que se enviará:");
  console.log(JSON.stringify(datosEnvio, null, 2));

  const response = await fetch(`${API_CONFIG.BASE_URL}/averias/debug-averia-raw`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(datosEnvio)
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("❌ DEBUG FRONTEND: Error en endpoint de debugging:", errorText);
    throw new Error(`Error en debugging: ${errorText}`);
  }

  const result = await response.text();
  console.log("✅ DEBUG FRONTEND: Respuesta del debugging:", result);
  return result;
}

// Nuevo servicio mejorado: POST /api/averias/averiar-camion-con-estado
export async function averiarCamionConEstado(
  codigoCamion: string,
  tipo: number,
  fechaHoraReporte: string,
  estadoCompleto: EstadoSimulacionCompleto
): Promise<AveriaResponse> {
  // console.log("🚛💥 AVERÍA: Enviando avería con estado completo para camión", codigoCamion);
  // console.log("📊 AVERÍA: Estado completo incluido en la petición");
  // console.log("📊 AVERÍA: Tamaño del estado (JSON):", JSON.stringify(estadoCompleto).length, "caracteres");

  // 🔍 AGREGADO: Logs detallados de todos los datos que se envían
  const estadoConvertido = convertirEstadoParaBackend(estadoCompleto) as EstadoConvertidoParaBackend;
  
  // 🔧 MEJORADO: Extraer la coordenada del camión averiado del estado con más precisión
  const camionAveriado = estadoCompleto.camiones.find(c => c.id === codigoCamion);
  let coordenadaAveria = null;
  
  // console.log("🔍 AVERÍA: Buscando coordenada del camión averiado:", {
  //   camionId: codigoCamion,
  //   camionEncontrado: !!camionAveriado,
  //   ubicacionCamion: camionAveriado?.ubicacion,
  //   estadoCamion: camionAveriado?.estado
  // });
  
  if (camionAveriado && camionAveriado.ubicacion) {
    // Parsear la coordenada del formato "(x,y)" a objeto Coordenada
    const match = camionAveriado.ubicacion.match(/\((\d+),(\d+)\)/);
    if (match) {
      coordenadaAveria = {
        fila: parseInt(match[2]), // y
        columna: parseInt(match[1]) // x
      };
      console.log("📍 AVERÍA: Coordenada extraída del camión averiado:", coordenadaAveria);
    } else {
      console.warn("⚠️ AVERÍA: No se pudo parsear la coordenada del formato:", camionAveriado.ubicacion);
    }
  }
  
  // 🔧 NUEVO: Si no se pudo extraer la coordenada del camión, intentar obtenerla de la ruta
  if (!coordenadaAveria) {
    console.log("🔍 AVERÍA: Intentando extraer coordenada de la ruta del camión...");
    const rutaCamion = estadoCompleto.rutasCamiones?.find(r => r.id === codigoCamion);
    if (rutaCamion && rutaCamion.ruta && rutaCamion.ruta.length > 0) {
      // Obtener la posición actual del camión en la ruta
      const porcentaje = camionAveriado?.porcentaje || 0;
      const posicionEnRuta = Math.floor(porcentaje);
      const coordenadaRuta = rutaCamion.ruta[posicionEnRuta];
      
      if (coordenadaRuta) {
        const matchRuta = coordenadaRuta.match(/\((\d+),(\d+)\)/);
        if (matchRuta) {
          coordenadaAveria = {
            fila: parseInt(matchRuta[2]), // y
            columna: parseInt(matchRuta[1]) // x
          };
          console.log("📍 AVERÍA: Coordenada extraída de la ruta del camión:", coordenadaAveria);
        }
      }
    }
  }
  
  // 🔧 NUEVO: Si aún no se tiene coordenada, usar coordenada por defecto del almacén central
  if (!coordenadaAveria) {
    console.warn("⚠️ AVERÍA: No se pudo extraer la coordenada del camión averiado, usando coordenada por defecto");
    coordenadaAveria = { fila: 8, columna: 12 }; // Coordenada del almacén central por defecto
  }
  
  // 🔧 NUEVO: Validar que la coordenada tenga valores válidos
  if (coordenadaAveria.fila < 0 || coordenadaAveria.columna < 0) {
    console.warn("⚠️ AVERÍA: Coordenada inválida detectada, usando coordenada por defecto:", coordenadaAveria);
    coordenadaAveria = { fila: 8, columna: 12 };
  }
  
  const datosEnvio = {
    codigoCamion,
    tipoIncidente: `TI${tipo}`,
    fechaHoraReporte,
    coordenada: coordenadaAveria, // 🔧 AGREGADO: Incluir la coordenada de la avería
    estadoSimulacion: estadoConvertido
  };

  console.log("📡 ===== DATOS COMPLETOS QUE SE ENVÍAN AL BACKEND =====");
  console.log("🏷️  DATOS BÁSICOS DE LA AVERÍA:");
  console.log("   - Código del camión:", datosEnvio.codigoCamion);
  console.log("   - Tipo de incidente:", datosEnvio.tipoIncidente);
  console.log("   - Fecha y hora del reporte:", datosEnvio.fechaHoraReporte);
  console.log("   - Coordenada de la avería:", datosEnvio.coordenada); // 🔧 AGREGADO: Log de la coordenada
  console.log("   - Coordenada (fila):", datosEnvio.coordenada.fila);
  console.log("   - Coordenada (columna):", datosEnvio.coordenada.columna); 

  const response = await fetch(`${API_CONFIG.BASE_URL}/averias/averiar-camion-con-estado`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(datosEnvio)
  });

  if (!response.ok) {
    const errorText = await response.text();
    console.error("❌ AVERÍA: Error al averiar camión con estado:", errorText);
    throw new Error(`No se pudo averiar el camión: ${errorText}`);
  }

  const result = await response.json();
  console.log("✅ AVERÍA: Camión averiado exitosamente con estado capturado");
  console.log("📥 RESPUESTA DEL BACKEND:", result);
  return result;
}

// Nuevo servicio: GET /api/averias/camion
export async function obtenerAveriasPorCamion(codigoCamion: string): Promise<AveriaResponse[]> {
  const response = await fetch(`${API_CONFIG.BASE_URL}/averias/camion?codigoCamion=${codigoCamion}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json"
    }
  });
  
  if (!response.ok) {
    throw new Error("No se pudieron obtener las averías del camión");
  }
  
  return response.json();
}


