import type { EstadoCargaArchivos } from "../../types";
import { validarArchivoVentas, validarArchivoBloqueos, validarArchivoCamiones, validarArchivoMantenimiento } from "./validadores";

// Función para debuggear el estado de carga
export const debugEstadoCarga = (estadoCarga: EstadoCargaArchivos) => {
  console.log("🔍 DEBUG: Estado de carga actual:");
  console.log("Ventas:", {
    cargado: estadoCarga.ventas.cargado,
    archivo: estadoCarga.ventas.archivo?.nombre,
    errores: estadoCarga.ventas.errores
  });
  console.log("Bloqueos:", {
    cargado: estadoCarga.bloqueos.cargado,
    archivo: estadoCarga.bloqueos.archivo?.nombre,
    errores: estadoCarga.bloqueos.errores
  });
  console.log("Camiones:", {
    cargado: estadoCarga.camiones.cargado,
    archivo: estadoCarga.camiones.archivo?.nombre,
    errores: estadoCarga.camiones.errores
  });
  console.log("Mantenimiento:", {
    cargado: estadoCarga.mantenimiento.cargado,
    archivo: estadoCarga.mantenimiento.archivo?.nombre,
    errores: estadoCarga.mantenimiento.errores
  });
};

// Función para validar un archivo de ejemplo
export const validarArchivoEjemplo = async (contenido: string, tipo: 'ventas' | 'bloqueos' | 'camiones' | 'mantenimiento') => {
  console.log(`🔍 DEBUG: Validando archivo de ${tipo}:`);
  console.log("Contenido:", contenido);
  
  let validacion;
  switch (tipo) {
    case 'ventas':
      validacion = validarArchivoVentas(contenido);
      break;
    case 'bloqueos':
      validacion = validarArchivoBloqueos(contenido);
      break;
    case 'camiones':
      validacion = validarArchivoCamiones(contenido);
      break;
    case 'mantenimiento':
      validacion = validarArchivoMantenimiento(contenido);
      break;
  }
  
  console.log("Resultado de validación:", validacion);
  return validacion;
}; 