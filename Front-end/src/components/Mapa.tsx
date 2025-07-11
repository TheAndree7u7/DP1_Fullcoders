import { useEffect, useRef, useState } from 'react';
import { useSimulacion } from '../context/SimulacionContext';
import { useMapaWithSimulacion } from '../hooks/useMapaContext';
import { CONFIGURACION_TIEMPO } from '../config/simulacion';
import type { Coordenada } from '../types';
import almacenCentralIcon from '../assets/almacen_central.svg';
import almacenIntermedioIcon from '../assets/almacen_intermedio.svg';
import clienteIcon from '../assets/cliente.svg';

import { CAMION_COLORS, ESTADO_COLORS } from '../config/colors';
import { ChevronDown, ChevronUp } from 'lucide-react';
import { 
  parseCoord, 
  calcularRotacion, 
  getPedidosPendientes
} from './mapa/utils';

interface CamionVisual {
  id: string;
  color: string;
  ruta: Coordenada[];
  posicion: Coordenada;
  rotacion: number;
}

const GRID_WIDTH = 70;
const GRID_HEIGHT = 50;
const CELL_SIZE = 14;
const SVG_WIDTH = GRID_WIDTH * CELL_SIZE;
const SVG_HEIGHT = GRID_HEIGHT * CELL_SIZE;

// Parametrización del grosor de línea de bloqueos
const BLOQUEO_STROKE_WIDTH = 4;

interface MapaProps {
  elementoResaltado?: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null;
  contextType?: 'simulacion' | 'tiempo-real';
}

const Mapa: React.FC<MapaProps> = ({ elementoResaltado, contextType = 'simulacion' }) => {
  const [camionesVisuales, setCamionesVisuales] = useState<CamionVisual[]>([]);
  const [running, setRunning] = useState(false);
  const [intervalo, setIntervalo] = useState(CONFIGURACION_TIEMPO.INTERVALO_ACTUALIZACION_MS); // Usar configuración parametrizable
  const intervalRef = useRef<number | null>(null);
  
  // Siempre usar contexto de simulación por ahora
  const simulacionMapa = useMapaWithSimulacion();
  const simulacionContext = useSimulacion();
  
  // Usar el contexto de simulación
  const mapaContext = simulacionMapa;
  
  const { 
    camiones, 
    rutasCamiones, 
    almacenes, 
    cargando, 
    bloqueos, 
    simulacionActiva,
    onTimeToggle
  } = mapaContext;
  
  // Extraer funciones específicas de simulación cuando sea necesario
  const actualizarAlmacenes = contextType === 'simulacion' ? simulacionContext?.actualizarAlmacenes : undefined;
  const iniciarContadorTiempo = contextType === 'simulacion' ? simulacionContext?.iniciarContadorTiempo : undefined;
  
  // Estados del componente
  const [tooltipCamion, setTooltipCamion] = useState<string | null>(null);
  const [tooltipPos, setTooltipPos] = useState<{x: number, y: number} | null>(null);
  const [clickedCamion, setClickedCamion] = useState<string | null>(null);
  const [clickedPos, setClickedPos] = useState<{x: number, y: number} | null>(null);
  const [leyendaVisible, setLeyendaVisible] = useState(false);
  const [clickedAlmacen, setClickedAlmacen] = useState<string | null>(null);
  const [clickedAlmacenPos, setClickedAlmacenPos] = useState<{x: number, y: number} | null>(null);

  const pedidosPendientes = getPedidosPendientes(rutasCamiones, camiones);

  // Inicializar camiones visuales cuando cambien las rutas
  useEffect(() => {
    const iniciales = rutasCamiones.map((info: any, idx: number) => {
      const rutaValida = info.ruta.filter((nodo: any) => nodo && typeof nodo === 'string');
      const ruta = rutaValida.map(parseCoord);
      
      if (ruta.length === 0) {
        console.warn('🚨 Ruta vacía para camión:', info.id);
        ruta.push({ x: 0, y: 0 });
      }
      
      return {
        id: info.id,
        color: CAMION_COLORS[idx % CAMION_COLORS.length],
        ruta,
        posicion: ruta[0],
        rotacion: 0,
      };
    });
    setCamionesVisuales(iniciales);
  }, [rutasCamiones]);

  // Controlar el intervalo de animación
  useEffect(() => {
    if (running && simulacionActiva) {
      intervalRef.current = window.setInterval(() => {
        if (onTimeToggle) {
          onTimeToggle();
        }
      }, intervalo);
    } else {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    }
    return () => {
      if (intervalRef.current) window.clearInterval(intervalRef.current);
    };
  }, [running, intervalo, onTimeToggle, simulacionActiva]);

  // Actualizar posiciones de camiones cuando cambien sus estados
  useEffect(() => {
    setCamionesVisuales(
      rutasCamiones.map((info, idx) => {
        const rutaValida = info.ruta.filter(nodo => nodo && typeof nodo === 'string');
        const rutaCoords = rutaValida.map(parseCoord);
        
        if (rutaCoords.length === 0) {
          console.warn('🚨 Ruta vacía para camión:', info.id);
          rutaCoords.push({ x: 0, y: 0 });
        }
        
        const estadoCamion = camiones.find(c => c.id === info.id);
        
        // Determinar posición actual
        let currentPos = rutaCoords[0];
        
        if (estadoCamion && estadoCamion.ubicacion && typeof estadoCamion.ubicacion === 'string') {
          try {
            currentPos = parseCoord(estadoCamion.ubicacion);
          } catch (error) {
            console.warn('Error parsing location:', estadoCamion.ubicacion, error);
          }
        }
        
        // Calcular rotación basada en el movimiento
        let rotacion = 0;
        if (estadoCamion && rutaCoords.length > 1) {
          const porcentaje = estadoCamion.porcentaje || 0;
          const currentIdx = Math.floor(porcentaje);
          
          if (currentIdx + 1 < rutaCoords.length) {
            const nextPos = rutaCoords[currentIdx + 1];
            rotacion = calcularRotacion(currentPos, nextPos);
          } else if (currentIdx > 0) {
            const prevPos = rutaCoords[currentIdx - 1];
            rotacion = calcularRotacion(prevPos, currentPos);
          }
        }
        
        // Calcular ruta restante
        const porcentaje = estadoCamion ? estadoCamion.porcentaje || 0 : 0;
        const idxRest = Math.ceil(porcentaje);
        const rutaRestante = rutaCoords.slice(idxRest);
        
        return {
          id: info.id,
          color: CAMION_COLORS[idx % CAMION_COLORS.length],
          ruta: rutaRestante,
          posicion: currentPos,
          rotacion: rotacion
        } as CamionVisual;
      })
    );
  }, [camiones, rutasCamiones]);

  if (cargando) {
    return (
      <div className="w-full h-full flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-lg font-semibold text-gray-700">Cargando nueva solución...</p>
          <p className="text-sm text-gray-500">Los camiones iniciarán sus rutas en breve</p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full h-full flex flex-col">
      <div className="flex items-start gap-3 flex-1">
        {/* Leyenda lateral compacta */}
        <div className="bg-white rounded-lg shadow-md border border-gray-200 p-2 w-32 flex-shrink-0">
          <button
            onClick={() => setLeyendaVisible(!leyendaVisible)}
            className="flex items-center justify-between w-full text-left text-xs font-semibold text-gray-800 hover:text-gray-900 mb-2"
          >
            <span>LEYENDA</span>
            {leyendaVisible ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
          </button>
          
          {leyendaVisible && (
            <div className="space-y-1.5">
              <div className="flex items-center gap-1.5">
                <img src={almacenCentralIcon} alt="Almacén Central" className="w-4 h-4" />
                <span className="text-xs text-gray-700">A. Central</span>
              </div>
              <div className="flex items-center gap-1.5">
                <img src={almacenIntermedioIcon} alt="Almacén Intermedio" className="w-4 h-4" />
                <span className="text-xs text-gray-700">A. Intermedio</span>
              </div>
              <div className="flex items-center gap-1.5">
                <img src={clienteIcon} alt="Cliente" className="w-4 h-4" />
                <span className="text-xs text-gray-700">Cliente</span>
              </div>
              <div className="flex items-center gap-1.5">
                <svg width="16" height="12" viewBox="0 0 16 12" className="border border-gray-300 rounded">
                  <rect x="2" y="4" width="10" height="4" rx="0.5" fill="#3b82f6" stroke="black" strokeWidth="0.3" />
                  <rect x="10" y="5" width="3" height="2" rx="0.3" fill="#3b82f6" stroke="black" strokeWidth="0.3" />
                  <circle cx="4" cy="9" r="1" fill="black" />
                  <circle cx="8" cy="9" r="1" fill="black" />
                  <circle cx="11" cy="9" r="1" fill="black" />
                  <polygon points="13,6 12,5.5 12,6.5" fill="white" stroke="black" strokeWidth="0.2" />
                </svg>
                <span className="text-xs text-gray-700">Camión</span>
              </div>
              <div className="flex items-center gap-1.5">
                <div className="w-4 h-0.5 border-t border-dashed border-blue-500"></div>
                <span className="text-xs text-gray-700">Ruta</span>
              </div>
              <div className="flex items-center gap-1.5">
                <div className="w-4 h-0.5 bg-red-600 rounded-full"></div>
                <span className="text-xs text-gray-700">Bloqueos</span>
              </div>
              <div className="pt-1 border-t border-gray-200">
                <div className="text-xs font-medium text-gray-600 mb-1">Estados:</div>
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
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Contenedor del mapa */}
        <div className="flex-1 flex flex-col items-center justify-center">
          <div className="w-full max-w-full overflow-auto">
            <svg
              width={SVG_WIDTH}
              height={SVG_HEIGHT}
              className="border border-gray-500 bg-white rounded-xl mx-auto"
              style={{ maxWidth: '100%', height: 'auto' }}
              viewBox={`0 0 ${SVG_WIDTH} ${SVG_HEIGHT}`}
              preserveAspectRatio="xMidYMid meet"
              onClick={(evt) => {
                // Cerrar modales si se hace click en un área vacía del mapa
                if (evt.target === evt.currentTarget) {
                  setClickedCamion(null);
                  setClickedAlmacen(null);
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
              }}
            />
            
            {/* Grid */}
            {[...Array(GRID_WIDTH + 1)].map((_, i) => (
              <line key={`v-${i}`} x1={i * CELL_SIZE} y1={0} x2={i * CELL_SIZE} y2={SVG_HEIGHT} stroke="#d1d5db" strokeWidth={1} />
            ))}
            {[...Array(GRID_HEIGHT + 1)].map((_, i) => (
              <line key={`h-${i}`} x1={0} y1={i * CELL_SIZE} x2={SVG_WIDTH} y2={i * CELL_SIZE} stroke="#d1d5db" strokeWidth={1} />
            ))}

            {/* Bloqueos */}
            {bloqueos && bloqueos.map((bloqueo, idx) => (
              <polyline
                key={`bloqueo-${idx}`}
                fill="none"
                stroke="#dc2626"
                strokeWidth={BLOQUEO_STROKE_WIDTH}
                strokeLinecap="round"
                strokeLinejoin="round"
                points={bloqueo.coordenadas.map((coord: Coordenada) => `${coord.x * CELL_SIZE},${coord.y * CELL_SIZE}`).join(' ')}
              />
            ))}

            {/* Clientes/Pedidos */}
            {pedidosPendientes.map(pedido => {
              const esResaltado = elementoResaltado?.tipo === 'pedido' && elementoResaltado?.id === pedido.codigo;
              return (
                <g key={pedido.codigo}>
                  {esResaltado && (
                    <circle
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
                        attributeName="r"
                        values="20;30;20"
                        dur="2s"
                        repeatCount="indefinite"
                      />
                    </circle>
                  )}
                  
                  <image
                    href={clienteIcon}
                    x={pedido.coordenada.x * CELL_SIZE - 15}
                    y={pedido.coordenada.y * CELL_SIZE - 15}
                    width={30}
                    height={30}
                  />
                  <text
                    x={pedido.coordenada.x * CELL_SIZE}
                    y={pedido.coordenada.y * CELL_SIZE + 25}
                    textAnchor="middle"
                    fontSize="10"
                    fill="#dc2626"
                    fontWeight="bold"
                    stroke="#fff"
                    strokeWidth="0.5"
                  >
                    {pedido.codigo}
                  </text>
                </g>
              );
            })}

            {/* Almacenes */}
            {almacenes.map(almacen => {
              const esResaltado = elementoResaltado?.tipo === 'almacen' && elementoResaltado?.id === almacen.id;
              return (
                <g key={almacen.id} style={{ cursor: 'pointer' }}>
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
                  
                  <image
                    href={almacen.tipo === 'CENTRAL' ? almacenCentralIcon : almacenIntermedioIcon}
                    x={almacen.coordenada.x * CELL_SIZE - 20}
                    y={almacen.coordenada.y * CELL_SIZE - 20}
                    width={40}
                    height={40}
                    onClick={evt => {
                      if (!clickedAlmacen) {
                        setClickedAlmacen(almacen.id);
                        setClickedAlmacenPos({ x: evt.clientX, y: evt.clientY });
                        if (actualizarAlmacenes) {
                          actualizarAlmacenes();
                        }
                      }
                    }}
                  />
                  <text
                    x={almacen.coordenada.x * CELL_SIZE}
                    y={almacen.coordenada.y * CELL_SIZE + 30}
                    textAnchor="middle"
                    fontSize="12"
                    fill={almacen.tipo === 'CENTRAL' ? '#2563eb' : '#16a34a'}
                    fontWeight="bold"
                    stroke="#fff"
                    strokeWidth="0.5"
                    onClick={evt => {
                      if (!clickedAlmacen) {
                        setClickedAlmacen(almacen.id);
                        setClickedAlmacenPos({ x: evt.clientX, y: evt.clientY });
                        if (actualizarAlmacenes) {
                          actualizarAlmacenes();
                        }
                      }
                    }}
                  >
                    {almacen.nombre}
                  </text>
                </g>
              );
            })}

            {/* Rutas de camiones */}
            {camionesVisuales
              .filter(camion => {
                const estadoCamion = camiones.find(c => c.id === camion.id);
                return estadoCamion?.estado !== 'Entregado' && 
                       camion.ruta.length > 1;
              })
              .map(camion => (
                <polyline
                  key={`ruta-${camion.id}`}
                  fill="none"
                  stroke={camion.color}
                  strokeWidth={2}
                  strokeDasharray="4 2"
                  points={camion.ruta.map((p: Coordenada) => `${p.x * CELL_SIZE},${p.y * CELL_SIZE}`).join(' ')}
                />
              ))}

            {/* Camiones */}
            {camionesVisuales
              .filter(camion => camiones.find(c => c.id === camion.id)?.estado !== 'Entregado')
              .map(camion => {
                 const estadoCamion = camiones.find(c => c.id === camion.id);
                 const esAveriado = estadoCamion?.estado?.includes('Averiado') || false;
                 const esEnMantenimiento = estadoCamion?.estado?.includes('Mantenimiento') || false;
                 const esResaltado = elementoResaltado?.tipo === 'camion' && elementoResaltado?.id === camion.id;
                 const { posicion, rotacion, color } = camion;
                 
                 // Determinar color basado en estado
                 const colorFinal = esAveriado ? ESTADO_COLORS.AVERIADO : 
                                   esEnMantenimiento ? ESTADO_COLORS.MANTENIMIENTO : 
                                   color;
                 
                 const cx = posicion.x * CELL_SIZE;
                 const cy = posicion.y * CELL_SIZE;
                 
                 return (
                   <g key={camion.id}>
                     <g
                       transform={`translate(${cx}, ${cy}) rotate(${rotacion})`}
                       style={{ 
                         transition: 'transform 0.8s ease-in-out', 
                         cursor: 'pointer' 
                       }}
                       onMouseEnter={evt => {
                         if (!clickedCamion) {
                           setTooltipCamion(camion.id);
                           setTooltipPos({ x: evt.clientX, y: evt.clientY });
                         }
                       }}
                       onMouseMove={evt => {
                         if (!clickedCamion && tooltipCamion === camion.id) {
                           setTooltipPos({ x: evt.clientX, y: evt.clientY });
                         }
                       }}
                       onMouseLeave={() => {
                         setTooltipCamion(null);
                       }}
                       onClick={evt => {
                         if (!clickedCamion) {
                           setClickedCamion(camion.id);
                           setClickedPos({ x: evt.clientX, y: evt.clientY });
                           setTooltipCamion(null);
                         }
                       }}
                     >
                       {esResaltado && (
                         <circle
                           cx={0}
                           cy={0}
                           r={25}
                           fill="none"
                           stroke="#f59e0b"
                           strokeWidth={3}
                           strokeDasharray="8 4"
                           opacity={0.9}
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
                       
                       <rect x={-8} y={-3} width={16} height={6} rx={1} fill={colorFinal} stroke="black" strokeWidth={0.5} />
                       <rect x={6} y={-2} width={4} height={4} rx={0.5} fill={colorFinal} stroke="black" strokeWidth={0.5} />
                       <circle cx={-5} cy={4} r={1.5} fill="black" />
                       <circle cx={2} cy={4} r={1.5} fill="black" />
                       <circle cx={7} cy={4} r={1.5} fill="black" />
                       <polygon points="10,0 8,-1.5 8,1.5" fill="white" stroke="black" strokeWidth={0.3} />
                       <line x1={-6} y1={-1} x2={4} y2={-1} stroke="black" strokeWidth={0.3} opacity={0.6} />
                       <line x1={-6} y1={1} x2={4} y2={1} stroke="black" strokeWidth={0.3} opacity={0.6} />
                       
                       {esAveriado && (
                         <text x={0} y={-10} textAnchor="middle" fontSize="8" fill="#dc2626" fontWeight="bold">
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

      {/* Tooltip para camión (hover) */}
      {tooltipCamion && tooltipPos && (
        (() => {
          const camion = camiones.find(c => c.id === tooltipCamion);
          const ruta = rutasCamiones.find(r => r.id === tooltipCamion);
          const numPedidos = ruta?.pedidos?.length || 0;
          
          return (
            <div
              style={{
                position: 'fixed',
                left: tooltipPos.x + 10,
                top: tooltipPos.y + 10,
                background: 'white',
                border: '1px solid #ccc',
                borderRadius: 8,
                padding: 12,
                zIndex: 1000,
                boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
              }}
            >
              <div className="mb-2 font-bold">Camión: {tooltipCamion}</div>
              {camion && (
                <div className="text-xs mb-2">
                  Estado: {camion.estado}<br />
                  Pedidos asignados: {numPedidos}<br />
                  Capacidad GLP: {camion.capacidadActualGLP?.toFixed(2) || 'N/A'} / {camion.capacidadMaximaGLP || 'N/A'}<br />
                  Combustible: {camion.combustibleActual?.toFixed(2) || 'N/A'} / {camion.combustibleMaximo || 'N/A'}<br />
                  Ubicación: {camion.ubicacion}<br />
                  Progreso: {camion.porcentaje || 0}%
                </div>
              )}
            </div>
          );
        })()
      )}

      {/* Modal para camión (click) */}
      {clickedCamion && clickedPos && (
        (() => {
          const camion = camiones.find(c => c.id === clickedCamion);
          const ruta = rutasCamiones.find(r => r.id === clickedCamion);
          const numPedidos = ruta?.pedidos?.length || 0;
          const esAveriado = camion?.estado?.includes('Averiado') || false;
          
          return (
            <div
              style={{
                position: 'fixed',
                left: clickedPos.x + 10,
                top: clickedPos.y + 10,
                background: 'white',
                border: '1px solid #ccc',
                borderRadius: 8,
                padding: 12,
                zIndex: 1000,
                boxShadow: '0 2px 8px rgba(0,0,0,0.15)'
              }}
            >
              <div className="mb-2 font-bold">Camión: {clickedCamion}</div>
              {camion && (
                <div className="text-xs mb-2">
                  Estado: {camion.estado}<br />
                  Pedidos asignados: {numPedidos}<br />
                  Capacidad GLP: {camion.capacidadActualGLP?.toFixed(2) || 'N/A'} / {camion.capacidadMaximaGLP || 'N/A'}<br />
                  Combustible: {camion.combustibleActual?.toFixed(2) || 'N/A'} / {camion.combustibleMaximo || 'N/A'}<br />
                  Ubicación: {camion.ubicacion}<br />
                  Progreso: {camion.porcentaje || 0}%
                </div>
              )}
              {esAveriado ? (
                <div className="text-red-600 font-bold text-center py-2">
                  🚛💥 CAMIÓN AVERIADO
                </div>
              ) : (
                <div className="flex flex-col gap-2">
                  <button
                    className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded"
                    onClick={() => {
                      console.log('Avería leve simulada');
                      setClickedCamion(null);
                    }}
                  >
                    Avería leve
                  </button>
                  <button
                    className="bg-orange-500 hover:bg-orange-600 text-white px-3 py-1 rounded"
                    onClick={() => {
                      console.log('Avería moderada simulada');
                      setClickedCamion(null);
                    }}
                  >
                    Avería moderada
                  </button>
                  <button
                    className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded"
                    onClick={() => {
                      console.log('Avería grave simulada');
                      setClickedCamion(null);
                    }}
                  >
                    Avería grave
                  </button>
                </div>
              )}
              <button
                className="mt-2 bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded w-full"
                onClick={() => {
                  const event = new CustomEvent('mostrarRutaCamion', { detail: { camionId: clickedCamion } });
                  window.dispatchEvent(event);
                  setClickedCamion(null);
                }}
              >
                📍 Mostrar ruta del camión
              </button>
              <button
                className="mt-2 text-gray-500 hover:text-black"
                onClick={() => setClickedCamion(null)}
              >
                Cerrar
              </button>
            </div>
          );
        })()
      )}

      {/* Modal para almacén (click) */}
      {clickedAlmacen && clickedAlmacenPos && (
        (() => {
          const almacen = almacenes.find(a => a.id === clickedAlmacen);
          
          if (!almacen) return null;

          const porcentajeGLP = almacen.capacidadMaximaGLP > 0 
            ? (almacen.capacidadActualGLP / almacen.capacidadMaximaGLP) * 100 
            : 0;

          const modalWidth = 280;
          const modalHeight = 180;
          const viewportWidth = window.innerWidth;
          const viewportHeight = window.innerHeight;
          
          let modalLeft = clickedAlmacenPos.x + 10;
          let modalTop = clickedAlmacenPos.y + 10;
          
          if (modalLeft + modalWidth > viewportWidth) {
            modalLeft = clickedAlmacenPos.x - modalWidth - 10;
          }
          
          if (modalTop + modalHeight > viewportHeight) {
            modalTop = clickedAlmacenPos.y - modalHeight - 10;
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
                maxHeight: modalHeight
              }}
              onClick={(e) => e.stopPropagation()}
            >
              <div className="p-3 bg-blue-600 text-white">
                <div className="font-bold text-sm">{almacen.nombre}</div>
                <div className="text-xs opacity-90">
                  {almacen.tipo === 'CENTRAL' ? 'Almacén Central' : 'Almacén Secundario'} • ({almacen.coordenada.x}, {almacen.coordenada.y})
                </div>
              </div>
              
              <div className="p-3">
                <div className="mb-3 text-center">
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    almacen.activo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  }`}>
                    {almacen.activo ? '🟢 Activo' : '🔴 Inactivo'}
                  </span>
                </div>
                
                <div className="mb-3">
                  <div className="flex justify-between items-center mb-1">
                    <span className="text-xs font-medium text-gray-700">Gas Licuado (GLP)</span>
                    <span className="text-xs font-bold text-blue-600">{porcentajeGLP.toFixed(1)}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2 mb-2">
                    <div 
                      className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${Math.min(100, Math.max(0, porcentajeGLP))}%` }}
                    ></div>
                  </div>
                  <div className="text-xs text-gray-600 text-center">
                    {almacen.capacidadActualGLP.toFixed(1)} / {almacen.capacidadMaximaGLP.toFixed(1)} m³
                  </div>
                </div>

                {almacen.tipo === 'INTERMEDIO' && (
                  <div className="mb-3 p-2 bg-green-50 rounded text-center">
                    <div className="text-xs text-green-700">
                      🔄 Recarga automática a las 00:00
                    </div>
                  </div>
                )}
              </div>

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
        })()
      )}

      {/* Controles del mapa */}
      <div className="flex items-center gap-4 mt-2 justify-center flex-wrap">
        <button
          onClick={() => {
            if (!running) {
              if (iniciarContadorTiempo) {
                iniciarContadorTiempo();
              }
            }
            setRunning(prev => !prev);
          }}
          className={`px-4 py-1 rounded text-white ${
            !simulacionActiva && running 
              ? 'bg-yellow-500 hover:bg-yellow-600' 
              : 'bg-blue-500 hover:bg-blue-600'
          }`}
        >
          {!simulacionActiva && running 
            ? 'Pausado (Avería)' 
            : running 
              ? 'Pausar' 
              : 'Iniciar'
          }
        </button>
        
        <label className="flex items-center gap-1 text-sm">
          Velocidad:
          <select
            value={intervalo}
            onChange={e => setIntervalo(parseInt(e.target.value))}
            className="border rounded px-2 py-0.5"
          >
            <option value={CONFIGURACION_TIEMPO.VELOCIDAD_ANIMACION.RAPIDA}>Rápida</option>
            <option value={CONFIGURACION_TIEMPO.VELOCIDAD_ANIMACION.NORMAL}>Normal</option>
            <option value={CONFIGURACION_TIEMPO.VELOCIDAD_ANIMACION.LENTA}>Lenta</option>
          </select>
        </label>
        
        <label className="flex items-center gap-1 text-sm">
          Duración paquete:
          <select
            value={CONFIGURACION_TIEMPO.DURACION_PAQUETE_MINUTOS}
            onChange={e => {
              const nuevaDuracion = parseInt(e.target.value);
              CONFIGURACION_TIEMPO.DURACION_PAQUETE_MINUTOS = nuevaDuracion;
              console.log(`Duración de paquete actualizada a ${nuevaDuracion} minutos`);
            }}
            className="border rounded px-2 py-0.5"
          >
            <option value={60}>1 hora</option>
            <option value={120}>2 horas</option>
            <option value={180}>3 horas</option>
            <option value={240}>4 horas</option>
          </select>
        </label>
        
        <div className="text-xs text-gray-600">
          {intervalo >= 2000 ? 'Lento' : intervalo >= 1000 ? 'Normal' : 'Rápido'} | 
          Paquete: {CONFIGURACION_TIEMPO.DURACION_PAQUETE_MINUTOS}min
        </div>
        
        {/* Indicador de estado */}
        {running && (() => {
          const todosEntregados = camiones.every(camion => 
            camion.estado === "Entregado" || camion.estado === "Disponible"
          );
          
          if (todosEntregados && camiones.length > 0) {
            return (
              <div className="flex items-center gap-1 text-sm text-green-600">
                <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                <span>Rutas completadas - Cargando nueva solución...</span>
              </div>
            );
          }
          
          const camionesEnRuta = camiones.filter(camion => 
            camion.estado === "En Camino"
          ).length;
          
          if (camionesEnRuta > 0) {
            return (
              <div className="flex items-center gap-1 text-sm text-blue-600">
                <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse"></div>
                <span>{camionesEnRuta} camiones en ruta</span>
              </div>
            );
          }
          
          return null;
        })()}
      </div>
    </div>
  );
};

export default Mapa;