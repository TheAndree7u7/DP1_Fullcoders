import React, { useState, useEffect, useRef } from "react";
import Mapa from "../components/Mapa";
import Navbar from "../components/Navbar";
import BloqueosTable from "../components/BloqueosTable";
import RightMenu from "../components/RightMenu";
import BottomMenu from "../components/BottomMenu";
import { ChevronLeft } from "lucide-react";
import { useSimulacion } from "../context/SimulacionContext";
import { formatearTiempoTranscurrido } from "../context/simulacion/utils/tiempo";
import { useCurrentDateTime } from "../hooks/useCurrentDateTime";
 

const SimulacionSemanal: React.FC = () => {
  const [menuExpandido, setMenuExpandido] = useState(true);
  const [bottomMenuExpandido, setBottomMenuExpandido] = useState(false);
  const [mapaWidth, setMapaWidth] = useState(66); // Porcentaje inicial del mapa (66% = flex-[2])
  const [isResizing, setIsResizing] = useState(false);
  const resizeRef = useRef<HTMLDivElement>(null);
  
  const { 
    fechaHoraSimulacion, 
    tiempoTranscurridoSimulado, 
    fechaHoraAcumulada,
    camiones,
    rutasCamiones
  } = useSimulacion();
  const currentDateTime = useCurrentDateTime();
  const [tiempoSimulado, setTiempoSimulado] = useState<Date | null>(null);
  // Estado para alternar paneles
  const [panel, setPanel] = useState<'camiones' | 'bloqueos'>('camiones');
  // Estado para el camión seleccionado desde el modal del mapa
  const [camionSeleccionadoExterno, setCamionSeleccionadoExterno] = useState<string | null>(null);
  // Estado para resaltar elementos en el mapa
  const [elementoResaltado, setElementoResaltado] = useState<{tipo: 'camion' | 'pedido' | 'almacen', id: string} | null>(null);
  // Estado para el panel de control


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

  // Event handlers para el resize
  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isResizing) return;
      
      const container = document.querySelector('.resize-container');
      if (!container) return;
      
      const containerRect = container.getBoundingClientRect();
      const newMapaWidth = ((e.clientX - containerRect.left) / containerRect.width) * 100;
      
      // Limitar el tamaño del mapa entre 30% y 80%
      if (newMapaWidth >= 30 && newMapaWidth <= 80) {
        setMapaWidth(newMapaWidth);
      }
    };

    const handleMouseUp = () => {
      setIsResizing(false);
    };

    if (isResizing) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };
  }, [isResizing]);

  // Calcular capacidad total y disponible
  const capacidadTotal = camiones.reduce((acc, c) => acc + (c.capacidadMaximaGLP || 0), 0);
  const capacidadDisponible = camiones.reduce((acc, c) => acc + (c.capacidadActualGLP || 0), 0);
  const porcentajeDisponible = capacidadTotal > 0 ? capacidadDisponible / capacidadTotal : 0;
  
  // Calcular GLP en uso (suma del GLP de pedidos PENDIENTE / capacidad total)
  const pedidosAsignados = rutasCamiones.flatMap(r => r.pedidos);
  const pedidosPendientes = pedidosAsignados.filter(p => p.estado === 'PENDIENTE');
  const glpEnUso = pedidosPendientes.reduce((acc, p) => acc + (p.volumenGLPAsignado || 0), 0);
  const porcentajeGLPEnUso = capacidadTotal > 0 ? glpEnUso / capacidadTotal : 0;
  const getColorPorcentaje = (porcentaje: number) => {
    if (porcentaje >= 0.7) return '#22c55e'; // verde
    if (porcentaje >= 0.4) return '#eab308'; // amarillo
    return '#f97316'; // naranja
  };

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar />
      <div className="bg-[#10093B] text-white py-2 px-4 flex justify-between items-center">
        <h1 className="font-bold">Ejecución Semanal - {formatearTiempoTranscurrido(tiempoTranscurridoSimulado)}</h1>
        <div className="flex items-center gap-4">
          {tiempoSimulado && (
            <div className="text-sm flex items-center gap-4">
              <div>
                <span className="mr-2">Fecha y hora de la simulacion:</span>
                <span className="font-bold text-blue-300">{fechaHoraAcumulada}</span>
              </div> 
              <div className="flex items-center gap-2">
                <span className="mr-2">Hora y fecha Actual:</span>
                <span className="font-bold text-blue-300">{currentDateTime.toLocaleString('es-ES', {
                  day: '2-digit',
                  month: '2-digit',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                  second: '2-digit'
                })}</span>
                {/* Badges GLP */}
                <span style={{
                  background: getColorPorcentaje(porcentajeGLPEnUso),
                  color: 'white',
                  borderRadius: 8,
                  padding: '4px 10px',
                  fontWeight: 'bold',
                  minWidth: 80,
                  textAlign: 'center',
                  fontSize: 13
                }}>
                  Estado GLP USO: {(porcentajeGLPEnUso * 100).toFixed(0)}%
                </span>
                <span style={{
                  background: getColorPorcentaje(porcentajeDisponible),
                  color: 'white',
                  borderRadius: 8,
                  padding: '4px 10px',
                  fontWeight: 'bold',
                  minWidth: 80,
                  textAlign: 'center',
                  fontSize: 13
                }}>
                  Estado GLP Disponible: {(porcentajeDisponible * 100).toFixed(0)}%
                </span>
              </div>
            </div>
          )}
        </div>
      </div>
      
      
      
      
      {/* Contenido principal - ahora con altura dinámica */}
      <div className={`resize-container flex flex-row flex-1 px-4 overflow-hidden relative transition-all duration-300 ${bottomMenuExpandido ? 'pb-4' : ''}`}>
        {panel === 'camiones' ? (
          <>
            {/* Mapa */}
            <div 
              className={`transition-all duration-300 ${menuExpandido ? "" : "w-full"}`}
              style={menuExpandido ? { width: `${mapaWidth}%` } : {}}
            >
              <div className="bg-white p-4 rounded-xl overflow-auto w-full h-full">
                <Mapa elementoResaltado={elementoResaltado} onElementoSeleccionado={setElementoResaltado} />
              </div>
            </div>
            
            {/* Separador movible */}
            {menuExpandido && (
              <div
                ref={resizeRef}
                className="w-1 bg-gray-300 hover:bg-blue-500 cursor-col-resize transition-colors relative"
                onMouseDown={() => setIsResizing(true)}
                style={{ minWidth: '4px' }}
              >
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="w-1 h-8 bg-gray-400 rounded-full"></div>
                </div>
              </div>
            )}
            
            {/* Menú derecho - que desplaza el mapa */}
            <div 
              className={`transition-all duration-300 ${menuExpandido ? "" : "w-0 overflow-hidden"}`}
              style={menuExpandido ? { width: `${100 - mapaWidth}%` } : {}}
            >
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
      
      {/* Menú inferior */}
      <BottomMenu 
        expanded={bottomMenuExpandido} 
        setExpanded={setBottomMenuExpandido}
        camionSeleccionadoExterno={camionSeleccionadoExterno}
      />
    </div>
  );
};

export default SimulacionSemanal;
