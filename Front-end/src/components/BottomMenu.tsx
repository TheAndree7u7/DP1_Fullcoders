import React, { useState, useEffect } from 'react';
import { ChevronDown, MapPin, Package, Truck, Navigation } from 'lucide-react';
import { useSimulacion } from '../context/SimulacionContext';
import type { Coordenada } from '../types';

interface BottomMenuProps {
  expanded: boolean;
  setExpanded: (value: boolean) => void;
}

interface NodoRuta {
  coordenada: Coordenada;
  tipo: 'inicio' | 'ruta' | 'pedido' | 'almacen' | 'fin';
  indice: number;
  completado: boolean;
  actual: boolean;
  pedido?: {
    codigo: string;
    volumenGLP: number;
  };
}

const BottomMenu: React.FC<BottomMenuProps> = ({ expanded, setExpanded }) => {
  const { rutasCamiones, camiones } = useSimulacion();
  const [camionSeleccionado, setCamionSeleccionado] = useState<string | null>(null);
  const timelineRef = React.useRef<HTMLDivElement>(null);

  // Función para parsear coordenadas
  const parseCoord = (s: string): Coordenada => {
    const match = s.match(/\((\d+),\s*(\d+)\)/);
    if (!match) throw new Error(`Coordenada inválida: ${s}`);
    return { x: parseInt(match[1]), y: parseInt(match[2]) };
  };

  // Función para obtener la ruta procesada del camión seleccionado
  const obtenerRutaProcesada = (): NodoRuta[] => {
    if (!camionSeleccionado) return [];

    const ruta = rutasCamiones.find(r => r.id === camionSeleccionado);
    const camion = camiones.find(c => c.id === camionSeleccionado);
    
    if (!ruta || !camion) return [];

    const nodos: NodoRuta[] = [];
    const porcentajeActual = camion.porcentaje;

    ruta.ruta.forEach((coordStr, index) => {
      const coordenada = parseCoord(coordStr);
      const completado = index < porcentajeActual;
      const actual = index === Math.floor(porcentajeActual);

      // Determinar el tipo de nodo
      let tipo: NodoRuta['tipo'] = 'ruta';
      
      if (index === 0) {
        tipo = 'inicio';
      } else if (index === ruta.ruta.length - 1) {
        tipo = 'fin';
      } else {
        // Verificar si es un pedido
        const pedidoEnNodo = ruta.pedidos.find(p => 
          p.coordenada.x === coordenada.x && p.coordenada.y === coordenada.y
        );
        if (pedidoEnNodo) {
          tipo = 'pedido';
        }
        // Verificar si es un almacén (coordenadas específicas de almacenes)
        else if ((coordenada.x === 12 && coordenada.y === 8) || 
                 (coordenada.x === 25 && coordenada.y === 15) ||
                 (coordenada.x === 40 && coordenada.y === 25)) {
          tipo = 'almacen';
        }
      }

      const nodo: NodoRuta = {
        coordenada,
        tipo,
        indice: index,
        completado,
        actual,
        pedido: tipo === 'pedido' ? (() => {
          const pedido = ruta.pedidos.find(p => 
            p.coordenada.x === coordenada.x && p.coordenada.y === coordenada.y
          );
          return pedido ? {
            codigo: pedido.codigo,
            volumenGLP: pedido.volumenGLPAsignado || 0
          } : undefined;
        })() : undefined
      };

      nodos.push(nodo);
    });

    return nodos;
  };

  const rutaProcesada = obtenerRutaProcesada();

  // Hacer scroll automático al nodo actual cuando cambie
  useEffect(() => {
    if (camionSeleccionado && rutaProcesada.length > 0) {
      scrollToCurrentNode();
    }
  }, [camionSeleccionado, rutaProcesada]);

  // Función para hacer scroll al nodo actual
  const scrollToCurrentNode = () => {
    if (timelineRef.current && camionSeleccionado) {
      const camion = camiones.find(c => c.id === camionSeleccionado);
      if (camion) {
        const currentNodeIndex = Math.floor(camion.porcentaje);
        const nodeElement = timelineRef.current.children[currentNodeIndex] as HTMLElement;
        if (nodeElement) {
          nodeElement.scrollIntoView({
            behavior: 'smooth',
            block: 'nearest',
            inline: 'center'
          });
        }
      }
    }
  };

  // Función para obtener el ícono según el tipo de nodo
  const obtenerIcono = (tipo: NodoRuta['tipo']) => {
    switch (tipo) {
      case 'inicio':
        return <Truck size={16} className="text-green-600" />;
      case 'pedido':
        return <Package size={16} className="text-blue-600" />;
      case 'almacen':
        return <MapPin size={16} className="text-orange-600" />;
      case 'fin':
        return <Truck size={16} className="text-red-600" />;
      default:
        return <div className="w-2 h-2 bg-gray-400 rounded-full" />;
    }
  };

  // Función para obtener el color de fondo según el estado
  const obtenerColorFondo = (nodo: NodoRuta) => {
    if (nodo.actual) return 'bg-blue-100 border-blue-500';
    if (nodo.completado) return 'bg-green-100 border-green-500';
    return 'bg-gray-100 border-gray-300';
  };

  if (!expanded) return null;

  return (
    <div className="bg-white rounded-t-xl shadow-lg border-t-2 border-gray-200">

      {/* Header */}
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <h3 className="text-lg font-bold text-gray-800">Ruta del Camión</h3>
            {camionSeleccionado && (
              <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded text-sm font-medium">
                {camionSeleccionado}
              </span>
            )}
          </div>
          <button
            onClick={() => setExpanded(false)}
            className="p-1 hover:bg-gray-100 rounded"
            title="Ocultar menú"
          >
            <ChevronDown size={20} />
          </button>
        </div>

        {/* Selector de camión */}
        <div className="mt-3">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Seleccionar camión:
          </label>
          <select
            value={camionSeleccionado || ''}
            onChange={(e) => setCamionSeleccionado(e.target.value || null)}
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">Selecciona un camión</option>
            {rutasCamiones.map((ruta) => (
              <option key={ruta.id} value={ruta.id}>
                Camión {ruta.id} ({ruta.pedidos.length} pedidos)
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Contenido de la ruta */}
      {camionSeleccionado && rutaProcesada.length > 0 && (
        <div className="p-4">
          {/* Información compacta en la parte superior */}
          <div className="flex justify-between items-center mb-3 text-sm">
            <div className="flex gap-4">
              <span className="font-medium">Nodos: <span className="text-blue-600 font-bold">{rutaProcesada.length}</span></span>
              <span className="font-medium">Actual: <span className="text-green-600 font-bold">{Math.floor(camiones.find(c => c.id === camionSeleccionado)?.porcentaje || 0) + 1}</span></span>
              <span className="font-medium">Pedidos: <span className="text-orange-600 font-bold">{rutaProcesada.filter(n => n.tipo === 'pedido').length}</span></span>
            </div>
            <div className="flex items-center gap-2">
              <span className="font-medium">Progreso: <span className="text-purple-600 font-bold">{Math.round((camiones.find(c => c.id === camionSeleccionado)?.porcentaje || 0) / (rutaProcesada.length - 1) * 100)}%</span></span>
              <button
                onClick={scrollToCurrentNode}
                className="p-1 hover:bg-gray-100 rounded"
                title="Ir al nodo actual"
              >
                <Navigation size={16} className="text-blue-600" />
              </button>
            </div>
          </div>

          {/* Barra de progreso */}
          <div className="mb-3">
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ 
                  width: `${(camiones.find(c => c.id === camionSeleccionado)?.porcentaje || 0) / (rutaProcesada.length - 1) * 100}%` 
                }}
              ></div>
            </div>
          </div>

          {/* Línea de tiempo scrolleable */}
          <div className="space-y-2">
            <h4 className="text-sm font-semibold text-gray-700">Línea de tiempo de la ruta:</h4>
            <div className="relative">
              {/* Contenedor scrolleable horizontal */}
              <div 
                ref={timelineRef}
                className="flex gap-1 overflow-x-auto pb-2 max-h-20 timeline-scroll" 
                style={{
                  scrollbarWidth: 'thin',
                  scrollbarColor: '#d1d5db #f3f4f6'
                }}
              >
                {rutaProcesada.map((nodo, index) => (
                  <div
                    key={index}
                    className={`flex-shrink-0 flex flex-col items-center gap-1 p-2 rounded-lg border-2 transition-all duration-200 min-w-[80px] max-w-[100px] ${obtenerColorFondo(nodo)}`}
                  >
                    <div className="flex items-center gap-1">
                      {obtenerIcono(nodo.tipo)}
                      <span className="text-xs font-medium text-center">
                        {nodo.coordenada.x},{nodo.coordenada.y}
                      </span>
                    </div>
                    
                    {nodo.pedido && (
                      <div className="text-xs text-blue-700 bg-blue-50 px-1 py-0.5 rounded text-center w-full">
                        P{nodo.pedido.codigo}
                      </div>
                    )}
                    
                    {nodo.actual && (
                      <div className="text-xs font-bold text-blue-700 bg-blue-200 px-1 py-0.5 rounded text-center w-full">
                        ACTUAL
                      </div>
                    )}
                    
                    {nodo.completado && !nodo.actual && (
                      <div className="text-xs text-green-700 bg-green-50 px-1 py-0.5 rounded text-center w-full">
                        ✓
                      </div>
                    )}
                  </div>
                ))}
              </div>
              
              {/* Indicador de scroll */}
              <div className="flex justify-center mt-1">
                <div className="text-xs text-gray-500">← Desliza para ver más nodos →</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Estado cuando no hay camión seleccionado */}
      {!camionSeleccionado && (
        <div className="p-8 text-center text-gray-500">
          <Truck size={48} className="mx-auto mb-4 text-gray-300" />
          <p className="text-lg font-medium">Selecciona un camión para ver su ruta</p>
          <p className="text-sm">La ruta se mostrará en formato de línea de tiempo con el progreso actual</p>
        </div>
      )}
    </div>
  );
};

export default BottomMenu;