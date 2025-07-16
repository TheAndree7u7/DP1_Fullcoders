import React, { useState, useEffect } from "react";
import Mapa from "../components/Mapa";
import Navbar from "../components/Navbar";
import RightMenu from "../components/RightMenu";
import ControlAnimacion from "../components/ControlAnimacion";
import { useSimulacion } from "../hooks/useSimulacionContext";
import { useMapaElementos } from "../hooks/useMapaContext";
import { useMapa } from "../hooks/useMapaContext";
import { formatearTiempoTranscurrido } from "../context/simulacion/utils/tiempo";
import { useAnimacionControlada } from "../context/animacion/useAnimacionControlada";
import BotonSimulacionUnificado from "../components/BotonSimulacionUnificado";
import PaqueteDisplay from "../components/PaqueteDisplay";
import EstadisticasMapa from "../components/EstadisticasMapa";

const SimulacionDiaria: React.FC = () => {
  const { tiempoTranscurridoSimulado, camiones, rutasCamiones, almacenes, bloqueos } = useSimulacion();
  const elementosMapa = useMapaElementos();
  const { camiones: camionesMapa, actualizarPosicionesCamiones, actualizarDatosSimulacion } = useMapa();
  const { iniciarAnimacion, detenerAnimacion, estaAnimando } = useAnimacionControlada();
  
  const [elementoResaltado, setElementoResaltado] = useState<{tipo: "camion" | "pedido" | "almacen", id: string} | null>(null);

  console.log("🔍 SimulacionDiaria - Datos recibidos:", {
    camiones: camiones.length,
    rutasCamiones: rutasCamiones.length,
    almacenes: almacenes.length,
    bloqueos: bloqueos.length
  });

  console.log("📊 Elementos transformados para mapa:", {
    camiones: elementosMapa.camiones.length,
    pedidos: elementosMapa.pedidos.length,
    almacenes: elementosMapa.almacenes.length,
    bloqueos: elementosMapa.bloqueos.length
  });

  const toggleAnimacion = () => {
    if (estaAnimando()) {
      detenerAnimacion();
    } else {
      iniciarAnimacion(camionesMapa, actualizarPosicionesCamiones);
    }
  };

  useEffect(() => {
    console.log("🚀 useEffect disparado - Verificando condiciones:", {
      hayCamiones: camiones.length > 0,
      hayRutas: rutasCamiones.length > 0,
      totalCamiones: camiones.length,
      totalRutas: rutasCamiones.length
    });

    if (camiones.length > 0 && rutasCamiones.length > 0) {
      const bloqueosFormateados = bloqueos.map(b => ({
        coordenadas: b.coordenadas,
        activo: true,
        fechaInicio: b.fechaInicio,
        fechaFin: b.fechaFin,
      }));
      
      console.log("✅ Llamando actualizarDatosSimulacion con:", {
        camiones: camiones.length,
        rutasCamiones: rutasCamiones.length,
        almacenes: almacenes.length,
        bloqueos: bloqueosFormateados.length
      });
      
      actualizarDatosSimulacion(camiones, rutasCamiones, almacenes, bloqueosFormateados);
    } else {
      console.log("❌ No se cumplen las condiciones para actualizar datos");
    }
  }, [camiones, rutasCamiones, almacenes, bloqueos, actualizarDatosSimulacion]);

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar />
      <div className="bg-[#1E293B] text-white py-2 px-4 flex justify-between items-center">
        <h1 className="font-bold">Ejecución en Tiempo Real - {formatearTiempoTranscurrido(tiempoTranscurridoSimulado.toString())}</h1>
        <div className="flex items-center gap-4">
          <div className="bg-[#334155] rounded-lg px-3 py-1">
            <PaqueteDisplay variant="compact" showHeader={false} className="text-white" />
          </div>
          <div className="bg-[#334155] rounded-lg px-3 py-1">
            <EstadisticasMapa variant="compact" className="text-white" />
          </div>
          <ControlAnimacion 
            estaAnimando={estaAnimando()} 
            onToggle={toggleAnimacion}
            disabled={camionesMapa.length === 0}
          />
          <BotonSimulacionUnificado />
        </div>
      </div>
      
      <div className="flex flex-row flex-1 gap-4 px-4 overflow-hidden relative">
        <div className="flex-[2]">
          <div className="bg-white p-4 rounded-xl overflow-auto w-full h-full">
            <Mapa 
              elementoResaltado={elementoResaltado}
              camiones={elementosMapa.camiones}
              pedidos={elementosMapa.pedidos}
              almacenes={elementosMapa.almacenes}
              bloqueos={elementosMapa.bloqueos}
            />
          </div>
        </div>
        <div className="flex-[1]">
          <RightMenu expanded={true} setExpanded={() => {}} onElementoSeleccionado={setElementoResaltado} />
        </div>
      </div>
    </div>
  );
};

export default SimulacionDiaria;
