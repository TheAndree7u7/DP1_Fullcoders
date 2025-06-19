import React, { useState } from "react";
import Mapa from "../components/Mapa";
import Navbar from "../components/Navbar";
import RightMenu from "../components/RightMenu";
import { ChevronLeft } from "lucide-react";
import { useSimulacion } from "../context/SimulacionContext";

const SimulacionSemanal: React.FC = () => {
  const [menuExpandido, setMenuExpandido] = useState(true);
  const { diaSimulacion, fechaHoraSimulacion } = useSimulacion();

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar />
      <div className="bg-[#1E293B] text-white py-2 px-4 flex justify-between items-center">
        <h1 className="font-bold">Ejecución Semanal {diaSimulacion && `- Día ${diaSimulacion}`}</h1>
        {fechaHoraSimulacion && (
          <div className="text-sm">
            <span className="mr-2">Fecha de la simulación:</span>
            <span className="font-bold text-blue-300">
              {new Date(fechaHoraSimulacion).toLocaleString('es-ES')}
            </span>
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
            className="absolute right-2 top-2 z-10 bg-white rounded-full shadow p-1 hover:bg-gray-100 transition"
            title="Mostrar menú"
          >
            <ChevronLeft size={16} />
          </button>
        )}
      </div>
    </div>
  );
};

export default SimulacionSemanal;
