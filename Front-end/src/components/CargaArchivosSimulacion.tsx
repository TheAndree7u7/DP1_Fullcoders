import React, { useState, useEffect } from "react";
import { useSimulacion } from '../context/SimulacionContext';
import { iniciarSimulacion, obtenerInfoSimulacion } from '../services/simulacionApiService';

interface CargaArchivosSimulacionProps {
  onSaltarCarga: () => void;
}

const CargaArchivosSimulacion: React.FC<CargaArchivosSimulacionProps> = ({
  onSaltarCarga
}) => {
  const { 
    fechaInicioSimulacion, 
    setFechaInicioSimulacion,
    limpiarEstadoParaNuevaSimulacion,
    iniciarPollingPrimerPaquete
  } = useSimulacion();
  
  const [fechaSimulacion, setFechaSimulacion] = useState<string>(fechaInicioSimulacion || new Date().toISOString().substring(0, 10) + 'T00:00');
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState<string>('');
  const [tipoMensaje, setTipoMensaje] = useState<'success' | 'error' | 'info'>('info');

  // Cuando cambia la fecha local, actualizar el contexto global
  useEffect(() => {
    if (fechaSimulacion) {
      setFechaInicioSimulacion(fechaSimulacion);
    }
  }, [fechaSimulacion]);

  const confirmarIniciarSimulacion = async () => {
    if (!fechaSimulacion) {
      setMensaje('Por favor, selecciona una fecha y hora válidas');
      setTipoMensaje('error');
      return;
    }

    setCargando(true);
    setMensaje('Iniciando simulación con datos de prueba...');
    setTipoMensaje('info');

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
      
      // console.log("🚀 FRONTEND: Simulación iniciada en backend, limpiando estado...");
      
      // Limpiar el estado y cargar nuevos datos
      await limpiarEstadoParaNuevaSimulacion();
      // console.log("🧹 FRONTEND: Estado limpiado y datos cargados para nueva simulación");
      
      setMensaje('Iniciando visualización automática...');
      
      // Iniciar el polling para obtener el primer paquete automáticamente
      iniciarPollingPrimerPaquete();
      // console.log("🔄 FRONTEND: Polling iniciado para obtener primer paquete automáticamente");
      
      // Actualizar información después de unos segundos para dar tiempo al backend
      setTimeout(async () => {
        try {
          const info = await obtenerInfoSimulacion();
          // console.log("📊 FRONTEND: Info de simulación actualizada:", info);
          
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

  return (
    <div className="max-w-2xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">
          Configurar Simulación Semanal
        </h2>
        
        <div className="mb-6">
          <p className="text-gray-600 mb-4">
            Selecciona la fecha y hora de inicio para la simulación semanal. 
            Se utilizarán los datos de prueba disponibles en el sistema.
          </p>
        </div>

        {/* Campo para seleccionar la fecha y hora de simulación */}
        <div className="mb-8">
          <label className="block text-gray-700 font-medium mb-4">
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

        {/* Información adicional */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-8">
          <h4 className="text-sm font-semibold text-blue-800 mb-2">ℹ️ Información:</h4>
          <ul className="text-sm text-blue-700 space-y-1">
            <li>• Se utilizarán los archivos de datos de prueba disponibles en el sistema</li>
            <li>• La simulación comenzará desde la fecha y hora especificadas</li>
            <li>• Los datos se actualizarán automáticamente durante la simulación</li>
          </ul>
        </div>

        {/* Botón de Acción */}
        <div className="flex justify-center">
          <button
            onClick={confirmarIniciarSimulacion}
            disabled={cargando}
            className={`px-8 py-3 rounded-md text-sm font-medium transition-colors flex items-center gap-2 ${
              cargando
                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                : 'bg-green-600 hover:bg-green-700 text-white'
            }`}
          >
            {cargando ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                Iniciando...
              </>
            ) : (
              <>
                <span>🚀</span>
                Iniciar Simulación Semanal
              </>
            )}
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
    </div>
  );
};

export default CargaArchivosSimulacion; 