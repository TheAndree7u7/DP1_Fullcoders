import React from "react";
import Mapa from "../components/Mapa";
import Navbar from "../components/Navbar";
import BloqueosTable from "../components/BloqueosTable";
import RightMenu from "../components/RightMenu";
import BottomMenu from "../components/BottomMenu";
import BotonSimulacionUnificado from "../components/BotonSimulacionUnificado";
import { ChevronLeft, ChevronUp } from "lucide-react";
import { 
  SimulationHeader, 
  ElementHighlightIndicator 
} from "../components/SimulacionSemanal";
import ErrorBoundary from "../components/common/ErrorBoundary";
import {
  useUIState,
  useElementSelection,
  useSimulationData,
  usePanelNavigation,
  useCamionRouteModal,
  useBottomMenuSync
} from "../hooks/useSimulacionSemanal";

const SimulacionSemanal: React.FC = () => {
  // Custom hooks for better organization
  const uiState = useUIState();
  const elementSelection = useElementSelection();
  const simulationData = useSimulationData();

  // Effect hooks for side effects
  usePanelNavigation(uiState.setPanel);
  useCamionRouteModal(uiState.setBottomMenuExpandido, elementSelection.setCamionSeleccionadoExterno);
  useBottomMenuSync(uiState.bottomMenuExpandido, elementSelection.setCamionSeleccionadoExterno);

  return (
    <ErrorBoundary>
      <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
        <Navbar />
        <SimulationHeader 
          tiempoTranscurrido={simulationData.tiempoTranscurrido}
          nodoActual={simulationData.nodoActual}
          tiempoSimulado={simulationData.tiempoSimulado}
          botonControl={<BotonSimulacionUnificado />}
        />
        
        {/* Contenido principal - ahora con altura dinámica */}
        <div className={`flex flex-row flex-1 gap-4 px-4 overflow-hidden relative transition-all duration-300 ${uiState.bottomMenuExpandido ? 'pb-4' : ''}`}>
          {uiState.panel === 'camiones' ? (
            <>
              {/* Mapa */}
              <div className={`transition-all duration-300 ${uiState.menuExpandido ? "flex-[2]" : "flex-[1]"}`}>
                <div className="bg-white p-4 rounded-xl overflow-auto w-full h-full">
                  <Mapa 
                    elementoResaltado={elementSelection.elementoResaltado}
                    camiones={[]}
                    pedidos={[]}
                    almacenes={[]}
                    bloqueos={[]}
                  />
                </div>
              </div>
              {/* Menú derecho */}
              <div className={`transition-all duration-300 ${uiState.menuExpandido ? "flex-[1]" : "w-0 overflow-hidden"}`}>
                <RightMenu 
                  expanded={uiState.menuExpandido} 
                  setExpanded={uiState.setMenuExpandido} 
                  onElementoSeleccionado={elementSelection.setElementoResaltado} 
                />
              </div>
              {/* Botón flotante para mostrar menú si está oculto */}
              {!uiState.menuExpandido && (
                <button
                  onClick={() => uiState.setMenuExpandido(true)}
                  className="absolute right-2 top-2 z-10 bg-white rounded-full shadow p-1 hover:bg-gray-100 transition"
                  title="Mostrar menú"
                  aria-label="Mostrar menú lateral"
                >
                  <ChevronLeft size={16} />
                </button>
              )}
            </>
          ) : (
            <div className="w-full flex flex-col items-center justify-start pt-8">
              <div className="text-xl font-bold mb-4">Bloqueos activos</div>
              <BloqueosTable />
            </div>
          )}
          
          {/* Indicador de elemento resaltado */}
          <ElementHighlightIndicator 
            elemento={elementSelection.elementoResaltado}
            panel={uiState.panel}
            onClear={elementSelection.clearElementoResaltado}
          />
        </div>

        {/* Botón flotante para mostrar menú inferior */}
        {!uiState.bottomMenuExpandido && uiState.panel === 'camiones' && (
          <button
            onClick={() => uiState.setBottomMenuExpandido(true)}
            className="absolute bottom-4 right-4 z-20 bg-blue-600 hover:bg-blue-700 text-white rounded-full shadow-lg p-3 transition-all duration-200"
            title="Mostrar ruta del camión"
            aria-label="Mostrar panel inferior con rutas"
          >
            <ChevronUp size={20} />
          </button>
        )}

        {/* Menú inferior - ahora empuja el contenido hacia arriba */}
        <div className={`transition-all duration-300 ${uiState.bottomMenuExpandido ? 'flex-shrink-0' : 'h-0 overflow-hidden'}`}>
          <BottomMenu 
            expanded={uiState.bottomMenuExpandido} 
            setExpanded={uiState.setBottomMenuExpandido} 
            camionSeleccionadoExterno={elementSelection.camionSeleccionadoExterno} 
          />
        </div>
      </div>
    </ErrorBoundary>
  );
};

export default SimulacionSemanal;
