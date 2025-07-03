
import { useState, useRef, useEffect } from "react";
import logo from "../assets/logo.png";
import { useSimulacion } from "../context/SimulacionContext";


const Navbar: React.FC = () => {
  const [, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const { tiempoRealSimulacion, diaSimulacion, horaSimulacion } = useSimulacion();

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


  return (
    <nav className="bg-white p-4 max-h-[48px] flex items-center justify-between fixed top-0 left-0 right-0 z-50" >
      <div className="flex items-center space-x-8">
        <img src={logo} alt="logo" className="w-[24px] h-[24p]" />
        <div className="font-bold text-[14px] text-[#1890FF]">GLPSoft</div>
        <div className="text-black font-bold text-xl">
          Ejecución Semanal {diaSimulacion && `- Día ${diaSimulacion}`} {horaSimulacion && `(${horaSimulacion})`}
        </div>
      </div>
      
      <div className="flex items-center space-x-4">
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
