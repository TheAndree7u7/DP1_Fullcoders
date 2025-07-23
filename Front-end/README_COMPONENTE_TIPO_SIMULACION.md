# Componente TipoSimulacionInfo

## Descripción

El componente `TipoSimulacionInfo` es un ejemplo de cómo mostrar información sobre el tipo de simulación actual del sistema. Este componente se conecta automáticamente con el backend para obtener y mostrar el estado actual de la configuración de simulación.

## Características

### ✅ Información en Tiempo Real
- Consulta automática del tipo de simulación actual
- Actualización en tiempo real de la información
- Timestamp de la última actualización

### ✅ Diseño Responsivo
- Interfaz limpia y moderna
- Colores diferenciados por tipo de simulación
- Iconos descriptivos para cada tipo

### ✅ Estados de Carga
- Spinner durante la carga inicial
- Manejo de errores con mensajes descriptivos
- Estado vacío cuando no hay datos

### ✅ Tipos de Simulación Soportados
- **DIARIA** ⚡ - Color azul
- **SEMANAL** 📊 - Color verde  
- **COLAPSO** 🚨 - Color rojo

## Uso del Componente

### Importación
```typescript
import TipoSimulacionInfo from '../components/TipoSimulacionInfo';
```

### Uso Básico
```tsx
<TipoSimulacionInfo />
```

### Con Clases CSS Personalizadas
```tsx
<TipoSimulacionInfo className="mt-4 mb-6" />
```

### En un Layout
```tsx
<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
  <div className="bg-gray-50 p-4 rounded-lg">
    <h2 className="text-lg font-semibold mb-4">Configuración del Sistema</h2>
    <TipoSimulacionInfo />
  </div>
  <div className="bg-gray-50 p-4 rounded-lg">
    <h2 className="text-lg font-semibold mb-4">Otra Información</h2>
    {/* Otro contenido */}
  </div>
</div>
```

## Estructura del Componente

### Props
```typescript
interface TipoSimulacionInfoProps {
  className?: string; // Clases CSS opcionales
}
```

### Estados Internos
```typescript
const [tipoActual, setTipoActual] = useState<TipoSimulacionActualResponse | null>(null);
const [cargando, setCargando] = useState(true);
const [error, setError] = useState<string | null>(null);
```

### Funciones Auxiliares
- `cargarTipoSimulacion()` - Carga los datos del backend
- `obtenerIconoTipo(tipo)` - Retorna el icono correspondiente
- `obtenerColorTipo(tipo)` - Retorna las clases CSS de color

## Ejemplo de Renderizado

### Estado de Carga
```tsx
<div className="bg-white rounded-lg shadow-md p-4">
  <div className="flex items-center justify-center">
    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-600 mr-2"></div>
    <span className="text-gray-600">Cargando tipo de simulación...</span>
  </div>
</div>
```

### Estado de Error
```tsx
<div className="bg-white rounded-lg shadow-md p-4">
  <div className="flex items-center justify-center text-red-600">
    <span className="mr-2">❌</span>
    <span>Error: No se pudo conectar con el servidor</span>
  </div>
</div>
```

### Estado Normal (SEMANAL)
```tsx
<div className="bg-white rounded-lg shadow-md p-4">
  <div className="flex items-center justify-between">
    <div className="flex items-center">
      <span className="text-2xl mr-3">📊</span>
      <div>
        <h3 className="font-semibold text-gray-900">Tipo de Simulación Actual</h3>
        <p className="text-sm text-gray-600">Simulación semanal - Simula una semana completa de operaciones</p>
      </div>
    </div>
    <div className="px-3 py-1 rounded-full border text-sm font-medium text-green-600 bg-green-100 border-green-200">
      SEMANAL
    </div>
  </div>
  <div className="mt-3 pt-3 border-t border-gray-200">
    <div className="flex justify-between text-xs text-gray-500">
      <span>Última actualización:</span>
      <span>15/01/2025, 10:30:00</span>
    </div>
  </div>
</div>
```

## Integración con el Sistema

### Dependencias
- `obtenerTipoSimulacionActual()` - Función del servicio de API
- `TipoSimulacionActualResponse` - Tipo de respuesta del backend

### Flujo de Datos
1. **Montaje del componente** → `useEffect` se ejecuta
2. **Carga de datos** → `cargarTipoSimulacion()` llama al backend
3. **Actualización de estado** → `setTipoActual(data)` actualiza la UI
4. **Renderizado** → Componente muestra la información

### Manejo de Errores
- Errores de red se capturan y muestran
- Estados de carga previenen errores de UI
- Mensajes descriptivos para el usuario

## Personalización

### Colores Personalizados
```typescript
const obtenerColorTipo = (tipo: string) => {
  switch (tipo) {
    case 'DIARIA':
      return 'text-blue-600 bg-blue-100 border-blue-200';
    case 'SEMANAL':
      return 'text-green-600 bg-green-100 border-green-200';
    case 'COLAPSO':
      return 'text-red-600 bg-red-100 border-red-200';
    default:
      return 'text-gray-600 bg-gray-100 border-gray-200';
  }
};
```

### Iconos Personalizados
```typescript
const obtenerIconoTipo = (tipo: string) => {
  switch (tipo) {
    case 'DIARIA':
      return '⚡';
    case 'SEMANAL':
      return '📊';
    case 'COLAPSO':
      return '🚨';
    default:
      return '❓';
  }
};
```

## Casos de Uso

### 1. Dashboard Principal
Mostrar el tipo de simulación actual en el dashboard principal para que los usuarios sepan qué configuración está activa.

### 2. Panel de Configuración
Incluir en un panel de configuración para mostrar el estado actual antes de permitir cambios.

### 3. Página de Información
Usar en una página de información del sistema para mostrar detalles técnicos.

### 4. Modal de Confirmación
Mostrar en un modal antes de cambiar el tipo de simulación para confirmar la acción.

## Próximas Mejoras

1. **Actualización Automática** - Polling para actualizar automáticamente
2. **Animaciones** - Transiciones suaves entre estados
3. **Tema Oscuro** - Soporte para modo oscuro
4. **Internacionalización** - Soporte para múltiples idiomas
5. **Accesibilidad** - Mejoras de accesibilidad (ARIA labels, etc.)

## Testing

### Casos de Prueba
- ✅ Carga inicial exitosa
- ✅ Manejo de errores de red
- ✅ Estados de carga
- ✅ Diferentes tipos de simulación
- ✅ Responsive design
- ✅ Accesibilidad básica

### Ejemplo de Test
```typescript
import { render, screen, waitFor } from '@testing-library/react';
import TipoSimulacionInfo from './TipoSimulacionInfo';

test('muestra el tipo de simulación actual', async () => {
  render(<TipoSimulacionInfo />);
  
  await waitFor(() => {
    expect(screen.getByText('Tipo de Simulación Actual')).toBeInTheDocument();
  });
});
``` 