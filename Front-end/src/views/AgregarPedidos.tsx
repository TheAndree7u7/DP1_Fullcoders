import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Upload, Download, AlertCircle, CheckCircle, FileText, MapPin, ArrowLeft, Home } from 'lucide-react';
import { validarArchivoVentas } from '../components/cargar_archivos/validadores';
import { ejemplos, descargarEjemplo } from '../components/cargar_archivos/ejemplos';
import type { ValidacionArchivo, DatosVentas } from '../types';
import { ArchivosApiService } from '../services/archivosApiService';
import type { ArchivoPedidosRequest } from '../services/archivosApiService';
import { getMejorIndividuo } from '../services/simulacionApiService';
import { useSimulacion } from '../context/SimulacionContext';
import { calcularTimestampSimulacion } from '../context/simulacion/utils/tiempo';
import { toast } from 'react-toastify';
import { Bounce } from 'react-toastify';
import logo from '../assets/logo.png';

// Constantes del mapa
const MAP_WIDTH = 70;
const MAP_HEIGHT = 50;

const AgregarPedidos: React.FC = () => {
  const navigate = useNavigate();
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
    nombreCliente: '',
    glp: 0,
    horasLimite: 0
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
      toast.error('Por favor ingrese el nombre del cliente', {
        position: "top-right",
        autoClose: 4000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      return;
    }

    if (pedidoIndividual.glp <= 0) {
      toast.error('El volumen GLP debe ser mayor a 0', {
        position: "top-right",
        autoClose: 4000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      return;
    }

    // Validar horas límite (debe ser mayor o igual a 4)
    if (pedidoIndividual.horasLimite < 4) {
      toast.warning('Las horas límite deben ser mayor o igual a 4 horas. El pedido será omitido.', {
        position: "top-right",
        autoClose: 6000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      return;
    }

    // Validar coordenadas
    if (pedidoIndividual.coordenadaX < 0 || pedidoIndividual.coordenadaX >= MAP_WIDTH) {
      toast.error(`La coordenada X debe estar entre 0 y ${MAP_WIDTH - 1}`, {
        position: "top-right",
        autoClose: 4000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
      return;
    }

    if (pedidoIndividual.coordenadaY < 0 || pedidoIndividual.coordenadaY >= MAP_HEIGHT) {
      toast.error(`La coordenada Y debe estar entre 0 y ${MAP_HEIGHT - 1}`, {
        position: "top-right",
        autoClose: 4000,
        hideProgressBar: false,
        closeOnClick: true,
        pauseOnHover: true,
        draggable: true,
        progress: undefined,
        theme: "light",
        transition: Bounce,
      });
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
        nombreCliente: '',
        glp: 0,
        horasLimite: 0
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
      nombreCliente: '',
      glp: 0,
      horasLimite: 0
    });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
      {/* Header */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center">
              <img src={logo} alt="Logo" className="h-8 w-auto mr-3" />
              <h1 className="text-2xl font-bold text-gray-900">Sistema de Simulación Logística</h1>
            </div>
            <div className="flex items-center gap-4">
              <button
                onClick={() => navigate('/')}
                className="flex items-center gap-2 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                <Home className="w-4 h-4" />
                Inicio
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Breadcrumb */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-2">
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <button
            onClick={() => navigate('/')}
            className="flex items-center gap-1 hover:text-blue-600 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Volver
          </button>
          <span>/</span>
          <span className="text-gray-900 font-medium">Agregar Pedidos</span>
        </div>
      </div>

      {/* Contenido principal */}
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-lg shadow-lg p-6">
          {/* Título */}
          <div className="text-center mb-8">
            <div className="flex items-center justify-center gap-3 mb-4">
              <MapPin className="w-8 h-8 text-blue-500" />
              <h2 className="text-3xl font-bold text-gray-900">Agregar Pedidos</h2>
            </div>
            <p className="text-gray-600 max-w-2xl mx-auto">
              Registra nuevos pedidos de GLP de forma individual o mediante archivos de texto. 
              Los pedidos se integrarán automáticamente al sistema de simulación logística.
            </p>
          </div>

          {/* Información del timestamp de simulación */}
          <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <h4 className="text-sm font-semibold text-blue-800 mb-2 flex items-center gap-2">
              🕐 Timestamp de Simulación
            </h4>
            <p className="text-sm text-blue-700">
              <strong>Actual:</strong> {timestampSimulacion || 'No disponible'}
            </p>
          </div>

          {/* Selector de modo */}
          <div className="mb-6">
            <div className="flex gap-2 max-w-md mx-auto">
              <button
                onClick={() => setModo('individual')}
                className={`flex-1 px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
                  modo === 'individual'
                    ? 'bg-blue-500 text-white shadow-md'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                <Plus className="w-4 h-4 inline mr-2" />
                Pedido Individual
              </button>
              <button
                onClick={() => setModo('archivo')}
                className={`flex-1 px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
                  modo === 'archivo'
                    ? 'bg-blue-500 text-white shadow-md'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                <Upload className="w-4 h-4 inline mr-2" />
                Archivo de Pedidos
              </button>
            </div>
          </div>

          {/* Contenido del formulario */}
          <div className="max-w-2xl mx-auto">
            {modo === 'individual' ? (
              /* Formulario para pedido individual */
              <div className="space-y-6">
                <div className="bg-gray-50 p-4 rounded-lg">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">Información del Pedido</h3>
                  
                  {/* Coordenadas */}
                  <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Coordenadas del Cliente (X: 0-{MAP_WIDTH - 1}, Y: 0-{MAP_HEIGHT - 1})
                    </label>
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <input
                          type="number"
                          value={pedidoIndividual.coordenadaX}
                          onChange={(e) => setPedidoIndividual(prev => ({ ...prev, coordenadaX: parseInt(e.target.value) }))}
                          className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                          placeholder="Coordenada X"
                          min={0}
                          max={MAP_WIDTH - 1}
                        />
                      </div>
                      <div>
                        <input
                          type="number"
                          value={pedidoIndividual.coordenadaY}
                          onChange={(e) => setPedidoIndividual(prev => ({ ...prev, coordenadaY: parseInt(e.target.value) }))}
                          className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                          placeholder="Coordenada Y"
                          min={0}
                          max={MAP_HEIGHT - 1}
                        />
                      </div>
                    </div>
                  </div>

                  {/* Cliente */}
                  <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Nombre del Cliente
                    </label>
                    <input
                      type="text"
                      value={pedidoIndividual.nombreCliente}
                      onChange={(e) => setPedidoIndividual(prev => ({ ...prev, nombreCliente: e.target.value }))}
                      className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="Ingrese el nombre del cliente"
                    />
                  </div>

                  {/* GLP y Horas Límite */}
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Volumen GLP (m³)
                      </label>
                      <input
                        type="number"
                        value={pedidoIndividual.glp}
                        onChange={(e) => setPedidoIndividual(prev => ({ ...prev, glp: parseInt(e.target.value) }))}
                        className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        min={1}
                        placeholder="Cantidad de GLP"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        Horas Límite (≥4)
                      </label>
                      <input
                        type="number"
                        value={pedidoIndividual.horasLimite}
                        onChange={(e) => setPedidoIndividual(prev => ({ ...prev, horasLimite: parseInt(e.target.value) }))}
                        className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        min={4}
                        placeholder="Horas límite"
                      />
                    </div>
                  </div>
                </div>

                {/* Botones de acción */}
                <div className="flex gap-3 justify-center">
                  <button
                    onClick={handleAgregarPedidoIndividual}
                    disabled={cargando}
                    className={`px-6 py-3 rounded-lg text-sm font-medium transition-colors ${
                      cargando
                        ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                        : 'bg-blue-500 text-white hover:bg-blue-600 shadow-md'
                    }`}
                  >
                    {cargando ? 'Procesando...' : 'Agregar Pedido'}
                  </button>
                  <button
                    onClick={limpiarFormulario}
                    type="button"
                    className="px-6 py-3 bg-gray-500 text-white rounded-lg text-sm font-medium hover:bg-gray-600 transition-colors shadow-md"
                  >
                    Limpiar Formulario
                  </button>
                </div>
              </div>
            ) : (
              /* Formulario para archivo */
              <div className="space-y-6">
                {/* Información del formato */}
                <div className="bg-blue-50 p-4 border border-blue-200 rounded-lg">
                  <h3 className="font-medium text-blue-800 mb-2">📁 Formato Requerido</h3>
                  <div className="text-sm text-blue-700 space-y-1">
                    <p><strong>Nombre:</strong> ventasYYYYMM.txt</p>
                    <p><strong>Contenido:</strong> fechaHora:coordenadaX,coordenadaY,codigoCliente,volumenGLP,horasLimite</p>
                    <p><strong>Ejemplo:</strong> 01d00h24m:16,13,c-198,3m3,4h</p>
                  </div>
                </div>

                {/* Descargar ejemplo */}
                <div className="text-center">
                  <button
                    onClick={descargarEjemploVentas}
                    className="flex items-center justify-center gap-2 px-6 py-3 bg-green-500 text-white rounded-lg text-sm hover:bg-green-600 transition-colors shadow-md mx-auto"
                  >
                    <Download className="w-4 h-4" />
                    Descargar Archivo de Ejemplo
                  </button>
                </div>

                {/* Cargar archivo */}
                <div
                  className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors cursor-pointer ${
                    isDragOver 
                      ? 'border-blue-500 bg-blue-50' 
                      : 'border-gray-300 hover:border-gray-400'
                  }`}
                  onDragOver={handleDragOver}
                  onDragLeave={handleDragLeave}
                  onDrop={handleDrop}
                  onClick={() => fileInputRef.current?.click()}
                >
                  <FileText className="w-12 h-12 mx-auto mb-4 text-gray-400" />
                  <p className="text-lg text-gray-600 mb-2">
                    Arrastra tu archivo aquí o haz clic para seleccionar
                  </p>
                  <p className="text-sm text-gray-500">
                    Solo archivos .txt con formato ventasYYYYMM.txt
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
                  <div className={`p-4 rounded-lg border ${
                    validacion.esValido 
                      ? 'bg-green-50 border-green-200' 
                      : 'bg-red-50 border-red-200'
                  }`}>
                    <div className="flex items-center gap-2 mb-3">
                      {validacion.esValido ? (
                        <CheckCircle className="w-5 h-5 text-green-600" />
                      ) : (
                        <AlertCircle className="w-5 h-5 text-red-600" />
                      )}
                      <span className={`font-medium ${
                        validacion.esValido ? 'text-green-800' : 'text-red-800'
                      }`}>
                        {validacion.esValido ? 'Archivo válido' : 'Archivo con errores'}
                      </span>
                    </div>
                    
                    {validacion.errores.length > 0 && (
                      <div className="mb-3">
                        <h5 className="font-medium text-red-800 mb-2">Errores encontrados:</h5>
                        <ul className="text-red-700 space-y-1 text-sm">
                          {validacion.errores.slice(0, 5).map((error, index) => (
                            <li key={index}>• {error}</li>
                          ))}
                          {validacion.errores.length > 5 && (
                            <li>• ... y {validacion.errores.length - 5} más</li>
                          )}
                        </ul>
                      </div>
                    )}
                    
                    {validacion.esValido && validacion.datosParseados && (
                      <div>
                        <p className="text-green-700 font-medium">
                          ✅ {validacion.datosParseados.length} pedido(s) válido(s) listo(s) para procesar
                        </p>
                      </div>
                    )}
                  </div>
                )}

                {/* Botón de procesar */}
                <div className="text-center">
                  <button
                    onClick={handleAgregarArchivo}
                    disabled={!validacion?.esValido || cargando}
                    className={`px-8 py-3 rounded-lg text-sm font-medium transition-colors ${
                      validacion?.esValido && !cargando
                        ? 'bg-blue-500 text-white hover:bg-blue-600 shadow-md'
                        : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                    }`}
                  >
                    {cargando ? 'Procesando Archivo...' : 'Procesar Archivo de Pedidos'}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AgregarPedidos; 