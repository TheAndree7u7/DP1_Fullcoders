import React, { useState, useRef, useEffect } from "react";
import type { EstadoCargaArchivos } from "../types";
import { useSimulacion } from '../context/SimulacionContext';
import { 
  ejemplos, 
  descargarEjemplo, 
  manejarCargaArchivo, 
  puedenCargarseArchivos, 
  formatearTamanoArchivo 
} from './cargar_archivos';

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
  const { fechaInicioSimulacion, setFechaInicioSimulacion } = useSimulacion();
  const [estadoCarga, setEstadoCarga] = useState<EstadoCargaArchivos>({
    ventas: { cargado: false, errores: [] },
    bloqueos: { cargado: false, errores: [] },
    camiones: { cargado: false, errores: [] }
  });
  const [mostrarConfirmacion, setMostrarConfirmacion] = useState(false);
  const [fechaSimulacion, setFechaSimulacion] = useState<string>(fechaInicioSimulacion || '');

  const fileInputVentasRef = useRef<HTMLInputElement>(null);
  const fileInputBloqueosRef = useRef<HTMLInputElement>(null);

  // Cuando cambia la fecha local, actualizar el contexto global
  useEffect(() => {
    if (fechaSimulacion) {
      setFechaInicioSimulacion(fechaSimulacion);
    }
  }, [fechaSimulacion]);



  // Función para manejar la carga de archivos
  const handleCargaArchivo = async (
    archivo: File, 
    tipo: 'ventas' | 'bloqueos' | 'camiones'
  ) => {
    await manejarCargaArchivo(archivo, tipo, estadoCarga, (nuevoEstado) => {
      setEstadoCarga(nuevoEstado);
      onArchivosCargados(nuevoEstado);
    });
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
        {/* Campo para seleccionar la fecha de simulación */}
        <div className="mb-6">
          <label className="block text-gray-700 font-medium mb-2" htmlFor="fechaSimulacion">
            Fecha de inicio de la simulación
          </label>
          <input
            id="fechaSimulacion"
            type="date"
            className="border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={fechaSimulacion ? fechaSimulacion.substring(0, 10) : ''}
            onChange={e => setFechaSimulacion(e.target.value)}
          />
        </div>
        
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
                  <strong>Tamaño:</strong> {formatearTamanoArchivo(estadoCarga.ventas.archivo.tamano)} KB
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
                  handleCargaArchivo(file, 'ventas');
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
                  <strong>Tamaño:</strong> {formatearTamanoArchivo(estadoCarga.bloqueos.archivo.tamano)} KB
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
                  handleCargaArchivo(file, 'bloqueos');
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