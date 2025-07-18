import React, { useRef, useCallback } from 'react';

interface DragAndDropZoneProps {
  onFilesDrop: (files: File[]) => void;
  onFileClassified: (file: File, tipo: 'ventas' | 'bloqueos' | 'camiones' | 'mantenimiento') => Promise<void>;
  isDragOver: boolean;
  setIsDragOver: (isDragOver: boolean) => void;
}

const DragAndDropZone: React.FC<DragAndDropZoneProps> = ({
  onFilesDrop,
  onFileClassified,
  isDragOver,
  setIsDragOver
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Función para clasificar archivos según su nombre
  const clasificarArchivo = (fileName: string): 'ventas' | 'bloqueos' | 'camiones' | 'mantenimiento' | null => {
    const nombreLower = fileName.toLowerCase();
    
    // Patrones para clasificar archivos
    if (nombreLower.indexOf('venta') !== -1 || nombreLower.indexOf('pedido') !== -1 || nombreLower.indexOf('ventas') !== -1) {
      return 'ventas';
    }
    
    if (nombreLower.indexOf('bloqueo') !== -1 || nombreLower.indexOf('bloqueos') !== -1) {
      return 'bloqueos';
    }
    
    if (nombreLower.indexOf('camion') !== -1 || nombreLower.indexOf('camiones') !== -1) {
      return 'camiones';
    }
    
    if (nombreLower.indexOf('mant') !== -1 || nombreLower.indexOf('mantenimiento') !== -1 || nombreLower.indexOf('preventivo') !== -1) {
      return 'mantenimiento';
    }
    
    // Si no coincide con ningún patrón, intentar clasificar por extensión y contenido
    if (fileName.indexOf('.txt') !== -1 && fileName.length > 4 && fileName.substring(fileName.length - 4) === '.txt') {
      // Por defecto, si es .txt y no coincide con patrones específicos, asumir que es ventas
      return 'ventas';
    }
    
    return null;
  };

  // Función para procesar archivos
  const procesarArchivos = useCallback(async (files: FileList | File[]) => {
    const archivosArray = Array.prototype.slice.call(files);
    const archivosClasificados: { [key: string]: File[] } = {
      ventas: [],
      bloqueos: [],
      camiones: [],
      mantenimiento: [],
      noClasificados: []
    };

    // Procesar archivos de forma asíncrona
    for (const file of archivosArray) {
      const tipo = clasificarArchivo(file.name);
      if (tipo) {
        archivosClasificados[tipo].push(file);
        await onFileClassified(file, tipo);
      } else {
        archivosClasificados.noClasificados.push(file);
      }
    }

    // Mostrar resumen de clasificación
    const resumen = Object.keys(archivosClasificados)
      .filter(tipo => archivosClasificados[tipo].length > 0)
      .map(tipo => `${tipo}: ${archivosClasificados[tipo].length} archivo(s)`)
      .join(', ');

    console.log('Archivos clasificados:', resumen);
    
    // Llamar a la función callback con todos los archivos
    onFilesDrop(archivosArray);
  }, [onFilesDrop, onFileClassified]);

  // Eventos de drag and drop
  const handleDragEnter = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(true);
  }, [setIsDragOver]);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  }, [setIsDragOver]);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  const handleDrop = useCallback(async (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    const files = e.dataTransfer.files;
    if (files.length > 0) {
      await procesarArchivos(files);
    }
  }, [setIsDragOver, procesarArchivos]);

  // Evento para selección manual de archivos
  const handleFileSelect = useCallback(async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      await procesarArchivos(files);
    }
  }, [procesarArchivos]);

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div
      className={`border-2 border-dashed rounded-lg p-8 text-center transition-all duration-200 ${
        isDragOver
          ? 'border-blue-500 bg-blue-50 scale-105'
          : 'border-gray-300 hover:border-gray-400 hover:bg-gray-50'
      }`}
      onDragEnter={handleDragEnter}
      onDragLeave={handleDragLeave}
      onDragOver={handleDragOver}
      onDrop={handleDrop}
      onClick={handleClick}
    >
      <div className="space-y-4">
        <div className="text-6xl">📁</div>
        
        <div className="space-y-2">
          <h3 className="text-lg font-semibold text-gray-700">
            {isDragOver ? 'Suelta los archivos aquí' : 'Arrastra y suelta archivos aquí'}
          </h3>
          
          <p className="text-sm text-gray-500">
            O haz clic para seleccionar archivos manualmente
          </p>
        </div>

        <div className="text-xs text-gray-400 space-y-1">
          <p><strong>Tipos de archivos soportados:</strong></p>
          <p>• Ventas/Pedidos: archivos con "venta", "pedido" o "ventas" en el nombre</p>
          <p>• Bloqueos: archivos con "bloqueo" o "bloqueos" en el nombre</p>
          <p>• Camiones: archivos con "camion" o "camiones" en el nombre</p>
          <p>• Mantenimiento: archivos con "mant", "mantenimiento" o "preventivo" en el nombre</p>
          <p>• Formato: archivos .txt</p>
        </div>

        <div className="text-xs text-blue-600">
          <p>💡 <strong>Consejo:</strong> Los archivos se clasificarán automáticamente según su nombre</p>
        </div>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept=".txt"
        onChange={handleFileSelect}
        className="hidden"
      />
    </div>
  );
};

export default DragAndDropZone; 