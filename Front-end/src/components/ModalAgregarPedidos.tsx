import React, { useState, useRef } from 'react';
import { X, Plus, Upload, Download, AlertCircle, CheckCircle, FileText } from 'lucide-react';
import { validarArchivoVentas } from './cargar_archivos/validadores';
import { ejemplos, descargarEjemplo } from './cargar_archivos/ejemplos';
import type { ValidacionArchivo, DatosVentas } from '../types';

// Constantes del mapa
const MAP_WIDTH = 70;
const MAP_HEIGHT = 50;

interface ModalAgregarPedidosProps {
  isOpen: boolean;
  onClose: () => void;
  onAgregarPedido: (archivo: { nombre: string; contenido: string; datos: DatosVentas[] }) => void;
  onAgregarArchivo: (archivo: { nombre: string; contenido: string; datos: DatosVentas[] }) => void;
}

const ModalAgregarPedidos: React.FC<ModalAgregarPedidosProps> = ({
  isOpen,
  onClose,
  onAgregarPedido,
  onAgregarArchivo
}) => {
  const [modo, setModo] = useState<'individual' | 'archivo'>('individual');
  const [archivoSeleccionado, setArchivoSeleccionado] = useState<File | null>(null);
  const [validacion, setValidacion] = useState<ValidacionArchivo | null>(null);
  const [cargando, setCargando] = useState(false);
  const [isDragOver, setIsDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Campos para pedido individual
  const [pedidoIndividual, setPedidoIndividual] = useState({
    año: new Date().getFullYear(),
    mes: new Date().getMonth() + 1,
    dia: new Date().getDate(),
    hora: 0,
    minuto: 0,
    coordenadaX: 0,
    coordenadaY: 0,
    nombreCliente: '',
    glp: 0,
    horasLimite: 0
  });

  if (!isOpen) return null;

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

  const handleAgregarPedidoIndividual = () => {
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

    // Formatear fecha y hora
    const fechaHora = `${pedidoIndividual.dia.toString().padStart(2, '0')}d${pedidoIndividual.hora.toString().padStart(2, '0')}h${pedidoIndividual.minuto.toString().padStart(2, '0')}m`;
    
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

    // Generar nombre del archivo usando año y mes del formulario
    const nombreArchivo = `ventas${pedidoIndividual.año}${pedidoIndividual.mes.toString().padStart(2, '0')}.txt`;
    
    // Generar contenido del archivo (una sola línea)
    const contenido = `${fechaHora}:${pedidoIndividual.coordenadaX},${pedidoIndividual.coordenadaY},${codigoCliente},${pedidoIndividual.glp}m3,${pedidoIndividual.horasLimite}h`;

    // Crear el objeto con el mismo formato que los archivos
    const archivoPedido = {
      nombre: nombreArchivo,
      contenido: contenido,
      datos: [pedido]
    };

    onAgregarPedido(archivoPedido);
    
    // Limpiar formulario
    setPedidoIndividual({
      año: new Date().getFullYear(),
      mes: new Date().getMonth() + 1,
      dia: new Date().getDate(),
      hora: 0,
      minuto: 0,
      coordenadaX: 0,
      coordenadaY: 0,
      nombreCliente: '',
      glp: 0,
      horasLimite: 0
    });
  };

  const handleAgregarArchivo = () => {
    if (!archivoSeleccionado || !validacion || !validacion.esValido) {
      alert('Por favor seleccione un archivo válido');
      return;
    }

    setCargando(true);

    const reader = new FileReader();
    reader.onload = (e) => {
      const contenido = e.target?.result as string;
      const datos = validacion.datosParseados as DatosVentas[];
      
      // Generar nombre de archivo
      const fecha = new Date();
      const nombreArchivo = `ventas${fecha.getFullYear()}${(fecha.getMonth() + 1).toString().padStart(2, '0')}.txt`;

      onAgregarArchivo({
        nombre: nombreArchivo,
        contenido,
        datos
      });

      setCargando(false);
      onClose();
    };
    reader.readAsText(archivoSeleccionado);
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

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-gray-800">Agregar Pedidos</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700"
          >
            <X size={24} />
          </button>
        </div>

        {/* Selector de modo */}
        <div className="mb-6">
          <div className="flex gap-2">
            <button
              onClick={() => setModo('individual')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                modo === 'individual'
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              <Plus className="w-4 h-4 inline mr-2" />
              Pedido Individual
            </button>
            <button
              onClick={() => setModo('archivo')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                modo === 'archivo'
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              <Upload className="w-4 h-4 inline mr-2" />
              Archivo de Pedidos
            </button>
          </div>
        </div>

        {modo === 'individual' ? (
          /* Formulario para pedido individual */
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-700 mb-4">Información del Pedido</h3>
            
            {/* Fecha y hora */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Año
                </label>
                <input
                  type="number"
                  value={pedidoIndividual.año}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, año: parseInt(e.target.value) }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  min={2020}
                  max={2030}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Mes
                </label>
                <input
                  type="number"
                  value={pedidoIndividual.mes}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, mes: parseInt(e.target.value) }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  min={1}
                  max={12}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Día
                </label>
                <input
                  type="number"
                  value={pedidoIndividual.dia}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, dia: parseInt(e.target.value) }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  min={1}
                  max={31}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Hora
                </label>
                <input
                  type="number"
                  value={pedidoIndividual.hora}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, hora: parseInt(e.target.value) }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  min={0}
                  max={23}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Minutos
                </label>
                <input
                  type="number"
                  value={pedidoIndividual.minuto}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, minuto: parseInt(e.target.value) }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  min={0}
                  max={59}
                />
              </div>
            </div>

            {/* Coordenadas */}
            <div>
              <div className="mb-2">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Coordenadas del Pedido
                </label>
                <p className="text-xs text-gray-500 mb-2">
                  El mapa tiene dimensiones de {MAP_WIDTH} x {MAP_HEIGHT}. Las coordenadas deben estar entre 0 y {MAP_WIDTH - 1} para X, y entre 0 y {MAP_HEIGHT - 1} para Y.
                </p>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Coordenada X (0-{MAP_WIDTH - 1})
                  </label>
                  <input
                    type="number"
                    value={pedidoIndividual.coordenadaX}
                    onChange={(e) => setPedidoIndividual(prev => ({ ...prev, coordenadaX: parseInt(e.target.value) }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    min={0}
                    max={MAP_WIDTH - 1}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Coordenada Y (0-{MAP_HEIGHT - 1})
                  </label>
                  <input
                    type="number"
                    value={pedidoIndividual.coordenadaY}
                    onChange={(e) => setPedidoIndividual(prev => ({ ...prev, coordenadaY: parseInt(e.target.value) }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    min={0}
                    max={MAP_HEIGHT - 1}
                  />
                </div>
              </div>
            </div>

            {/* Cliente y GLP */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre del Cliente
                </label>
                <input
                  type="text"
                  value={pedidoIndividual.nombreCliente}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, nombreCliente: e.target.value }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Nombre del cliente"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Volumen GLP (m³)
                </label>
                <input
                  type="number"
                  value={pedidoIndividual.glp}
                  onChange={(e) => setPedidoIndividual(prev => ({ ...prev, glp: parseInt(e.target.value) }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  min={1}
                />
              </div>
            </div>

            {/* Horas límite */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Horas Límite para Entrega
              </label>
              <p className="text-xs text-gray-500 mb-2">
                Las horas límite deben ser mayor o igual a 4. Pedidos con menos de 4 horas serán omitidos.
              </p>
              <input
                type="number"
                value={pedidoIndividual.horasLimite}
                onChange={(e) => setPedidoIndividual(prev => ({ ...prev, horasLimite: parseInt(e.target.value) }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                min={4}
              />
            </div>

            <button
              onClick={handleAgregarPedidoIndividual}
              className="w-full bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 transition-colors font-medium"
            >
              Agregar Pedido
            </button>
          </div>
        ) : (
          /* Formulario para archivo */
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-700 mb-4">Cargar Archivo de Pedidos</h3>
            
            {/* Información del formato */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <h4 className="font-medium text-blue-800 mb-2">Formato requerido:</h4>
              
              {/* Nombre del archivo */}
              <div className="mb-3 p-2 bg-yellow-50 border border-yellow-200 rounded">
                <p className="text-sm font-medium text-yellow-800 mb-1">
                  📁 Nombre del archivo:
                </p>
                <p className="text-sm text-yellow-700">
                  <code className="bg-yellow-100 px-2 py-1 rounded">ventasYYYYMM.txt</code>
                </p>
                <p className="text-xs text-yellow-600 mt-1">
                  Ejemplo: <code className="bg-yellow-100 px-1 py-0.5 rounded">ventas202501.txt</code> (enero 2025)
                </p>
              </div>
              
              {/* Formato del contenido */}
              <div className="mb-3">
                <p className="text-sm font-medium text-blue-800 mb-1">📄 Formato del contenido:</p>
                <p className="text-sm text-blue-700 mb-2">
                  <code className="bg-blue-100 px-2 py-1 rounded">fechaHora:coordenadaX,coordenadaY,codigoCliente,volumenGLP,horasLimite</code>
                </p>
                <p className="text-sm text-blue-700 mb-2">
                  Ejemplo: <code className="bg-blue-100 px-2 py-1 rounded">01d00h24m:16,13,c-198,3m3,4h</code>
                </p>
              </div>
              
              {/* Validaciones */}
              <div className="text-xs text-blue-700 space-y-1">
                <p><strong>✅ Validaciones:</strong></p>
                <p>• Coordenadas: X (0-{MAP_WIDTH - 1}), Y (0-{MAP_HEIGHT - 1})</p>
                <p>• Horas límite: ≥ 4 horas (pedidos con menos tiempo serán omitidos)</p>
                <p>• Volumen GLP: debe incluir "m3" al final</p>
                <p>• Código cliente: debe seguir el patrón "c-NUMERO"</p>
                <p>• Nombre archivo: formato "ventasYYYYMM.txt"</p>
              </div>
            </div>

            {/* Descargar ejemplo */}
            <div className="flex justify-center">
              <button
                onClick={descargarEjemploVentas}
                className="flex items-center gap-2 px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
              >
                <Download className="w-4 h-4" />
                Descargar Ejemplo
              </button>
            </div>

            {/* Cargar archivo */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Seleccionar archivo:
              </label>
              
              {/* Drag and Drop Zone */}
              <div
                className={`border-2 border-dashed rounded-lg p-6 text-center transition-colors ${
                  isDragOver 
                    ? 'border-blue-500 bg-blue-50' 
                    : 'border-gray-300 hover:border-gray-400'
                }`}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onClick={() => fileInputRef.current?.click()}
              >
                <FileText className="w-8 h-8 mx-auto mb-2 text-gray-400" />
                <p className="text-sm text-gray-600 mb-1">
                  Arrastra y suelta tu archivo aquí, o haz clic para seleccionar
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
            </div>

            {/* Validación */}
            {validacion && (
              <div className={`p-4 rounded-lg border ${
                validacion.esValido 
                  ? 'bg-green-50 border-green-200' 
                  : 'bg-red-50 border-red-200'
              }`}>
                <div className="flex items-center gap-2 mb-2">
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
                  <div className="mb-2">
                    <h5 className="font-medium text-red-800 mb-1">Errores:</h5>
                    <ul className="text-sm text-red-700 space-y-1">
                      {validacion.errores.map((error, index) => (
                        <li key={index}>• {error}</li>
                      ))}
                    </ul>
                  </div>
                )}
                
                {validacion.advertencias.length > 0 && (
                  <div className="mb-2">
                    <h5 className="font-medium text-yellow-800 mb-1">Advertencias:</h5>
                    <ul className="text-sm text-yellow-700 space-y-1">
                      {validacion.advertencias.map((warning, index) => (
                        <li key={index}>• {warning}</li>
                      ))}
                    </ul>
                  </div>
                )}
                
                {validacion.esValido && validacion.datosParseados && (
                  <div>
                    <h5 className="font-medium text-green-800 mb-1">Resumen:</h5>
                    <p className="text-sm text-green-700">
                      {validacion.datosParseados.length} pedido(s) válido(s) encontrado(s)
                    </p>
                  </div>
                )}
              </div>
            )}

            <button
              onClick={handleAgregarArchivo}
              disabled={!validacion?.esValido || cargando}
              className={`w-full py-2 px-4 rounded-lg font-medium transition-colors ${
                validacion?.esValido && !cargando
                  ? 'bg-blue-500 text-white hover:bg-blue-600'
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              }`}
            >
              {cargando ? 'Cargando...' : 'Agregar Archivo'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ModalAgregarPedidos; 