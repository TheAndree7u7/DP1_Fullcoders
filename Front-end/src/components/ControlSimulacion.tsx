import React, { useState, useEffect } from 'react';
import { Play, RotateCcw, Clock, Calendar, Info } from 'lucide-react';
import { iniciarSimulacion, obtenerInfoSimulacion } from '../services/simulacionApiService';
import { useSimulacion } from '../context/SimulacionContext';
import { reanudarSimulacion as reanudarSimulacionUtil } from '../context/simulacion/utils/controles';

interface InfoSimulacion {
  totalPaquetes: number;
  paqueteActual: number;
  enProceso: boolean;
  tiempoActual: string;
}

const ControlSimulacion: React.FC = () => {
  const [fechaInicio, setFechaInicio] = useState<string>('');
  const [horaInicio, setHoraInicio] = useState<string>('00:00');
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState<string>('');
  const [tipoMensaje, setTipoMensaje] = useState<'success' | 'error' | 'info'>('info');
  const [infoSimulacion, setInfoSimulacion] = useState<InfoSimulacion | null>(null);
  const { reiniciar, limpiarEstadoParaNuevaSimulacion, iniciarPollingPrimerPaquete, setSimulacionActiva, simulacionActiva } = useSimulacion();

  // Establecer fecha por defecto (hoy)
  useEffect(() => {
    const hoy = new Date();
    const fechaFormateada = hoy.toISOString().split('T')[0];
    setFechaInicio(fechaFormateada);
  }, []);

  // Actualizar información de la simulación cada 5 segundos
  useEffect(() => {
    const intervalo = setInterval(async () => {
      try {
        const info = await obtenerInfoSimulacion();
        setInfoSimulacion(info);
      } catch (error) {
        console.error('Error al obtener info de simulación:', error);
      }
    }, 5000);

    // Obtener información inicial
    obtenerInfoSimulacion().then(setInfoSimulacion).catch(console.error);

    return () => clearInterval(intervalo);
  }, []);

  const manejarInicioSimulacion = async () => {
    if (!fechaInicio || !horaInicio) {
      setMensaje('Por favor, selecciona una fecha y hora válidas');
      setTipoMensaje('error');
      return;
    }

    setCargando(true);
    setMensaje('Iniciando simulación...');
    setTipoMensaje('info');

    try {
      const fechaHoraISO = `${fechaInicio}T${horaInicio}:00`;
      
      // Primero iniciar la simulación en el backend
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
          setInfoSimulacion(info);
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
      
    } catch (error) {
      console.error('Error al iniciar simulación:', error);
      const errorMessage = error instanceof Error ? error.message : 'Error desconocido';
      setMensaje(`Error al iniciar simulación: ${errorMessage}`);
      setTipoMensaje('error');
    } finally {
      setCargando(false);
    }
  };

  const manejarReinicioSimulacion = async () => {
    setCargando(true);
    setMensaje('');

    try {
      // Reiniciar tanto el backend como el contexto local
      await reiniciar();
      
      setMensaje('Simulación reiniciada exitosamente');
      setTipoMensaje('success');
      
      console.log("🔄 FRONTEND: Simulación reiniciada completamente");
      
      // Actualizar información después de unos segundos para dar tiempo al backend
      setTimeout(async () => {
        try {
          const info = await obtenerInfoSimulacion();
          setInfoSimulacion(info);
          console.log("📊 FRONTEND: Info de simulación actualizada después de reiniciar:", info);
        } catch (error) {
          console.error('Error al actualizar info:', error);
        }
      }, 3000); // Aumentamos el tiempo para dar más margen al backend
      
    } catch (error) {
      setMensaje(`Error: ${error instanceof Error ? error.message : 'Error desconocido'}`);
      setTipoMensaje('error');
    } finally {
      setCargando(false);
    }
  };

  const obtenerColorEstado = () => {
    if (!infoSimulacion) return 'bg-gray-500';
    if (!infoSimulacion.enProceso) return 'bg-red-500';
    return simulacionActiva ? 'bg-green-500' : 'bg-yellow-500';
  };

  const obtenerTextoEstado = () => {
    if (!infoSimulacion) return 'Desconocido';
    if (!infoSimulacion.enProceso) return 'Detenida';
    return simulacionActiva ? 'En Proceso' : 'Pausada';
  };

  // Manejador para el cambio de hora que garantiza el formato correcto
  const manejarCambioHora = (e: React.ChangeEvent<HTMLInputElement>) => {
    const valor = e.target.value;
    // Validar que el valor sea un formato de hora válido (HH:MM)
    if (valor === '' || /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/.test(valor)) {
      setHoraInicio(valor);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-4">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
          <Clock className="w-5 h-5" />
          Control de Simulación
        </h2>
        
        {/* Indicador de estado */}
        <div className="flex items-center gap-2">
          <div className={`w-3 h-3 rounded-full ${obtenerColorEstado()}`}></div>
          <span className="text-sm font-medium text-gray-700">
            {obtenerTextoEstado()}
          </span>
        </div>
      </div>

      {/* Información de la simulación */}
      {infoSimulacion && (
        <div className="bg-gray-50 rounded-md p-3 mb-4">
          <div className="flex items-center gap-2 mb-2">
            <Info className="w-4 h-4 text-blue-500" />
            <span className="text-sm font-medium text-gray-700">Estado Actual</span>
          </div>
          
          {/* Paquete actual destacado */}
          <div className="bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg p-4 mb-3">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-bold">Paquete Actual</h3>
                <p className="text-blue-100 text-sm">Paquete siendo consumido</p>
              </div>
              <div className="text-right">
                <div className="text-3xl font-bold">
                  {infoSimulacion.paqueteActual}
                </div>
                <div className="text-blue-100 text-sm">
                  de {infoSimulacion.totalPaquetes}
                </div>
              </div>
            </div>
          </div>

          {/* Información detallada en grid */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm mb-3">
            <div className="bg-white rounded-lg p-3 border border-gray-200">
              <div className="text-gray-600 text-xs uppercase tracking-wide">Total Paquetes</div>
              <div className="text-xl font-bold text-gray-800">{infoSimulacion.totalPaquetes}</div>
            </div>
            <div className="bg-white rounded-lg p-3 border border-gray-200">
              <div className="text-gray-600 text-xs uppercase tracking-wide">Progreso</div>
              <div className="text-xl font-bold text-gray-800">
                {infoSimulacion.totalPaquetes > 0 
                  ? Math.round((infoSimulacion.paqueteActual / infoSimulacion.totalPaquetes) * 100)
                  : 0}%
              </div>
            </div>
            <div className="bg-white rounded-lg p-3 border border-gray-200">
              <div className="text-gray-600 text-xs uppercase tracking-wide">Estado</div>
              <div className={`text-xl font-bold ${
                !infoSimulacion.enProceso ? 'text-red-600' : 
                simulacionActiva ? 'text-green-600' : 'text-yellow-600'
              }`}>
                {obtenerTextoEstado()}
              </div>
            </div>
          </div>

          {/* Información adicional */}
          {infoSimulacion.tiempoActual && (
            <div className="bg-white rounded-lg p-3 border border-gray-200 mb-3">
              <div className="text-gray-600 text-xs uppercase tracking-wide mb-1">Tiempo de Simulación</div>
              <div className="text-sm font-medium text-gray-800">
                {new Date(infoSimulacion.tiempoActual).toLocaleString('es-ES', {
                  year: 'numeric',
                  month: '2-digit',
                  day: '2-digit',
                  hour: '2-digit',
                  minute: '2-digit',
                  second: '2-digit'
                })}
              </div>
            </div>
          )}
          
          {/* Barra de progreso mejorada */}
          {infoSimulacion.totalPaquetes > 0 && (
            <div className="mt-3">
              <div className="flex justify-between text-xs text-gray-600 mb-2">
                <span className="font-medium">Progreso de la Simulación</span>
                <span className="font-medium">{Math.round((infoSimulacion.paqueteActual / infoSimulacion.totalPaquetes) * 100)}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
                <div 
                  className="bg-gradient-to-r from-blue-500 to-blue-600 h-3 rounded-full transition-all duration-500 ease-out shadow-sm"
                  style={{ width: `${(infoSimulacion.paqueteActual / infoSimulacion.totalPaquetes) * 100}%` }}
                ></div>
              </div>
              <div className="flex justify-between text-xs text-gray-500 mt-1">
                <span>Paquete 0</span>
                <span>Paquete {infoSimulacion.totalPaquetes}</span>
              </div>
            </div>
          )}

          {/* Mensaje cuando no hay paquetes */}
          {infoSimulacion.totalPaquetes === 0 && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 text-center">
              <div className="text-yellow-800 font-medium">No hay paquetes disponibles</div>
              <div className="text-yellow-600 text-sm mt-1">
                Inicia una simulación para generar paquetes de datos
              </div>
            </div>
          )}
        </div>
      )}

      {/* Controles de fecha y hora */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            <Calendar className="w-4 h-4 inline mr-1" />
            Fecha de Inicio
          </label>
          <input
            type="date"
            value={fechaInicio}
            onChange={(e) => setFechaInicio(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={cargando}
          />
        </div>
        
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            <Clock className="w-4 h-4 inline mr-1" />
            Hora de Inicio
          </label>
          <input
            type="time"
            value={horaInicio}
            onChange={manejarCambioHora}
            min="00:00"
            max="23:59"
            step="60"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={cargando}
            placeholder="00:00"
          />
          <p className="text-xs text-gray-500 mt-1">
            Formato: HH:MM (00:00 - 23:59)
          </p>
        </div>
      </div>

      {/* Botones de control */}
      <div className="flex gap-3 mb-4">
        <button
          onClick={manejarInicioSimulacion}
          disabled={cargando || (infoSimulacion?.enProceso ?? false)}
          className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          <Play className="w-4 h-4" />
          {cargando ? 'Iniciando...' : 'Iniciar Simulación'}
        </button>
        
        {/* Botón de reanudar - solo se muestra si la simulación está pausada */}
        {!simulacionActiva && (infoSimulacion?.enProceso ?? false) && (
          <button
            onClick={() => reanudarSimulacionUtil(setSimulacionActiva)}
            disabled={cargando}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
          >
            <Play className="w-4 h-4" />
            Reanudar Simulación
          </button>
        )}
        
        <button
          onClick={manejarReinicioSimulacion}
          disabled={cargando}
          className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          <RotateCcw className="w-4 h-4" />
          Reiniciar
        </button>
      </div>

      {/* Mensaje de estado */}
      {mensaje && (
        <div className={`p-3 rounded-md text-sm ${
          tipoMensaje === 'success' 
            ? 'bg-green-100 text-green-800 border border-green-200' 
            : tipoMensaje === 'error'
            ? 'bg-red-100 text-red-800 border border-red-200'
            : 'bg-blue-100 text-blue-800 border border-blue-200'
        }`}>
          {mensaje}
        </div>
      )}
    </div>
  );
};

export default ControlSimulacion; 