# RightMenu Components

Esta carpeta contiene los componentes refactorizados del `RightMenu` original, separados en archivos individuales para mejor organización y mantenimiento.

## Estructura de Archivos

### `DatosCamionesTable.tsx`
Componente para mostrar y gestionar la tabla de datos de camiones.

**Características:**
- Búsqueda y filtrado de camiones
- Ordenamiento por columnas
- Validación de datos
- Interacción con el mapa (resaltado de camiones)

**Props:**
```typescript
interface DatosCamionesTableProps {
  onElementoSeleccionado: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void;
}
```

### `TablaAlmacenes.tsx`
Componente para mostrar y gestionar la tabla de almacenes.

**Características:**
- Búsqueda y filtrado de almacenes
- Ordenamiento por columnas
- Visualización de porcentajes de GLP
- Interacción con el mapa (resaltado de almacenes)

**Props:**
```typescript
interface TablaAlmacenesProps {
  onElementoSeleccionado: (elemento: {tipo: 'camion' | 'pedido' | 'almacen', id: string} | null) => void;
}
```

### `index.ts`
Archivo de índice que exporta todos los componentes de la carpeta para facilitar las importaciones.

### `README.md`
Documentación de la estructura y uso de los componentes.

## Uso

### Importación desde el componente principal
```typescript
import DatosCamionesTable from './rightmenu/DatosCamionesTable';
import TablaAlmacenes from './rightmenu/TablaAlmacenes';
```

### Importación usando el índice
```typescript
import { DatosCamionesTable, TablaAlmacenes } from './rightmenu';
```

### Componente Principal
El componente principal `RightMenu` se encuentra en `../RightMenu.tsx` y utiliza estos componentes:

```typescript
import DatosCamionesTable from './rightmenu/DatosCamionesTable';
import TablaAlmacenes from './rightmenu/TablaAlmacenes';
```

## Beneficios de la Refactorización

1. **Separación de Responsabilidades**: Cada componente tiene una responsabilidad específica
2. **Mantenibilidad**: Es más fácil mantener y modificar componentes individuales
3. **Reutilización**: Los componentes pueden ser reutilizados en otras partes de la aplicación
4. **Legibilidad**: El código es más fácil de leer y entender
5. **Testing**: Es más fácil escribir pruebas unitarias para componentes específicos

## Dependencias

- `react`: Framework principal
- `lucide-react`: Iconos
- `../../context/SimulacionContext`: Contexto de simulación
- `../../utils/validacionCamiones`: Utilidades de validación

## Notas de Desarrollo

- Todos los componentes mantienen la misma funcionalidad que el `RightMenu` original
- Los estilos y clases CSS se mantienen consistentes
- La interfaz de usuario no ha cambiado, solo la estructura del código
- Los componentes son completamente tipados con TypeScript
- El componente principal `RightMenu` coordina la navegación entre paneles 