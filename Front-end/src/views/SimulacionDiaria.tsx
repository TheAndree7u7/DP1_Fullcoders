import React, { useState, useEffect } from "react";
import Mapa from "../components/Mapa";
import Navbar from "../components/Navbar";
import BloqueosTable from "../components/BloqueosTable";
import RightMenu from "../components/RightMenu";
import BottomMenu from "../components/BottomMenu";
import { ChevronLeft, ChevronUp } from "lucide-react";
import { useSimulacion,  } from "../context/SimulacionContext";
import { formatearTiempoTranscurrido } from "../context/simulacion/utils/tiempo";
import ControlSimulacion from "../components/ControlSimulacion";
import IndicadorPaqueteActual from "../components/IndicadorPaqueteActual";


// Constante que define cuánto tiempo (en segundos) representa cada nodo en la simulación
const SEGUNDOS_POR_NODO = 36;

const SimulacionDiaria: React.FC = () => {
  const [menuExpandido, setMenuExpandido] = useState(true);
  const [bottomMenuExpandido, setBottomMenuExpandido] = useState(false);
  const { 
    fechaHoraSimulacion, 
    horaActual, 
    tiempoTranscurridoSimulado, 
    fechaHoraAcumulada
  } = useSimulacion();
  const [tiempoSimulado, setTiempoSimulado] = useState<Date | null>(null);
  // Estado para alternar paneles
  const [panel, setPanel] = useState<'camiones' | 'bloqueos'>('camiones');
  // Estado para el camión seleccionado desde el modal del mapa
  const [camionSeleccionadoExterno, setCamionSeleccionadoExterno] = useState<string | null>(null);
  // Estado para resaltar elementos en el mapa
  const [elementoResaltado, setElementoResaltado] = useState<{tipo: 'camion' | 'pedido' | 'almacen', id: string} | null>(null);
  // Estado para el panel de control
  const [controlPanelExpandido, setControlPanelExpandido] = useState(false);


  

  
  // Actualizar la hora simulada solo cuando cambia la fecha del backend
  useEffect(() => {
    if (fechaHoraSimulacion) {
      // Solo actualizamos cuando recibimos datos nuevos del backend
      setTiempoSimulado(new Date(fechaHoraSimulacion));
    }
  }, [fechaHoraSimulacion]);
  




  // Efecto para escuchar clicks en los botones de la navbar
  useEffect(() => {
    const btnCamiones = document.getElementById('btn-panel-camiones');
    const btnBloqueos = document.getElementById('btn-panel-bloqueos');
    if (btnCamiones && btnBloqueos) {
      btnCamiones.onclick = () => setPanel('camiones');
      btnBloqueos.onclick = () => setPanel('bloqueos');
    }
    return () => {
      if (btnCamiones) btnCamiones.onclick = null;
      if (btnBloqueos) btnBloqueos.onclick = null;
    };
  }, []);

  // Efecto para escuchar el evento de mostrar ruta del camión desde el modal del mapa
  useEffect(() => {
    const handleMostrarRutaCamion = (event: Event) => {
      const customEvent = event as CustomEvent;
      const camionId = customEvent.detail.camionId;
      if (camionId) {
        setCamionSeleccionadoExterno(camionId);
        setBottomMenuExpandido(true);
      }
    };

    window.addEventListener('mostrarRutaCamion', handleMostrarRutaCamion);
    return () => {
      window.removeEventListener('mostrarRutaCamion', handleMostrarRutaCamion);
    };
  }, []);

  // Resetear el camión seleccionado externo cuando se cierre el menú inferior
  useEffect(() => {
    if (!bottomMenuExpandido) {
      setCamionSeleccionadoExterno(null);
    }
  }, [bottomMenuExpandido]);

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar />
      <div className="bg-[#1E293B] text-white py-2 px-4 flex justify-between items-center">
        <h1 className="font-bold">Ejecución en Tiempo Real - {formatearTiempoTranscurrido(tiempoTranscurridoSimulado)}</h1>
        <div className="flex items-center gap-4">
          {/* Indicador compacto del paquete actual */}
          <div className="bg-[#334155] rounded-lg px-3 py-1">
            <IndicadorPaqueteActual variant="compact" showProgress={false} showTime={false} />
          </div>
          {tiempoSimulado && (
            <div className="text-sm flex items-center gap-4">
              <div>
                <span className="mr-2">Fecha y hora de la simulacion:</span>
                <span className="font-bold text-blue-300">{fechaHoraAcumulada}</span>
              </div> 
              <div>
                <span className="mr-2">Nodo actual:</span>
                <span className="font-bold text-blue-300">{horaActual}</span>
              </div>
              <div>
                <span className="mr-2">Seg/nodo:</span>
                <span className="font-bold text-blue-300">{SEGUNDOS_POR_NODO}</span>
              </div>
            </div>
          )}
        </div>
      </div>
      
      {/* Panel de control de simulación */}
      <div className="px-4 py-2">
        <div className="flex items-center justify-between mb-2">
          <button
            onClick={() => setControlPanelExpandido(!controlPanelExpandido)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
          >
            {controlPanelExpandido ? '🔼 Ocultar Control' : '🔽 Mostrar Control de Simulación'}
          </button>
        </div>
        
        {controlPanelExpandido && (
          <div className="transition-all duration-300">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
              {/* Control de simulación */}
              <div className="lg:col-span-2">
                <ControlSimulacion />
              </div>
              {/* Indicador detallado del paquete actual */}
              <div className="lg:col-span-1">
                <IndicadorPaqueteActual 
                  variant="detailed" 
                  showProgress={true} 
                  showTime={true}
                />
              </div>
            </div>
          </div>
        )}
      </div>
      
      {/* Contenido principal - ahora con altura dinámica */}
      <div className={`flex flex-row flex-1 gap-4 px-4 overflow-hidden relative transition-all duration-300 ${bottomMenuExpandido ? 'pb-4' : ''}`}>
        {panel === 'camiones' ? (
          <>
            {/* Mapa */}
            <div className={`transition-all duration-300 ${menuExpandido ? "flex-[2]" : "flex-[1]"}`}>
              <div className="bg-white p-4 rounded-xl overflow-auto w-full h-full">
                <Mapa elementoResaltado={elementoResaltado} />
              </div>
            </div>
            {/* Menú derecho */}
            <div className={`transition-all duration-300 ${menuExpandido ? "flex-[1]" : "w-0 overflow-hidden"}`}>
              <RightMenu expanded={menuExpandido} setExpanded={setMenuExpandido} onElementoSeleccionado={setElementoResaltado} />
            </div>
            {/* Botón flotante para mostrar menú si está oculto */}
            {!menuExpandido && (
              <button
                onClick={() => setMenuExpandido(true)}
                className="absolute right-2 top-2 z-10 bg-white rounded-full shadow p-1 hover:bg-gray-100 transition"
                title="Mostrar menú"
              >
                <ChevronLeft size={16} />
              </button>
            )}
          </>
        ) : (
          <div className="w-full flex flex-col items-center justify-start pt-8">
            <div className="text-xl font-bold mb-4">Bloqueos activos</div>
            <BloqueosTable />
          </div>
        )}
        
        {/* Indicador de elemento resaltado - debajo de la leyenda para camiones */}
        {elementoResaltado && panel === 'camiones' && (
          <div className="absolute top-4 left-4 z-20" style={{ marginTop: '280px' }}>
            <div className="bg-amber-100 border border-amber-300 rounded-lg p-3 shadow-lg w-32">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-3 h-3 bg-amber-500 rounded-full animate-pulse"></div>
                <span className="text-xs font-semibold text-amber-800">
                  {elementoResaltado.tipo === 'camion' ? 'Camión' : 
                   elementoResaltado.tipo === 'pedido' ? 'Pedido' : 'Almacén'}
                </span>
              </div>
              <div className="text-xs text-amber-700 mb-2 font-bold">
                {elementoResaltado.id}
              </div>
              <button
                onClick={() => setElementoResaltado(null)}
                className="text-xs bg-amber-500 hover:bg-amber-600 text-white px-2 py-1 rounded w-full"
              >
                Limpiar
              </button>
            </div>
          </div>
        )}
        
        {/* Indicador de elemento resaltado - para panel de bloqueos */}
        {elementoResaltado && panel === 'bloqueos' && (
          <div className="absolute top-20 left-4 z-20">
            <div className="bg-amber-100 border border-amber-300 rounded-lg p-3 shadow-lg">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-3 h-3 bg-amber-500 rounded-full animate-pulse"></div>
                <span className="text-sm font-semibold text-amber-800">
                  {elementoResaltado.tipo === 'camion' ? 'Camión' : 
                   elementoResaltado.tipo === 'pedido' ? 'Pedido' : 'Almacén'} seleccionado
                </span>
              </div>
              <div className="text-sm text-amber-700 mb-2 font-bold">
                {elementoResaltado.id}
              </div>
              <button
                onClick={() => setElementoResaltado(null)}
                className="text-xs bg-amber-500 hover:bg-amber-600 text-white px-2 py-1 rounded w-full"
              >
                Limpiar selección
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Botón flotante para mostrar menú inferior */}
      {!bottomMenuExpandido && panel === 'camiones' && (
        <button
          onClick={() => setBottomMenuExpandido(true)}
          className="absolute bottom-4 right-4 z-20 bg-blue-600 hover:bg-blue-700 text-white rounded-full shadow-lg p-3 transition-all duration-200"
          title="Mostrar ruta del camión"
        >
          <ChevronUp size={20} />
        </button>
      )}

      {/* Menú inferior - ahora empuja el contenido hacia arriba */}
      <div className={`transition-all duration-300 ${bottomMenuExpandido ? 'flex-shrink-0' : 'h-0 overflow-hidden'}`}>
        <BottomMenu expanded={bottomMenuExpandido} setExpanded={setBottomMenuExpandido} camionSeleccionadoExterno={camionSeleccionadoExterno} />
      </div>


    </div>
  );
};

export default SimulacionDiaria;
