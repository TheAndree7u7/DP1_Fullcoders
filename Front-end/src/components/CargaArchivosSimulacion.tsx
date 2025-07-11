import React, { useState, useRef } from "react";
import type { 
  ArchivoCarga, 
  EstadoCargaArchivos, 
  EjemploArchivo, 
  ValidacionArchivo,
  DatosVentas,
  DatosBloqueo
} from "../types";

interface CargaArchivosSimulacionProps {
  onArchivosCargados: (estado: EstadoCargaArchivos) => void;
  onContinuar: () => void;
  onSaltarCarga: () => void;
}

const CargaArchivosSimulacion: React.FC<CargaArchivosSimulacionProps> = ({
  onArchivosCargados,
  onContinuar,
  onSaltarCarga
}) => {
  const [estadoCarga, setEstadoCarga] = useState<EstadoCargaArchivos>({
    ventas: { cargado: false, errores: [] },
    bloqueos: { cargado: false, errores: [] },
    camiones: { cargado: false, errores: [] }
  });
  const [mostrarConfirmacion, setMostrarConfirmacion] = useState(false);

  const fileInputVentasRef = useRef<HTMLInputElement>(null);
  const fileInputBloqueosRef = useRef<HTMLInputElement>(null);

  // Ejemplos de archivos
  const ejemplos: EjemploArchivo[] = [
    {
      nombre: "ventas202501.txt",
      descripcion: "Archivo de ventas/pedidos",
      tipo: "ventas",
      formato: "Formato: fechaHora:coordenadaX,coordenadaY,codigoCliente,volumenGLP,horasLimite",
      contenido: `01d00h24m:16,13,c-198,3m3,4h
01d00h48m:5,18,c-12,9m3,17h
01d01h12m:63,13,c-83,2m3,9h
01d01h35m:4,6,c-37,2m3,16h
01d01h59m:54,16,c-115,9m3,5h`
    },
    {
      nombre: "202501.bloqueos.txt",
      descripcion: "Archivo de bloqueos",
      tipo: "bloqueos",
      formato: "Formato: fechaInicio-fechaFin:coordenadas",
      contenido: `01d00h31m-01d21h35m:15,10,30,10,30,18
01d01h13m-01d20h38m:08,03,08,23,20,23
01d02h40m-01d22h32m:57,30,57,45
01d03h54m-01d21h25m:25,25,30,25,30,30,35,30
01d05h05m-01d21h37m:42,08,42,15,47,15,47,27,55,27`
    }
  ];

  // Función para validar archivo de ventas
  const validarArchivoVentas = (contenido: string): ValidacionArchivo => {
    const lineas = contenido.trim().split('\n');
    const errores: string[] = [];
    const advertencias: string[] = [];
    const datosParseados: DatosVentas[] = [];

    lineas.forEach((linea, index) => {
      if (!linea.trim()) return;

      const partes = linea.split(':');
      if (partes.length !== 2) {
        errores.push(`Línea ${index + 1}: Formato incorrecto. Debe ser 'fechaHora:datos'`);
        return;
      }

      const fechaHora = partes[0].trim();
      const datos = partes[1].trim();

      // Validar formato de fecha
      if (!/^\d{2}d\d{2}h\d{2}m$/.test(fechaHora)) {
        errores.push(`Línea ${index + 1}: Formato de fecha incorrecto. Debe ser 'DDdHHhMMm'`);
        return;
      }

      // Validar datos
      const valores = datos.split(',');
      if (valores.length !== 5) {
        errores.push(`Línea ${index + 1}: Debe tener 5 valores separados por coma`);
        return;
      }

      const [x, y, cliente, volumen, horas] = valores;
      
      if (!/^\d+$/.test(x) || !/^\d+$/.test(y)) {
        errores.push(`Línea ${index + 1}: Coordenadas deben ser números`);
        return;
      }

      if (!/^c-\d+$/.test(cliente)) {
        errores.push(`Línea ${index + 1}: Código de cliente debe ser 'c-NUMERO'`);
        return;
      }

      if (!/^\d+m3$/.test(volumen)) {
        errores.push(`Línea ${index + 1}: Volumen debe ser 'NUMEROm3'`);
        return;
      }

      if (!/^\d+h$/.test(horas)) {
        errores.push(`Línea ${index + 1}: Horas límite debe ser 'NUMEROh'`);
        return;
      }

      datosParseados.push({
        fechaHora,
        coordenadaX: parseInt(x),
        coordenadaY: parseInt(y),
        codigoCliente: cliente,
        volumenGLP: parseInt(volumen.replace('m3', '')),
        horasLimite: parseInt(horas.replace('h', ''))
      });
    });

    return {
      esValido: errores.length === 0,
      errores,
      advertencias,
      datosParseados
    };
  };

  // Función para validar archivo de bloqueos
  const validarArchivoBloqueos = (contenido: string): ValidacionArchivo => {
    const lineas = contenido.trim().split('\n');
    const errores: string[] = [];
    const advertencias: string[] = [];
    const datosParseados: DatosBloqueo[] = [];

    lineas.forEach((linea, index) => {
      if (!linea.trim()) return;

      const partes = linea.split(':');
      if (partes.length !== 2) {
        errores.push(`Línea ${index + 1}: Formato incorrecto. Debe ser 'fechaInicio-fechaFin:coordenadas'`);
        return;
      }

      const fechas = partes[0].trim();
      const coordenadas = partes[1].trim();

      // Validar formato de fechas
      const fechasPartes = fechas.split('-');
      if (fechasPartes.length !== 2) {
        errores.push(`Línea ${index + 1}: Formato de fechas incorrecto. Debe ser 'fechaInicio-fechaFin'`);
        return;
      }

      const [fechaInicio, fechaFin] = fechasPartes;
      if (!/^\d{2}d\d{2}h\d{2}m$/.test(fechaInicio) || !/^\d{2}d\d{2}h\d{2}m$/.test(fechaFin)) {
        errores.push(`Línea ${index + 1}: Formato de fecha incorrecto. Debe ser 'DDdHHhMMm'`);
        return;
      }

      // Validar coordenadas
      const coordValores = coordenadas.split(',');
      if (coordValores.length < 2 || coordValores.length % 2 !== 0) {
        errores.push(`Línea ${index + 1}: Debe tener un número par de coordenadas (x,y)`);
        return;
      }

      const coordenadasArray: Array<{x: number, y: number}> = [];
      for (let i = 0; i < coordValores.length; i += 2) {
        const x = parseInt(coordValores[i]);
        const y = parseInt(coordValores[i + 1]);
        
        if (isNaN(x) || isNaN(y)) {
          errores.push(`Línea ${index + 1}: Coordenadas deben ser números`);
          return;
        }
        
        coordenadasArray.push({ x, y });
      }

      datosParseados.push({
        fechaInicio,
        fechaFin,
        coordenadas: coordenadasArray
      });
    });

    return {
      esValido: errores.length === 0,
      errores,
      advertencias,
      datosParseados
    };
  };

  // Función para manejar la carga de archivos
  const manejarCargaArchivo = async (
    archivo: File, 
    tipo: 'ventas' | 'bloqueos' | 'camiones'
  ) => {
    try {
      const contenido = await archivo.text();
      const archivoCarga: ArchivoCarga = {
        nombre: archivo.name,
        contenido,
        tipo,
        fechaCreacion: new Date(),
        tamano: archivo.size
      };

      let validacion: ValidacionArchivo;
      
      if (tipo === 'ventas') {
        validacion = validarArchivoVentas(contenido);
      } else if (tipo === 'bloqueos') {
        validacion = validarArchivoBloqueos(contenido);
      } else {
        validacion = { esValido: false, errores: ['Tipo de archivo no soportado'], advertencias: [] };
      }

      setEstadoCarga(prev => ({
        ...prev,
        [tipo]: {
          cargado: validacion.esValido,
          archivo: validacion.esValido ? archivoCarga : undefined,
          errores: validacion.errores
        }
      }));

      // Notificar al componente padre
      const nuevoEstado = {
        ...estadoCarga,
        [tipo]: {
          cargado: validacion.esValido,
          archivo: validacion.esValido ? archivoCarga : undefined,
          errores: validacion.errores
        }
      };
      onArchivosCargados(nuevoEstado);

    } catch (error) {
      setEstadoCarga(prev => ({
        ...prev,
        [tipo]: {
          cargado: false,
          errores: [`Error al leer el archivo: ${error}`]
        }
      }));
    }
  };

  // Función para descargar ejemplo
  const descargarEjemplo = (ejemplo: EjemploArchivo) => {
    const blob = new Blob([ejemplo.contenido], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = ejemplo.nombre;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  // Función para verificar si se pueden cargar todos los archivos
  const puedenCargarseArchivos = () => {
    return estadoCarga.ventas.cargado && estadoCarga.bloqueos.cargado;
  };

  // Función para manejar el salto de carga
  const manejarSaltarCarga = () => {
    setMostrarConfirmacion(true);
  };

  const confirmarSaltarCarga = () => {
    setMostrarConfirmacion(false);
    onSaltarCarga();
  };

  const cancelarSaltarCarga = () => {
    setMostrarConfirmacion(false);
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">
          Cargar Archivos para Simulación Semanal
        </h2>
        
        <div className="mb-6">
          <p className="text-gray-600 mb-4">
            Para continuar con la simulación semanal, debes cargar los siguientes archivos:
          </p>
        </div>

        {/* Sección de Archivos de Ventas */}
        <div className="mb-8">
          <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
            <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm mr-2">1</span>
            Archivo de Ventas/Pedidos
          </h3>
          
          <div className="bg-gray-50 rounded-lg p-4 mb-4">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm font-medium text-gray-700">Estado:</span>
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                estadoCarga.ventas.cargado 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-red-100 text-red-800'
              }`}>
                {estadoCarga.ventas.cargado ? 'Cargado' : 'Pendiente'}
              </span>
            </div>
            
            {estadoCarga.ventas.archivo && (
              <div className="bg-white rounded p-3 mb-3">
                <p className="text-sm text-gray-600">
                  <strong>Archivo:</strong> {estadoCarga.ventas.archivo.nombre}
                </p>
                <p className="text-sm text-gray-600">
                  <strong>Tamaño:</strong> {(estadoCarga.ventas.archivo.tamano / 1024).toFixed(2)} KB
                </p>
              </div>
            )}

            {estadoCarga.ventas.errores.length > 0 && (
              <div className="bg-red-50 border border-red-200 rounded p-3 mb-3">
                <p className="text-sm font-medium text-red-800 mb-2">Errores encontrados:</p>
                <ul className="text-sm text-red-700 space-y-1">
                  {estadoCarga.ventas.errores.map((error, index) => (
                    <li key={index}>• {error}</li>
                  ))}
                </ul>
              </div>
            )}

            <div className="flex gap-3">
              <button
                onClick={() => fileInputVentasRef.current?.click()}
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Seleccionar Archivo
              </button>
              
              <button
                onClick={() => descargarEjemplo(ejemplos[0])}
                className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Descargar Ejemplo
              </button>
            </div>

            <input
              ref={fileInputVentasRef}
              type="file"
              accept=".txt"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  manejarCargaArchivo(file, 'ventas');
                }
              }}
              className="hidden"
            />
          </div>
        </div>

        {/* Sección de Archivos de Bloqueos */}
        <div className="mb-8">
          <h3 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
            <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm mr-2">2</span>
            Archivo de Bloqueos
          </h3>
          
          <div className="bg-gray-50 rounded-lg p-4 mb-4">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm font-medium text-gray-700">Estado:</span>
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                estadoCarga.bloqueos.cargado 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-red-100 text-red-800'
              }`}>
                {estadoCarga.bloqueos.cargado ? 'Cargado' : 'Pendiente'}
              </span>
            </div>
            
            {estadoCarga.bloqueos.archivo && (
              <div className="bg-white rounded p-3 mb-3">
                <p className="text-sm text-gray-600">
                  <strong>Archivo:</strong> {estadoCarga.bloqueos.archivo.nombre}
                </p>
                <p className="text-sm text-gray-600">
                  <strong>Tamaño:</strong> {(estadoCarga.bloqueos.archivo.tamano / 1024).toFixed(2)} KB
                </p>
              </div>
            )}

            {estadoCarga.bloqueos.errores.length > 0 && (
              <div className="bg-red-50 border border-red-200 rounded p-3 mb-3">
                <p className="text-sm font-medium text-red-800 mb-2">Errores encontrados:</p>
                <ul className="text-sm text-red-700 space-y-1">
                  {estadoCarga.bloqueos.errores.map((error, index) => (
                    <li key={index}>• {error}</li>
                  ))}
                </ul>
              </div>
            )}

            <div className="flex gap-3">
              <button
                onClick={() => fileInputBloqueosRef.current?.click()}
                className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Seleccionar Archivo
              </button>
              
              <button
                onClick={() => descargarEjemplo(ejemplos[1])}
                className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Descargar Ejemplo
              </button>
            </div>

            <input
              ref={fileInputBloqueosRef}
              type="file"
              accept=".txt"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  manejarCargaArchivo(file, 'bloqueos');
                }
              }}
              className="hidden"
            />
          </div>
        </div>

        {/* Información de Ejemplos */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <h4 className="text-sm font-semibold text-blue-800 mb-3">Información sobre los archivos:</h4>
          <div className="space-y-3">
            {ejemplos.map((ejemplo, index) => (
              <div key={index} className="bg-white rounded p-3">
                <p className="text-sm font-medium text-gray-800 mb-1">
                  {ejemplo.nombre}
                </p>
                <p className="text-sm text-gray-600 mb-2">{ejemplo.descripcion}</p>
                <p className="text-xs text-gray-500 font-mono bg-gray-100 p-2 rounded">
                  {ejemplo.formato}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* Botones de Acción */}
        <div className="flex justify-between items-center">
          <button
            onClick={manejarSaltarCarga}
            className="bg-orange-500 hover:bg-orange-600 text-white px-6 py-3 rounded-md text-sm font-medium transition-colors flex items-center gap-2"
          >
            <span>⚠️</span>
            Continuar con Datos de Prueba
          </button>

          <button
            onClick={onContinuar}
            disabled={!puedenCargarseArchivos()}
            className={`px-6 py-3 rounded-md text-sm font-medium transition-colors ${
              puedenCargarseArchivos()
                ? 'bg-green-600 hover:bg-green-700 text-white'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            Continuar con la Simulación
          </button>
        </div>
      </div>

      {/* Modal de Confirmación */}
      {mostrarConfirmacion && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md mx-4">
            <div className="flex items-center mb-4">
              <span className="text-2xl mr-3">⚠️</span>
              <h3 className="text-lg font-semibold text-gray-900">
                ¿Continuar con datos de prueba?
              </h3>
            </div>
            
            <p className="text-gray-600 mb-6">
              Al continuar con datos de prueba, se utilizarán los archivos existentes en el sistema. 
              ¿Estás seguro de que deseas proceder sin cargar archivos personalizados?
            </p>
            
            <div className="flex gap-3 justify-end">
              <button
                onClick={cancelarSaltarCarga}
                className="bg-gray-300 hover:bg-gray-400 text-gray-700 px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={confirmarSaltarCarga}
                className="bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors"
              >
                Continuar con Datos de Prueba
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CargaArchivosSimulacion; 