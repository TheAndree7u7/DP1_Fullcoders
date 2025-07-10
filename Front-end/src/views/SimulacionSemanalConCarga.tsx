import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { EstadoCargaArchivos } from "../types";
import CargaArchivosSimulacion from "../components/CargaArchivosSimulacion";
import SimulacionSemanal from "./SimulacionSemanal";
import { cargaArchivosService } from "../services/cargaArchivosService";
import { toast } from "react-toastify";

const SimulacionSemanalConCarga: React.FC = () => {
  const [paso, setPaso] = useState<'carga' | 'simulacion'>('carga');
  const [estadoCarga, setEstadoCarga] = useState<EstadoCargaArchivos>({
    ventas: { cargado: false, errores: [] },
    bloqueos: { cargado: false, errores: [] },
    camiones: { cargado: false, errores: [] }
  });
  const [cargando, setCargando] = useState(false);
  const navigate = useNavigate();

  const manejarArchivosCargados = (nuevoEstado: EstadoCargaArchivos) => {
    setEstadoCarga(nuevoEstado);
  };

  const manejarContinuar = async () => {
    setCargando(true);
    
    try {
      // Cargar archivos al backend
      const resultado = await cargaArchivosService.cargarTodosLosArchivos(estadoCarga);
      
      if (resultado.success) {
        toast.success('Archivos cargados exitosamente. Iniciando simulación...');
        setPaso('simulacion');
      } else {
        toast.error(`Error al cargar archivos: ${resultado.message}`);
        if (resultado.errores) {
          resultado.errores.forEach(error => {
            toast.error(error);
          });
        }
      }
    } catch (error) {
      toast.error('Error inesperado al cargar archivos');
      console.error('Error al cargar archivos:', error);
    } finally {
      setCargando(false);
    }
  };

  const manejarVolver = () => {
    navigate("/");
  };

  if (paso === 'simulacion') {
    return <SimulacionSemanal />;
  }

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center">
              <button
                onClick={manejarVolver}
                className="mr-4 text-gray-600 hover:text-gray-900 transition-colors"
              >
                ⬅️ Volver
              </button>
              <h1 className="text-2xl font-bold text-gray-900">
                Simulación Semanal - Carga de Datos
              </h1>
            </div>
            <div className="text-sm text-gray-500">
              Paso 1 de 2: Carga de Archivos
            </div>
          </div>
        </div>
      </div>

      {/* Contenido */}
      <div className="flex-1 overflow-auto">
        <CargaArchivosSimulacion
          onArchivosCargados={manejarArchivosCargados}
          onContinuar={manejarContinuar}
        />
      </div>

      {/* Overlay de carga */}
      {cargando && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 flex flex-col items-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
            <p className="text-gray-700">Cargando archivos al servidor...</p>
            <p className="text-sm text-gray-500 mt-2">Por favor espera</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default SimulacionSemanalConCarga; 