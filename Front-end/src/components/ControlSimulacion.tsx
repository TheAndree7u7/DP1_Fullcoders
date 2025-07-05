import React, { useState, useEffect } from 'react';
import { Play, RotateCcw, Clock, Calendar, Info } from 'lucide-react';
import { iniciarSimulacion, obtenerInfoSimulacion } from '../services/simulacionApiService';
import { useSimulacion } from '../context/SimulacionContext';
import { API_CONFIG } from '../config/api';

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
  const { reiniciar, limpiarEstadoParaNuevaSimulacion, iniciarPollingPrimerPaquete, pausadaPorAveria } = useSimulacion();

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
      // Crear fecha explícitamente en zona horaria local
      const fechaLocal = new Date(`${fechaInicio}T${horaInicio}:00`);
      
      // Convertir a ISO manteniendo la hora local (no UTC)
      const fechaHoraISO = new Date(fechaLocal.getTime() - (fechaLocal.getTimezoneOffset() * 60000)).toISOString().slice(0, -1);
      
      console.log('📅 FRONTEND: Fecha seleccionada por usuario:', `${fechaInicio} ${horaInicio}`);
      console.log('📅 FRONTEND: Fecha local creada:', fechaLocal.toString());
      console.log('📅 FRONTEND: Fecha ISO enviada al backend:', fechaHoraISO);
      
      // Primero iniciar la simulación en el backend
      setMensaje(`Configurando simulación para ${fechaInicio} ${horaInicio}...`);
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
    return infoSimulacion.enProceso ? 'bg-green-500' : 'bg-red-500';
  };

  const obtenerTextoEstado = () => {
    if (!infoSimulacion) return 'Desconocido';
    return infoSimulacion.enProceso ? 'En Proceso' : 'Detenida';
  };

  // Manejador para el cambio de hora que garantiza el formato correcto
  const manejarCambioHora = (e: React.ChangeEvent<HTMLInputElement>) => {
    const valor = e.target.value;
    // Validar que el valor sea un formato de hora válido (HH:MM)
    if (valor === '' || /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/.test(valor)) {
      setHoraInicio(valor);
    }
  };

  // Función para debuggear la fecha que se envía al backend
  const manejarDebugFecha = async () => {
    if (!fechaInicio || !horaInicio) {
      setMensaje('Por favor, selecciona una fecha y hora válidas para el debug');
      setTipoMensaje('error');
      return;
    }

    setCargando(true);
    setMensaje('Enviando fecha al backend para debug...');
    setTipoMensaje('info');

    try {
      // Crear fecha de la misma manera que en iniciar simulación
      const fechaLocal = new Date(`${fechaInicio}T${horaInicio}:00`);
      const fechaHoraISO = new Date(fechaLocal.getTime() - (fechaLocal.getTimezoneOffset() * 60000)).toISOString().slice(0, -1);
      
      console.log('🔧 DEBUG: Enviando fecha debug al backend...');
      console.log('📅 Frontend - Fecha ISO enviada:', fechaHoraISO);
      
      const response = await fetch(`${API_CONFIG.BASE_URL}/api/simulacion/debug-fecha`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          fechaInicio: fechaHoraISO
        })
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const resultado = await response.text();
      console.log('✅ Respuesta del backend:', resultado);
      
      setMensaje(`Debug completado. Revisa la consola del navegador y del backend para ver los detalles.`);
      setTipoMensaje('success');
      
      // También mostrar un resumen en el mensaje
      setTimeout(() => {
        setMensaje(`Frontend envió: ${fechaInicio} ${horaInicio} | ISO: ${fechaHoraISO}\nBackend respondió con información detallada en consola.`);
      }, 2000);
      
    } catch (error) {
      console.error('Error en debug de fecha:', error);
      const errorMessage = error instanceof Error ? error.message : 'Error desconocido';
      setMensaje(`Error en debug: ${errorMessage}`);
      setTipoMensaje('error');
    } finally {
      setCargando(false);
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
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <div className={`w-3 h-3 rounded-full ${obtenerColorEstado()}`}></div>
            <span className="text-sm font-medium text-gray-700">
              {obtenerTextoEstado()}
            </span>
          </div>
          
          {/* Indicador de pausa por avería */}
          {pausadaPorAveria && (
            <div className="flex items-center gap-2 bg-yellow-100 px-3 py-1 rounded-full">
              <div className="w-2 h-2 bg-yellow-500 rounded-full animate-pulse"></div>
              <span className="text-xs font-medium text-yellow-800">
                Pausada por avería
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Información de la simulación */}
      {infoSimulacion && (
        <div className="bg-gray-50 rounded-md p-3 mb-4">
          <div className="flex items-center gap-2 mb-2">
            <Info className="w-4 h-4 text-blue-500" />
            <span className="text-sm font-medium text-gray-700">Estado Actual</span>
          </div>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-gray-600">Total de paquetes:</span>
              <span className="ml-2 font-medium">{infoSimulacion.totalPaquetes}</span>
            </div>
            <div>
              <span className="text-gray-600">Paquete actual:</span>
              <span className="ml-2 font-medium">{infoSimulacion.paqueteActual}</span>
            </div>
          </div>
          
          {/* Barra de progreso */}
          {infoSimulacion.totalPaquetes > 0 && (
            <div className="mt-3">
              <div className="flex justify-between text-xs text-gray-600 mb-1">
                <span>Progreso</span>
                <span>{Math.round((infoSimulacion.paqueteActual / infoSimulacion.totalPaquetes) * 100)}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div 
                  className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${(infoSimulacion.paqueteActual / infoSimulacion.totalPaquetes) * 100}%` }}
                ></div>
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
          {fechaInicio && (
            <p className="text-xs text-gray-500 mt-1">
              Fecha seleccionada: {new Date(fechaInicio).toLocaleDateString('es-ES', { 
                weekday: 'long', 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
              })}
            </p>
          )}
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

      {/* Preview de fecha y hora */}
      {fechaInicio && horaInicio && (
        <div className="bg-blue-50 border border-blue-200 rounded-md p-3 mb-4">
          <div className="flex items-center gap-2 mb-2">
            <Info className="w-4 h-4 text-blue-600" />
            <span className="text-sm font-medium text-blue-800">Vista Previa</span>
          </div>
          <div className="text-sm text-blue-700">
            <p><strong>Fecha y hora a enviar:</strong> {fechaInicio} a las {horaInicio}</p>
            <p className="text-xs mt-1">
              Esto equivale a: {new Date(`${fechaInicio}T${horaInicio}:00`).toLocaleDateString('es-ES', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
              })}
            </p>
          </div>
        </div>
      )}

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
        
        <button
          onClick={manejarReinicioSimulacion}
          disabled={cargando}
          className="flex items-center gap-2 px-4 py-2 bg-orange-600 text-white rounded-md hover:bg-orange-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          <RotateCcw className="w-4 h-4" />
          Reiniciar
        </button>
        
        <button
          onClick={manejarDebugFecha}
          disabled={cargando || !fechaInicio || !horaInicio}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
        >
          <Info className="w-4 h-4" />
          Debug Fecha
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
          <div className="whitespace-pre-line">{mensaje}</div>
        </div>
      )}

      {/* Información de debug */}
      {fechaInicio && horaInicio && (
        <div className="mt-4 p-3 bg-gray-50 rounded-md text-xs text-gray-600">
          <div className="font-medium mb-1">🔧 Información de Debug:</div>
          <div>• Zona horaria del navegador: {Intl.DateTimeFormat().resolvedOptions().timeZone}</div>
          <div>• Offset UTC: {new Date().getTimezoneOffset() / -60} horas</div>
          <div>• Fecha/hora actual: {new Date().toLocaleString('es-ES')}</div>
        </div>
      )}
    </div>
  );
};

export default ControlSimulacion; 