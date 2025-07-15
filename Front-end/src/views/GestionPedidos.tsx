import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Package, CheckCircle, AlertTriangle } from 'lucide-react';
import CargaPedidos from '../components/CargaPedidos';
import { PedidosLoteResponse } from '../services/pedidosApiService';
import { toast } from 'react-toastify';

const GestionPedidos: React.FC = () => {
  const navigate = useNavigate();
  const [ultimaCarga, setUltimaCarga] = useState<PedidosLoteResponse | null>(null);

  const manejarPedidosCargados = (response: PedidosLoteResponse) => {
    setUltimaCarga(response);
    
    // Mostrar resumen de la carga
    if (response.errores.length === 0) {
      toast.success(
        `🎉 Carga exitosa: ${response.totalPedidosCreados} pedidos creados`
      );
    } else {
      toast.warning(
        `⚠️ Carga parcial: ${response.pedidosExitosos} exitosos, ${response.errores.length} errores`
      );
    }
  };

  const irASimulacion = () => {
    toast.info('🔄 Redirigiéndose a la simulación...');
    navigate('/simulacion-semanal');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-4">
              <button
                onClick={() => navigate('/')}
                className="flex items-center gap-2 text-gray-600 hover:text-gray-800 transition-colors"
              >
                <ArrowLeft size={20} />
                Volver a simulación
              </button>
              <div className="h-6 w-px bg-gray-300"></div>
              <h1 className="text-xl font-semibold text-gray-900">Gestión de Pedidos</h1>
            </div>
          </div>
        </div>
      </div>

      {/* Contenido principal */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* Componente de carga */}
        <div className="mb-8">
          <CargaPedidos onPedidosCargados={manejarPedidosCargados} />
        </div>

        {/* Resumen de la última carga */}
        {ultimaCarga && (
          <div className="bg-white rounded-lg shadow-lg p-6">
            <div className="flex items-center gap-3 mb-6">
              {ultimaCarga.errores.length === 0 ? (
                <CheckCircle className="text-green-600" size={24} />
              ) : (
                <AlertTriangle className="text-yellow-600" size={24} />
              )}
              <h2 className="text-xl font-bold text-gray-800">Resumen de la Última Carga</h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
              <div className="bg-blue-50 rounded-lg p-4">
                <div className="text-center">
                  <div className="text-3xl font-bold text-blue-600 mb-2">
                    {ultimaCarga.totalRecibidos}
                  </div>
                  <div className="text-sm text-blue-800 font-medium">Pedidos Recibidos</div>
                </div>
              </div>

              <div className="bg-green-50 rounded-lg p-4">
                <div className="text-center">
                  <div className="text-3xl font-bold text-green-600 mb-2">
                    {ultimaCarga.pedidosExitosos}
                  </div>
                  <div className="text-sm text-green-800 font-medium">Procesados Exitosamente</div>
                </div>
              </div>

              <div className="bg-orange-50 rounded-lg p-4">
                <div className="text-center">
                  <div className="text-3xl font-bold text-orange-600 mb-2">
                    {ultimaCarga.pedidosDivididos}
                  </div>
                  <div className="text-sm text-orange-800 font-medium">Pedidos Divididos</div>
                </div>
              </div>

              <div className="bg-purple-50 rounded-lg p-4">
                <div className="text-center">
                  <div className="text-3xl font-bold text-purple-600 mb-2">
                    {ultimaCarga.totalPedidosCreados}
                  </div>
                  <div className="text-sm text-purple-800 font-medium">Total Creados</div>
                </div>
              </div>
            </div>

            {/* Información adicional */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div>
                <h3 className="font-semibold text-gray-800 mb-2">Información de la Carga</h3>
                <div className="space-y-2 text-sm">
                  <div>
                    <span className="font-medium text-gray-600">Fecha de inicio:</span>
                    <span className="ml-2 text-gray-800">
                      {new Date(ultimaCarga.fechaInicio).toLocaleString('es-ES')}
                    </span>
                  </div>
                  {ultimaCarga.descripcion && (
                    <div>
                      <span className="font-medium text-gray-600">Descripción:</span>
                      <span className="ml-2 text-gray-800">{ultimaCarga.descripcion}</span>
                    </div>
                  )}
                </div>
              </div>

              <div>
                <h3 className="font-semibold text-gray-800 mb-2">Estado de la Carga</h3>
                <div className="flex items-center gap-2">
                  {ultimaCarga.errores.length === 0 ? (
                    <>
                      <CheckCircle className="text-green-600" size={16} />
                      <span className="text-green-600 font-medium">Carga completada sin errores</span>
                    </>
                  ) : (
                    <>
                      <AlertTriangle className="text-yellow-600" size={16} />
                      <span className="text-yellow-600 font-medium">
                        Carga completada con {ultimaCarga.errores.length} errores
                      </span>
                    </>
                  )}
                </div>
              </div>
            </div>

            {/* Mostrar errores si los hay */}
            {ultimaCarga.errores.length > 0 && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
                <h3 className="font-semibold text-red-800 mb-3">
                  Errores encontrados ({ultimaCarga.errores.length})
                </h3>
                <ul className="list-disc list-inside text-sm text-red-700 space-y-1 max-h-32 overflow-y-auto">
                  {ultimaCarga.errores.map((error, index) => (
                    <li key={index}>{error}</li>
                  ))}
                </ul>
              </div>
            )}

            {/* Acción siguiente */}
            <div className="flex justify-center">
              <button
                onClick={irASimulacion}
                className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg transition-colors font-medium"
              >
                <Package size={20} />
                Ir a la Simulación
              </button>
            </div>
          </div>
        )}

        {/* Información sobre el proceso */}
        <div className="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-800 mb-4">¿Cómo funciona la carga de pedidos?</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-sm text-blue-700">
            <div>
              <h4 className="font-semibold mb-2">1. Seleccionar archivo</h4>
              <p>Sube un archivo de texto con el formato específico de pedidos. Puedes descargar un ejemplo para ver el formato correcto.</p>
            </div>
            <div>
              <h4 className="font-semibold mb-2">2. Validar datos</h4>
              <p>El sistema valida automáticamente el formato y detecta pedidos que necesitan ser divididos según la capacidad de los camiones.</p>
            </div>
            <div>
              <h4 className="font-semibold mb-2">3. Cargar a simulación</h4>
              <p>Los pedidos se procesan y quedan disponibles para la simulación. Automáticamente se sincronizan con el motor de optimización.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GestionPedidos;