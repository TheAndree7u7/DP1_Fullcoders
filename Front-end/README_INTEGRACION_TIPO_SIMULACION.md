# Integración de Tipo de Simulación en el Frontend

## Resumen

Se ha integrado la funcionalidad de cambio de tipo de simulación en el frontend, específicamente en la pantalla de selección de vista (`SeleccionVista.tsx`). Ahora cuando el usuario selecciona una vista, automáticamente se cambia el tipo de simulación correspondiente en el backend antes de navegar a la vista seleccionada.

## Archivos Modificados

### 1. `src/config/api.ts`
- **Agregados nuevos endpoints:**
  - `CAMBIAR_TIPO_SIMULACION: "/simulacion/cambiar-tipo-simulacion"`
  - `TIPO_SIMULACION_ACTUAL: "/simulacion/tipo-simulacion-actual"`

### 2. `src/services/simulacionApiService.ts`
- **Nuevos tipos:**
  - `TipoSimulacion` - Enum con los tipos disponibles
  - `TipoSimulacionResponse` - Respuesta del cambio de tipo
  - `TipoSimulacionActualResponse` - Respuesta de consulta del tipo actual

- **Nuevas funciones:**
  - `cambiarTipoSimulacion(tipoSimulacion)` - Cambia el tipo de simulación
  - `obtenerTipoSimulacionActual()` - Consulta el tipo actual

### 3. `src/views/SeleccionVista.tsx`
- **Nuevas funcionalidades:**
  - Estado de carga para cada opción
  - Mensajes de estado (éxito, error, info)
  - Cambio automático de tipo de simulación al seleccionar vista
  - Indicadores visuales de carga

## Mapeo de Vistas a Tipos de Simulación

| Vista | Tipo de Simulación | Descripción |
|-------|-------------------|-------------|
| Ejecución en Tiempo Real | `DIARIA` | Simulación diaria con actualizaciones continuas |
| Simulación Semanal | `SEMANAL` | Vista semanal completa con análisis de rendimiento |
| Colapso Logístico | `COLAPSO` | Simulación de escenarios de colapso |

## Flujo de Funcionamiento

### 1. Selección de Vista
```typescript
const handleSeleccionVista = async (opcion) => {
  // 1. Mostrar estado de carga
  setCargando(opcion.id);
  setMensaje({ texto: "Configurando simulación...", tipo: 'info' });

  // 2. Cambiar tipo de simulación en backend
  const respuesta = await cambiarTipoSimulacion(opcion.tipoSimulacion);
  
  // 3. Manejar respuesta
  if (respuesta.exito) {
    setMensaje({ texto: "✅ Configuración exitosa", tipo: 'success' });
    setTimeout(() => navigate(opcion.ruta), 1000);
  } else {
    setMensaje({ texto: "❌ Error en configuración", tipo: 'error' });
  }
};
```

### 2. Llamada al Backend
```typescript
export async function cambiarTipoSimulacion(tipoSimulacion: TipoSimulacion) {
  const response = await fetch(API_URLS.CAMBIAR_TIPO_SIMULACION, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ tipoSimulacion })
  });
  
  return response.json();
}
```

## Características Implementadas

### ✅ Integración Automática
- Cambio automático de tipo de simulación al seleccionar vista
- Mapeo directo entre vistas y tipos de simulación

### ✅ Feedback Visual
- Indicadores de carga en cada botón
- Mensajes de estado con colores apropiados
- Spinner de carga durante la configuración

### ✅ Manejo de Errores
- Captura y visualización de errores del backend
- Mensajes descriptivos para el usuario
- Prevención de navegación en caso de error

### ✅ Experiencia de Usuario
- Botones deshabilitados durante la carga
- Transición suave después del éxito
- Feedback inmediato de las acciones

## Estructura de Datos

### Tipo de Simulación
```typescript
export type TipoSimulacion = 'DIARIA' | 'SEMANAL' | 'COLAPSO';
```

### Respuesta del Backend
```typescript
export interface TipoSimulacionResponse {
  tipoSimulacionAnterior: TipoSimulacion;
  tipoSimulacionNuevo: TipoSimulacion;
  mensaje: string;
  exito: boolean;
}
```

### Opción de Vista
```typescript
interface OpcionVista {
  id: string;
  titulo: string;
  descripcion: string;
  ruta: string;
  imagen: string;
  color: string;
  icono: string;
  tipoSimulacion: TipoSimulacion; // Nuevo campo
}
```

## Consideraciones Técnicas

### 1. **Sincronización Backend-Frontend**
- El tipo de simulación se cambia antes de navegar
- Se espera confirmación del backend antes de continuar
- Se maneja el caso de error para evitar navegación incorrecta

### 2. **Estados de Carga**
- Cada opción tiene su propio estado de carga
- Los botones se deshabilitan durante la operación
- Se muestra feedback visual inmediato

### 3. **Manejo de Errores**
- Errores de red se capturan y muestran al usuario
- Errores del backend se procesan y muestran
- Se previene la navegación en caso de error

### 4. **UX/UI**
- Mensajes con colores apropiados (verde=éxito, rojo=error, azul=info)
- Spinner de carga para indicar progreso
- Transiciones suaves entre estados

## Pruebas

Para probar la integración:

1. **Iniciar la aplicación** - Backend y frontend corriendo
2. **Navegar a la pantalla de selección** - `/` o ruta principal
3. **Seleccionar una vista** - Hacer clic en cualquier opción
4. **Verificar el comportamiento:**
   - Botón muestra "Configurando..." con spinner
   - Mensaje de estado aparece
   - Navegación ocurre después del éxito
   - Errores se muestran apropiadamente

## Logs del Sistema

El sistema registra las siguientes operaciones:

```
🔄 TIPO SIMULACIÓN: Cambiando tipo de simulación a: DIARIA
✅ TIPO SIMULACIÓN: Tipo de simulación cambiado exitosamente: {...}
```

## Próximos Pasos

1. **Persistencia de Configuración** - Guardar preferencias del usuario
2. **Validación de Compatibilidad** - Verificar que los datos cargados sean compatibles
3. **Configuración Avanzada** - Permitir ajustes específicos por tipo
4. **Tests E2E** - Agregar pruebas automatizadas
5. **Métricas de Uso** - Registrar qué tipos se usan más frecuentemente

## Dependencias

- **Backend:** Endpoints de tipo de simulación implementados
- **Frontend:** React, TypeScript, Tailwind CSS
- **API:** Fetch API para comunicación con backend 