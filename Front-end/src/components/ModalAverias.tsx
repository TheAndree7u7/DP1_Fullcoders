import React, { useState } from 'react';
import { useSimulacion } from '../context/SimulacionContext';

interface ModalAveriasProps {
  isOpen: boolean;
  onClose: () => void;
}

const ModalAverias: React.FC<ModalAveriasProps> = ({ isOpen, onClose }) => {
  const { camiones, registrarAveriaConRecalculo } = useSimulacion();
  const [camionSeleccionado, setCamionSeleccionado] = useState('');
  const [tipoIncidente, setTipoIncidente] = useState('');
  const [cargando, setCargando] = useState(false);

  const tiposIncidente = [
    { value: 'TI1', label: 'Avería Mecánica' },
    { value: 'TI2', label: 'Avería Eléctrica' },
    { value: 'TI3', label: 'Avería de Combustible' },
    // { value: 'TI4', label: 'Avería de Neumáticos' },
    // { value: 'TI5', label: 'Avería de Sistema de Frenos' },
  ];

  const camionesDisponibles = camiones.filter(camion => 
    camion.estado !== 'Averiado' && camion.estado !== 'En Mantenimiento'
  );

  const handleRegistrarAveria = async () => {
    if (!camionSeleccionado || !tipoIncidente) {
      alert('Por favor selecciona un camión y tipo de incidente');
      return;
    }

    try {
      setCargando(true);
      
      // Usar la nueva función que incluye recálculo
      await registrarAveriaConRecalculo(camionSeleccionado, tipoIncidente);
      
      // Cerrar modal y mostrar mensaje de éxito
      onClose();
      alert("Avería registrada y simulación recalculada exitosamente");
      
      // Limpiar formulario
      setCamionSeleccionado('');
      setTipoIncidente('');
      
    } catch (error) {
      console.error("Error al registrar avería:", error);
      alert("Error al registrar avería: " + (error as Error).message);
    } finally {
      setCargando(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-96 max-w-md">
        <h2 className="text-xl font-bold mb-4 text-red-600">
          🚨 Registrar Avería con Recálculo
        </h2>
        
        <div className="mb-4">
          <label className="block text-sm font-medium mb-2">
            Camión:
          </label>
          <select
            value={camionSeleccionado}
            onChange={(e) => setCamionSeleccionado(e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-md"
            disabled={cargando}
          >
            <option value="">Selecciona un camión</option>
            {camionesDisponibles.map((camion) => (
              <option key={camion.id} value={camion.id}>
                {camion.id} - {camion.tipo} ({camion.estado})
              </option>
            ))}
          </select>
        </div>

        <div className="mb-4">
          <label className="block text-sm font-medium mb-2">
            Tipo de Incidente:
          </label>
          <select
            value={tipoIncidente}
            onChange={(e) => setTipoIncidente(e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-md"
            disabled={cargando}
          >
            <option value="">Selecciona tipo de incidente</option>
            {tiposIncidente.map((tipo) => (
              <option key={tipo.value} value={tipo.value}>
                {tipo.label}
              </option>
            ))}
          </select>
        </div>

        <div className="mb-4 p-3 bg-yellow-50 border border-yellow-200 rounded-md">
          <p className="text-sm text-yellow-800">
            ⚠️ <strong>Importante:</strong> Al registrar esta avería, se descartarán 
            todas las simulaciones anticipadas y se generará una nueva simulación 
            optimizada que considere el estado del camión averiado.
          </p>
        </div>

        <div className="flex justify-end space-x-2">
          <button
            onClick={onClose}
            disabled={cargando}
            className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            onClick={handleRegistrarAveria}
            disabled={cargando || !camionSeleccionado || !tipoIncidente}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {cargando ? 'Procesando...' : 'Registrar Avería'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ModalAverias;
