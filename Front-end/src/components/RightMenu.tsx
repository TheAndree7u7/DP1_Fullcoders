import React from "react";
import { ChevronRight, Plus } from "lucide-react";
import MetricasRendimiento from "./MetricasRendimiento";
import CardsCamiones from "./CardCamion";
import IndicadoresCamiones from "./IndicadoresCamiones";
import BloqueosTable from "./BloqueosTable";
import TablaPedidos from "./TablaPedidos";
import AgregarPedidosPanel from "./AgregarPedidosPanel";
import DatosCamionesTable from "./rightmenu/DatosCamionesTable";
import CamionesAveriadosTable from "./rightmenu/CamionesAveriadosTable";
import TablaAlmacenes from "./rightmenu/TablaAlmacenes";

interface RightMenuProps {
  expanded: boolean;
  setExpanded: (value: boolean) => void;
  onElementoSeleccionado: (
    elemento: { tipo: "camion" | "pedido" | "almacen"; id: string } | null,
  ) => void;
}

const RightMenu: React.FC<RightMenuProps> = ({
  expanded,
  setExpanded,
  onElementoSeleccionado,
}) => {
  const [panel, setPanel] = React.useState<
    | "camiones"
    | "camionesAveriados"
    | "bloqueos"
    | "metricas"
    | "estadoCamiones"
    | "pedidos"
    | "almacenes"
    | "agregarPedidos"
  >("camiones");

  if (!expanded) return null;

  return (
    <div className="right-menu-container transition-all duration-300 bg-white rounded-xl p-4 shadow-md w-full min-w-80 max-w-full">
      <button
        onClick={() => setExpanded(false)}
        className="self-end mb-2 flex-shrink-0"
        title="Ocultar menú"
      >
        <ChevronRight size={12} />
      </button>

      {/* Botones de alternancia de panel */}
      <div className="flex gap-2 mb-4 flex-wrap flex-shrink-0">
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === "camiones" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800 hover:bg-blue-100"}`}
          onClick={() => setPanel("camiones")}
        >
          Camiones
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === "camionesAveriados" ? "bg-red-500 text-white" : "bg-gray-200 text-gray-800 hover:bg-red-100"}`}
          onClick={() => setPanel("camionesAveriados")}
        >
          Averiar Camion
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === "bloqueos" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800 hover:bg-blue-100"}`}
          onClick={() => setPanel("bloqueos")}
        >
          Bloqueos
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === "metricas" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800 hover:bg-blue-100"}`}
          onClick={() => setPanel("metricas")}
        >
          Métricas
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === "pedidos" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800 hover:bg-blue-100"}`}
          onClick={() => setPanel("pedidos")}
        >
          Pedidos
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === "almacenes" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800 hover:bg-blue-100"}`}
          onClick={() => setPanel("almacenes")}
        >
          Almacenes
        </button>
        <button
          className={`px-3 py-1 rounded font-semibold transition ${panel === "agregarPedidos" ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-800 hover:bg-blue-100"}`}
          onClick={() => setPanel("agregarPedidos")}
        >
          <Plus className="w-4 h-4 inline mr-1" />
          Agregar Pedidos
        </button>
      </div>

      <div className="right-menu-content">
        {panel === "camiones" && (
          <DatosCamionesTable onElementoSeleccionado={onElementoSeleccionado} />
        )}
        {panel === "camionesAveriados" && (
          <CamionesAveriadosTable
            onElementoSeleccionado={onElementoSeleccionado}
          />
        )}
        {panel === "estadoCamiones" && (
          <div className="right-menu-table-container">
            <div className="text-md font-bold text-gray-700 mb-2 flex-shrink-0">
              Estado de los camiones
            </div>
            <div className="right-menu-table-scroll">
              <CardsCamiones />
            </div>
          </div>
        )}
        {panel === "bloqueos" && (
          <div className="right-menu-table-container">
            <div className="text-md font-bold text-gray-700 mb-2 flex-shrink-0">
              Bloqueos activos
            </div>
            <div className="right-menu-table-scroll">
              <BloqueosTable />
            </div>
          </div>
        )}
        {panel === "metricas" && (
          <div className="right-menu-table-container">
            <div className="text-md font-bold text-gray-700 mb-2 flex-shrink-0">
              Métricas de rendimiento
            </div>
            <div className="mb-4 flex-shrink-0">
              <MetricasRendimiento />
            </div>
            <div className="text-xs font-bold text-gray-700 mb-2 flex-shrink-0">
              Indicadores de camiones
            </div>
            <div className="right-menu-table-scroll">
              <IndicadoresCamiones />
            </div>
          </div>
        )}
        {panel === "pedidos" && (
          <TablaPedidos onElementoSeleccionado={onElementoSeleccionado} />
        )}
        {panel === "almacenes" && (
          <TablaAlmacenes onElementoSeleccionado={onElementoSeleccionado} />
        )}
        {panel === "agregarPedidos" && <AgregarPedidosPanel />}
      </div>
    </div>
  );
};

export default RightMenu;
