import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import MapaTiempoReal from "../components/MapaTiempoReal";
import RightMenu from "../components/RightMenu";
import IndicadoresCamiones from "../components/IndicadoresCamiones";
import MetricasRendimiento from "../components/MetricasRendimiento";
import { ChevronLeft } from "lucide-react";

const TiempoReal: React.FC = () => {
  const [menuExpandido, setMenuExpandido] = useState(true);
  // Removed modal state as per user request
  // const [modalOpen, setModalOpen] = useState(false);
  const navigate = useNavigate();

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar title="Tiempo Real" />
      {/* Removed Registrar Avería button as per user request */}
      {/* <button
        onClick={() => setModalOpen(true)}
        className="absolute right-4 top-4 z-20 bg-green-600 text-white rounded-md px-4 py-2 hover:bg-green-700 transition"
        title="Registrar Avería"
      >
        Registrar Avería
      </button> */}
      <div className="flex flex-row flex-1 gap-4 px-4 pb-4 overflow-hidden relative">
        {/* Mapa */}
        <div className={`transition-all duration-300 relative ${menuExpandido ? "flex-[2]" : "flex-[1]"}`}>
          <button
            onClick={() => navigate("/")}
            className="absolute left-4 top-4 z-20 bg-white rounded-full shadow p-2 hover:bg-gray-100 transition"
            title="Volver"
          >
            <ChevronLeft size={24} />
          </button>
          <div className="bg-white p-4 rounded-xl overflow-auto w-full h-full relative">
            <MapaTiempoReal />
            <div className="absolute bottom-4 left-4">
              <MetricasRendimiento />
            </div>
            <div className="absolute bottom-4 right-4">
              <IndicadoresCamiones />
            </div>
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

export default TiempoReal;
