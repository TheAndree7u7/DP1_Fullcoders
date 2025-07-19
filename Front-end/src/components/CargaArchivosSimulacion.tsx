import React, { useState, useRef, useEffect } from "react";
import type { EstadoCargaArchivos } from "../types";
import { useSimulacion } from '../context/SimulacionContext';
import { 
  ejemplos, 
  descargarEjemplo, 
  manejarCargaArchivo, 
  puedenCargarseArchivos, 
  formatearTamanoArchivo,
  descargarArchivoCargado
} from './cargar_archivos';
import { iniciarSimulacion, obtenerInfoSimulacion } from '../services/simulacionApiService';
import DragAndDropZone from './DragAndDropZone';

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
  const { 
    fechaInicioSimulacion, 
    setFechaInicioSimulacion,
    limpiarEstadoParaNuevaSimulacion,
    iniciarPollingPrimerPaquete
  } = useSimulacion();
  const [estadoCarga, setEstadoCarga] = useState<EstadoCargaArchivos>({
    ventas: { cargado: false, cargando: false, errores: [] },
    bloqueos: { cargado: false, cargando: false, errores: [] },
    camiones: { cargado: false, cargando: false, errores: [] },
    mantenimiento: { cargado: false, cargando: false, errores: [] }
  });
  const [mostrarConfirmacion, setMostrarConfirmacion] = useState(false);
  const [fechaSimulacion, setFechaSimulacion] = useState<string>(fechaInicioSimulacion || new Date().toISOString().substring(0, 10) + 'T00:00');
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState<string>('');
  const [tipoMensaje, setTipoMensaje] = useState<'success' | 'error' | 'info'>('info');
  const [isDragOver, setIsDragOver] = useState(false);


  const fileInputVentasRef = useRef<HTMLInputElement>(null);
  const fileInputBloqueosRef = useRef<HTMLInputElement>(null);
  const fileInputCamionesRef = useRef<HTMLInputElement>(null);
  const fileInputMantenimientoRef = useRef<HTMLInputElement>(null);

  // Cuando cambia la fecha local, actualizar el contexto global
  useEffect(() => {
    if (fechaSimulacion) {
      setFechaInicioSimulacion(fechaSimulacion);
    }
  }, [fechaSimulacion]);



  // Función para manejar la carga de archivos
  const handleCargaArchivo = async (
    archivo: File, 
    tipo: 'ventas' | 'bloqueos' | 'camiones' | 'mantenimiento'
  ) => {
    await manejarCargaArchivo(archivo, tipo, estadoCarga, (nuevoEstado) => {
      setEstadoCarga(nuevoEstado);
      onArchivosCargados(nuevoEstado);
    });
  };

  // Función para manejar archivos clasificados por drag and drop
  const handleFileClassified = async (file: File, tipo: 'ventas' | 'bloqueos' | 'camiones' | 'mantenimiento') => {
    // Procesar el archivo inmediatamente
    await handleCargaArchivo(file, tipo);
  };

  // Función para manejar múltiples archivos
  const handleFilesDrop = (files: File[]) => {
    console.log(`Se procesaron ${files.length} archivos`);
    
    // Mostrar mensaje de éxito
    setMensaje(`Se procesaron ${files.length} archivos. Revisa el estado de cada sección.`);
    setTipoMensaje('success');
    
    // Limpiar mensaje después de 3 segundos
    setTimeout(() => {
      setMensaje('');
    }, 3000);
  };



  // Función para manejar el salto de carga
  const manejarSaltarCarga = () => {
    setMostrarConfirmacion(true);
  };

  const confirmarSaltarCarga = async () => {
    if (!fechaSimulacion) {
      setMensaje('Por favor, selecciona una fecha y hora válidas');
      setTipoMensaje('error');
      return;
    }

    setCargando(true);
    setMensaje('Iniciando simulación con datos de prueba...');
    setTipoMensaje('info');
    setMostrarConfirmacion(false);

    try {
      console.log("===================🚀 FRONTEND: Iniciando SIMULACIÓN CON DATOS DE PRUEBA==============");
      const fechaHoraISO = fechaSimulacion;
      
      // 1. Guarda la fecha de inicio en el contexto global
      setFechaInicioSimulacion(fechaHoraISO);

      // 2. Inicia la simulación en el backend
      setMensaje('Configurando simulación en el backend...');
      await iniciarSimulacion(fechaHoraISO);
      
      setMensaje('Simulación iniciada exitosamente. Cargando datos...');
      setTipoMensaje('success');
      
      console.log("🚀 FRONTEND: Simulación iniciada en backend, limpiando estado...");
      
      // Limpiar el estado y cargar nuevos datos
      await limpiarEstadoParaNuevaSimulacion();
      console.log("🧹 FRONTEND: Estado limpiado y datos cargados para nueva simulación");
      
      setMensaje('Iniciando visualización automática...');
      
      // Iniciar el polling para obtener el primer paquete automáticamente
      iniciarPollingPrimerPaquete();
      console.log("🔄 FRONTEND: Polling iniciado para obtener primer paquete automáticamente");
      
      // Actualizar información después de unos segundos para dar tiempo al backend
      setTimeout(async () => {
        try {
          const info = await obtenerInfoSimulacion();
          console.log("📊 FRONTEND: Info de simulación actualizada:", info);
          
          if (info.enProceso) {
            setMensaje('Simulación en progreso - Los datos se actualizan automáticamente');
            setTipoMensaje('success');
          } else {
            setMensaje('Simulación completada o detenida');
            setTipoMensaje('info');
          }
        } catch (error) {
          console.error('Error al actualizar info:', error);
          setMensaje('Simulación iniciada pero no se pudo obtener el estado');
          setTipoMensaje('error');
        }
      }, 3000); // Esperamos 3 segundos para que el backend empiece a generar paquetes
      
      // Llamar a la función original para continuar con el flujo
      onSaltarCarga();
      
    } catch (error) {
      console.error('Error al iniciar simulación:', error);
      const errorMessage = error instanceof Error ? error.message : 'Error desconocido';
      setMensaje(`Error al iniciar simulación: ${errorMessage}`);
      setTipoMensaje('error');
    } finally {
      setCargando(false);
    }
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
        {/* Campo para seleccionar la fecha y hora de simulación */}
        <div className="mb-6">
          <label className="block text-gray-700 font-medium mb-2">
            Fecha y hora de inicio de la simulación
          </label>
          <div className="flex gap-4">
            <div className="flex-1">
              <label className="block text-sm text-gray-600 mb-1" htmlFor="fechaSimulacion">
                Fecha
              </label>
              <input
                id="fechaSimulacion"
                type="date"
                className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={fechaSimulacion ? fechaSimulacion.substring(0, 10) : ''}
                onChange={e => {
                  const nuevaFecha = e.target.value;
                  const horaActual = fechaSimulacion ? fechaSimulacion.substring(11) : '00:00';
                  setFechaSimulacion(`${nuevaFecha}T${horaActual}`);
                }}
              />
            </div>
            
            <div className="flex-1">
              <label className="block text-sm text-gray-600 mb-1" htmlFor="horaSimulacion">
                Hora
              </label>
              <input
                id="horaSimulacion"
                type="time"
                className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={fechaSimulacion ? fechaSimulacion.substring(11, 16) : '00:00'}
                onChange={e => {
                  const fechaActual = fechaSimulacion ? fechaSimulacion.substring(0, 10) : new Date().toISOString().substring(0, 10);
                  const nuevaHora = e.target.value;
                  setFechaSimulacion(`${fechaActual}T${nuevaHora}:00`);
                }}
              />
            </div>
          </div>
        </div>
        
        <div className="mb-6">
          <p className="text-gray-600 mb-4">
            Para continuar con la simulación semanal, debes cargar los siguientes archivos:
          </p>
        </div>

        {/* Zona de Drag and Drop */}
        <div className="mb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-3 flex items-center">
            <span className="bg-green-100 text-green-800 px-2 py-1 rounded-full text-sm mr-2">🚀</span>
            Carga Rápida - Arrastra y Suelta Archivos
          </h3>
          
          <DragAndDropZone
            onFilesDrop={handleFilesDrop}
            onFileClassified={handleFileClassified}
            isDragOver={isDragOver}
            setIsDragOver={setIsDragOver}
          />
        </div>

        {/* Sección de Archivos de Ventas */}
        <div className="mb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-3 flex items-center">
            <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm mr-2">1</span>
            Archivo de Ventas/Pedidos
          </h3>
          
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm font-medium text-gray-700">Estado:</span>
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                estadoCarga.ventas.cargando
                  ? 'bg-yellow-100 text-yellow-800'
                  : estadoCarga.ventas.cargado 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
              }`}>
                {estadoCarga.ventas.cargando ? 'Cargando...' : estadoCarga.ventas.cargado ? 'Cargado' : 'Pendiente'}
              </span>
            </div>
            
            {estadoCarga.ventas.cargando && (
              <div className="bg-yellow-50 border border-yellow-200 rounded p-2 mb-3">
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-yellow-600 mr-2"></div>
                  <p className="text-xs text-yellow-800">Procesando archivo...</p>
                </div>
              </div>
            )}
            
            {estadoCarga.ventas.archivo && !estadoCarga.ventas.cargando && (
              <div className="bg-white rounded p-3 mb-3">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">
                      <strong>Archivo:</strong> {estadoCarga.ventas.archivo.nombre}
                    </p>
                    <p className="text-xs text-gray-500">
                      Tamaño: {formatearTamanoArchivo(estadoCarga.ventas.archivo.tamano)} KB
                    </p>
                  </div>
                  <button
                    onClick={() => descargarArchivoCargado(estadoCarga.ventas.archivo!)}
                    className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-xs font-medium transition-colors"
                  >
                    📥 Descargar
                  </button>
                </div>
              </div>
            )}

            {estadoCarga.ventas.errores.length > 0 && (
              <div className="bg-red-50 border border-red-200 rounded p-2 mb-3">
                <p className="text-xs font-medium text-red-800 mb-1">Errores:</p>
                <ul className="text-xs text-red-700 space-y-1">
                  {estadoCarga.ventas.errores.map((error, index) => (
                    <li key={index}>• {error}</li>
                  ))}
                </ul>
              </div>
            )}

            <div className="flex gap-2">
              <button
                onClick={() => fileInputVentasRef.current?.click()}
                className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Seleccionar
              </button>
              
              <button
                onClick={() => descargarEjemplo(ejemplos[0])}
                className="bg-gray-600 hover:bg-gray-700 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Ejemplo
              </button>
            </div>

            <input
              ref={fileInputVentasRef}
              type="file"
              accept=".txt"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  handleCargaArchivo(file, 'ventas');
                }
              }}
              className="hidden"
            />
          </div>
        </div>

        {/* Sección de Archivos de Bloqueos */}
        <div className="mb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-3 flex items-center">
            <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm mr-2">2</span>
            Archivo de Bloqueos
          </h3>
          
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm font-medium text-gray-700">Estado:</span>
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                estadoCarga.bloqueos.cargando
                  ? 'bg-yellow-100 text-yellow-800'
                  : estadoCarga.bloqueos.cargado 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
              }`}>
                {estadoCarga.bloqueos.cargando ? 'Cargando...' : estadoCarga.bloqueos.cargado ? 'Cargado' : 'Pendiente'}
              </span>
            </div>
            
            {estadoCarga.bloqueos.cargando && (
              <div className="bg-yellow-50 border border-yellow-200 rounded p-2 mb-3">
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-yellow-600 mr-2"></div>
                  <p className="text-xs text-yellow-800">Procesando archivo...</p>
                </div>
              </div>
            )}
            
            {estadoCarga.bloqueos.archivo && !estadoCarga.bloqueos.cargando && (
              <div className="bg-white rounded p-3 mb-3">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">
                      <strong>Archivo:</strong> {estadoCarga.bloqueos.archivo.nombre}
                    </p>
                    <p className="text-xs text-gray-500">
                      Tamaño: {formatearTamanoArchivo(estadoCarga.bloqueos.archivo.tamano)} KB
                    </p>
                  </div>
                  <button
                    onClick={() => descargarArchivoCargado(estadoCarga.bloqueos.archivo!)}
                    className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-xs font-medium transition-colors"
                  >
                    📥 Descargar
                  </button>
                </div>
              </div>
            )}

            {estadoCarga.bloqueos.errores.length > 0 && (
              <div className="bg-red-50 border border-red-200 rounded p-2 mb-3">
                <p className="text-xs font-medium text-red-800 mb-1">Errores:</p>
                <ul className="text-xs text-red-700 space-y-1">
                  {estadoCarga.bloqueos.errores.map((error, index) => (
                    <li key={index}>• {error}</li>
                  ))}
                </ul>
              </div>
            )}

            <div className="flex gap-2">
              <button
                onClick={() => fileInputBloqueosRef.current?.click()}
                className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Seleccionar
              </button>
              
              <button
                onClick={() => descargarEjemplo(ejemplos[1])}
                className="bg-gray-600 hover:bg-gray-700 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Ejemplo
              </button>
            </div>

            <input
              ref={fileInputBloqueosRef}
              type="file"
              accept=".txt"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  handleCargaArchivo(file, 'bloqueos');
                }
              }}
              className="hidden"
            />
          </div>
        </div>

        {/* Sección de Archivos de Camiones */}
        <div className="mb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-3 flex items-center">
            <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm mr-2">3</span>
            Archivo de Camiones
          </h3>
          
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm font-medium text-gray-700">Estado:</span>
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                estadoCarga.camiones.cargando
                  ? 'bg-yellow-100 text-yellow-800'
                  : estadoCarga.camiones.cargado 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
              }`}>
                {estadoCarga.camiones.cargando ? 'Cargando...' : estadoCarga.camiones.cargado ? 'Cargado' : 'Pendiente'}
              </span>
            </div>
            
            {estadoCarga.camiones.cargando && (
              <div className="bg-yellow-50 border border-yellow-200 rounded p-2 mb-3">
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-yellow-600 mr-2"></div>
                  <p className="text-xs text-yellow-800">Procesando archivo...</p>
                </div>
              </div>
            )}
            
            {estadoCarga.camiones.archivo && !estadoCarga.camiones.cargando && (
              <div className="bg-white rounded p-3 mb-3">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">
                      <strong>Archivo:</strong> {estadoCarga.camiones.archivo.nombre}
                    </p>
                    <p className="text-xs text-gray-500">
                      Tamaño: {formatearTamanoArchivo(estadoCarga.camiones.archivo.tamano)} KB
                    </p>
                  </div>
                  <button
                    onClick={() => descargarArchivoCargado(estadoCarga.camiones.archivo!)}
                    className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-xs font-medium transition-colors"
                  >
                    📥 Descargar
                  </button>
                </div>
              </div>
            )}

            {estadoCarga.camiones.errores.length > 0 && (
              <div className="bg-red-50 border border-red-200 rounded p-2 mb-3">
                <p className="text-xs font-medium text-red-800 mb-1">Errores:</p>
                <ul className="text-xs text-red-700 space-y-1">
                  {estadoCarga.camiones.errores.map((error, index) => (
                    <li key={index}>• {error}</li>
                  ))}
                </ul>
              </div>
            )}

            <div className="flex gap-2">
              <button
                onClick={() => fileInputCamionesRef.current?.click()}
                className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Seleccionar
              </button>
              
              <button
                onClick={() => descargarEjemplo(ejemplos[2])}
                className="bg-gray-600 hover:bg-gray-700 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Ejemplo
              </button>
            </div>

            <input
              ref={fileInputCamionesRef}
              type="file"
              accept=".txt"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  handleCargaArchivo(file, 'camiones');
                }
              }}
              className="hidden"
            />
          </div>
        </div>

        {/* Sección de Archivos de Mantenimiento Preventivo */}
        <div className="mb-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-3 flex items-center">
            <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm mr-2">4</span>
            Archivo de Mantenimiento Preventivo
          </h3>
          
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex items-center justify-between mb-3">
              <span className="text-sm font-medium text-gray-700">Estado:</span>
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                estadoCarga.mantenimiento.cargando
                  ? 'bg-yellow-100 text-yellow-800'
                  : estadoCarga.mantenimiento.cargado 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
              }`}>
                {estadoCarga.mantenimiento.cargando ? 'Cargando...' : estadoCarga.mantenimiento.cargado ? 'Cargado' : 'Pendiente'}
              </span>
            </div>
            
            {estadoCarga.mantenimiento.cargando && (
              <div className="bg-yellow-50 border border-yellow-200 rounded p-2 mb-3">
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-yellow-600 mr-2"></div>
                  <p className="text-xs text-yellow-800">Procesando archivo...</p>
                </div>
              </div>
            )}
            
            {estadoCarga.mantenimiento.archivo && !estadoCarga.mantenimiento.cargando && (
              <div className="bg-white rounded p-3 mb-3">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">
                      <strong>Archivo:</strong> {estadoCarga.mantenimiento.archivo.nombre}
                    </p>
                    <p className="text-xs text-gray-500">
                      Tamaño: {formatearTamanoArchivo(estadoCarga.mantenimiento.archivo.tamano)} KB
                    </p>
                  </div>
                  <button
                    onClick={() => descargarArchivoCargado(estadoCarga.mantenimiento.archivo!)}
                    className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-xs font-medium transition-colors"
                  >
                    📥 Descargar
                  </button>
                </div>
              </div>
            )}

            {estadoCarga.mantenimiento.errores.length > 0 && (
              <div className="bg-red-50 border border-red-200 rounded p-2 mb-3">
                <p className="text-xs font-medium text-red-800 mb-1">Errores:</p>
                <ul className="text-xs text-red-700 space-y-1">
                  {estadoCarga.mantenimiento.errores.map((error, index) => (
                    <li key={index}>• {error}</li>
                  ))}
                </ul>
              </div>
            )}

            <div className="flex gap-2">
              <button
                onClick={() => fileInputMantenimientoRef.current?.click()}
                className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Seleccionar
              </button>
              
              <button
                onClick={() => descargarEjemplo(ejemplos[3])}
                className="bg-gray-600 hover:bg-gray-700 text-white px-3 py-2 rounded text-sm font-medium transition-colors"
              >
                Ejemplo
              </button>
            </div>

            <input
              ref={fileInputMantenimientoRef}
              type="file"
              accept=".txt"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  handleCargaArchivo(file, 'mantenimiento');
                }
              }}
              className="hidden"
            />
          </div>
        </div>

        {/* Información de Ejemplos */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-6">
          <h4 className="text-sm font-semibold text-blue-800 mb-2">Información sobre los archivos:</h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
            {ejemplos.map((ejemplo, index) => (
              <div key={index} className="bg-white rounded p-2">
                <p className="text-xs font-medium text-gray-800 mb-1">
                  {ejemplo.nombre}
                </p>
                <p className="text-xs text-gray-600 mb-1">{ejemplo.descripcion}</p>
                <p className="text-xs text-gray-500 font-mono bg-gray-100 p-1 rounded">
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
            disabled={!puedenCargarseArchivos(estadoCarga)}
            className={`px-6 py-3 rounded-md text-sm font-medium transition-colors ${
              puedenCargarseArchivos(estadoCarga)
                ? 'bg-green-600 hover:bg-green-700 text-white'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            Continuar con la Simulación
          </button>
        </div>
      </div>

      {/* Indicador de estado */}
      {(cargando || mensaje) && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md mx-4">
            <div className="flex items-center mb-4">
              {cargando && (
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600 mr-3"></div>
              )}
              <h3 className="text-lg font-semibold text-gray-900">
                {cargando ? 'Iniciando simulación...' : 'Estado'}
              </h3>
            </div>
            
            {mensaje && (
              <p className={`text-sm mb-4 ${
                tipoMensaje === 'error' ? 'text-red-600' : 
                tipoMensaje === 'success' ? 'text-green-600' : 
                'text-blue-600'
              }`}>
                {mensaje}
              </p>
            )}
            
            {cargando && (
              <div className="flex justify-center">
                <div className="animate-pulse text-sm text-gray-500">
                  Por favor espera...
                </div>
              </div>
            )}
          </div>
        </div>
      )}

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