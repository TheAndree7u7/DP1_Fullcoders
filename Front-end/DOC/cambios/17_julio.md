Se elimino del archivo de simulaion semanal lo siguiente 

import ControlSimulacion from "../components/ControlSimulacion";
import IndicadorPaqueteActual from "../components/IndicadorPaqueteActual";


const [controlPanelExpandido, setControlPanelExpandido] = useState(false);

En la linea 102 del SimulacionSemanal.tsx
```js
      {/* Panel de control de simulación */}
      
    
      <div className="px-4 py-2">
        <div className="flex items-center justify-between mb-2">
          <button
            onClick={() => setControlPanelExpandido(!controlPanelExpandido)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
          >
            {controlPanelExpandido ? '🔼 Ocultar Control' : '🔽 Mostrar Control de Simulación'}
          </button>
        </div>
        
        {controlPanelExpandido && (
          <div className="transition-all duration-300">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
              {/* Control de simulación */}
              <div className="lg:col-span-2">
                <ControlSimulacion />
              </div>
              {/* Indicador detallado del paquete actual */}
              <div className="lg:col-span-1">
                <IndicadorPaqueteActual />
              </div>
            </div>
          </div>
        )}
      </div>
```