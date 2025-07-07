import React, { useState, useEffect } from 'react';
import { Play, RotateCcw, Clock, Calendar } from 'lucide-react';
import { useSimulacion } from '../context/SimulacionContext';



const ControlSimulacion: React.FC = () => {
  const [fechaInicio, setFechaInicio] = useState<string>('');
  const [horaInicio, setHoraInicio] = useState<string>('00:00');
  const [cargando, setCargando] = useState(false);
  const [mensaje, setMensaje] = useState<string>('');
  const [tipoMensaje, setTipoMensaje] = useState<'success' | 'error' | 'info'>('info');
  // const [infoSimulacion, setInfoSimulacion] = useState<InfoSimulacion | null>(null);
  const { reiniciar, iniciarSimulacion } = useSimulacion();

  // Establecer fecha por defecto (hoy)
  useEffect(() => {
    // Fecha por defecto: 1 de enero de 2025
    setFechaInicio('2025-01-01');
  }, []);

  // Actualizar información de la simulación cada 5 segundos
  // useEffect(() => {
  //   const intervalo = setInterval(async () => {
  //     try {
  //       const info = await obtenerInfoSimulacion();
  //       setInfoSimulacion(info);
  //     } catch (error) {
  //       console.error('Error al obtener info de simulación:', error);
  //     }
  //   }, 5000);
  //
  //   // Obtener información inicial
  //   obtenerInfoSimulacion().then(setInfoSimulacion).catch(console.error);
  //
  //   return () => clearInterval(intervalo);
  // }, []);

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
      
      console.log("🚀 FRONTEND: Iniciando nueva simulación con fecha:", fechaHoraISO);
      
      // Usar la nueva función del contexto que maneja todo el proceso
      await iniciarSimulacion(fechaHoraISO);
      console.log("🎉 FRONTEND: Simulación iniciada exitosamente");
      
      setMensaje('¡Simulación iniciada exitosamente!');
      setTipoMensaje('success');
      
      // Comentado: Actualizar información después de unos segundos para dar tiempo al backend
      // setTimeout(async () => {
      //   try {
      //     const info = await obtenerInfoSimulacion();
      //     setInfoSimulacion(info);
      //     console.log("📊 FRONTEND: Info de simulación actualizada:", info);
      //     
      //     if (info.enProceso) {
      //       setMensaje('Simulación en progreso - Los datos se actualizan automáticamente');
      //       setTipoMensaje('success');
      //     } else {
      //       setMensaje('Simulación completada o detenida');
      //       setTipoMensaje('info');
      //     }
      //   } catch (error) {
      //     console.error('Error al actualizar info:', error);
      //     setMensaje('Simulación iniciada pero no se pudo obtener el estado');
      //     setTipoMensaje('error');
      //   }
      // }, 3000); // Esperamos 3 segundos para que el backend empiece a generar paquetes
      
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
      
      // Comentado: Actualizar información después de unos segundos para dar tiempo al backend
      // setTimeout(async () => {
      //   try {
      //     const info = await obtenerInfoSimulacion();
      //     setInfoSimulacion(info);
      //     console.log("📊 FRONTEND: Info de simulación actualizada después de reiniciar:", info);
      //   } catch (error) {
      //     console.error('Error al actualizar info:', error);
      //   }
      // }, 3000); // Aumentamos el tiempo para dar más margen al backend
      
    } catch (error) {
      setMensaje(`Error: ${error instanceof Error ? error.message : 'Error desconocido'}`);
      setTipoMensaje('error');
    } finally {
      setCargando(false);
    }
  };

  // const obtenerColorEstado = () => {
  //   if (!infoSimulacion) return 'bg-gray-500';
  //   return infoSimulacion.enProceso ? 'bg-green-500' : 'bg-red-500';
  // };

  // const obtenerTextoEstado = () => {
  //   if (!infoSimulacion) return 'Desconocido';
  //   return infoSimulacion.enProceso ? 'En Proceso' : 'Detenida';
  // };

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
        {/*
        <div className="flex items-center gap-2">
          <div className={`w-3 h-3 rounded-full ${obtenerColorEstado()}`}></div>
          <span className="text-sm font-medium text-gray-700">
            {obtenerTextoEstado()}
          </span>
        </div>
        */}
      </div>

      {/* Información de la simulación (deshabilitada temporalmente) */}
      {/*
        Aquí se mostraba el estado y progreso de la simulación. Deshabilitado por solicitud.
      */}

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
          disabled={cargando}
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