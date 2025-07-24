
import { useState, useRef, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import logo from "../assets/logo.png";
import { useSimulacion,  } from "../context/SimulacionContext";
import { formatearTiempoTranscurrido } from "../context/simulacion/utils/tiempo";
import IndicadorGLPTotal from "./IndicadorGLPTotal";

const Navbar: React.FC = () => {
  const [, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const { tiempoTranscurridoSimulado, tiempoRealSimulacion } = useSimulacion();
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // Función para obtener el título según la ruta actual
  const getTituloVista = () => {
    switch (location.pathname) {
      case "/simulacion-semanal":
        return "Simulación Semanal";
      case "/colapso-logistico":
        return "Colapso Logístico";
      case "/ejecucion-tiempo-real":
        return "Ejecución en Tiempo Real";
      default:
        return "Sistema de Simulación";
    }
  };

  // Solo mostrar la navbar si no estamos en la pantalla de selección
  if (location.pathname === "/") {
    return null;
  }

  return (
    <nav className="bg-white p-4 max-h-[48px] flex items-center justify-between fixed top-0 left-0 right-0 z-50 shadow-sm border-b border-gray-200" >
      <div className="flex items-center space-x-8">
        <img src={logo} alt="logo" className="w-[24px] h-[24px]" />
        <div className="font-bold text-[14px] text-[#1890FF]">GLPSoft</div>
        <div className="text-black font-bold text-xl">
          {getTituloVista()} - {formatearTiempoTranscurrido(tiempoTranscurridoSimulado)}
        </div>
      </div>
      
      <div className="flex items-center space-x-4">
        {/* Botón de retroceso */}
        <button
          onClick={() => navigate(-1)}
          className="bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1 rounded-md text-sm font-medium transition-colors flex items-center space-x-1"
          title="Volver atrás"
        >
          <span>⬅️</span>
          <span>Atrás</span>
        </button>

        {/* Botón para volver a la selección de vista */}
        <button
          onClick={() => navigate("/")}
          className="bg-blue-100 hover:bg-blue-200 text-blue-700 px-3 py-1 rounded-md text-sm font-medium transition-colors flex items-center space-x-1"
          title="Ir al inicio"
        >
          <span>🏠</span>
          <span>Inicio</span>
        </button>
        
        <div className="flex items-center space-x-2">
          <div className="text-gray-600 text-sm">⏱️ Duracion de la simulacion:</div>
          <div className="font-mono font-bold text-[#1890FF] text-lg bg-gray-100 px-3 py-1 rounded">
            {tiempoRealSimulacion}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
