import React, { useState, useRef } from 'react';
import { Plus, Upload, Download, AlertCircle, CheckCircle, FileText, MapPin } from 'lucide-react';
import { validarArchivoVentas } from './cargar_archivos/validadores';
import { ejemplos, descargarEjemplo } from './cargar_archivos/ejemplos';
import type { ValidacionArchivo, DatosVentas } from '../types';
import { ArchivosApiService } from '../services/archivosApiService';
import type { ArchivoPedidosRequest } from '../services/archivosApiService';
import { getMejorIndividuo } from '../services/simulacionApiService';
import { useSimulacion } from '../context/SimulacionContext';
import { calcularTimestampSimulacion } from '../context/simulacion/utils/tiempo';
import { toast } from 'react-toastify';
import { Bounce } from 'react-toastify';

// Constantes del mapa
const MAP_WIDTH = 70;
const MAP_HEIGHT = 50;

const AgregarPedidosPanel: React.FC = () => {
  const [modo, setModo] = useState<'individual' | 'archivo'>('individual');
  const [archivoSeleccionado, setArchivoSeleccionado] = useState<File | null>(null);
  const [validacion, setValidacion] = useState<ValidacionArchivo | null>(null);
  const [cargando, setCargando] = useState(false);
  const [isDragOver, setIsDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Obtener el contexto de simulación
  const { 
    fechaHoraSimulacion, 
    horaSimulacion, 
    aplicarNuevaSolucionDespuesAveria,
    setPollingActivo,
    setSimulacionActiva
  } = useSimulacion();

  // Calcular timestamp de simulación actual
  const timestampSimulacion = calcularTimestampSimulacion(
    fechaHoraSimulacion,
    horaSimulacion
  );

  // Función para extraer fecha y hora del timestamp de simulación
  const extraerFechaHoraSimulacion = () => {
    if (!timestampSimulacion) {
      return {
        año: new Date().getFullYear(),
        mes: new Date().getMonth() + 1,
        dia: new Date().getDate(),
        hora: 0,
        minuto: 0
      };
    }

    // El timestamp tiene formato: "YYYY-MM-DD HH:mm:ss"
    const fecha = new Date(timestampSimulacion);
    return {
      año: fecha.getFullYear(),
      mes: fecha.getMonth() + 1,
      dia: fecha.getDate(),
      hora: fecha.getHours(),
      minuto: fecha.getMinutes()
    };
  };

  // Campos para pedido individual - solo coordenadas y datos del cliente
  const [pedidoIndividual, setPedidoIndividual] = useState({
    coordenadaX: 0,
    coordenadaY: 0,
    nombreCliente: 'Cliente Default',
    glp: 1,
    horasLimite: 6
  });

  const handleArchivoChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    procesarArchivo(file);
  };

  const procesarArchivo = (file: File) => {
    // Validar nombre del archivo
    const nombreArchivo = file.name;
    const formatoCorrecto = /^ventas\d{6}\.txt$/;
    
    if (!formatoCorrecto.test(nombreArchivo)) {
      alert(`El nombre del archivo debe seguir el formato: ventasYYYYMM.txt\nEjemplo: ventas202501.txt\n\nArchivo actual: ${nombreArchivo}`);
      return;
    }

    setArchivoSeleccionado(file);
    setValidacion(null);

    const reader = new FileReader();
    reader.onload = (e) => {
      const contenido = e.target?.result as string;
      const validacionResult = validarArchivoVentas(contenido);
      setValidacion(validacionResult);
    };
    reader.readAsText(file);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragOver(false);
    
    const files = Array.from(e.dataTransfer.files);
    const txtFile = files.find(file => file.type === 'text/plain' || file.name.endsWith('.txt'));
    
    if (txtFile) {
      procesarArchivo(txtFile);
    } else {
      alert('Por favor selecciona un archivo de texto (.txt)');
    }
  };

  // Función para procesar pedidos y recalcular algoritmo
  const handleProcesarPedidos = async (request: ArchivoPedidosRequest) => {
    try {
      console.log("📦 PROCESANDO PEDIDOS: Iniciando procesamiento de pedidos...");
      
      // 1. Detener polling y pausar simulación
      console.log("🛑 DETENIENDO POLLING Y PAUSANDO SIMULACIÓN...");
      if (setPollingActivo) {
        setPollingActivo(false);
      }
      setSimulacionActiva(false);
      
      // 2. Procesar pedidos en el backend
      console.log("📡 ENVIANDO PEDIDOS AL BACKEND...");
      const response = await ArchivosApiService.procesarPedidosIndividuales(request);
      console.log("✅ PEDIDOS PROCESADOS:", response);
      
      // 3. Calcular timestamp de simulación actual
      const timestampSimulacion = calcularTimestampSimulacion(
        fechaHoraSimulacion,
        horaSimulacion
      );
      
      // 4. Recalcular algoritmo genético
      console.log("🧬 RECALCULANDO ALGORITMO GENÉTICO...");
      const nuevaSolucion = await getMejorIndividuo(timestampSimulacion || "");
      
      // 5. Aplicar nueva solución
      if (aplicarNuevaSolucionDespuesAveria) {
        console.log("🔄 APLICANDO NUEVA SOLUCIÓN...");
        await aplicarNuevaSolucionDespuesAveria(nuevaSolucion);
        console.log("✅ NUEVA SOLUCIÓN APLICADA");
      }
      
      // 6. Reanudar simulación
      console.log("▶️ REANUDANDO SIMULACIÓN...");
      setSimulacionActiva(true);
      
      // 7. Mostrar mensaje de éxito
      toast.success(`✅ Pedidos agregados exitosamente: ${response.mensaje} - Algoritmo recalculado`, {
        position: "top-right",
        autoClose: 6000,
        hideProgressBar: false,
        closeOnClick: false,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      
      console.log("✅ PROCESAMIENTO COMPLETADO:", {
        pedidosAgregados: response.totalPedidosAgregados,
        algoritmoRecalculado: true,
        nuevaSolucionAplicada: !!aplicarNuevaSolucionDespuesAveria
      });
      
    } catch (error) {
      console.error("❌ ERROR PROCESANDO PEDIDOS:", error);
      
      // Reanudar simulación en caso de error
      setSimulacionActiva(true);
      if (setPollingActivo) {
        setPollingActivo(true);
      }
      
      toast.error(`❌ Error al procesar pedidos: ${error instanceof Error ? error.message : 'Error desconocido'}`, {
        position: "top-right",
        autoClose: 8000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      
      throw error;
    }
  };

  const handleAgregarPedidoIndividual = async () => {
    // Validar campos
    if (!pedidoIndividual.nombreCliente.trim()) {
      alert('Por favor ingrese el nombre del cliente');
      return;
    }

    if (pedidoIndividual.glp <= 0) {
      alert('El volumen GLP debe ser mayor a 0');
      return;
    }

    // Validar horas límite (debe ser mayor o igual a 4)
    if (pedidoIndividual.horasLimite < 4) {
      alert('Las horas límite deben ser mayor o igual a 4 horas. El pedido será omitido.');
      return;
    }

    // Validar coordenadas
    if (pedidoIndividual.coordenadaX < 0 || pedidoIndividual.coordenadaX >= MAP_WIDTH) {
      alert(`La coordenada X debe estar entre 0 y ${MAP_WIDTH - 1}`);
      return;
    }

    if (pedidoIndividual.coordenadaY < 0 || pedidoIndividual.coordenadaY >= MAP_HEIGHT) {
      alert(`La coordenada Y debe estar entre 0 y ${MAP_HEIGHT - 1}`);
      return;
    }

    setCargando(true);

    try {
      // Obtener fecha y hora actual de la simulación
      const fechaSimulacion = extraerFechaHoraSimulacion();
      
      // Formatear fecha y hora usando el timestamp de simulación
      const fechaHora = `${fechaSimulacion.dia.toString().padStart(2, '0')}d${fechaSimulacion.hora.toString().padStart(2, '0')}h${fechaSimulacion.minuto.toString().padStart(2, '0')}m`;
      
      // Generar código de cliente
      const codigoCliente = `c-${Math.floor(Math.random() * 1000)}`;

      const pedido: DatosVentas = {
        fechaHora,
        coordenadaX: pedidoIndividual.coordenadaX,
        coordenadaY: pedidoIndividual.coordenadaY,
        codigoCliente,
        volumenGLP: pedidoIndividual.glp,
        horasLimite: pedidoIndividual.horasLimite
      };

      // Generar nombre del archivo usando año y mes de la simulación
      const nombreArchivo = `ventas${fechaSimulacion.año}${fechaSimulacion.mes.toString().padStart(2, '0')}.txt`;
      
      // Generar contenido del archivo (una sola línea)
      const contenido = `${fechaHora}:${pedidoIndividual.coordenadaX},${pedidoIndividual.coordenadaY},${codigoCliente},${pedidoIndividual.glp}m3,${pedidoIndividual.horasLimite}h`;

      // Crear el objeto de solicitud
      const request: ArchivoPedidosRequest = {
        nombre: nombreArchivo,
        contenido: contenido,
        datos: [pedido]
      };

      // Procesar pedidos y recalcular algoritmo
      await handleProcesarPedidos(request);
      
      // Limpiar formulario
      setPedidoIndividual({
        coordenadaX: 0,
        coordenadaY: 0,
        nombreCliente: 'Cliente Default',
        glp: 1,
        horasLimite: 6
      });
      
    } catch (error) {
      console.error('Error al agregar pedido individual:', error);
    } finally {
      setCargando(false);
    }
  };

  const handleAgregarArchivo = async () => {
    if (!archivoSeleccionado || !validacion || !validacion.esValido) {
      alert('Por favor seleccione un archivo válido');
      return;
    }

    setCargando(true);

    try {
      const reader = new FileReader();
      reader.onload = async (e) => {
        const contenido = e.target?.result as string;
        const datos = validacion.datosParseados as DatosVentas[];
        
        // Generar nombre de archivo
        const fecha = new Date();
        const nombreArchivo = `ventas${fecha.getFullYear()}${(fecha.getMonth() + 1).toString().padStart(2, '0')}.txt`;

        // Crear el objeto de solicitud
        const request: ArchivoPedidosRequest = {
          nombre: nombreArchivo,
          contenido: contenido,
          datos: datos
        };

        // Procesar pedidos y recalcular algoritmo
        await handleProcesarPedidos(request);

        setCargando(false);
      };
      reader.readAsText(archivoSeleccionado);
    } catch (error) {
      console.error('Error al procesar archivo:', error);
      setCargando(false);
    }
  };

  const descargarEjemploVentas = () => {
    const ejemploVentas = ejemplos.find(e => e.tipo === 'ventas');
    if (ejemploVentas) {
      // Crear un ejemplo con el nombre de archivo correcto
      const fecha = new Date();
      const nombreArchivo = `ventas${fecha.getFullYear()}${(fecha.getMonth() + 1).toString().padStart(2, '0')}.txt`;
      
      const ejemploConFormatoCorrecto = {
        ...ejemploVentas,
        nombre: nombreArchivo
      };
      
      descargarEjemplo(ejemploConFormatoCorrecto);
    }
  };

  const limpiarFormulario = () => {
    setPedidoIndividual({
      coordenadaX: 0,
      coordenadaY: 0,
      nombreCliente: 'Cliente Default',
      glp: 1,
      horasLimite: 6
    });
  };

  return (
    <div className="flex flex-col flex-1 min-h-0">
      <div className="text-lg font-bold text-black mb-3 flex items-center gap-2">
        <MapPin className="w-5 h-5" />
        Agregar Pedidos
      </div>

      {/* Información del timestamp de simulación */}
      <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
        <h4 className="text-sm font-semibold text-blue-800 mb-1 flex items-center gap-2">
          🕐 Timestamp de Simulación
        </h4>
        <p className="text-xs text-blue-700">
          <strong>Actual:</strong> {timestampSimulacion || 'No disponible'}
        </p>
      </div>

      {/* Selector de modo */}
      <div className="mb-4">
        <div className="flex gap-1">
          <button
            onClick={() => setModo('individual')}
            className={`flex-1 px-2 py-1 rounded text-xs font-medium transition-colors ${
              modo === 'individual'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            <Plus className="w-3 h-3 inline mr-1" />
            Individual
          </button>
          <button
            onClick={() => setModo('archivo')}
            className={`flex-1 px-2 py-1 rounded text-xs font-medium transition-colors ${
              modo === 'archivo'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            <Upload className="w-3 h-3 inline mr-1" />
            Archivo
          </button>
        </div>
      </div>

      <div className="flex-1 min-h-0 overflow-y-auto">
        {modo === 'individual' ? (
          /* Formulario para pedido individual */
          <div className="space-y-3">

            {/* Coordenadas */}
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">
                Coordenadas (X: 0-{MAP_WIDTH - 1}, Y: 0-{MAP_HEIGHT - 1})
              </label>
              <div className="grid grid-cols-2 gap-2">
                <input
                  type="number"
                  value={pedidoIndividual.coordenadaX}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, coordenadaX: parseInt(e.target.value) }))}
                  className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
                  placeholder="X"
                  min={0}
                  max={MAP_WIDTH - 1}
                />
                <input
                  type="number"
                  value={pedidoIndividual.coordenadaY}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, coordenadaY: parseInt(e.target.value) }))}
                  className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
                  placeholder="Y"
                  min={0}
                  max={MAP_HEIGHT - 1}
                />
              </div>
            </div>

            {/* Cliente y GLP */}
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">
                Nombre del Cliente
              </label>
              <input
                type="text"
                value={pedidoIndividual.nombreCliente}
                onChange={(e) => setPedidoIndividual(prev => ({ ...prev, nombreCliente: e.target.value }))}
                className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
                placeholder="Nombre del cliente"
              />
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                  GLP (m³)
                </label>
                <input
                  type="number"
                  value={pedidoIndividual.glp}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, glp: parseInt(e.target.value) }))}
                  className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
                  min={1}
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">
                  Horas Límite (≥4)
                </label>
                <input
                  type="number"
                  value={pedidoIndividual.horasLimite}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, horasLimite: parseInt(e.target.value) }))}
                  className="w-full px-2 py-1 text-xs border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500"
                  min={4}
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={handleAgregarPedidoIndividual}
                disabled={cargando}
                className={`py-2 px-3 rounded text-sm font-medium transition-colors ${
                  cargando
                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    : 'bg-blue-500 text-white hover:bg-blue-600'
                }`}
              >
                {cargando ? 'Procesando...' : 'Agregar Pedido'}
              </button>
              <button
                onClick={limpiarFormulario}
                type="button"
                className="py-2 px-3 bg-gray-500 text-white rounded text-sm font-medium hover:bg-gray-600 transition-colors"
              >
                Limpiar
              </button>
            </div>
          </div>
        ) : (
          /* Formulario para archivo */
          <div className="space-y-3">
            {/* Información del formato */}
            <div className="p-2 bg-blue-50 border border-blue-200 rounded text-xs">
              <p className="font-medium text-blue-800 mb-1">📁 Formato: ventasYYYYMM.txt</p>
              <p className="text-blue-700 mb-1">📄 Contenido: fechaHora:coordenadaX,coordenadaY,codigoCliente,volumenGLP,horasLimite</p>
              <p className="text-blue-600">Ejemplo: 01d00h24m:16,13,c-198,3m3,4h</p>
            </div>

            {/* Descargar ejemplo */}
            <button
              onClick={descargarEjemploVentas}
              className="w-full flex items-center justify-center gap-2 px-3 py-2 bg-green-500 text-white rounded text-sm hover:bg-green-600 transition-colors"
            >
              <Download className="w-4 h-4" />
              Descargar Ejemplo
            </button>

            {/* Cargar archivo */}
            <div
              className={`border-2 border-dashed rounded-lg p-4 text-center transition-colors cursor-pointer ${
                isDragOver 
                  ? 'border-blue-500 bg-blue-50' 
                  : 'border-gray-300 hover:border-gray-400'
              }`}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current?.click()}
            >
              <FileText className="w-6 h-6 mx-auto mb-2 text-gray-400" />
              <p className="text-xs text-gray-600 mb-1">
                Arrastra archivo aquí o haz clic
              </p>
              <p className="text-xs text-gray-500">
                Solo archivos .txt
              </p>
            </div>
            
            <input
              ref={fileInputRef}
              type="file"
              accept=".txt"
              onChange={handleArchivoChange}
              className="hidden"
            />

            {/* Validación */}
            {validacion && (
              <div className={`p-3 rounded-lg border text-xs ${
                validacion.esValido 
                  ? 'bg-green-50 border-green-200' 
                  : 'bg-red-50 border-red-200'
              }`}>
                <div className="flex items-center gap-2 mb-2">
                  {validacion.esValido ? (
                    <CheckCircle className="w-4 h-4 text-green-600" />
                  ) : (
                    <AlertCircle className="w-4 h-4 text-red-600" />
                  )}
                  <span className={`font-medium ${
                    validacion.esValido ? 'text-green-800' : 'text-red-800'
                  }`}>
                    {validacion.esValido ? 'Archivo válido' : 'Archivo con errores'}
                  </span>
                </div>
                
                {validacion.errores.length > 0 && (
                  <div className="mb-2">
                    <h5 className="font-medium text-red-800 mb-1">Errores:</h5>
                    <ul className="text-red-700 space-y-1">
                      {validacion.errores.slice(0, 3).map((error, index) => (
                        <li key={index}>• {error}</li>
                      ))}
                      {validacion.errores.length > 3 && (
                        <li>• ... y {validacion.errores.length - 3} más</li>
                      )}
                    </ul>
                  </div>
                )}
                
                {validacion.esValido && validacion.datosParseados && (
                  <div>
                    <p className="text-green-700">
                      {validacion.datosParseados.length} pedido(s) válido(s)
                    </p>
                  </div>
                )}
              </div>
            )}

            <button
              onClick={handleAgregarArchivo}
              disabled={!validacion?.esValido || cargando}
              className={`w-full py-2 px-3 rounded text-sm font-medium transition-colors ${
                validacion?.esValido && !cargando
                  ? 'bg-blue-500 text-white hover:bg-blue-600'
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              }`}
            >
              {cargando ? 'Procesando...' : 'Agregar Archivo'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default AgregarPedidosPanel; 