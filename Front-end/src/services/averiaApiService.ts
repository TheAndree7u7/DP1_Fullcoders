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

// Nuevo servicio mejorado: POST /api/averias/averiar-camion-con-estado
export async function averiarCamionConEstado(
  codigoCamion: string,
  tipo: number,
  fechaHoraReporte: string,
  estadoCompleto: EstadoSimulacionCompleto
): Promise<AveriaResponse> {
  console.log("🚛💥 AVERÍA: Enviando avería con estado completo para camión", codigoCamion);
  console.log("📊 AVERÍA: Estado completo incluido en la petición");
  console.log("📊 AVERÍA: Tamaño del estado (JSON):", JSON.stringify(estadoCompleto).length, "caracteres");

  // 🔍 AGREGADO: Logs detallados de todos los datos que se envían
  const estadoConvertido = convertirEstadoParaBackend(estadoCompleto) as EstadoConvertidoParaBackend;
  const datosEnvio = {
    codigoCamion,
    tipoIncidente: `TI${tipo}`,
    fechaHoraReporte,
    estadoSimulacion: estadoConvertido
  };

  console.log("📡 ===== DATOS COMPLETOS QUE SE ENVÍAN AL BACKEND =====");
  console.log("🏷️  DATOS BÁSICOS DE LA AVERÍA:");
  console.log("   - Código del camión:", datosEnvio.codigoCamion);
  console.log("   - Tipo de incidente:", datosEnvio.tipoIncidente);
  console.log("   - Fecha y hora del reporte:", datosEnvio.fechaHoraReporte);
  
  console.log("🔢 ESTADO DE LA SIMULACIÓN (CONVERTIDO):");
  console.log("   - Timestamp:", estadoConvertido.timestamp);
  console.log("   - Hora actual:", estadoConvertido.horaActual);
  console.log("   - Hora simulación:", estadoConvertido.horaSimulacion);
  console.log("   - Fecha/hora simulación:", estadoConvertido.fechaHoraSimulacion);
  console.log("   - Fecha inicio simulación:", estadoConvertido.fechaInicioSimulacion);
  console.log("   - Día simulación:", estadoConvertido.diaSimulacion);
  console.log("   - Tiempo real simulación:", estadoConvertido.tiempoRealSimulacion);
  console.log("   - Tiempo transcurrido simulado:", estadoConvertido.tiempoTranscurridoSimulado);
  
  console.log("🚛 CAMIONES EN EL ESTADO (CONVERTIDO):");
  console.log("   - Cantidad total de camiones:", estadoConvertido.camiones?.length || 0);
  if (estadoConvertido.camiones?.length && estadoConvertido.camiones.length > 0) {
    estadoConvertido.camiones.forEach((camion, index) => {
      console.log(`   📋 Camión ${index + 1}:`, {
        id: camion.id,
        estado: camion.estado,
        ubicacion: camion.ubicacion,
        porcentaje: camion.porcentaje,
        capacidadGLP: `${camion.capacidadActualGLP}/${camion.capacidadMaximaGLP}`,
        combustible: `${camion.combustibleActual}/${camion.combustibleMaximo}`,
        tipo: camion.tipo,
        // velocidad: camion.velocidadPromedio
      });
    });
  }
  
  console.log("🗺️ RUTAS DE CAMIONES (CONVERTIDO):");
  console.log("   - Cantidad total de rutas:", estadoConvertido.rutasCamiones?.length || 0);
  if (estadoConvertido.rutasCamiones?.length && estadoConvertido.rutasCamiones.length > 0) {
    estadoConvertido.rutasCamiones.forEach((ruta, index) => {
      console.log(`   📋 Ruta ${index + 1}:`, {
        id: ruta.id,
        puntoDestino: ruta.puntoDestino,
        cantidadPedidos: ruta.pedidos?.length || 0,
        pedidos: ruta.pedidos?.map((p) => ({
          codigo: p.codigo,
          coordenada: `(x:${p.coordenadaX}, y:${p.coordenadaY})`,
          volumen: p.volumenGLPAsignado,
          estado: p.estado,
          fechaLimite: p.fechaLimite
        }))
      });
    });
  }
  
  console.log("🏪 ALMACENES EN EL ESTADO (CONVERTIDO):");
  console.log("   - Cantidad total de almacenes:", estadoConvertido.almacenes?.length || 0);
  if (estadoConvertido.almacenes?.length && estadoConvertido.almacenes.length > 0) {
    estadoConvertido.almacenes.forEach((almacen, index) => {
      console.log(`   📋 Almacén ${index + 1}:`, {
        nombre: almacen.nombre,
        coordenada: `(x:${almacen.coordenadaX}, y:${almacen.coordenadaY})`,
        capacidadGLP: `${almacen.capacidadActualGLP}/${almacen.capacidadMaximaGLP}`,
        tipo: almacen.tipo,
        activo: almacen.activo,
        esCentral: almacen.esCentral
      });
    });
  }
  
  console.log("🚧 BLOQUEOS EN EL ESTADO (CONVERTIDO):");
  console.log("   - Cantidad total de bloqueos:", estadoConvertido.bloqueos?.length || 0);
  if (estadoConvertido.bloqueos?.length && estadoConvertido.bloqueos.length > 0) {
    estadoConvertido.bloqueos.forEach((bloqueo, index) => {
      console.log(`   📋 Bloqueo ${index + 1}:`, {
        coordenadas: bloqueo.coordenadas?.map(coord => `(x:${coord.x}, y:${coord.y})`),
        fechaInicio: bloqueo.fechaInicio,
        fechaFin: bloqueo.fechaFin
      });
    });
  }
  
  console.log("📝 DATOS COMPLETOS EN JSON (para copiar/pegar):");
  console.log(JSON.stringify(datosEnvio, null, 2));
  
  console.log("📊 ESTADÍSTICAS DEL ENVÍO:");
  console.log("   - Tamaño total del JSON:", JSON.stringify(datosEnvio).length, "caracteres");
  console.log("   - Tamaño del estado convertido:", JSON.stringify(estadoConvertido).length, "caracteres");
  console.log("   - URL destino:", `${API_CONFIG.BASE_URL}/averias/averiar-camion-con-estado`);
  console.log("🔚 ===== FIN DE DATOS DE ENVÍO =====");

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


