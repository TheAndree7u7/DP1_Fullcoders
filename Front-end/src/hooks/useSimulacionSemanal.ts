import { useState, useEffect } from 'react';

// Types
export type Panel = 'camiones' | 'bloqueos';
export type ElementoResaltado = {
  tipo: 'camion' | 'pedido' | 'almacen';
  id: string;
} | null;

// Custom hooks
export const useUIState = () => {
  const [menuExpandido, setMenuExpandido] = useState(true);
  const [bottomMenuExpandido, setBottomMenuExpandido] = useState(false);
  const [controlPanelExpandido, setControlPanelExpandido] = useState(false);
  const [panel, setPanel] = useState<Panel>('camiones');

  const toggleControlPanel = () => setControlPanelExpandido(prev => !prev);

  return {
    menuExpandido,
    setMenuExpandido,
    bottomMenuExpandido,
    setBottomMenuExpandido,
    controlPanelExpandido,
    toggleControlPanel,
    panel,
    setPanel
  };
};

export const useElementSelection = () => {
  const [camionSeleccionadoExterno, setCamionSeleccionadoExterno] = useState<string | null>(null);
  const [elementoResaltado, setElementoResaltado] = useState<ElementoResaltado>(null);

  const clearElementoResaltado = () => setElementoResaltado(null);

  return {
    camionSeleccionadoExterno,
    setCamionSeleccionadoExterno,
    elementoResaltado,
    setElementoResaltado,
    clearElementoResaltado
  };
};

export const useSimulationData = () => {
  const [tiempoTranscurrido] = useState(0);
  const [nodoActual] = useState(0);
  const [tiempoSimulado] = useState<Date | null>(null);

  return {
    tiempoTranscurrido,
    nodoActual,
    tiempoSimulado
  };
};

export const usePanelNavigation = (setPanel: (panel: Panel) => void) => {
  useEffect(() => {
    const handlePanelClick = (panelType: Panel) => {
      setPanel(panelType);
    };

    const btnCamiones = document.getElementById('btn-panel-camiones');
    const btnBloqueos = document.getElementById('btn-panel-bloqueos');
    
    if (btnCamiones && btnBloqueos) {
      const camioneHandler = () => handlePanelClick('camiones');
      const bloqueosHandler = () => handlePanelClick('bloqueos');
      
      btnCamiones.addEventListener('click', camioneHandler);
      btnBloqueos.addEventListener('click', bloqueosHandler);
      
      return () => {
        btnCamiones.removeEventListener('click', camioneHandler);
        btnBloqueos.removeEventListener('click', bloqueosHandler);
      };
    }
  }, [setPanel]);
};

export const useCamionRouteModal = (
  setBottomMenuExpandido: (expanded: boolean) => void,
  setCamionSeleccionadoExterno: (id: string | null) => void
) => {
  useEffect(() => {
    const handleMostrarRutaCamion = (event: Event) => {
      const customEvent = event as CustomEvent;
      const camionId = customEvent.detail?.camionId;
      if (camionId) {
        setCamionSeleccionadoExterno(camionId);
        setBottomMenuExpandido(true);
      }
    };

    window.addEventListener('mostrarRutaCamion', handleMostrarRutaCamion);
    return () => {
      window.removeEventListener('mostrarRutaCamion', handleMostrarRutaCamion);
    };
  }, [setBottomMenuExpandido, setCamionSeleccionadoExterno]);
};

export const useBottomMenuSync = (
  bottomMenuExpandido: boolean,
  setCamionSeleccionadoExterno: (id: string | null) => void
) => {
  useEffect(() => {
    if (!bottomMenuExpandido) {
      setCamionSeleccionadoExterno(null);
    }
  }, [bottomMenuExpandido, setCamionSeleccionadoExterno]);
};
