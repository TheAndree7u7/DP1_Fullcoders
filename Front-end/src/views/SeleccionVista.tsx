import React from "react";
import { useNavigate } from "react-router-dom";
import semanalImg from "../assets/semanal.svg";
import diarioImg from "../assets/diario.svg";
import colapsoImg from "../assets/colapso.svg";

const SeleccionVista: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col">
      <div className="flex flex-col flex-1 p-8 justify-center items-center">
        <h2 className="text-lg font-semibold mb-4">Bienvenido al Sistema</h2>
        <p className="mb-8">Seleccione escenario</p>
        <div className="flex gap-6">
          <div
            onClick={() => navigate("/ejecucion-tiempo-real")}
            className="cursor-pointer bg-white rounded-lg shadow-md p-4 flex flex-col items-center w-64 hover:shadow-lg transition"
          >
            <button className="bg-blue-600 text-white px-4 py-2 rounded mb-4 w-full text-center font-semibold">
              Ejecución en tiempo Real
            </button>
            <img
              src={diarioImg}
              alt="Ejecución en tiempo Real"
              className="w-auto h-32 object-cover rounded"
            />
          </div>
          <div
            onClick={() => navigate("/simulacion-semanal")}
            className="cursor-pointer bg-white rounded-lg shadow-md p-4 flex flex-col items-center w-64 hover:shadow-lg transition"
          >
            <button className="bg-blue-600 text-white px-4 py-2 rounded mb-4 w-full text-center font-semibold">
              Simulación semanal
            </button>
            <img
              src={semanalImg}
              alt="Simulación semanal"
              className="w-auto h-32 object-cover rounded"
            />
          </div>
          <div
            onClick={() => navigate("/colapso-logistico")}
            className="cursor-pointer bg-white rounded-lg shadow-md p-4 flex flex-col items-center w-64 hover:shadow-lg transition"
          >
            <button className="bg-blue-600 text-white px-4 py-2 rounded mb-4 w-full text-center font-semibold">
              Colapso Logístico
            </button>
            <img
              src={colapsoImg}
              alt="Colapso Logístico"
              className="w-auto h-32 object-cover rounded"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default SeleccionVista;
