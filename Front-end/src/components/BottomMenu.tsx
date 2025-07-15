import React, { useState, useEffect } from 'react';
import { ChevronDown, MapPin, Package, Truck, Navigation, Pause } from 'lucide-react';
import { useSimulacion } from '../context/SimulacionContext';
import type { Coordenada } from '../types';

interface BottomMenuProps {
  expanded: boolean;
  setExpanded: (value: boolean) => void;
  camionSeleccionadoExterno?: string | null;
}

interface NodoRuta {
  coordenada: Coordenada;
  tipo: 'inicio' | 'ruta' | 'pedido' | 'almacen' | 'fin';
  indice: number;
  completado: boolean;
  actual: boolean;
  duracionMinutos?: number;
  nodosAgrupados?: number; // Número de nodos que se agruparon
  pedido?: {
    codigo: string;
    volumenGLP: number;
  };
}

const BottomMenu: React.FC<BottomMenuProps> = ({ expanded, setExpanded, camionSeleccionadoExterno }) => {
  const { rutasCamiones, camiones } = useSimulacion();
  const [camionSeleccionado, setCamionSeleccionado] = useState<string | null>(null);
  const [seguimientoAutomatico, setSeguimientoAutomatico] = useState<boolean>(false);
  // const [scrollAutomatico, setScrollAutomatico] = useState<boolean>(false);
  const timelineRef = React.useRef<HTMLDivElement>(null);

  // Efecto para manejar la selección externa de camión
  useEffect(() => {
    if (camionSeleccionadoExterno) {
      setCamionSeleccionado(camionSeleccionadoExterno);
    }
  }, [camionSeleccionadoExterno]);

  // Función para parsear coordenadas
  const parseCoord = (s: string): Coordenada => {
    const match = s.match(/\((\d+),\s*(\d+)\)/);
    if (!match) throw new Error(`Coordenada inválida: ${s}`);
    return { x: parseInt(match[1]), y: parseInt(match[2]) };
  };

  // Función para obtener la ruta procesada del camión seleccionado (agrupando nodos consecutivos)
  const obtenerRutaProcesada = (): NodoRuta[] => {
    if (!camionSeleccionado) return [];

    const ruta = rutasCamiones.find(r => r.id === camionSeleccionado);
    const camion = camiones.find(c => c.id === camionSeleccionado);
    
    if (!ruta || !camion) return [];

    const porcentajeActual = camion.porcentaje;
    const nodosAgrupados: NodoRuta[] = [];
    let nodoActual: NodoRuta | null = null;

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

      // Verificar si podemos agrupar con el nodo anterior
      if (nodoActual && 
          nodoActual.coordenada.x === coordenada.x && 
          nodoActual.coordenada.y === coordenada.y &&
          nodoActual.tipo === tipo) {
        // Es el mismo nodo, actualizar información
        nodoActual.duracionMinutos = (nodoActual.duracionMinutos || 0) + 1.2; // 1.2 minutos por nodo
        nodoActual.nodosAgrupados = (nodoActual.nodosAgrupados || 1) + 1;
        nodoActual.completado = completado;
        nodoActual.actual = actual;
        
        // Si es un pedido, mantener la información del pedido
        if (tipo === 'pedido') {
          const pedido = ruta.pedidos.find(p => 
            p.coordenada.x === coordenada.x && p.coordenada.y === coordenada.y
          );
          if (pedido) {
            nodoActual.pedido = {
              codigo: pedido.codigo,
              volumenGLP: pedido.volumenGLPAsignado || 0
            };
          }
        }
      } else {
        // Es un nodo diferente, crear nuevo nodo agrupado
        if (nodoActual) {
          nodosAgrupados.push(nodoActual);
        }

        const pedido = tipo === 'pedido' ? ruta.pedidos.find(p => 
          p.coordenada.x === coordenada.x && p.coordenada.y === coordenada.y
        ) : undefined;

        nodoActual = {
          coordenada,
          tipo,
          indice: index,
          completado,
          actual,
          duracionMinutos: 1.2, // 1.2 minutos por nodo
          nodosAgrupados: 1,
          pedido: pedido ? {
            codigo: pedido.codigo,
            volumenGLP: pedido.volumenGLPAsignado || 0
          } : undefined
        };
      }
    });

    // Agregar el último nodo
    if (nodoActual) {
      nodosAgrupados.push(nodoActual);
    }

    return nodosAgrupados;
  };

  const rutaProcesada = obtenerRutaProcesada();

  // Activar seguimiento automático por defecto cuando se selecciona un camión
  useEffect(() => {
    if (camionSeleccionado) {
      // console.log('🎯 SEGUIMIENTO: Activando seguimiento automático por defecto para camión', camionSeleccionado);
      setSeguimientoAutomatico(true);
    } else {
      setSeguimientoAutomatico(false);
    }
  }, [camionSeleccionado]);

  // Hacer scroll automático cuando el seguimiento automático esté activo y cambien los camiones
  useEffect(() => {
    if (seguimientoAutomatico && camionSeleccionado && rutaProcesada.length > 0) {
      // console.log('🎯 SEGUIMIENTO: Activando scroll automático para camión', camionSeleccionado);
      const timer = setTimeout(() => {
        // setScrollAutomatico(true);
        scrollToCurrentNodeInternal();
        // Resetear la bandera después de un breve delay
        // setTimeout(() => setScrollAutomatico(false), 200);
      }, 100);
      
      return () => clearTimeout(timer);
    }
  }, [seguimientoAutomatico, camionSeleccionado, camiones, rutaProcesada]);

  // Función interna para hacer scroll al nodo actual
  const scrollToCurrentNodeInternal = () => {
    if (timelineRef.current && camionSeleccionado) {
      const camion = camiones.find(c => c.id === camionSeleccionado);
      if (camion) {
        // console.log('📍 SCROLL: Buscando nodo actual para camión', camionSeleccionado, 'con porcentaje', camion.porcentaje);
        
        // Encontrar el nodo actual en la ruta procesada (agrupada)
        const nodoActualIndex = rutaProcesada.findIndex(nodo => nodo.actual);
        
        // console.log('🔍 SCROLL: Índice del nodo actual encontrado:', nodoActualIndex);
        
        if (nodoActualIndex !== -1) {
          const nodeElement = timelineRef.current.children[nodoActualIndex] as HTMLElement;
          if (nodeElement) {
            // console.log('✅ SCROLL: Haciendo scroll al nodo actual');
            nodeElement.scrollIntoView({
              behavior: 'smooth',
              block: 'nearest',
              inline: 'center'
            });
          }
        }
      }
    }
  };

  // Función para hacer scroll al nodo actual (para el botón)
  const scrollToCurrentNode = () => {
    // console.log('🎯 ACCIÓN: Botón "Ir al nodo actual" presionado');
    scrollToCurrentNodeInternal();
    // Si el seguimiento está pausado, reactivarlo
    if (!seguimientoAutomatico) {
      setSeguimientoAutomatico(true);
      console.log('🔄 SEGUIMIENTO: Reactivado desde botón de navegación');
    }
  };

  // Función para pausar/reanudar el seguimiento automático
  const toggleSeguimientoAutomatico = () => {
    const nuevoEstado = !seguimientoAutomatico;
    setSeguimientoAutomatico(nuevoEstado);
    console.log('🔄 SEGUIMIENTO:', nuevoEstado ? 'Reanudado' : 'Pausado');
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
              <div className="flex items-center gap-2 px-3 py-2 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg shadow-md">
                <Truck size={16} />
                <span className="font-bold text-sm">Camión {camionSeleccionado}</span>
                <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
              </div>
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
            Seleccionar camión para seguimiento:
          </label>
          <select
            value={camionSeleccionado || ''}
            onChange={(e) => setCamionSeleccionado(e.target.value || null)}
            className="w-full p-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-medium"
          >
            <option value="">🚛 Selecciona un camión para seguir su ruta</option>
            {rutasCamiones.map((ruta) => {
              const camion = camiones.find(c => c.id === ruta.id);
              const estado = camion?.estado || 'Desconocido';
              return (
                <option key={ruta.id} value={ruta.id}>
                  🚛 Camión {ruta.id} • {estado} • {ruta.pedidos.length} pedidos
                </option>
              );
            })}
          </select>
        </div>
      </div>

      {/* Contenido de la ruta */}
      {camionSeleccionado && rutaProcesada.length > 0 && (() => {
        const ruta = rutasCamiones.find(r => r.id === camionSeleccionado);
        if (!ruta) return null;
        
        return (
        <div className="p-4">
          {/* Información compacta en la parte superior */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 mb-3">
            <div className="flex justify-between items-center text-sm">
              <div className="flex gap-4">
                <span className="font-medium">📍 Paradas: <span className="text-blue-600 font-bold">{rutaProcesada.length}</span></span>
                <span className="font-medium">🔢 Nodos originales: <span className="text-gray-600 font-bold">{ruta.ruta.length}</span></span>
                <span className="font-medium">📦 Pedidos: <span className="text-orange-600 font-bold">{rutaProcesada.filter(n => n.tipo === 'pedido').length}</span></span>
                <span className="font-medium">⏱️ Tiempo total: <span className="text-indigo-600 font-bold">{Math.round(rutaProcesada.reduce((total, nodo) => total + (nodo.duracionMinutos || 1.2), 0))}min</span></span>
              </div>
              <div className="flex items-center gap-2">
                <span className="font-medium">📊 Progreso: <span className="text-purple-600 font-bold">{Math.round((camiones.find(c => c.id === camionSeleccionado)?.porcentaje || 0) / (ruta.ruta.length - 1) * 100)}%</span></span>
                <button
                  onClick={scrollToCurrentNode}
                  className={`p-2 rounded-lg transition-colors ${
                    seguimientoAutomatico 
                      ? 'bg-green-100 hover:bg-green-200' 
                      : 'bg-blue-100 hover:bg-blue-200'
                  }`}
                  title={seguimientoAutomatico ? "Ir al nodo actual" : "Ir al nodo actual y reactivar seguimiento"}
                >
                  <Navigation size={16} className={seguimientoAutomatico ? "text-green-600" : "text-blue-600"} />
                </button>
                {seguimientoAutomatico && (
                  <>
                    <div className="flex items-center gap-1 text-xs text-green-600 bg-green-50 px-2 py-1 rounded-full">
                      <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                      Auto
                    </div>
                    <button
                      onClick={toggleSeguimientoAutomatico}
                      className={`p-2 rounded-lg transition-colors ${
                        seguimientoAutomatico 
                          ? 'bg-red-100 hover:bg-red-200' 
                          : 'bg-gray-100 hover:bg-gray-200'
                      }`}
                      title={seguimientoAutomatico ? "Pausar seguimiento automático" : "Reanudar seguimiento automático"}
                    >
                      <Pause size={16} className={seguimientoAutomatico ? "text-red-600" : "text-gray-600"} />
                    </button>
                  </>
                )}
              </div>
            </div>
          </div>

          {/* Barra de progreso */}
          <div className="mb-3">
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ 
                  width: `${(camiones.find(c => c.id === camionSeleccionado)?.porcentaje || 0) / (ruta.ruta.length - 1) * 100}%` 
                }}
              ></div>
            </div>
          </div>

          {/* Línea de tiempo scrolleable */}
          <div className="space-y-2">
            <h4 className="text-sm font-semibold text-gray-700">Paradas de la ruta (nodos agrupados):</h4>
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
                    
                    {/* Mostrar información de nodos agrupados */}
                    {nodo.nodosAgrupados && nodo.nodosAgrupados > 1 && (
                      <div className="text-xs text-gray-600 bg-gray-50 px-1 py-0.5 rounded text-center w-full">
                        {nodo.nodosAgrupados} nodos • {Math.round(nodo.duracionMinutos || 0)}min
                      </div>
                    )}
                      
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
              <div className="flex justify-center mt-2">
                <div className="text-xs text-gray-600 bg-gray-100 px-3 py-1 rounded-full">
                  {seguimientoAutomatico ? (
                    <span className="flex items-center gap-1">
                      <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                      Seguimiento automático activo • Usa el botón de pausa para desactivar
                    </span>
                  ) : (
                    <span>👆 Desliza horizontalmente para ver más paradas • 📍 Nodos agrupados por ubicación</span>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
        );
      })()}

      {/* Estado cuando no hay camión seleccionado */}
      {!camionSeleccionado && (
        <div className="p-8 text-center text-gray-500">
          <div className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl p-6 border-2 border-dashed border-blue-200">
            <Truck size={48} className="mx-auto mb-4 text-blue-300" />
            <h3 className="text-lg font-bold text-gray-700 mb-2">🚛 Seguimiento de Ruta</h3>
            <p className="text-sm text-gray-600 mb-4">
              Selecciona un camión del menú superior para comenzar a seguir su ruta en tiempo real
            </p>
            <div className="text-xs text-gray-500 space-y-1">
              <p>• La ruta se mostrará en formato de línea de tiempo</p>
              <p>• Los nodos consecutivos se agrupan automáticamente</p>
              <p>• Puedes hacer scroll horizontal para ver todas las paradas</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BottomMenu;