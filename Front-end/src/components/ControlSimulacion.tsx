import React, { useState, useEffect } from 'react';
import { Play, RotateCcw, Clock, Calendar, Info } from 'lucide-react';
import { iniciarSimulacion, obtenerInfoSimulacion } from '../services/simulacionApiService';
import { useSimulacion } from '../context/SimulacionContext';

interface InfoSimulacion {
  totalPaquetes: number;
  paqueteActual: number;
  enProceso: boolean;
  tiempoActual: string;
}

const ControlSimulacion: React.FC = () => {
  const [fechaInicio, setFechaInicio] = useState<string>('');
  const [horaInicio, setHoraInicio] = useState<string>('08:00');
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState<string>('');
  const [tipoMensaje, setTipoMensaje] = useState<'success' | 'error' | 'info'>('info');
  const [infoSimulacion, setInfoSimulacion] = useState<InfoSimulacion | null>(null);
  const { reiniciar, limpiarEstadoParaNuevaSimulacion } = useSimulacion();

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
    setMensaje('');

    try {
      const fechaHoraISO = `${fechaInicio}T${horaInicio}:00`;
      
      // Primero limpiar el estado para la nueva simulación
      limpiarEstadoParaNuevaSimulacion();
      console.log("🧹 FRONTEND: Estado limpiado para nueva simulación");
      
      // Luego iniciar la simulación en el backend
      const respuesta = await iniciarSimulacion(fechaHoraISO);
      
      setMensaje(respuesta);
      setTipoMensaje('success');
      
      console.log("🚀 FRONTEND: Simulación iniciada en backend, esperando que genere paquetes...");
      
      // El sistema de polling normal del contexto se encargará de obtener los paquetes
      // conforme el backend los vaya generando
      
      // Actualizar información después de unos segundos para dar tiempo al backend
      setTimeout(async () => {
        try {
          const info = await obtenerInfoSimulacion();
          setInfoSimulacion(info);
          console.log("📊 FRONTEND: Info de simulación actualizada:", info);
        } catch (error) {
          console.error('Error al actualizar info:', error);
        }
      }, 3000); // Esperamos 3 segundos para que el backend empiece a generar paquetes
      
    } catch (error) {
      setMensaje(`Error: ${error instanceof Error ? error.message : 'Error desconocido'}`);
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
      
      // Actualizar información inmediatamente
      setTimeout(async () => {
        try {
          const info = await obtenerInfoSimulacion();
          setInfoSimulacion(info);
        } catch (error) {
          console.error('Error al actualizar info:', error);
        }
      }, 1000);
      
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
            onChange={(e) => setHoraInicio(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={cargando}
          />
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