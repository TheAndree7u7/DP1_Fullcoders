import React, { useState } from "react";
import SimulacionSemanal from "./SimulacionSemanal";
import TiempoReal from "./TiempoReal";
import Navbar from "../components/Navbar";

const SimulacionTabs: React.FC = () => {
  const [activeTab, setActiveTab] = useState<"semanal" | "tiempoReal">("semanal");

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-12">
      <Navbar title={activeTab === "semanal" ? "Simulación Semanal" : "Tiempo Real"} />
      <div className="flex flex-col flex-1 h-[calc(100vh-48px)]">
        <div className="flex border-b border-gray-300">
          <button
            className={`px-4 py-2 font-semibold ${
              activeTab === "semanal" ? "border-b-2 border-blue-600 text-blue-600" : "text-gray-600"
            }`}
            onClick={() => setActiveTab("semanal")}
          >
            Simulación Semanal
          </button>
          <button
            className={`px-4 py-2 font-semibold ${
              activeTab === "tiempoReal" ? "border-b-2 border-blue-600 text-blue-600" : "text-gray-600"
            }`}
            onClick={() => setActiveTab("tiempoReal")}
          >
            Tiempo Real
          </button>
        </div>
        <div className="flex-1 overflow-auto h-full">
          {activeTab === "semanal" ? <SimulacionSemanal /> : <TiempoReal />}
        </div>
      </div>
    </div>
  );
};

export default SimulacionTabs;
