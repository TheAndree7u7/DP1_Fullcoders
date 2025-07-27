# Pantalla de Agregar Pedidos

## Descripción

Se ha creado una nueva pantalla independiente para agregar pedidos al sistema de simulación logística. Esta pantalla está disponible en la ruta `/agregar-pedidos` y proporciona la misma funcionalidad que el panel de agregar pedidos, pero en una interfaz dedicada y completa.

## Características

### 🎯 Funcionalidades Principales

1. **Pedidos Individuales**: Permite agregar pedidos uno por uno con:
   - Coordenadas del cliente (X, Y)
   - Nombre del cliente
   - Volumen de GLP (m³)
   - Horas límite (≥4 horas)

2. **Archivos de Pedidos**: Permite cargar archivos de texto con múltiples pedidos:
   - Formato: `ventasYYYYMM.txt`
   - Contenido: `fechaHora:coordenadaX,coordenadaY,codigoCliente,volumenGLP,horasLimite`
   - Ejemplo: `01d00h24m:16,13,c-198,3m3,4h`

### 🎨 Interfaz de Usuario

- **Diseño Responsivo**: Adaptado para diferentes tamaños de pantalla
- **Navegación Intuitiva**: Breadcrumb y botones de navegación
- **Validación en Tiempo Real**: Feedback inmediato sobre errores
- **Drag & Drop**: Soporte para arrastrar archivos
- **Notificaciones Toast**: Mensajes de éxito y error

### 🔧 Integración con el Sistema

- **Timestamp de Simulación**: Muestra la fecha/hora actual de la simulación
- **Recálculo Automático**: Recalcula el algoritmo genético después de agregar pedidos
- **Pausa/Reanudación**: Pausa la simulación durante el procesamiento
- **Contexto de Simulación**: Utiliza el contexto global de simulación

## Acceso

### Desde la Pantalla Principal

1. Navegar a la página principal (`/`)
2. Hacer clic en el botón **"📦 Agregar Pedidos"** en la sección de "Gestión de Pedidos"

### URL Directa

```
http://localhost:5173/agregar-pedidos
```

## Estructura de Archivos

```
Front-end/src/views/
└── AgregarPedidos.tsx          # Nueva pantalla independiente

Front-end/src/App.tsx           # Ruta agregada: /agregar-pedidos
Front-end/src/views/SeleccionVista.tsx  # Botón de acceso agregado
```

## Validaciones

### Pedidos Individuales

- ✅ Nombre del cliente requerido
- ✅ Volumen GLP > 0
- ✅ Horas límite ≥ 4
- ✅ Coordenadas X: 0-69, Y: 0-49

### Archivos de Pedidos

- ✅ Formato de nombre: `ventasYYYYMM.txt`
- ✅ Formato de contenido válido
- ✅ Validación de coordenadas
- ✅ Validación de volúmenes y horas límite

## Flujo de Procesamiento

1. **Validación**: Verifica datos del pedido/archivo
2. **Pausa Simulación**: Detiene el polling y pausa la simulación
3. **Envío al Backend**: Procesa pedidos en el servidor
4. **Recálculo**: Ejecuta algoritmo genético con nuevos datos
5. **Aplicación**: Aplica nueva solución al sistema
6. **Reanudación**: Reanuda la simulación
7. **Notificación**: Muestra resultado al usuario

## Mensajes de Error

### Errores de Validación

- ❌ "Por favor ingrese el nombre del cliente"
- ❌ "El volumen GLP debe ser mayor a 0"
- ⚠️ "Las horas límite deben ser mayor o igual a 4 horas"
- ❌ "La coordenada X debe estar entre 0 y 69"
- ❌ "La coordenada Y debe estar entre 0 y 49"

### Errores de Archivo

- ❌ "El nombre del archivo debe seguir el formato: ventasYYYYMM.txt"
- ❌ "Por favor selecciona un archivo de texto (.txt)"
- ❌ "Archivo con errores" (con lista detallada)

## Mensajes de Éxito

- ✅ "Pedidos agregados exitosamente: [mensaje] - Algoritmo recalculado"

## Dependencias

### Importaciones Principales

```typescript
import { useNavigate } from 'react-router-dom';
import { useSimulacion } from '../context/SimulacionContext';
import { ArchivosApiService } from '../services/archivosApiService';
import { getMejorIndividuo } from '../services/simulacionApiService';
import { validarArchivoVentas } from '../components/cargar_archivos/validadores';
```

### Servicios Utilizados

- `ArchivosApiService.procesarPedidosIndividuales()`: Procesa pedidos en el backend
- `getMejorIndividuo()`: Recalcula algoritmo genético
- `aplicarNuevaSolucionDespuesAveria()`: Aplica nueva solución

## Estilos

### Clases CSS Utilizadas

- **Layout**: `min-h-screen`, `max-w-4xl`, `mx-auto`
- **Cards**: `bg-white`, `rounded-lg`, `shadow-lg`
- **Botones**: `bg-blue-500`, `hover:bg-blue-600`, `transition-colors`
- **Formularios**: `border-gray-300`, `focus:ring-2`, `focus:ring-blue-500`
- **Estados**: `bg-green-50`, `bg-red-50`, `bg-blue-50`

## Compatibilidad

- ✅ React 18+
- ✅ TypeScript
- ✅ React Router v6
- ✅ Tailwind CSS
- ✅ Lucide React Icons
- ✅ React Toastify

## Próximas Mejoras

1. **Historial de Pedidos**: Mostrar pedidos agregados recientemente
2. **Edición de Pedidos**: Permitir modificar pedidos existentes
3. **Búsqueda y Filtros**: Filtrar pedidos por fecha, cliente, etc.
4. **Exportación**: Exportar pedidos a diferentes formatos
5. **Validación Avanzada**: Validación de rutas y conflictos logísticos

## Notas de Desarrollo

- La pantalla reutiliza la lógica del componente `AgregarPedidosPanel`
- Se mantiene la consistencia con el diseño del sistema
- Se integra completamente con el contexto de simulación
- Se siguen las mejores prácticas de React y TypeScript 