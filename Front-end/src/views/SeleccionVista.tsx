import React from "react";
import { useNavigate } from "react-router-dom";
import semanalImg from "../assets/semanal.svg";
import diarioImg from "../assets/diario.svg";
import colapsoImg from "../assets/colapso.svg";
import logo from "../assets/logo.png";

const SeleccionVista: React.FC = () => {
  const navigate = useNavigate();

  const opciones = [
    {
      id: "tiempo-real",
      titulo: "Ejecución en Tiempo Real",
      descripcion: "Simulación diaria que muestra la operación logística en tiempo real con actualizaciones continuas",
      ruta: "/ejecucion-tiempo-real",
      imagen: diarioImg,
      color: "from-blue-500 to-blue-600",
      icono: "⚡"
    },
    {
      id: "semanal",
      titulo: "Simulación Semanal",
      descripcion: "Vista semanal completa del sistema logístico con análisis de rendimiento y métricas",
      ruta: "/simulacion-semanal",
      imagen: semanalImg,
      color: "from-green-500 to-green-600",
      icono: "📊"
    },
    {
      id: "colapso",
      titulo: "Colapso Logístico",
      descripcion: "Simulación de escenarios de colapso para análisis de contingencia y planificación",
      ruta: "/colapso-logistico",
      imagen: colapsoImg,
      color: "from-red-500 to-red-600",
      icono: "🚨"
    }
  ];

  const handleSeleccionVista = async (opcion: typeof opciones[0]) => {
    // Para todas las vistas, navegar directamente
    navigate(opcion.ruta);
  };

  return (
    <div className="bg-gradient-to-br from-gray-50 to-gray-100 w-screen h-screen flex flex-col">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center">
              <img src={logo} alt="Logo" className="h-8 w-auto mr-3" />
              <h1 className="text-2xl font-bold text-gray-900">Sistema de Simulación Logística</h1>
            </div>
            <div className="text-sm text-gray-500">
              Versión 2.0
            </div>
          </div>
        </div>
      </div>

      {/* Contenido principal */}
      <div className="flex-1 flex flex-col justify-center items-center p-8">
        <div className="text-center mb-12">
          <h2 className="text-4xl font-bold text-gray-900 mb-4">
            Bienvenido al Sistema de Simulación
          </h2>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Selecciona el tipo de simulación que deseas ejecutar para analizar diferentes escenarios logísticos
          </p>
        </div>

        {/* Opciones de simulación */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl w-full">
          {opciones.map((opcion) => (
            <div
              key={opcion.id}
              onClick={() => handleSeleccionVista(opcion)}
              className="group cursor-pointer transform transition-all duration-300 hover:scale-105 hover:shadow-2xl"
            >
              <div className="bg-white rounded-2xl shadow-lg overflow-hidden border border-gray-200 hover:border-gray-300 transition-all duration-300">
                {/* Header de la tarjeta */}
                <div className={`bg-gradient-to-r ${opcion.color} p-6 text-white`}>
                  <div className="flex items-center justify-between">
                    <span className="text-3xl">{opcion.icono}</span>
                    <div className="text-right">
                      <h3 className="text-xl font-bold">{opcion.titulo}</h3>
                    </div>
                  </div>
                </div>

                {/* Imagen */}
                <div className="p-6 bg-gray-50">
                  <img
                    src={opcion.imagen}
                    alt={opcion.titulo}
                    className="w-full h-48 object-contain rounded-lg group-hover:opacity-80 transition-opacity duration-300"
                  />
                </div>

                {/* Descripción */}
                <div className="p-6">
                  <p className="text-gray-600 text-sm leading-relaxed mb-4">
                    {opcion.descripcion}
                  </p>
                  
                  {/* Botón de acción */}
                  <button 
                    className={`w-full bg-gradient-to-r ${opcion.color} text-white py-3 px-6 rounded-lg font-semibold text-sm transition-all duration-300 transform group-hover:translate-y-[-2px] group-hover:shadow-lg`}
                  >
                    Iniciar Simulación
                  </button>
                </div>

                {/* Indicador de hover */}
                <div className={`h-1 bg-gradient-to-r ${opcion.color} transform scale-x-0 group-hover:scale-x-100 transition-transform duration-300`}></div>
              </div>
            </div>
          ))}
        </div>

        {/* Información adicional */}
        <div className="mt-12 text-center">
          <div className="bg-white rounded-lg shadow-md p-6 max-w-4xl mx-auto">
            <h3 className="text-lg font-semibold text-gray-900 mb-3">
              Información del Sistema
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-sm text-gray-600">
              <div className="flex items-center justify-center">
                <span className="mr-2">📈</span>
                <span>Monitoreo en tiempo real</span>
              </div>
              <div className="flex items-center justify-center">
                <span className="mr-2">🗺️</span>
                <span>Visualización de rutas</span>
              </div>
              <div className="flex items-center justify-center">
                <span className="mr-2">📊</span>
                <span>Métricas de rendimiento</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="bg-white border-t border-gray-200 py-4">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center text-sm text-gray-500">
            © 2025 Sistema de Simulación Logística - Desarrollado para DP1 Fullcoders
          </div>
        </div>
      </div>
    </div>
  );
};

export default SeleccionVista;
