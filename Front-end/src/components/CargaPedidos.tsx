import React, { useState } from 'react';
import { Upload, Download, Check, AlertCircle, FileText, Calendar, Package } from 'lucide-react';
import { toast } from 'react-toastify';
import { 
  cargarLotePedidos, 
  validarLotePedidos, 
  parsearArchivoPedidos, 
  generarEjemploArchivoPedidos,
  calcularFechaInicioMinima,
  extraerFechaDesdeNombreArchivo,
  PedidosLoteRequest,
  PedidosLoteResponse,
  ValidacionResponse 
} from '../services/pedidosApiService';

interface CargaPedidosProps {
  onPedidosCargados?: (response: PedidosLoteResponse) => void;
}

const CargaPedidos: React.FC<CargaPedidosProps> = ({ onPedidosCargados }) => {
  const [archivos, setArchivos] = useState<File[]>([]);
  const [fechaInicioCalculada, setFechaInicioCalculada] = useState<Date | null>(null);
  const [descripcion, setDescripcion] = useState<string>('');
  const [cargando, setCargando] = useState(false);
  const [validando, setValidando] = useState(false);
  const [validacion, setValidacion] = useState<ValidacionResponse | null>(null);
  const [contenidosArchivos, setContenidosArchivos] = useState<{[key: string]: string}>({});
  const [resultadosCarga, setResultadosCarga] = useState<{[key: string]: PedidosLoteResponse | string}>({});
  const [fechasArchivos, setFechasArchivos] = useState<{[key: string]: Date | null}>({});

  const manejarSeleccionArchivos = (event: React.ChangeEvent<HTMLInputElement>) => {
    const archivosSeleccionados = Array.from(event.target.files || []);
    if (archivosSeleccionados.length > 0) {
      setArchivos(archivosSeleccionados);
      setValidacion(null);
      setResultadosCarga({});
      
      // Calcular fecha de inicio automáticamente
      const fechaInicio = calcularFechaInicioMinima(archivosSeleccionados);
      setFechaInicioCalculada(fechaInicio);
      
      // Extraer fechas individuales de cada archivo
      const nuevasFechas: {[key: string]: Date | null} = {};
      archivosSeleccionados.forEach(archivo => {
        const fecha = extraerFechaDesdeNombreArchivo(archivo.name);
        nuevasFechas[archivo.name] = fecha;
      });
      setFechasArchivos(nuevasFechas);
      
      // Leer contenido de todos los archivos
      const nuevosContenidos: {[key: string]: string} = {};
      let archivosLeidos = 0;
      
      archivosSeleccionados.forEach((archivo) => {
        const reader = new FileReader();
        reader.onload = (e) => {
          const contenido = e.target?.result as string;
          nuevosContenidos[archivo.name] = contenido;
          archivosLeidos++;
          
          if (archivosLeidos === archivosSeleccionados.length) {
            setContenidosArchivos(nuevosContenidos);
            console.log('📄 Archivos leídos:', Object.keys(nuevosContenidos));
          }
        };
        reader.readAsText(archivo);
      });
    }
  };

  const eliminarArchivo = (nombreArchivo: string) => {
    const nuevosArchivos = archivos.filter(archivo => archivo.name !== nombreArchivo);
    setArchivos(nuevosArchivos);
    
    // Recalcular fecha de inicio si quedan archivos
    if (nuevosArchivos.length > 0) {
      const nuevaFechaInicio = calcularFechaInicioMinima(nuevosArchivos);
      setFechaInicioCalculada(nuevaFechaInicio);
    } else {
      setFechaInicioCalculada(null);
    }
    
    setContenidosArchivos(prev => {
      const nuevos = { ...prev };
      delete nuevos[nombreArchivo];
      return nuevos;
    });
    setResultadosCarga(prev => {
      const nuevos = { ...prev };
      delete nuevos[nombreArchivo];
      return nuevos;
    });
    setFechasArchivos(prev => {
      const nuevos = { ...prev };
      delete nuevos[nombreArchivo];
      return nuevos;
    });
  };

  const validarFormulario = (): boolean => {
    if (archivos.length === 0) {
      toast.error('Debe seleccionar al menos un archivo de pedidos');
      return false;
    }
    
    if (!fechaInicioCalculada) {
      toast.error('No se pudo calcular la fecha de inicio automáticamente');
      return false;
    }
    
    return true;
  };

  const prepararDatosArchivo = (nombreArchivo: string): PedidosLoteRequest => {
    const contenido = contenidosArchivos[nombreArchivo];
    if (!fechaInicioCalculada) {
      throw new Error('Fecha de inicio no calculada');
    }
    const datos = parsearArchivoPedidos(contenido, fechaInicioCalculada, nombreArchivo);
    datos.descripcion = descripcion ? `${descripcion} - ${nombreArchivo}` : `Pedidos desde ${nombreArchivo}`;
    return datos;
  };

  const manejarValidacion = async () => {
    if (!validarFormulario()) return;

    setValidando(true);
    try {
      // Validar el primer archivo como muestra
      const primerArchivo = archivos[0].name;
      const datos = prepararDatosArchivo(primerArchivo);
      
      const respuesta = await validarLotePedidos(datos);
      setValidacion(respuesta);
      
      if (respuesta.valido) {
        toast.success(`✅ Validación exitosa (muestra ${primerArchivo}): ${respuesta.pedidosValidos} pedidos válidos`);
      } else {
        toast.warning(`⚠️ Validación con errores (muestra ${primerArchivo}): ${respuesta.errores.length} problemas encontrados`);
      }
      
    } catch (error) {
      console.error('Error en validación:', error);
      toast.error('Error al validar los pedidos');
    } finally {
      setValidando(false);
    }
  };

  const manejarCarga = async () => {
    if (!validarFormulario()) return;

    setCargando(true);
    const resultados: {[key: string]: PedidosLoteResponse | string} = {};
    let totalPedidosCreados = 0;
    let archivosExitosos = 0;
    
    try {
      // Procesar archivos secuencialmente para evitar sobrecarga del servidor
      for (const archivo of archivos) {
        try {
          console.log(`📤 Cargando archivo: ${archivo.name}`);
          toast.info(`📤 Procesando ${archivo.name}...`);
          
          const datos = prepararDatosArchivo(archivo.name);
          const respuesta = await cargarLotePedidos(datos);
          
          resultados[archivo.name] = respuesta;
          totalPedidosCreados += respuesta.totalPedidosCreados;
          archivosExitosos++;
          
          console.log(`✅ Archivo ${archivo.name} procesado: ${respuesta.totalPedidosCreados} pedidos`);
          
        } catch (error) {
          console.error(`❌ Error en archivo ${archivo.name}:`, error);
          resultados[archivo.name] = `Error: ${error instanceof Error ? error.message : 'Error desconocido'}`;
          toast.error(`❌ Error en ${archivo.name}`);
        }
      }
      
      setResultadosCarga(resultados);
      
      if (archivosExitosos > 0) {
        toast.success(`🎉 Carga completada: ${archivosExitosos}/${archivos.length} archivos exitosos, ${totalPedidosCreados} pedidos creados`);
        
        // Notificar al componente padre con el resumen
        if (onPedidosCargados && archivosExitosos > 0) {
          // Crear respuesta consolidada
          const respuestaConsolidada: PedidosLoteResponse = {
            totalRecibidos: Object.values(resultados).reduce((sum, res) => 
              typeof res === 'object' ? sum + res.totalRecibidos : sum, 0),
            pedidosExitosos: Object.values(resultados).reduce((sum, res) => 
              typeof res === 'object' ? sum + res.pedidosExitosos : sum, 0),
            pedidosDivididos: Object.values(resultados).reduce((sum, res) => 
              typeof res === 'object' ? sum + res.pedidosDivididos : sum, 0),
            totalPedidosCreados,
            errores: Object.values(resultados).flatMap(res => 
              typeof res === 'object' ? res.errores : [res]),
            fechaInicio: fechaInicioCalculada!.toISOString(),
            descripcion: `Carga múltiple: ${archivosExitosos} archivos procesados`
          };
          onPedidosCargados(respuestaConsolidada);
        }
      } else {
        toast.error('❌ No se pudo procesar ningún archivo correctamente');
      }
      
    } catch (error) {
      console.error('Error general en carga:', error);
      toast.error('Error general al cargar los pedidos');
    } finally {
      setCargando(false);
    }
  };

  const descargarEjemplo = () => {
    const ejemplo = generarEjemploArchivoPedidos();
    const blob = new Blob([ejemplo], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'ejemplo_pedidos.txt';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    toast.info('📄 Archivo de ejemplo descargado');
  };

  return (
    <div className="bg-white rounded-lg shadow-lg p-6 max-w-4xl mx-auto">
      <div className="flex items-center gap-3 mb-6">
        <Package className="text-blue-600" size={24} />
        <h2 className="text-2xl font-bold text-gray-800">Cargar Pedidos</h2>
      </div>

      {/* Información sobre el formato */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <div className="flex items-start gap-3">
          <FileText className="text-blue-600 mt-1" size={20} />
          <div>
            <h3 className="font-semibold text-blue-800 mb-2">Carga Múltiple de Archivos</h3>
            <p className="text-blue-700 text-sm mb-2">
              <strong>✨ Nueva funcionalidad:</strong> Ahora puedes seleccionar múltiples archivos para cargar de una vez.
            </p>
            <p className="text-blue-700 text-sm mb-2">
              <strong>Formato requerido:</strong> <code>[fecha]:[x],[y],[cliente],[volumen],[horas]</code>
            </p>
            <p className="text-blue-700 text-sm mb-2">
              <strong>Ejemplo:</strong> <code>01d00h24m:16,13,c-198,3m3,4h</code>
            </p>
            <p className="text-blue-700 text-sm mb-3">
              <strong>Fecha en formato:</strong> <code>DDdHHhMMm</code> (ejemplo: 01d00h24m = día 1, hora 0, minuto 24)
            </p>
            <div className="bg-blue-100 border border-blue-300 rounded p-3 mb-3">
              <h4 className="font-medium text-blue-800 mb-1">🚀 Características avanzadas:</h4>
              <ul className="text-blue-700 text-sm space-y-1">
                <li>• <strong>Detección automática de fechas:</strong> Extrae fechas de los nombres de archivo (ventas202501.txt = Enero 2025)</li>
                <li>• <strong>Fecha de inicio inteligente:</strong> Usa automáticamente la fecha más temprana encontrada</li>
                <li>• <strong>Procesamiento secuencial:</strong> Los archivos se procesan uno por uno para evitar sobrecarga</li>
                <li>• <strong>Reportes detallados:</strong> Cada archivo mantiene su propio reporte de errores y estadísticas</li>
                <li>• <strong>Consolidación automática:</strong> Los pedidos de todos los archivos se unifican para la simulación</li>
              </ul>
            </div>
            <button
              onClick={descargarEjemplo}
              className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-sm transition-colors"
            >
              <Download size={16} />
              Descargar ejemplo
            </button>
          </div>
        </div>
      </div>

      {/* Formulario */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        {/* Selección de archivos */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Archivos de pedidos
          </label>
          <div className="relative">
            <input
              type="file"
              accept=".txt,.csv"
              multiple
              onChange={manejarSeleccionArchivos}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
            {archivos.length > 0 && (
              <div className="mt-3 space-y-2">
                <div className="text-sm font-medium text-gray-700">
                  {archivos.length} archivo(s) seleccionado(s):
                </div>
                <div className="max-h-32 overflow-y-auto space-y-1">
                  {archivos.map((archivo, index) => {
                    const fechaArchivo = fechasArchivos[archivo.name];
                    return (
                      <div key={index} className="flex items-center justify-between bg-gray-50 px-3 py-2 rounded-lg">
                        <div className="flex items-center gap-2 text-sm">
                          <FileText size={16} className="text-blue-600" />
                          <span className="text-gray-800">{archivo.name}</span>
                          <span className="text-gray-500">({(archivo.size / 1024).toFixed(1)} KB)</span>
                          {fechaArchivo && (
                            <span className="text-green-600 text-xs bg-green-100 px-2 py-1 rounded">
                              {fechaArchivo.toLocaleDateString('es-ES', { year: 'numeric', month: '2-digit' })}
                            </span>
                          )}
                          {!fechaArchivo && (
                            <span className="text-orange-600 text-xs bg-orange-100 px-2 py-1 rounded">
                              Fecha no detectada
                            </span>
                          )}
                        </div>
                        <button
                          onClick={() => eliminarArchivo(archivo.name)}
                          className="text-red-600 hover:text-red-800 p-1"
                          title="Eliminar archivo"
                        >
                          ×
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Fecha de inicio automática */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Fecha de inicio (calculada automáticamente)
          </label>
          <div className="relative">
            <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
            <div className="w-full pl-10 border border-gray-300 rounded-lg px-3 py-2 bg-gray-50 text-gray-700">
              {fechaInicioCalculada ? (
                <div className="flex items-center gap-2">
                  <span className="font-medium">
                    {fechaInicioCalculada.toLocaleDateString('es-ES', { 
                      year: 'numeric', 
                      month: 'long', 
                      day: 'numeric' 
                    })}
                  </span>
                  <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded">
                    Detectada automáticamente
                  </span>
                </div>
              ) : (
                <span className="text-gray-500">Selecciona archivos para calcular fecha</span>
              )}
            </div>
          </div>
          {fechaInicioCalculada && (
            <div className="mt-2 text-xs text-gray-600">
              💡 La fecha se calcula automáticamente desde los nombres de archivo (formato: ventas202501.txt)
            </div>
          )}
        </div>
      </div>

      {/* Descripción */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Descripción (opcional)
        </label>
        <input
          type="text"
          value={descripcion}
          onChange={(e) => setDescripcion(e.target.value)}
          placeholder="Ej: Pedidos enero 2025"
          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
        />
      </div>

      {/* Botones de acción */}
      <div className="flex flex-wrap gap-3 mb-6">
        <button
          onClick={manejarValidacion}
          disabled={validando || archivos.length === 0 || !fechaInicioCalculada}
          className="flex items-center gap-2 bg-yellow-600 hover:bg-yellow-700 disabled:bg-gray-400 text-white px-4 py-2 rounded-lg transition-colors"
        >
          <AlertCircle size={16} />
          {validando ? 'Validando...' : `Validar (muestra)`}
        </button>

        <button
          onClick={manejarCarga}
          disabled={cargando || archivos.length === 0 || !fechaInicioCalculada}
          className="flex items-center gap-2 bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white px-4 py-2 rounded-lg transition-colors"
        >
          <Upload size={16} />
          {cargando ? `Cargando... (${Object.keys(resultadosCarga).length}/${archivos.length})` : `Cargar ${archivos.length} archivo(s)`}
        </button>
      </div>

      {/* Resultados de validación */}
      {validacion && (
        <div className={`rounded-lg p-4 ${validacion.valido ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'}`}>
          <h3 className={`font-semibold mb-3 ${validacion.valido ? 'text-green-800' : 'text-red-800'}`}>
            {validacion.valido ? '✅ Validación Exitosa' : '❌ Errores de Validación'}
          </h3>
          
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">{validacion.totalPedidos}</div>
              <div className="text-sm text-gray-600">Total</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">{validacion.pedidosValidos}</div>
              <div className="text-sm text-gray-600">Válidos</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-orange-600">{validacion.pedidosQueDividirian}</div>
              <div className="text-sm text-gray-600">Se dividirán</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-red-600">{validacion.errores.length}</div>
              <div className="text-sm text-gray-600">Errores</div>
            </div>
          </div>

          {validacion.errores.length > 0 && (
            <div className="mb-4">
              <h4 className="font-medium text-red-800 mb-2">Errores:</h4>
              <ul className="list-disc list-inside text-sm text-red-700 space-y-1">
                {validacion.errores.map((error, index) => (
                  <li key={index}>{error}</li>
                ))}
              </ul>
            </div>
          )}

          {validacion.advertencias.length > 0 && (
            <div>
              <h4 className="font-medium text-yellow-800 mb-2">Advertencias:</h4>
              <ul className="list-disc list-inside text-sm text-yellow-700 space-y-1">
                {validacion.advertencias.map((advertencia, index) => (
                  <li key={index}>{advertencia}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* Resultados de carga por archivo */}
      {Object.keys(resultadosCarga).length > 0 && (
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <h3 className="font-semibold mb-4 text-gray-800">Resultados de Carga por Archivo</h3>
          
          <div className="space-y-3">
            {Object.entries(resultadosCarga).map(([nombreArchivo, resultado]) => (
              <div key={nombreArchivo} className={`border rounded-lg p-3 ${
                typeof resultado === 'string' ? 'border-red-200 bg-red-50' : 'border-green-200 bg-green-50'
              }`}>
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <FileText size={16} className={typeof resultado === 'string' ? 'text-red-600' : 'text-green-600'} />
                    <span className="font-medium text-gray-800">{nombreArchivo}</span>
                  </div>
                  <div className={`text-sm font-medium ${
                    typeof resultado === 'string' ? 'text-red-600' : 'text-green-600'
                  }`}>
                    {typeof resultado === 'string' ? '❌ Error' : '✅ Exitoso'}
                  </div>
                </div>
                
                {typeof resultado === 'string' ? (
                  <div className="text-sm text-red-700">{resultado}</div>
                ) : (
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
                    <div>
                      <span className="text-gray-600">Recibidos:</span>
                      <span className="ml-1 font-medium">{resultado.totalRecibidos}</span>
                    </div>
                    <div>
                      <span className="text-gray-600">Exitosos:</span>
                      <span className="ml-1 font-medium">{resultado.pedidosExitosos}</span>
                    </div>
                    <div>
                      <span className="text-gray-600">Divididos:</span>
                      <span className="ml-1 font-medium">{resultado.pedidosDivididos}</span>
                    </div>
                    <div>
                      <span className="text-gray-600">Creados:</span>
                      <span className="ml-1 font-medium text-green-600">{resultado.totalPedidosCreados}</span>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
          
          {/* Resumen total */}
          <div className="mt-4 pt-4 border-t border-gray-200">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
              <div className="bg-blue-50 rounded p-3">
                <div className="text-lg font-bold text-blue-600">
                  {Object.keys(resultadosCarga).length}
                </div>
                <div className="text-sm text-blue-800">Archivos Procesados</div>
              </div>
              <div className="bg-green-50 rounded p-3">
                <div className="text-lg font-bold text-green-600">
                  {Object.values(resultadosCarga).filter(r => typeof r !== 'string').length}
                </div>
                <div className="text-sm text-green-800">Exitosos</div>
              </div>
              <div className="bg-red-50 rounded p-3">
                <div className="text-lg font-bold text-red-600">
                  {Object.values(resultadosCarga).filter(r => typeof r === 'string').length}
                </div>
                <div className="text-sm text-red-800">Con Errores</div>
              </div>
              <div className="bg-purple-50 rounded p-3">
                <div className="text-lg font-bold text-purple-600">
                  {Object.values(resultadosCarga).reduce((sum, res) => 
                    typeof res === 'object' ? sum + res.totalPedidosCreados : sum, 0)}
                </div>
                <div className="text-sm text-purple-800">Total Pedidos</div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CargaPedidos;