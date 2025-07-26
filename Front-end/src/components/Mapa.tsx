import { useEffect, useRef, useState } from "react";
import { useSimulacion } from "../context/SimulacionContext";
import type { Coordenada } from "../types";
import clienteIcon from "../assets/cliente.svg";
import { CAMION_COLORS, ESTADO_COLORS } from "../config/colors";
import { ChevronDown, ChevronUp } from "lucide-react";
import {
  parseCoord,
  calcularRotacion,
  getPedidosPendientes,
  handleAveriar,
  colorSemaforoGLP, // <-- importar la función
  obtenerPedidosAsignadosAlAlmacen,
  obtenerCamionesAsignadosAlAlmacen,
  formatearFecha,
} from "./mapa/utils";
import type { Pedido } from "../types";

const RETRASO_CONSUMO_RUTA = 0;

// Definir el tipo localmente para evitar problemas de importación
interface PedidoConAsignacion extends Pedido {
  esNoAsignado: boolean;
  estadoPedido: string; // 'NO_ASIGNADO', 'PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'RETRASO'
}

// Función de validación de coordenadas definida localmente para evitar problemas de importación
const esCoordenadaValida = (
  coord: Coordenada | undefined | null
): coord is Coordenada => {
  return (
    coord !== null &&
    coord !== undefined &&
    typeof coord === "object" &&
    typeof coord.x === "number" &&
    typeof coord.y === "number" &&
    !isNaN(coord.x) &&
    !isNaN(coord.y)
  );
};
import {
  formatearCapacidadGLP,
  formatearCombustible,
  calcularGLPEntregaPorCamion,
} from "../utils/validacionCamiones";
import {
  obtenerIntervaloPorDefecto,
  calcularIntervaloSegunTipo,
  obtenerSegundosPorNodoSegunTipo,
} from "../context/simulacion/types";
import ControlVelocidad from "./ControlVelocidad";

interface CamionVisual {
  id: string;
  color: string;
  ruta: Coordenada[];
  posicion: Coordenada;
  rotacion: number;
  espejo: boolean;
}

const GRID_WIDTH = 70;
const GRID_HEIGHT = 50;
const CELL_SIZE = 14;
const SVG_WIDTH = GRID_WIDTH * CELL_SIZE;
const SVG_HEIGHT = GRID_HEIGHT * CELL_SIZE;

// Parametrización del grosor de línea de bloqueos
const BLOQUEO_STROKE_WIDTH = 4;

// Agregar constante global para habilitar/deshabilitar controles
export const CONTROLES_SIMULACION_HABILITADOS = false;

interface MapaProps {
  elementoResaltado?: {
    tipo: "camion" | "pedido" | "almacen";
    id: string;
  } | null;
  onElementoSeleccionado?: (
    elemento: { tipo: "camion" | "pedido" | "almacen"; id: string } | null
  ) => void;
  iniciarAutomaticamente?: boolean; // Nueva prop para iniciar automáticamente
  controlesSimulacionHabilitados?: boolean; // Nueva prop para habilitar/deshabilitar controles
}

const Mapa: React.FC<MapaProps> = ({
  elementoResaltado,
  onElementoSeleccionado,
  iniciarAutomaticamente = false,
  controlesSimulacionHabilitados = CONTROLES_SIMULACION_HABILITADOS,
}) => {
  const [camionesVisuales, setCamionesVisuales] = useState<CamionVisual[]>([]);
  const [running, setRunning] = useState(false);
  const [intervalo, setIntervalo] = useState(obtenerIntervaloPorDefecto());
  const [segundosPorNodo, setSegundosPorNodo] = useState(62.9);
  const [mostrarControlVelocidad, setMostrarControlVelocidad] = useState(false);
  const [tipoSimulacion, setTipoSimulacion] = useState<string>("DIARIA"); // Estado para el tipo de simulación
  const intervalRef = useRef<number | null>(null);
  const {
    camiones,
    rutasCamiones,
    almacenes,
    pedidosNoAsignados,
    avanzarHora,
    cargando,
    bloqueos,
    marcarCamionAveriado,
    iniciarContadorTiempo,
    setSimulacionActiva,
    simulacionActiva,
    setPollingActivo,
    horaActual,
    horaSimulacion,
    fechaHoraSimulacion,
    fechaInicioSimulacion,
    diaSimulacion,
    tiempoRealSimulacion,
    tiempoTranscurridoSimulado,
    aplicarNuevaSolucionDespuesAveria,
  } = useSimulacion();

  // Estado para el modal fijo (click)
  const [clickedCamion, setClickedCamion] = useState<string | null>(null);
  const [averiando, setAveriando] = useState<string | null>(null);
  // Estado para la leyenda desplegable
  const [leyendaVisible, setLeyendaVisible] = useState(false);
  // Estados para el modal de almacenes
  const [clickedAlmacen, setClickedAlmacen] = useState<string | null>(null);
  const [clickedAlmacenPos, setClickedAlmacenPos] = useState<{
    x: number;
    y: number;
  } | null>(null);
  // Estados para tooltip de almacenes
  const [tooltipAlmacen, setTooltipAlmacen] = useState<string | null>(null);

  // DEBUG: Verificar que almacenes llega al componente
  // console.log('🗺️ MAPA: Almacenes recibidos:', almacenes);

  const pedidosPendientes = getPedidosPendientes(
    rutasCamiones,
    camiones,
    pedidosNoAsignados
  );

  // Efecto para detectar el tipo de simulación basado en la URL
  useEffect(() => {
    const detectarTipoSimulacion = () => {
      const pathname = window.location.pathname;
      if (
        pathname.includes("/simulacion-semanal") ||
        pathname.includes("/colapso-logistico")
      ) {
        setTipoSimulacion(
          pathname.includes("/simulacion-semanal") ? "SEMANAL" : "COLAPSO"
        );
        // Para simulación semanal/colapso, usar velocidad fija
        const segundosFijos = obtenerSegundosPorNodoSegunTipo(
          pathname.includes("/simulacion-semanal") ? "SEMANAL" : "COLAPSO"
        );
        setSegundosPorNodo(segundosFijos);
        console.log(
          `📊 MAPA: Detectada simulación ${
            pathname.includes("/simulacion-semanal") ? "SEMANAL" : "COLAPSO"
          } - velocidad fija: ${segundosFijos}s por nodo`
        );
      } else {
        setTipoSimulacion("DIARIA");
        console.log(
          "⚡ MAPA: Detectada simulación DIARIA - velocidad configurable"
        );
      }
    };

    detectarTipoSimulacion();
  }, []);

  // Efecto para recalcular el intervalo cuando cambie la configuración o el tipo de simulación
  useEffect(() => {
    const nuevoIntervalo = calcularIntervaloSegunTipo(
      tipoSimulacion,
      segundosPorNodo
    );
    setIntervalo(nuevoIntervalo);
    
  }, [segundosPorNodo, tipoSimulacion]);

  // Efecto para iniciar automáticamente la simulación si se especifica
  useEffect(() => {
    if (
      iniciarAutomaticamente &&
      !running &&
      !cargando &&
      camiones.length > 0
    ) {
      console.log("🚀 MAPA: Iniciando simulación automáticamente...");
      // Activar la simulación y el contador de tiempo
      setSimulacionActiva(true);
      iniciarContadorTiempo();
      setRunning(true);
    }
  }, [
    iniciarAutomaticamente,
    running,
    setSimulacionActiva,
    iniciarContadorTiempo,
    cargando,
    camiones.length,
  ]);
  //console.log('👥 MAPA: Pedidos pendientes (clientes):', pedidosPendientes);
  //console.log('🚚 MAPA: Estado de camiones:', camiones);

  // Detectar recarga automática de almacenes
  useEffect(() => {
    // Verificar si todos los almacenes INTERMEDIOS están al 100% de capacidad
    const almacenesIntermedios = almacenes.filter(
      (almacen) => almacen.tipo === "SECUNDARIO"
    );
    const todosLlenos =
      almacenesIntermedios.length > 0 &&
      almacenesIntermedios.every(
        (almacen) =>
          almacen.capacidadActualGLP === almacen.capacidadMaximaGLP &&
          almacen.capacidadActualCombustible === almacen.capacidadCombustible
      );

    if (todosLlenos) {
      // Mostrar indicador de recarga automática
      // setMostrarRecargaAutomatica(true); // ELIMINADO

      // Ocultar el indicador después de 5 segundos
      const timer = setTimeout(() => {
        // setMostrarRecargaAutomatica(false); // ELIMINADO
      }, 5000);

      return () => clearTimeout(timer);
    }
  }, [almacenes]);

  // Removido: useEffect duplicado que causaba conflictos

  useEffect(() => {
    if (running && simulacionActiva) {
      intervalRef.current = window.setInterval(() => {
        avanzarHora();
      }, intervalo);
    } else {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    }
    return () => {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    };
  }, [running, intervalo, avanzarHora, simulacionActiva]);

  useEffect(() => {
    // Rebuild visuals whenever routes or truck states change

    // Log para verificar duplicados en rutasCamiones
    const rutasIds = rutasCamiones.map((r) => r.id);
    const rutasIdsUnicos = [...new Set(rutasIds)];
    if (rutasIds.length !== rutasIdsUnicos.length) {
      console.warn("🚨 MAPA: Hay IDs duplicados en rutasCamiones:", {
        total: rutasIds.length,
        unicos: rutasIdsUnicos.length,
        duplicados: rutasIds.filter(
          (id, index) => rutasIds.indexOf(id) !== index
        ),
      });
    }

    // // NUEVO: Log para monitorear averías automáticas
    // console.log('🗺️ MAPA: Procesando camiones para visualización:', {
    //   totalCamiones: camiones.length,
    //   totalRutas: rutasCamiones.length,
    //   camionesAveriados: camiones.filter(c => c.estado === 'Averiado').length,
    //   rutasConTiposNodos: rutasCamiones.filter(r => r.tiposNodos && r.tiposNodos.length > 0).length
    // });

    const nuevosVisuales = rutasCamiones.map((info, idx) => {
      // Filtrar valores undefined o null de la ruta
      const rutaValida = info.ruta.filter(
        (nodo) => nodo && typeof nodo === "string"
      );
      const rutaCoords = rutaValida.map(parseCoord);

      // Asegurar que hay al menos una coordenada válida
      if (rutaCoords.length === 0) {
        // console.warn('🚨 Ruta vacía para camión:', info.id);
        rutaCoords.push({ x: 0, y: 0 }); // Coordenada por defecto
      }

      const estadoCamion = camiones.find((c) => c.id === info.id);

      // NUEVO: Log para monitorear estado de camiones y tipos de nodos
      if (estadoCamion) {
        const porcentaje = estadoCamion.porcentaje;
        const siguientePaso = Math.floor(porcentaje);

        // Verificar si el camión está en un nodo con avería automática
        if (info.tiposNodos && siguientePaso < info.tiposNodos.length) {
          const tipoNodoActual = info.tiposNodos[siguientePaso];
          const esNodoAveriaAutomatica =
            tipoNodoActual === "AVERIA_AUTOMATICA_T1" ||
            tipoNodoActual === "AVERIA_AUTOMATICA_T2" ||
            tipoNodoActual === "AVERIA_AUTOMATICA_T3";

          if (esNodoAveriaAutomatica) {
            // console.log('🚛💥 MAPA: Camión en nodo de avería automática:', {
            //   camionId: estadoCamion.id,
            //   tipoNodo: tipoNodoActual,
            //   porcentaje: porcentaje,
            //   siguientePaso: siguientePaso,
            //   estadoActual: estadoCamion.estado,
            //   ubicacion: estadoCamion.ubicacion
            // });
          }
        }

        // Log para camiones averiados
        if (estadoCamion.estado === "Averiado") {
          // console.log('🚛🔴 MAPA: Camión averiado detectado:', {
          //   camionId: estadoCamion.id,
          //   ubicacion: estadoCamion.ubicacion,
          //   porcentaje: porcentaje
          // });
        }
      }

      // Determinar posición actual y dirección
      let currentPos = rutaCoords[0]; // Posición por defecto

      if (
        estadoCamion &&
        estadoCamion.ubicacion &&
        typeof estadoCamion.ubicacion === "string"
      ) {
        currentPos = parseCoord(estadoCamion.ubicacion);
      }

      // Asegurar que currentPos sea siempre una coordenada válida
      if (!esCoordenadaValida(currentPos)) {
        console.warn(
          "🚨 MAPA: Coordenada actual inválida para camión:",
          info.id,
          currentPos
        );
        currentPos = { x: 0, y: 0 }; // Coordenada por defecto
      }

      let rotacion = 0;
      let espejo = false;

      if (estadoCamion && rutaCoords.length > 1) {
        const porcentaje = estadoCamion.porcentaje;
        // Usar Math.floor para sincronizar con la línea restante
        const currentIdx = Math.floor(porcentaje);

        // Validar que currentPos sea válido antes de usarlo
        if (esCoordenadaValida(currentPos)) {
          // Si hay un siguiente nodo en la ruta, calcular dirección hacia él
          if (currentIdx + 1 < rutaCoords.length) {
            const nextPos = rutaCoords[currentIdx + 1];
            if (esCoordenadaValida(nextPos)) {
              const orientacion = calcularRotacion(currentPos, nextPos);
              rotacion = orientacion.rotacion;
              espejo = orientacion.espejo;
            }
          } else if (currentIdx > 0) {
            // Si estamos en el último nodo, usar la dirección del último movimiento
            const prevPos = rutaCoords[currentIdx - 1];
            if (esCoordenadaValida(prevPos)) {
              const orientacion = calcularRotacion(prevPos, currentPos);
              rotacion = orientacion.rotacion;
              espejo = orientacion.espejo;
            }
          }
        }
      }

      // Compute remaining path
      const porcentaje = estadoCamion ? estadoCamion.porcentaje : 0;
      // La línea se consume solo después de que el camión pase completamente por cada nodo
      // Cuando el camión está en porcentaje 1.0, está en el nodo 1, pero la línea debe mostrarse desde el nodo 2
      const idxRest = Math.floor(porcentaje) - RETRASO_CONSUMO_RUTA;
      const rutaRestante = rutaCoords.slice(idxRest);

      return {
        id: info.id,
        color: CAMION_COLORS[idx % CAMION_COLORS.length],
        ruta: rutaRestante,
        posicion: currentPos,
        rotacion: rotacion,
        espejo: espejo,
      } as CamionVisual;
    });

    // Eliminar duplicados basándose en el ID usando Map para garantizar unicidad
    const visualesMap = new Map();
    nuevosVisuales.forEach((visual) => {
      if (!visualesMap.has(visual.id)) {
        visualesMap.set(visual.id, visual);
      } else {
        console.warn("🚨 MAPA: Camión duplicado encontrado:", visual.id);
      }
    });
    const visualesUnicos = Array.from(visualesMap.values());

    // Log para debugging de duplicados
    if (nuevosVisuales.length !== visualesUnicos.length) {
      console.warn("🚨 MAPA: Se encontraron camiones duplicados:", {
        total: nuevosVisuales.length,
        unicos: visualesUnicos.length,
        duplicados: nuevosVisuales.length - visualesUnicos.length,
      });
    }

    setCamionesVisuales(visualesUnicos);

    // Log para verificar IDs únicos
    const ids = visualesUnicos.map((v) => v.id);
    const idsUnicos = [...new Set(ids)];
    if (ids.length !== idsUnicos.length) {
      console.error(
        "🚨 MAPA: ERROR - Hay IDs duplicados en camionesVisuales:",
        {
          total: ids.length,
          unicos: idsUnicos.length,
          duplicados: ids.filter((id, index) => ids.indexOf(id) !== index),
        }
      );
    }
  }, [camiones, rutasCamiones]);

  // Función handleAveriar movida a mapa/utils/averias.ts

  if (cargando) {
    return <p>Cargando simulación...</p>;
  }

  return (
    <div className="w-full h-full flex flex-col">
      {/* Contenedor horizontal para leyenda y mapa */}
      <div className="flex flex-row w-full h-full flex-1">
        {/* Leyenda lateral compacta */}
        <div
          className={`bg-white rounded-lg shadow-md border border-gray-200 flex-shrink-0 h-full flex flex-col transition-all duration-300 ${
            leyendaVisible ? "p-2 w-32" : "p-0 w-0 overflow-hidden"
          }`}
        >
          {leyendaVisible ? (
            <>
              <button
                onClick={() => setLeyendaVisible(false)}
                className="flex items-center justify-between w-full text-left text-xs font-semibold text-gray-800 hover:text-gray-900 mb-2"
              >
                <span>LEYENDA</span>
                <ChevronUp size={12} />
              </button>
              <div className="space-y-1.5 overflow-y-auto max-h-[80vh]">
                {/* Almacén Central */}
                <div className="flex items-center gap-1.5">
                  <svg width="16" height="12" viewBox="0 0 20 20">
                    <polygon
                      points="2,18 18,18 22,2 2,2"
                      fill="#2563eb"
                      stroke="black"
                      strokeWidth="0.5"
                    />
                    <text
                      x="12"
                      y="12"
                      textAnchor="middle"
                      fontSize="8"
                      fill="white"
                      fontWeight="bold"
                    >
                      C
                    </text>
                  </svg>
                  <span className="text-xs text-gray-700">A. Central</span>
                </div>

                {/* Almacén Intermedio */}
                <div className="flex items-center gap-1.5">
                  <svg width="16" height="12" viewBox="0 0 20 20">
                    <polygon
                      points="2,18 18,18 22,2 2,2"
                      fill="#16a34a"
                      stroke="black"
                      strokeWidth="0.5"
                    />
                    <text
                      x="12"
                      y="12"
                      textAnchor="middle"
                      fontSize="8"
                      fill="white"
                      fontWeight="bold"
                    >
                      I
                    </text>
                  </svg>
                  <span className="text-xs text-gray-700">A. Intermedio</span>
                </div>

                {/* Cliente */}
                <div className="flex items-center gap-1.5">
                  <img src={clienteIcon} alt="Cliente" className="w-4 h-4" />
                  <span className="text-xs text-gray-700">Cliente</span>
                </div>

                {/* Cliente No Asignado */}
                <div className="flex items-center gap-1.5">
                  <img
                    src={clienteIcon}
                    alt="Cliente No Asignado"
                    className="w-4 h-4"
                    style={{ filter: "grayscale(100%) brightness(0.7)" }}
                  />
                  <span className="text-xs text-gray-700">Cliente N/A</span>
                </div>

                {/* Estados de pedidos */}
                <div className="pt-1 border-t border-gray-200">
                  <div className="text-xs font-medium text-gray-600 mb-1">
                    Estados Pedidos:
                  </div>
                  <div className="space-y-1">
                    <div className="flex items-center gap-1">
                      <div className="w-2 h-2 bg-red-500 rounded-sm"></div>
                      <span className="text-xs text-gray-700">Pendiente</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <div className="w-2 h-2 bg-green-500 rounded-sm"></div>
                      <span className="text-xs text-gray-700">En Tránsito</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <div className="w-2 h-2 bg-gray-500 rounded-sm"></div>
                      <span className="text-xs text-gray-700">No Asignado</span>
                    </div>
                  </div>
                </div>
                {/* Camión */}
                <div className="flex items-center gap-1.5">
                  <svg
                    width="16"
                    height="12"
                    viewBox="0 0 16 12"
                    className="border border-gray-300 rounded"
                  >
                    <rect
                      x="2"
                      y="4"
                      width="10"
                      height="4"
                      rx="0.5"
                      fill="#3b82f6"
                      stroke="black"
                      strokeWidth="0.3"
                    />
                    <rect
                      x="10"
                      y="5"
                      width="3"
                      height="2"
                      rx="0.3"
                      fill="#3b82f6"
                      stroke="black"
                      strokeWidth="0.3"
                    />
                    <circle cx="4" cy="9" r="1" fill="black" />
                    <circle cx="8" cy="9" r="1" fill="black" />
                    <circle cx="11" cy="9" r="1" fill="black" />
                    <polygon
                      points="13,6 12,5.5 12,6.5"
                      fill="white"
                      stroke="black"
                      strokeWidth="0.2"
                    />
                  </svg>
                  <span className="text-xs text-gray-700">Camión</span>
                </div>

                {/* Ruta */}
                <div className="flex items-center gap-1.5">
                  <div className="w-4 h-0.5 border-t border-dashed border-blue-500"></div>
                  <span className="text-xs text-gray-700">Ruta</span>
                </div>

                {/* Bloqueos */}
                <div className="flex items-center gap-1.5">
                  <div className="w-4 h-0.5 bg-red-600 rounded-full"></div>
                  <span className="text-xs text-gray-700">Bloqueos</span>
                </div>

                {/* Estados de camiones */}
                <div className="pt-1 border-t border-gray-200">
                  <div className="text-xs font-medium text-gray-600 mb-1">
                    Estados:
                  </div>
                  <div className="space-y-1">
                    <div className="flex items-center gap-1">
                      <div className="w-2 h-2 bg-blue-500 rounded-sm"></div>
                      <span className="text-xs text-gray-700">Normal</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <div className="w-2 h-2 bg-red-500 rounded-sm"></div>
                      <span className="text-xs text-gray-700">Averiado</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <div className="w-2 h-2 bg-gray-800 rounded-sm"></div>
                      <span className="text-xs text-gray-700">Mant.</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <div className="w-2 h-2 bg-orange-500 rounded-sm"></div>
                      <span className="text-xs text-gray-700">
                        Mant. Avería
                      </span>
                    </div>
                  </div>
                </div>
                {/* Leyenda de colores de GLP para camión/ruta/almacén */}
                <div className="pt-1 border-t border-gray-200 mt-2">
                  <div className="text-xs font-medium text-gray-600 mb-1">
                    Nivel GLP camión/ruta/almacén:
                  </div>
                  <div className="flex items-center gap-1 mb-1">
                    <svg width="16" height="12" viewBox="0 0 20 20">
                      <polygon
                        points="2,18 18,18 22,2 2,2"
                        fill="#3b82f6"
                        stroke="black"
                        strokeWidth="0.5"
                      />
                    </svg>
                    <span className="text-xs text-gray-700">
                      100% (inicio, lleno)
                    </span>
                  </div>
                  <div className="flex items-center gap-1 mb-1">
                    <svg width="16" height="12" viewBox="0 0 20 20">
                      <polygon
                        points="2,18 18,18 22,2 2,2"
                        fill="#22c55e"
                        stroke="black"
                        strokeWidth="0.5"
                      />
                    </svg>
                    <span className="text-xs text-gray-700">
                      &gt; 75% (óptima)
                    </span>
                  </div>
                  <div className="flex items-center gap-1 mb-1">
                    <svg width="16" height="12" viewBox="0 0 20 20">
                      <polygon
                        points="2,18 18,18 22,2 2,2"
                        fill="#eab308"
                        stroke="black"
                        strokeWidth="0.5"
                      />
                    </svg>
                    <span className="text-xs text-gray-700">
                      40% - 75% (media)
                    </span>
                  </div>
                  <div className="flex items-center gap-1">
                    <svg width="16" height="12" viewBox="0 0 20 20">
                      <polygon
                        points="2,18 18,18 22,2 2,2"
                        fill="#f97316"
                        stroke="black"
                        strokeWidth="0.5"
                      />
                    </svg>
                    <span className="text-xs text-gray-700">
                      &lt; 40% (baja)
                    </span>
                  </div>
                </div>
              </div>
            </>
          ) : (
            <button
              onClick={() => setLeyendaVisible(true)}
              className="absolute left-0 top-4 bg-white border border-gray-300 rounded-r-lg shadow px-2 py-1 text-xs font-semibold text-gray-800 hover:text-gray-900 z-10"
              style={{ minWidth: "24px" }}
              title="Mostrar leyenda"
            >
              <ChevronDown size={12} />
            </button>
          )}
        </div>
        {/* Contenedor principal del mapa */}
        <div className="flex-1 flex flex-col items-center h-full">
          <div className="w-full max-w-full overflow-auto">
            <svg
              width={SVG_WIDTH}
              height={SVG_HEIGHT}
              className="border border-gray-500 bg-white rounded-xl mx-auto"
              style={{ maxWidth: "100%", height: "auto" }}
              viewBox={`0 0 ${SVG_WIDTH} ${SVG_HEIGHT}`}
              preserveAspectRatio="xMidYMid meet"
              onClick={(evt) => {
                // Cerrar modales si se hace click en un área vacía del mapa
                if (evt.target === evt.currentTarget) {
                  setClickedCamion(null);
                  setClickedAlmacen(null);
                  setTooltipAlmacen(null);

                  // Limpiar el resaltado
                  if (onElementoSeleccionado) {
                    console.log("🎯 MAPA: Limpiando resaltado");
                    onElementoSeleccionado(null);
                  }
                }
              }}
            >
              {/* Fondo invisible para capturar clicks */}
              <rect
                x={0}
                y={0}
                width={SVG_WIDTH}
                height={SVG_HEIGHT}
                fill="transparent"
                onClick={() => {
                  setClickedCamion(null);
                  setClickedAlmacen(null);
                  setTooltipAlmacen(null);

                  // Limpiar el resaltado
                  if (onElementoSeleccionado) {
                    console.log("🎯 MAPA: Limpiando resaltado");
                    onElementoSeleccionado(null);
                  }
                }}
              />

              {/* Grid */}
              {[...Array(GRID_WIDTH + 1)].map((_, i) => (
                <line
                  key={`v-${i}`}
                  x1={i * CELL_SIZE}
                  y1={0}
                  x2={i * CELL_SIZE}
                  y2={SVG_HEIGHT}
                  stroke="#d1d5db"
                  strokeWidth={1}
                />
              ))}
              {[...Array(GRID_HEIGHT + 1)].map((_, i) => (
                <line
                  key={`h-${i}`}
                  x1={0}
                  y1={i * CELL_SIZE}
                  x2={SVG_WIDTH}
                  y2={i * CELL_SIZE}
                  stroke="#d1d5db"
                  strokeWidth={1}
                />
              ))}

              {/* Bloqueos */}
              {bloqueos &&
                bloqueos.map((bloqueo, idx) => (
                  <polyline
                    key={`bloqueo-${idx}`}
                    fill="none"
                    stroke="#dc2626"
                    strokeWidth={BLOQUEO_STROKE_WIDTH}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    points={bloqueo.coordenadas
                      .map(
                        (coord) =>
                          `${coord.x * CELL_SIZE},${coord.y * CELL_SIZE}`
                      )
                      .join(" ")}
                  />
                ))}

              {/* Clientes/Pedidos */}
              {pedidosPendientes.map((pedido: PedidoConAsignacion) => {
                //console.log('👤 MAPA: Renderizando cliente:', pedido.codigo, 'en posición:', pedido.coordenada);
                const esResaltado =
                  elementoResaltado?.tipo === "pedido" &&
                  elementoResaltado?.id === pedido.codigo;
                const estadoPedido = pedido.estadoPedido;

                // Colores según el estado del pedido
                let colorTexto, colorVolumen, filtroIcono;

                switch (estadoPedido) {
                  case "NO_ASIGNADO":
                    colorTexto = "#6b7280"; // Gris
                    colorVolumen = "#6b7280";
                    filtroIcono = "grayscale(100%) brightness(0.7)";
                    break;
                  case "EN_TRANSITO":
                    colorTexto = "#16a34a"; // Verde
                    colorVolumen = "#16a34a";
                    filtroIcono = "none";
                    break;
                  case "RETRASO":
                    colorTexto = "#dc2626"; // Rojo
                    colorVolumen = "#dc2626";
                    filtroIcono = "none";
                    break;
                  case "PENDIENTE":
                  default:
                    colorTexto = "#dc2626"; // Rojo
                    colorVolumen = "#dc2626";
                    filtroIcono = "none";
                    break;
                }

                return (
                  <g key={pedido.codigo}>
                    {/* Círculo de resaltado para pedidos */}
                    {esResaltado && (
                      <circle
                        key={`${pedido.codigo}-highlight`}
                        cx={pedido.coordenada.x * CELL_SIZE}
                        cy={pedido.coordenada.y * CELL_SIZE}
                        r={25}
                        fill="none"
                        stroke="#f59e0b"
                        strokeWidth={3}
                        strokeDasharray="4 2"
                        opacity={0.8}
                      >
                        <animate
                          key={`${pedido.codigo}-animate-r`}
                          attributeName="r"
                          values="20;30;20"
                          dur="2s"
                          repeatCount="indefinite"
                        />
                      </circle>
                    )}

                    {/* Icono del cliente con filtro según estado */}
                    <image
                      key={`${pedido.codigo}-icon`}
                      href={clienteIcon}
                      x={pedido.coordenada.x * CELL_SIZE - 15}
                      y={pedido.coordenada.y * CELL_SIZE - 15}
                      width={30}
                      height={30}
                      style={{
                        filter: filtroIcono,
                      }}
                    />

                    {/* Etiqueta del código */}
                    <text
                      key={`${pedido.codigo}-label`}
                      x={pedido.coordenada.x * CELL_SIZE}
                      y={pedido.coordenada.y * CELL_SIZE + 25}
                      textAnchor="middle"
                      fontSize="10"
                      fill={colorTexto}
                      fontWeight="bold"
                      stroke="#fff"
                      strokeWidth="0.5"
                    >
                      {pedido.codigo}
                    </text>

                    {/* Volumen GLP */}
                    <text
                      key={`${pedido.codigo}-volume`}
                      x={pedido.coordenada.x * CELL_SIZE}
                      y={pedido.coordenada.y * CELL_SIZE + 37}
                      textAnchor="middle"
                      fontSize="8"
                      fill={colorVolumen}
                      fontWeight="bold"
                      stroke="#fff"
                      strokeWidth="0.5"
                    >
                      {pedido.volumenGLPAsignado.toFixed(1)}m³
                    </text>
                  </g>
                );
              })}

              {/* Almacenes */}
              {almacenes.map((almacen) => {
                // console.log('🏪 MAPA: Renderizando almacén:', almacen.nombre, 'en posición:', almacen.coordenada);
                const esResaltado =
                  elementoResaltado?.tipo === "almacen" &&
                  elementoResaltado?.id === almacen.id;

                // Calcular porcentaje de GLP para el color
                const porcentajeGLP =
                  almacen.capacidadMaximaGLP > 0
                    ? (almacen.capacidadActualGLP /
                        almacen.capacidadMaximaGLP) *
                      100
                    : 0;

                // Usar la función colorSemaforoGLP para obtener el color exacto
                const colorAlmacen = colorSemaforoGLP(porcentajeGLP);

                return (
                  <g key={almacen.id} style={{ cursor: "pointer" }}>
                    {/* Círculo de resaltado para almacenes */}
                    {esResaltado && (
                      <circle
                        cx={almacen.coordenada.x * CELL_SIZE}
                        cy={almacen.coordenada.y * CELL_SIZE}
                        r={30}
                        fill="none"
                        stroke="#10b981"
                        strokeWidth={3}
                        strokeDasharray="6 3"
                        opacity={0.8}
                      >
                        <animate
                          attributeName="r"
                          values="25;35;25"
                          dur="2s"
                          repeatCount="indefinite"
                        />
                        <animate
                          attributeName="opacity"
                          values="0.6;1;0.6"
                          dur="2s"
                          repeatCount="indefinite"
                        />
                      </circle>
                    )}

                    {/* Icono del almacén con color aplicado */}
                    <g
                      transform={`translate(${
                        almacen.coordenada.x * CELL_SIZE - 10
                      }, ${almacen.coordenada.y * CELL_SIZE - 10})`}
                      onMouseEnter={() => {
                        // Solo mostrar tooltip si no hay modal activo
                        if (!clickedAlmacen) {
                          setTooltipAlmacen(almacen.nombre);
                        }
                      }}
                      onMouseMove={() => {
                        if (
                          !clickedAlmacen &&
                          tooltipAlmacen === almacen.nombre
                        ) {
                          // setTooltipAlmacenPos({ x: evt.clientX, y: evt.clientY }); // ELIMINADO
                        }
                      }}
                      onMouseLeave={() => {
                        setTooltipAlmacen(null);
                      }}
                      onClick={(evt) => {
                        // Solo abrir el modal si no hay otro modal ya abierto
                        if (!clickedAlmacen) {
                          console.log(
                            "🖱️ Click en almacén:",
                            almacen.nombre,
                            "en posición:",
                            evt.clientX,
                            evt.clientY
                          );
                          setClickedAlmacen(almacen.nombre);
                          setClickedAlmacenPos({
                            x: evt.clientX,
                            y: evt.clientY,
                          });
                          // Ocultar el tooltip de hover
                          setTooltipAlmacen(null);

                          // Activar el resaltado del almacén en el mapa
                          if (onElementoSeleccionado) {
                            console.log(
                              "🎯 MAPA: Activando resaltado de almacén (texto):",
                              almacen.id
                            );
                            onElementoSeleccionado({
                              tipo: "almacen",
                              id: almacen.id,
                            });
                          }
                        }
                      }}
                    >
                      {/* Trapecio del almacén con el color del semáforo */}
                      <polygon
                        points="2,18 18,18 22,2 2,2"
                        fill={colorAlmacen}
                        stroke="black"
                        strokeWidth="1"
                      />

                      {/* Indicador del tipo de almacén (central o intermedio) */}
                      <text
                        x="12"
                        y="12"
                        textAnchor="middle"
                        fontSize="10"
                        fill="white"
                        fontWeight="bold"
                        stroke="black"
                        strokeWidth="0.3"
                      >
                        {almacen.tipo === "CENTRAL" ? "C" : "I"}
                      </text>
                    </g>

                    <text
                      x={almacen.coordenada.x * CELL_SIZE}
                      y={almacen.coordenada.y * CELL_SIZE + 30}
                      textAnchor="middle"
                      fontSize="12"
                      fill={almacen.tipo === "CENTRAL" ? "#2563eb" : "#16a34a"}
                      fontWeight="bold"
                      stroke="#fff"
                      strokeWidth="0.5"
                      onClick={(evt) => {
                        if (!clickedAlmacen) {
                          setClickedAlmacen(almacen.nombre);
                          setClickedAlmacenPos({
                            x: evt.clientX,
                            y: evt.clientY,
                          });

                          // Activar el resaltado del almacén en el mapa
                          if (onElementoSeleccionado) {
                            onElementoSeleccionado({
                              tipo: "almacen",
                              id: almacen.id,
                            });
                          }
                        }
                      }}
                    >
                      {almacen.nombre}
                    </text>
                  </g>
                );
              })}

              {/* Rutas de camiones normales (primero para que estén por debajo) */}
              {camionesVisuales
                .filter((camion) => {
                  const estadoCamion = camiones.find((c) => c.id === camion.id);
                  const esResaltado =
                    elementoResaltado?.tipo === "camion" &&
                    elementoResaltado?.id === camion.id;
                  return (
                    estadoCamion?.estado !== "Averiado" &&
                    estadoCamion?.estado !== "En Mantenimiento por Avería" &&
                    camion.ruta.length > 1 &&
                    !esResaltado
                  ); // Solo rutas normales (no resaltadas)
                })
                .map((camion) => {
                  const estadoCamion = camiones.find((c) => c.id === camion.id);

                  // Ruta normal con color según GLP
                  const tieneGLP =
                    estadoCamion &&
                    typeof estadoCamion.capacidadActualGLP === "number" &&
                    typeof estadoCamion.capacidadMaximaGLP === "number" &&
                    estadoCamion.capacidadMaximaGLP > 0;
                  const colorRuta = tieneGLP
                    ? colorSemaforoGLP(
                        (estadoCamion.capacidadActualGLP! /
                          estadoCamion.capacidadMaximaGLP!) *
                          100,
                        estadoCamion.estado === "Disponible" &&
                          estadoCamion.capacidadActualGLP ===
                            estadoCamion.capacidadMaximaGLP
                      )
                    : "#3b82f6"; // Azul por defecto

                  return (
                    <polyline
                      key={`ruta-normal-${camion.id}`}
                      fill="none"
                      stroke={colorRuta}
                      strokeWidth={2}
                      strokeDasharray="4 2"
                      points={camion.ruta
                        .map(
                          (p: Coordenada) =>
                            `${p.x * CELL_SIZE},${p.y * CELL_SIZE}`
                        )
                        .join(" ")}
                    />
                  );
                })}

              {/* Rutas de camiones seleccionados (después para que estén por encima) */}
              {camionesVisuales
                .filter((camion) => {
                  const estadoCamion = camiones.find((c) => c.id === camion.id);
                  const esResaltado =
                    elementoResaltado?.tipo === "camion" &&
                    elementoResaltado?.id === camion.id;
                  return (
                    estadoCamion?.estado !== "Averiado" &&
                    estadoCamion?.estado !== "En Mantenimiento por Avería" &&
                    camion.ruta.length > 1 &&
                    esResaltado
                  ); // Solo rutas de camiones seleccionados
                })
                .map((camion) => {
                  const estadoCamion = camiones.find((c) => c.id === camion.id);
                  // Obtener la ruta completa del camión desde rutasCamiones
                  const rutaCompleta = rutasCamiones.find(
                    (r) => r.id === camion.id
                  );
                  let rutaAMostrar = camion.ruta;

                  if (rutaCompleta && rutaCompleta.ruta.length > 1) {
                    const rutaCoordsCompleta = rutaCompleta.ruta
                      .filter((nodo) => nodo && typeof nodo === "string")
                      .map(parseCoord);

                    // Calcular qué parte de la ruta mostrar basándose en el progreso del camión
                    const porcentaje = estadoCamion
                      ? estadoCamion.porcentaje
                      : 0;
                    // La línea se consume solo después de que el camión pase completamente por cada nodo
                    const indiceInicio =
                      Math.floor(porcentaje) - RETRASO_CONSUMO_RUTA;

                    // Mostrar solo la parte de la ruta que aún no ha sido recorrida
                    rutaAMostrar = rutaCoordsCompleta.slice(indiceInicio);
                  }

                  return (
                    <polyline
                      key={`ruta-seleccionada-${camion.id}`}
                      fill="none"
                      stroke="#000000"
                      strokeWidth={2}
                      strokeDasharray="none"
                      points={rutaAMostrar
                        .map(
                          (p: Coordenada) =>
                            `${p.x * CELL_SIZE},${p.y * CELL_SIZE}`
                        )
                        .join(" ")}
                    />
                  );
                })}

              {/* Camiones */}
              {camionesVisuales
                .filter((camion) => {
                  const estadoCamion = camiones.find((c) => c.id === camion.id);
                  // Ocultar camiones en mantenimiento por avería
                  if (estadoCamion?.estado === "En Mantenimiento por Avería") {
                    return false;
                  }

                  // Ocultar camiones que están en el almacén central (12,8) y tienen estado "Disponible"
                  if (
                    estadoCamion?.estado === "Disponible" &&
                    camion.posicion.x === 12 &&
                    camion.posicion.y === 8
                  ) {
                    return false;
                  }

                  return true;
                })
                .map((camion) => {
                  const estadoCamion = camiones.find((c) => c.id === camion.id);
                  const esAveriado = estadoCamion?.estado === "Averiado";
                  const esEnMantenimiento =
                    estadoCamion?.estado === "En Mantenimiento";
                  const esEnMantenimientoPreventivo =
                    estadoCamion?.estado === "En Mantenimiento Preventivo";
                  const esResaltado =
                    elementoResaltado?.tipo === "camion" &&
                    elementoResaltado?.id === camion.id;
                  const { posicion, rotacion, espejo } = camion;
                  const tieneGLP =
                    estadoCamion &&
                    typeof estadoCamion.capacidadActualGLP === "number" &&
                    typeof estadoCamion.capacidadMaximaGLP === "number" &&
                    estadoCamion.capacidadMaximaGLP > 0;
                  const colorFinal = esAveriado
                    ? ESTADO_COLORS.AVERIADO
                    : esEnMantenimiento
                    ? ESTADO_COLORS.MANTENIMIENTO
                    : esEnMantenimientoPreventivo
                    ? ESTADO_COLORS.MANTENIMIENTO_PREVENTIVO
                    : tieneGLP
                    ? colorSemaforoGLP(
                        (estadoCamion.capacidadActualGLP! /
                          estadoCamion.capacidadMaximaGLP!) *
                          100,
                        estadoCamion.estado === "Disponible" &&
                          estadoCamion.capacidadActualGLP ===
                            estadoCamion.capacidadMaximaGLP
                      )
                    : "#3b82f6"; // Azul por defecto si no hay datos
                  const cx = posicion.x * CELL_SIZE;
                  const cy = posicion.y * CELL_SIZE;
                  return (
                    <g key={`camion-${camion.id}`}>
                      <g
                        transform={`translate(${cx}, ${cy}) rotate(${rotacion}) ${
                          espejo ? "scale(-1, 1)" : ""
                        }`}
                        style={{ cursor: "pointer" }}
                        onClick={() => {
                          // Solo abrir el modal si no hay otro modal ya abierto
                          if (!clickedCamion) {
                            setClickedCamion(camion.id);
                            // setClickedPos({ x: evt.clientX, y: evt.clientY }); // ELIMINADO

                            // Activar el resaltado del camión en el mapa
                            if (onElementoSeleccionado) {
                              // console.log(
                              //   "🎯 MAPA: Activando resaltado de camión:",
                              //   camion.id
                              // );
                              onElementoSeleccionado({
                                tipo: "camion",
                                id: camion.id,
                              });
                            }
                          }
                        }}
                      >
                        {/* Área de click invisible más grande para facilitar la selección */}
                        <circle
                          key={`area-click-${camion.id}`}
                          cx={0}
                          cy={0}
                          r={18}
                          fill="transparent"
                          style={{ cursor: "pointer" }}
                        />

                        {/* Círculo de resaltado que se mueve con el camión */}
                        {esResaltado && (
                          <circle
                            key={`resaltado-${camion.id}`}
                            cx={0}
                            cy={0}
                            r={25}
                            fill="none"
                            stroke="#f59e0b"
                            strokeWidth={3}
                            strokeDasharray="8 4"
                            opacity={0.9}
                            style={{}}
                          >
                            <animateTransform
                              attributeName="transform"
                              type="rotate"
                              values="0 0 0;360 0 0"
                              dur="4s"
                              repeatCount="indefinite"
                            />
                            <animate
                              attributeName="opacity"
                              values="0.7;1;0.7"
                              dur="1.5s"
                              repeatCount="indefinite"
                            />
                          </circle>
                        )}

                        {/* Cuerpo principal del camión */}
                        <rect
                          key={`cuerpo-${camion.id}`}
                          x={-8}
                          y={-3}
                          width={16}
                          height={6}
                          rx={1}
                          fill={colorFinal}
                          stroke="black"
                          strokeWidth={0.5}
                        />

                        {/* Cabina del camión (frente) */}
                        <rect
                          key={`cabina-${camion.id}`}
                          x={6}
                          y={-2}
                          width={4}
                          height={4}
                          rx={0.5}
                          fill={colorFinal}
                          stroke="black"
                          strokeWidth={0.5}
                        />

                        {/* Ruedas */}
                        <circle
                          key={`rueda1-${camion.id}`}
                          cx={-5}
                          cy={4}
                          r={1.5}
                          fill="black"
                        />
                        <circle
                          key={`rueda2-${camion.id}`}
                          cx={2}
                          cy={4}
                          r={1.5}
                          fill="black"
                        />
                        <circle
                          key={`rueda3-${camion.id}`}
                          cx={7}
                          cy={4}
                          r={1.5}
                          fill="black"
                        />

                        {/* Indicador de dirección (flecha) */}
                        <polygon
                          key={`flecha-${camion.id}`}
                          points="10,0 8,-1.5 8,1.5"
                          fill="white"
                          stroke="black"
                          strokeWidth={0.3}
                        />

                        {/* Líneas de detalle del camión */}
                        <line
                          key={`linea1-${camion.id}`}
                          x1={-6}
                          y1={-1}
                          x2={4}
                          y2={-1}
                          stroke="black"
                          strokeWidth={0.3}
                          opacity={0.6}
                        />
                        <line
                          key={`linea2-${camion.id}`}
                          x1={-6}
                          y1={1}
                          x2={4}
                          y2={1}
                          stroke="black"
                          strokeWidth={0.3}
                          opacity={0.6}
                        />

                        {esAveriado && (
                          <text
                            key={`averia-${camion.id}`}
                            x={0}
                            y={-10}
                            textAnchor="middle"
                            fontSize="8"
                            fill="#dc2626"
                            fontWeight="bold"
                          >
                            💥
                          </text>
                        )}
                      </g>
                    </g>
                  );
                })}
            </svg>
          </div>
        </div>
      </div>

      {/* Modal para almacén (click) */}
      {clickedAlmacen &&
        clickedAlmacenPos &&
        (() => {
          console.log(
            "�� Renderizando modal de almacén:",
            clickedAlmacen,
            "en posición:",
            clickedAlmacenPos
          );
          const almacen = almacenes.find((a) => a.nombre === clickedAlmacen);

          if (!almacen) {
            console.log("❌ No se encontró el almacén:", clickedAlmacen);
            return null;
          }

          // console.log("✅ Almacén encontrado:", almacen.nombre);

          const porcentajeGLP =
            almacen.capacidadMaximaGLP > 0
              ? (almacen.capacidadActualGLP / almacen.capacidadMaximaGLP) * 100
              : 0;

          // Obtener pedidos y camiones asignados al almacén
          const pedidosAsignados = obtenerPedidosAsignadosAlAlmacen(
            almacen,
            rutasCamiones,
            camiones,
            pedidosNoAsignados
          );
          const camionesAsignados = obtenerCamionesAsignadosAlAlmacen(
            almacen,
            rutasCamiones,
            camiones
          );

          // Calcular posición del modal para que se vea completo
          const modalWidth = 400;
          const modalHeight = 500;
          const viewportWidth = window.innerWidth;
          const viewportHeight = window.innerHeight;

          let modalLeft = clickedAlmacenPos.x + 10;
          let modalTop = clickedAlmacenPos.y + 10;

          // Ajustar horizontalmente si se sale de la pantalla
          if (modalLeft + modalWidth > viewportWidth) {
            modalLeft = clickedAlmacenPos.x - modalWidth - 10;
          }

          // Ajustar verticalmente si se sale de la pantalla
          if (modalTop + modalHeight > viewportHeight) {
            modalTop = clickedAlmacenPos.y - modalHeight - 10;
            // Si aún se sale por arriba, centrarlo verticalmente respecto al click
            if (modalTop < 0) {
              modalTop = Math.max(10, clickedAlmacenPos.y - modalHeight / 2);
            }
          }

          return (
            <div
              className="fixed bg-white border border-gray-300 rounded-lg shadow-lg z-50 overflow-hidden"
              style={{
                left: modalLeft,
                top: modalTop,
                width: modalWidth,
                maxHeight: modalHeight,
              }}
              onClick={(e) => e.stopPropagation()}
            >
              {/* Header */}
              <div className="p-3 bg-blue-600 text-white">
                <div className="font-bold text-sm">{almacen.nombre}</div>
                <div className="text-xs opacity-90">
                  {almacen.tipo === "CENTRAL"
                    ? "Almacén Central"
                    : "Almacén Secundario"}{" "}
                  • ({almacen.coordenada.x}, {almacen.coordenada.y})
                </div>
              </div>

              {/* Content */}
              <div className="p-3 overflow-y-auto" style={{ maxHeight: '400px' }}>
                {/* Estado */}
                <div className="mb-3 text-center">
                  <span
                    className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                      almacen.activo
                        ? "bg-green-100 text-green-800"
                        : "bg-red-100 text-red-800"
                    }`}
                  >
                    {almacen.activo ? "🟢 Activo" : "🔴 Inactivo"}
                  </span>
                </div>

                {/* GLP Info */}
                <div className="mb-3">
                  <div className="flex justify-between items-center mb-1">
                    <span className="text-xs font-medium text-gray-700">
                      Gas Licuado (GLP)
                    </span>
                    <span className="text-xs font-bold text-blue-600">
                      {porcentajeGLP.toFixed(1)}%
                    </span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2 mb-2">
                    <div
                      className="h-2 rounded-full transition-all duration-300"
                      style={{
                        width: `${Math.min(100, Math.max(0, porcentajeGLP))}%`,
                        background: colorSemaforoGLP(porcentajeGLP),
                      }}
                    ></div>
                  </div>
                  <div className="text-xs text-gray-600 text-center">
                    {almacen.capacidadActualGLP.toFixed(1)} /{" "}
                    {almacen.capacidadMaximaGLP.toFixed(1)} m³
                  </div>
                </div>

                {/* Info adicional para almacenes secundarios */}
                {almacen.tipo === "SECUNDARIO" && (
                  <div className="mb-3 p-2 bg-green-50 rounded text-center">
                    <div className="text-xs text-green-700">
                      {/* 🔄 Recarga automática a las 00:00 */}
                    </div>
                  </div>
                )}

                {/* Lista de Pedidos Programados */}
                <div className="mb-4">
                  <h4 className="text-sm font-bold text-gray-800 mb-2 flex items-center gap-2">
                    📦 Pedidos Programados ({pedidosAsignados.length})
                    <button
                      onClick={() => {
                        // Cambiar al panel de pedidos en el menú derecho
                        const btnPedidos = document.querySelector('[data-panel="pedidos"]') as HTMLButtonElement;
                        if (btnPedidos) {
                          btnPedidos.click();
                        }
                        setClickedAlmacen(null);
                      }}
                      className="ml-auto text-xs bg-blue-500 hover:bg-blue-600 text-white px-2 py-1 rounded transition-colors"
                      title="Ver panel de pedidos"
                    >
                      Ver Panel
                    </button>
                  </h4>
                  {pedidosAsignados.length > 0 ? (
                    <div className="max-h-32 overflow-y-auto border border-gray-200 rounded p-2 bg-gray-50">
                      {pedidosAsignados.map((pedido) => (
                        <div key={pedido.codigo} className="mb-2 p-2 bg-white rounded border-l-4 border-blue-500">
                          <div className="flex justify-between items-start">
                            <div className="flex-1">
                              <div className="text-xs font-bold text-gray-800">{pedido.codigo}</div>
                              <div className="text-xs text-gray-600">
                                ({pedido.coordenada.x}, {pedido.coordenada.y}) • {pedido.volumenGLPAsignado.toFixed(1)}m³
                              </div>
                              <div className="text-xs text-gray-500">
                                {formatearFecha(pedido.fechaRegistro)}
                              </div>
                            </div>
                            <div className="text-xs">
                              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                pedido.estadoPedido === 'ENTREGADO' ? 'bg-green-100 text-green-800' :
                                pedido.estadoPedido === 'EN_TRANSITO' ? 'bg-blue-100 text-blue-800' :
                                pedido.estadoPedido === 'RETRASO' ? 'bg-red-100 text-red-800' :
                                'bg-yellow-100 text-yellow-800'
                              }`}>
                                {pedido.estadoPedido}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-xs text-gray-500 text-center p-2 bg-gray-50 rounded border">
                      No hay pedidos programados
                    </div>
                  )}
                </div>

                {/* Lista de Camiones Asignados */}
                <div className="mb-4">
                  <h4 className="text-sm font-bold text-gray-800 mb-2 flex items-center gap-2">
                    🚛 Unidades de Transporte ({camionesAsignados.length})
                    <button
                      onClick={() => {
                        // Cambiar al panel de camiones en el menú derecho
                        const btnCamiones = document.querySelector('[data-panel="camiones"]') as HTMLButtonElement;
                        if (btnCamiones) {
                          btnCamiones.click();
                        }
                        setClickedAlmacen(null);
                      }}
                      className="ml-auto text-xs bg-green-500 hover:bg-green-600 text-white px-2 py-1 rounded transition-colors"
                      title="Ver panel de camiones"
                    >
                      Ver Panel
                    </button>
                  </h4>
                  {camionesAsignados.length > 0 ? (
                    <div className="max-h-32 overflow-y-auto border border-gray-200 rounded p-2 bg-gray-50">
                      {camionesAsignados.map((camion) => (
                        <div key={camion.id} className="mb-2 p-2 bg-white rounded border-l-4 border-green-500">
                          <div className="flex justify-between items-start">
                            <div className="flex-1">
                              <div className="text-xs font-bold text-gray-800">Camión {camion.id}</div>
                              <div className="text-xs text-gray-600">
                                {camion.ubicacion} • {(camion.capacidadActualGLP || 0).toFixed(1)}/{(camion.capacidadMaximaGLP || 0).toFixed(1)}m³
                              </div>
                              <div className="text-xs text-gray-500">
                                Combustible: {(camion.combustibleActual || 0).toFixed(1)}/{(camion.combustibleMaximo || 0).toFixed(1)}L
                              </div>
                            </div>
                            <div className="text-xs">
                              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                camion.estado === 'Disponible' ? 'bg-green-100 text-green-800' :
                                camion.estado === 'En Ruta' ? 'bg-blue-100 text-blue-800' :
                                camion.estado === 'Averiado' ? 'bg-red-100 text-red-800' :
                                'bg-gray-100 text-gray-800'
                              }`}>
                                {camion.estado}
                              </span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-xs text-gray-500 text-center p-2 bg-gray-50 rounded border">
                      No hay camiones asignados
                    </div>
                  )}
                </div>
              </div>

              {/* Footer */}
              <div className="p-2 bg-gray-50 border-t">
                <button
                  className="w-full bg-gray-500 hover:bg-gray-600 text-white py-1 px-3 rounded text-xs transition-colors"
                  onClick={() => setClickedAlmacen(null)}
                >
                  Cerrar
                </button>
              </div>
            </div>
          );
        })()}

      {/* Modal para camión (panel lateral derecho) */}
      {clickedCamion &&
        (() => {
          const camion = camiones.find((c) => c.id === clickedCamion);
          const ruta = rutasCamiones.find((r) => r.id === clickedCamion);
          const numPedidos = ruta?.pedidos?.length || 0;
          const esAveriado = camion?.estado === "Averiado";
          const glpEntrega = calcularGLPEntregaPorCamion(
            clickedCamion,
            rutasCamiones,
            camiones
          );
          return (
            <div
              className="fixed top-1/2 right-0 transform -translate-y-1/2 bg-white border-l border-gray-300 rounded-l-lg shadow-2xl z-50 w-96 max-w-full p-6 flex flex-col"
              style={{ minHeight: "400px", maxHeight: "90vh" }}
            >
              {/* Header con título y botón cerrar */}
              <div className="flex items-center justify-between mb-4">
                <div className="font-bold text-lg">Camión: {clickedCamion}</div>
                <button
                  className="text-gray-500 hover:text-black text-2xl font-bold"
                  onClick={() => setClickedCamion(null)}
                  title="Cerrar"
                >
                  ×
                </button>
              </div>
              {camion && (
                <div className="text-sm mb-4">
                  Estado: {camion.estado}
                  <br />
                  Pedidos asignados: {numPedidos}
                  {ruta?.pedidos && ruta.pedidos.length > 0 && (
                    <>
                      <br />
                      Nombres pedidos: {ruta.pedidos.map(p => p.codigo).join(', ')}
                    </>
                  )}
                  <br />
                  Capacidad GLP:{" "}
                  {formatearCapacidadGLP(
                    camion.capacidadActualGLP,
                    camion.capacidadMaximaGLP
                  )}
                  <br />
                  GLP a entregar: {glpEntrega.toFixed(2)} m³
                  <br />
                  Combustible:{" "}
                  {formatearCombustible(
                    camion.combustibleActual,
                    camion.combustibleMaximo
                  )}
                  <br />
                  Distancia máxima: {camion.distanciaMaxima.toFixed(2)} km
                  <br />
                  Peso carga: {camion.pesoCarga.toFixed(2)}
                  <br />
                  Peso combinado: {camion.pesoCombinado.toFixed(2)}
                  <br />
                  Tara: {camion.tara}
                  <br />
                  Tipo: {camion.tipo}
                  <br />
                  Ubicación: {camion.ubicacion}
                  <br />
                  Progreso: {camion.porcentaje}
                </div>
              )}
              {esAveriado ? (
                <div className="text-red-600 font-bold text-center py-2">
                  🚛💥 CAMIÓN AVERIADO
                </div>
              ) : (
                <div className="flex flex-col gap-2">
                  <button
                    className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded disabled:opacity-50"
                    disabled={averiando === clickedCamion + "-1"}
                    onClick={() =>
                      handleAveriar(
                        clickedCamion,
                        1,
                        marcarCamionAveriado,
                        setAveriando,
                        setClickedCamion,
                        setSimulacionActiva,
                        {
                          horaActual,
                          horaSimulacion,
                          fechaHoraSimulacion,
                          fechaInicioSimulacion,
                          diaSimulacion,
                          tiempoRealSimulacion,
                          tiempoTranscurridoSimulado,
                          camiones,
                          rutasCamiones,
                          almacenes,
                          bloqueos,
                        },
                        setPollingActivo,
                        aplicarNuevaSolucionDespuesAveria
                      )
                    }
                  >
                    {averiando === clickedCamion + "-1"
                      ? "Averiando..."
                      : "Avería tipo 1"}
                  </button>
                  <button
                    className="bg-orange-500 hover:bg-orange-600 text-white px-3 py-1 rounded disabled:opacity-50"
                    disabled={averiando === clickedCamion + "-2"}
                    onClick={() =>
                      handleAveriar(
                        clickedCamion,
                        2,
                        marcarCamionAveriado,
                        setAveriando,
                        setClickedCamion,
                        setSimulacionActiva,
                        {
                          horaActual,
                          horaSimulacion,
                          fechaHoraSimulacion,
                          fechaInicioSimulacion,
                          diaSimulacion,
                          tiempoRealSimulacion,
                          tiempoTranscurridoSimulado,
                          camiones,
                          rutasCamiones,
                          almacenes,
                          bloqueos,
                        },
                        setPollingActivo,
                        aplicarNuevaSolucionDespuesAveria
                      )
                    }
                  >
                    {averiando === clickedCamion + "-2"
                      ? "Averiando..."
                      : "Avería tipo 2"}
                  </button>
                  <button
                    className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded disabled:opacity-50"
                    disabled={averiando === clickedCamion + "-3"}
                    onClick={() =>
                      handleAveriar(
                        clickedCamion,
                        3,
                        marcarCamionAveriado,
                        setAveriando,
                        setClickedCamion,
                        setSimulacionActiva,
                        {
                          horaActual,
                          horaSimulacion,
                          fechaHoraSimulacion,
                          fechaInicioSimulacion,
                          diaSimulacion,
                          tiempoRealSimulacion,
                          tiempoTranscurridoSimulado,
                          camiones,
                          rutasCamiones,
                          almacenes,
                          bloqueos,
                        },
                        setPollingActivo,
                        aplicarNuevaSolucionDespuesAveria
                      )
                    }
                  >
                    {averiando === clickedCamion + "-3"
                      ? "Averiando..."
                      : "Avería tipo 3"}
                  </button>
                </div>
              )}
              <button
                className="mt-4 bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded w-full"
                onClick={() => {
                  // Aquí se activa el menú inferior para mostrar la ruta
                  const event = new CustomEvent("mostrarRutaCamion", {
                    detail: { camionId: clickedCamion },
                  });
                  window.dispatchEvent(event);
                  setClickedCamion(null);
                }}
              >
                📍 Mostrar ruta del camión
              </button>
            </div>
          );
        })()}

      {/* Controles de simulación debajo del mapa */}
      {controlesSimulacionHabilitados && (
        <div className="flex items-center gap-4 mt-2 justify-center w-full">
          <button
            onClick={() => {
              if (!running) {
                // Solo iniciar el contador cuando se presiona "Iniciar" por primera vez
                iniciarContadorTiempo();
              }
              setRunning((prev) => !prev);
            }}
            className={`px-4 py-1 rounded text-white ${
              !simulacionActiva && running
                ? "bg-yellow-500 hover:bg-yellow-600"
                : "bg-blue-500 hover:bg-blue-600"
            }`}
          >
            {!simulacionActiva && running
              ? "Pausado (Avería)"
              : running
              ? "Pausar"
              : "Iniciar"}
          </button>
          {/* Botón de control de velocidad y de iniciar solo si corresponde */}
          {tipoSimulacion !== "SEMANAL" && (
            <button
              onClick={() =>
                setMostrarControlVelocidad(!mostrarControlVelocidad)
              }
              className="px-4 py-1 rounded text-white bg-green-500 hover:bg-green-600"
            >
              {mostrarControlVelocidad
                ? "⚡ Ocultar Control"
                : "⚡ Control Velocidad"}
            </button>
          )}
          {/* Control de velocidad */}
          {tipoSimulacion !== "SEMANAL" && mostrarControlVelocidad && (
            <ControlVelocidad
              camiones={camiones}
              segundosPorNodo={segundosPorNodo}
              onSegundosPorNodoChange={setSegundosPorNodo}
              onIntervaloChange={setIntervalo}
              intervaloActual={intervalo}
              tipoSimulacion={tipoSimulacion}
            />
          )}
          <label className="flex items-center gap-1 text-sm">
            Segundos por nodo:
            <input
              type="number"
              min={0.1}
              max={100}
              step={0.1}
              value={segundosPorNodo}
              onChange={(e) => setSegundosPorNodo(parseFloat(e.target.value))}
              className={`border rounded px-2 py-0.5 w-20 ${
                tipoSimulacion !== "DIARIA"
                  ? "bg-gray-100 cursor-not-allowed"
                  : ""
              }`}
              disabled={tipoSimulacion !== "DIARIA"}
            />
            s
          </label>
          <label className="flex items-center gap-1 text-sm text-gray-600">
            Intervalo: {intervalo}ms
          </label>
        </div>
      )}
    </div>
  );
};

export default Mapa;
