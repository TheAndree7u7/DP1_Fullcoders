import React from "react";
import Navbar from "../components/Navbar";
import MapaTiempoReal from "../components/MapaTiempoReal";

const TiempoReal: React.FC = () => {

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar title="Tiempo Real" />
      <div className="flex flex-row flex-1 gap-4 px-4 pb-4 overflow-hidden relative">
        {/* Mapa */}
        <div className="bg-white p-4 rounded-xl overflow-auto w-full h-full">
          <MapaTiempoReal />
        </div>
      </div>

    </div>
  );
};

export default TiempoReal;
