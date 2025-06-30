import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Mapa from "../components/Mapa";
import Navbar from "../components/Navbar";
import RightMenu from "../components/RightMenu";
import { ChevronLeft, ArrowLeft } from "lucide-react";
import { useSimulacion } from "../context/SimulacionContext";

// Constante que define cuánto tiempo (en segundos) representa cada nodo en la simulación
const SEGUNDOS_POR_NODO = 36;

const SimulacionSemanal: React.FC = () => {
  const navigate = useNavigate();
  const [menuExpandido, setMenuExpandido] = useState(true);
  const { diaSimulacion, fechaHoraSimulacion, horaActual } = useSimulacion();
  const [tiempoSimulado, setTiempoSimulado] = useState<Date | null>(null);

  const handleVolverInicio = () => {
    navigate('/');
  };

  // Constante que indica cada cuántas horas se reciben datos del backend
  const HORAS_POR_ACTUALIZACION = 2;
  
  // Estado para guardar el tiempo en la simulación (actualizado por nodos)
  const [tiempoReal, setTiempoReal] = useState<Date | null>(null);
  
  // Actualizar la hora simulada solo cuando cambia la fecha del backend
  useEffect(() => {
    if (fechaHoraSimulacion) {
      // Solo actualizamos cuando recibimos datos nuevos del backend
      setTiempoSimulado(new Date(fechaHoraSimulacion));
    }
  }, [fechaHoraSimulacion]);
  
  // Actualizar la hora en tiempo real basado en los nodos actuales
  useEffect(() => {
    if (fechaHoraSimulacion && horaActual >= 0) {
      const fechaBase = new Date(fechaHoraSimulacion);
      
      // Número total de nodos para una actualización completa (cada 4 horas)
      const NODOS_POR_ACTUALIZACION = 100;
      
      // Calculamos qué nodo estamos dentro del ciclo actual (0-99)
      const nodoEnCicloActual = horaActual % NODOS_POR_ACTUALIZACION;
      
      // Calculamos el avance por nodo (segundos totales de 4 horas divididos por nodos totales)
      const segundosPorNodo = (HORAS_POR_ACTUALIZACION * 60 * 60) / NODOS_POR_ACTUALIZACION; // 4 horas / 100 nodos
      
      // Calculamos segundos adicionales solo para el incremento local dentro del ciclo actual
      const segundosAdicionales = nodoEnCicloActual * segundosPorNodo;
      
      // Crea nueva fecha sumando los segundos (no debe pasarse del próximo intervalo de 4 horas)
      const nuevaFecha = new Date(fechaBase.getTime() + segundosAdicionales * 1000);
      setTiempoReal(nuevaFecha);
    }
  }, [horaActual, fechaHoraSimulacion]);

  // Formato para la fecha y hora de simulación (del backend)
  const fechaSimulada = tiempoSimulado ? 
    tiempoSimulado.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: 'numeric',
      year: 'numeric'
    }) + ' ' + 
    tiempoSimulado.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }) : '';
    
  // Formato para la hora en tiempo real (actualizada por nodos)
  const horaRealSimulada = tiempoReal ? 
    tiempoReal.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }) : '';

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar />
      <div className="bg-[#1E293B] text-white py-2 px-4 flex justify-between items-center">
        <div className="flex items-center gap-3">
          <button
            onClick={handleVolverInicio}
            className="flex items-center gap-2 px-3 py-1.5 bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors duration-200 text-sm font-medium"
            title="Volver al inicio"
          >
            <ArrowLeft size={16} />
            Volver al inicio
          </button>
          <h1 className="font-bold">Ejecución Semanal {diaSimulacion && `- Día ${diaSimulacion}`}</h1>
        </div>
        {tiempoSimulado && (
          <div className="text-sm flex items-center gap-4">
            <div>
              <span className="mr-2">Fecha de la simulación:</span>
              <span className="font-bold text-blue-300">{fechaSimulada}</span>
            </div>
            <div>
              <span className="mr-2">Hora en tiempo real:</span>
              <span className="font-bold text-blue-300">{horaRealSimulada}</span>
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
      <div className="flex flex-row flex-1 gap-4 px-4 pb-4 overflow-hidden relative">
        {/* Mapa */}
        <div className={`transition-all duration-300 ${menuExpandido ? "flex-[2]" : "flex-[1]"}`}>
          <div className="bg-white p-4 rounded-xl overflow-auto w-full h-full">
            <Mapa />
          </div>
        </div>

        {/* Menú derecho */}
        <div className={`transition-all duration-300 ${menuExpandido ? "flex-[1]" : "w-0 overflow-hidden"}`}>
          <RightMenu expanded={menuExpandido} setExpanded={setMenuExpandido} />
        </div>

        {/* Botón flotante para mostrar menú si está oculto */}
        {!menuExpandido && (
          <button
            onClick={() => setMenuExpandido(true)}
            className="absolute right-4 top-4 z-20 bg-red-500 hover:bg-red-600 text-white rounded-full shadow-lg p-3 transition-all duration-200 transform hover:scale-110 border-2 border-white"
            title="Mostrar menú lateral"
          >
            <ChevronLeft size={20} />
          </button>
        )}
      </div>
    </div>
  );
};

export default SimulacionSemanal;
