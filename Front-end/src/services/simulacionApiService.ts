import type { Individuo } from "../types";
import { API_URLS } from "../config/api";
import { formatearFechaParaBackend } from "../context/simulacion/utils/tiempo";
//import { limpiarPedidos } from "./pedidoApiService";

// Tipos para el manejo de tipo de simulación
export type TipoSimulacion = 'DIARIA' | 'SEMANAL' | 'COLAPSO';

export interface TipoSimulacionResponse {
  tipoSimulacionAnterior: TipoSimulacion;
  tipoSimulacionNuevo: TipoSimulacion;
  mensaje: string;
  exito: boolean;
}

export interface TipoSimulacionActualResponse {
  tipoSimulacion: TipoSimulacion;
  descripcion: string;
  timestamp: string;
}

export async function getMejorIndividuo(fechaInicio: string): Promise<Individuo> {
  try {
    console.log("Iniciando solicitud al servidor (GET con fecha). Fecha: " + fechaInicio);

    // Construir URL con parámetro de consulta
    const url = `${API_URLS.MEJOR_INDIVIDUO}?fecha=${encodeURIComponent(fechaInicio)}`;

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
      // No se debe enviar body en un GET
    });

    console.log("Respuesta recibida:", {
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries())
    });

    // Verificar el tipo de contenido
    const contentType = response.headers.get("content-type");
    console.log("Content-Type:", contentType);

    if (!contentType || !contentType.includes("application/json")) {
      console.error("Tipo de contenido inválido:", contentType);
      throw new Error("La respuesta del servidor no es JSON válido");
    }

    if (response.status === 204) {
      console.log("No hay datos disponibles (204)");
      throw new Error("No hay datos disponibles en este momento");
    }

    if (!response.ok) {
      const errorData = await response.json();
      console.error("Error en la respuesta:", errorData);
      throw new Error(errorData.message || `Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    console.log("Datos recibidos:", data);

    if (!data) {
      console.error("Respuesta vacía");
      throw new Error("La respuesta está vacía");
    }

    return data as Individuo;
  } catch (error) {
    console.error("Error al obtener el mejor individuo:", error);
    throw error;
  }
}

/**
 * Inicia una nueva simulación con una fecha específica
 * @param fechaInicio - Fecha y hora de inicio en formato ISO (YYYY-MM-DDTHH:MM:SS)
 * @returns Promise con el mensaje de confirmación
 */
export async function iniciarSimulacion(fechaInicio: string): Promise<string> {
  try {
    console.log("Iniciando simulación con fecha:", fechaInicio);
    
    const response = await fetch(`${API_URLS.INICIAR_SIMULACION}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        fechaInicio: fechaInicio
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const mensaje = await response.text();
    console.log("Simulación iniciada exitosamente:", mensaje);
    return mensaje;
  } catch (error) {
    console.error("Error al iniciar simulación:", error);
    throw error;
  }
}

/**
 * Reinicia la simulación actual
 * @returns Promise con el mensaje de confirmación
 */
export async function reiniciarSimulacion(): Promise<string> {
  try {
    console.log("Reiniciando simulación...");
    
    const response = await fetch(`${API_URLS.REINICIAR_SIMULACION}`, {
      method: 'GET'
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const mensaje = await response.text();
    console.log("Simulación reiniciada exitosamente:", mensaje);
    return mensaje;
  } catch (error) {
    console.error("Error al reiniciar simulación:", error);
    throw error;
  }
}

/**
 * Obtiene información sobre el estado de la simulación
 * @returns Promise con la información de la simulación
 */
export async function obtenerInfoSimulacion(): Promise<{
  totalPaquetes: number;
  paqueteActual: number;
  enProceso: boolean;
  tiempoActual: string;
}> {
  try {
     console.log("Obteniendo información de simulación...");
    const info = {
      totalPaquetes: 199999999,
      paqueteActual: 991919191919,
      enProceso: true,
      tiempoActual: "1999-01-01T00:00:00Z"
    }
    // const response = await fetch(`${API_URLS.INFO_SIMULACION}`, {
    //   method: 'GET'
    // });

    // if (!response.ok) {
    //   const errorText = await response.text();
    //   throw new Error(`Error ${response.status}: ${errorText}`);
    // }

    // const info = await response.json();
    // console.log("Información de simulación obtenida:", info);
    return info;
  } catch (error) {
    console.error("Error al obtener información de simulación:", error);
    throw error;
  }
}

 

/**
 * Obtiene el siguiente paquete disponible en la simulación
 * @returns Promise con el siguiente paquete o null si no hay más paquetes
 */
export async function obtenerSiguientePaquete(): Promise<Individuo | null> {
  try {
    console.log("📦 PAQUETES: Obteniendo siguiente paquete de la simulación...");
    
    // Usar la fecha actual como parámetro por defecto en formato LocalDateTime
    const fechaActual = formatearFechaParaBackend(new Date());
    const url = `${API_URLS.MEJOR_INDIVIDUO}?fecha=${encodeURIComponent(fechaActual)}`;
    
    const response = await fetch(url, {
      method: 'GET'
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ PAQUETES: Error al obtener siguiente paquete:", errorText);
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const contentType = response.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
      console.error("❌ PAQUETES: Tipo de contenido inválido:", contentType);
      throw new Error("La respuesta del servidor no es JSON válido");
    }

    if (response.status === 204) {
      console.log("⏳ PAQUETES: No hay más paquetes disponibles (204)");
      return null;
    }

    const data = await response.json();
    
    if (!data) {
      console.log("⏳ PAQUETES: Respuesta vacía - no hay más paquetes");
      return null;
    }

    console.log("✅ PAQUETES: Siguiente paquete obtenido exitosamente");
    return data;
  } catch (error) {
    console.error("❌ PAQUETES: Error al obtener siguiente paquete:", error);
    throw error;
  }
}

/**
 * Recalcula el algoritmo genético después de una avería usando la fecha y hora actual de la simulación
 * @param fechaHoraActual - Fecha y hora actual de la simulación en formato ISO
 * @returns Promise con la nueva solución calculada
 */
export async function recalcularAlgoritmoDespuesAveria(fechaHoraActual: string): Promise<Individuo> {
  try {
    console.log("🧬 RECALCULANDO: Ejecutando algoritmo genético para fecha actual:", fechaHoraActual);
    
    // Llamar al endpoint con la fecha actual de la simulación
    const url = `${API_URLS.MEJOR_INDIVIDUO}?fecha=${encodeURIComponent(fechaHoraActual)}`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    console.log("🧬 RECALCULANDO: Respuesta recibida:", {
      status: response.status,
      statusText: response.statusText
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ RECALCULANDO: Error al recalcular algoritmo:", errorText);
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const contentType = response.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
      console.error("❌ RECALCULANDO: Tipo de contenido inválido:", contentType);
      throw new Error("La respuesta del servidor no es JSON válido");
    }

    const data = await response.json();
    
    if (!data) {
      console.error("❌ RECALCULANDO: Respuesta vacía");
      throw new Error("La respuesta está vacía");
    }

    console.log("✅ RECALCULANDO: Nuevo algoritmo ejecutado exitosamente");
    return data as Individuo;
  } catch (error) {
    console.error("❌ RECALCULANDO: Error al recalcular algoritmo:", error);
    throw error;
  }
}

/**
 * Cambia el tipo de simulación en el backend
 * @param tipoSimulacion - El nuevo tipo de simulación a establecer
 * @returns Promise con la respuesta del cambio
 */
export async function cambiarTipoSimulacion(tipoSimulacion: TipoSimulacion): Promise<TipoSimulacionResponse> {
  try {
    // console.log("🔄 TIPO SIMULACIÓN: Cambiando tipo de simulación a:", tipoSimulacion);
    
    const response = await fetch(API_URLS.CAMBIAR_TIPO_SIMULACION, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        tipoSimulacion: tipoSimulacion
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ TIPO SIMULACIÓN: Error al cambiar tipo de simulación:", errorText);
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const data = await response.json();
    // console.log("✅ TIPO SIMULACIÓN: Tipo de simulación cambiado exitosamente:", data);

 
    return data as TipoSimulacionResponse;
  } catch (error) {
    console.error("❌ TIPO SIMULACIÓN: Error al cambiar tipo de simulación:", error);
    throw error;
  }
}

/**
 * Obtiene el tipo de simulación actual del backend
 * @returns Promise con la información del tipo de simulación actual
 */
export async function obtenerTipoSimulacionActual(): Promise<TipoSimulacionActualResponse> {
  try {
    console.log("📋 TIPO SIMULACIÓN: Obteniendo tipo de simulación actual...");
    
    const response = await fetch(API_URLS.TIPO_SIMULACION_ACTUAL, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error("❌ TIPO SIMULACIÓN: Error al obtener tipo de simulación actual:", errorText);
      throw new Error(`Error ${response.status}: ${errorText}`);
    }

    const data = await response.json();
    // console.log("✅ TIPO SIMULACIÓN: Tipo de simulación actual obtenido:", data);
    return data as TipoSimulacionActualResponse;
  } catch (error) {
    console.error("❌ TIPO SIMULACIÓN: Error al obtener tipo de simulación actual:", error);
    throw error;
  }
}
