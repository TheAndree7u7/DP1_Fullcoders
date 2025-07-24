import type { ArchivoCarga, EstadoCargaArchivos } from "../../types";
import { validarArchivoVentas, validarArchivoBloqueos, validarArchivoCamiones, validarArchivoMantenimiento } from "./validadores";

// Función para manejar la carga de archivos
export const manejarCargaArchivo = async (
  archivo: File, 
  tipo: 'ventas' | 'bloqueos' | 'camiones' | 'mantenimiento',
  estadoCarga: EstadoCargaArchivos,
  onArchivosCargados: (estado: EstadoCargaArchivos) => void
): Promise<void> => {
  try {
    console.log(`🔍 DEBUG: Procesando archivo ${archivo.name} de tipo ${tipo}`);
    
    // Primero, mostrar estado de carga
    const estadoCargando = {
      ...estadoCarga,
      [tipo]: {
        ...estadoCarga[tipo],
        cargando: true,
        cargado: false
      }
    };
    onArchivosCargados(estadoCargando);
    
    const contenido = await archivo.text();
    
    // Verificar si ya existe un archivo del mismo tipo
    const archivoExistente = estadoCarga[tipo].archivo;
    
    if (archivoExistente) {
      console.log(`📄 DEBUG: Reemplazando archivo existente: ${archivoExistente.nombre} con ${archivo.name}`);
    }
    
    const archivoCarga: ArchivoCarga = {
      nombre: archivo.name,
      contenido: contenido,
      tipo,
      fechaCreacion: new Date(),
      tamano: archivo.size
    };

    console.log(`📄 DEBUG: Contenido del archivo (primeras 200 chars):`, contenido.substring(0, 200));

    let validacion;
    
    if (tipo === 'ventas') {
      validacion = validarArchivoVentas(contenido);
    } else if (tipo === 'bloqueos') {
      validacion = validarArchivoBloqueos(contenido);
    } else if (tipo === 'camiones') {
      validacion = validarArchivoCamiones(contenido);
    } else if (tipo === 'mantenimiento') {
      validacion = validarArchivoMantenimiento(contenido);
    } else {
      validacion = { esValido: false, errores: ['Tipo de archivo no soportado'], advertencias: [] };
    }

    console.log(`✅ DEBUG: Resultado de validación para ${tipo}:`, {
      esValido: validacion.esValido,
      errores: validacion.errores,
      advertencias: validacion.advertencias
    });

    const nuevoEstado = {
      ...estadoCarga,
      [tipo]: {
        cargado: validacion.esValido,
        cargando: false,
        archivo: validacion.esValido ? archivoCarga : undefined,
        errores: validacion.errores
      }
    };

    console.log(`🔄 DEBUG: Estado anterior:`, estadoCarga);
    console.log(`🔄 DEBUG: Nuevo estado:`, nuevoEstado);

    // Notificar al componente padre
    onArchivosCargados(nuevoEstado);

  } catch (error) {
    console.error(`❌ DEBUG: Error procesando archivo ${archivo.name}:`, error);
    const nuevoEstado = {
      ...estadoCarga,
      [tipo]: {
        cargado: false,
        cargando: false,
        errores: [`Error al leer el archivo: ${error}`]
      }
    };
    onArchivosCargados(nuevoEstado);
  }
}; 