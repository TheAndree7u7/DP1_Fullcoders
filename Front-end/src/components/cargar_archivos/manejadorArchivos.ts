import type { ArchivoCarga, EstadoCargaArchivos } from "../../types";
import { validarArchivoVentas, validarArchivoBloqueos } from "./validadores";

// Función para manejar la carga de archivos
export const manejarCargaArchivo = async (
  archivo: File, 
  tipo: 'ventas' | 'bloqueos' | 'camiones',
  estadoCarga: EstadoCargaArchivos,
  onArchivosCargados: (estado: EstadoCargaArchivos) => void
): Promise<void> => {
  try {
    const contenido = await archivo.text();
    const archivoCarga: ArchivoCarga = {
      nombre: archivo.name,
      contenido,
      tipo,
      fechaCreacion: new Date(),
      tamano: archivo.size
    };

    let validacion;
    
    if (tipo === 'ventas') {
      validacion = validarArchivoVentas(contenido);
    } else if (tipo === 'bloqueos') {
      validacion = validarArchivoBloqueos(contenido);
    } else {
      validacion = { esValido: false, errores: ['Tipo de archivo no soportado'], advertencias: [] };
    }

    const nuevoEstado = {
      ...estadoCarga,
      [tipo]: {
        cargado: validacion.esValido,
        archivo: validacion.esValido ? archivoCarga : undefined,
        errores: validacion.errores
      }
    };

    // Notificar al componente padre
    onArchivosCargados(nuevoEstado);

  } catch (error) {
    const nuevoEstado = {
      ...estadoCarga,
      [tipo]: {
        cargado: false,
        errores: [`Error al leer el archivo: ${error}`]
      }
    };
    onArchivosCargados(nuevoEstado);
  }
}; 