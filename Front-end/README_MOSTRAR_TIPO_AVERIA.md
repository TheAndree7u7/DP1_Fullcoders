# Mostrar Tipo de Avería en el Mapa

## Descripción

Esta funcionalidad permite mostrar el tipo de avería (T1, T2, T3) tanto para averías manuales como automáticas cuando se hace clic en un camión en el mapa.

## Implementación

### 1. Funciones Utilitarias (ya implementadas)

Se han creado las siguientes funciones en `src/components/mapa/utils/averias.ts`:

```typescript
/**
 * Obtiene el tipo de avería de un camión, incluyendo averías automáticas
 */
export const obtenerTipoAveriaCamion = (
  camionId: string,
  camiones: CamionEstado[],
  rutasCamiones: RutaCamion[]
): string | null => {
  // Buscar el camión
  const camion = camiones.find(c => c.id === camionId);
  if (!camion) return null;

  // Verificar si está en un nodo de avería automática
  const rutaCamion = rutasCamiones.find(r => r.id === camionId);
  if (rutaCamion && rutaCamion.tiposNodos) {
    const porcentaje = camion.porcentaje;
    const siguientePaso = Math.floor(porcentaje);
    
    if (siguientePaso < rutaCamion.tiposNodos.length) {
      const tipoNodoActual = rutaCamion.tiposNodos[siguientePaso];
      
      // Verificar si es un nodo de avería automática
      if (tipoNodoActual === 'AVERIA_AUTOMATICA_T1') {
        return 'T1';
      } else if (tipoNodoActual === 'AVERIA_AUTOMATICA_T2') {
        return 'T2';
      } else if (tipoNodoActual === 'AVERIA_AUTOMATICA_T3') {
        return 'T3';
      }
    }
  }

  // Si está marcado como averiado, intentar obtener el tipo del backend
  if (camion.estado === "Averiado") {
    // Aquí se podría hacer una llamada al API para obtener el tipo
    return null;
  }

  return null;
};

/**
 * Obtiene la descripción del tipo de avería
 */
export const obtenerDescripcionTipoAveria = (tipoAveria: string): string => {
  switch (tipoAveria) {
    case 'T1':
      return 'Avería Menor (2h)';
    case 'T2':
      return 'Avería Media (4h + traslado)';
    case 'T3':
      return 'Avería Grave (8h + traslado)';
    default:
      return 'Avería Desconocida';
  }
};
```

### 2. Modificación del Componente Mapa

En el componente `Mapa.tsx`, se debe modificar la sección donde se muestra el camión averiado:

```tsx
{esAveriado ? (
  <div className="text-red-600 font-bold text-center py-2">
    🚛💥 CAMIÓN AVERIADO
    {(() => {
      const tipoAveria = obtenerTipoAveriaCamion(clickedCamion, camiones, rutasCamiones);
      if (tipoAveria) {
        const descripcion = obtenerDescripcionTipoAveria(tipoAveria);
        return (
          <div className="text-sm font-normal mt-1">
            Tipo: {tipoAveria} - {descripcion}
          </div>
        );
      }
      return null;
    })()}
  </div>
) : (
  // Mostrar información de avería automática si está en un nodo de avería
  (() => {
    const tipoAveria = obtenerTipoAveriaCamion(clickedCamion, camiones, rutasCamiones);
    if (tipoAveria) {
      const descripcion = obtenerDescripcionTipoAveria(tipoAveria);
      return (
        <div className="bg-orange-100 border border-orange-300 rounded p-2 mb-2 text-center">
          <div className="text-orange-800 font-semibold text-sm">
            ⚠️ EN NODO DE AVERÍA AUTOMÁTICA
          </div>
          <div className="text-orange-700 text-xs mt-1">
            Tipo: {tipoAveria} - {descripcion}
          </div>
        </div>
      );
    }
    return null;
  })()
)}
```

### 3. Casos de Uso

#### Caso 1: Camión Averiado Manualmente
- **Estado**: "Averiado"
- **Muestra**: "🚛💥 CAMIÓN AVERIADO" + tipo de avería (si se puede obtener del backend)

#### Caso 2: Camión en Nodo de Avería Automática
- **Estado**: "En Ruta" pero en nodo `AVERIA_AUTOMATICA_T1/T2/T3`
- **Muestra**: "⚠️ EN NODO DE AVERÍA AUTOMÁTICA" + tipo de avería

#### Caso 3: Camión Normal
- **Estado**: Cualquier otro estado
- **Muestra**: Botones para averiar manualmente

### 4. Tipos de Avería

| Tipo | Descripción | Tiempo Reparación | Requiere Traslado |
|------|-------------|-------------------|-------------------|
| T1 | Avería Menor | 2 horas | No |
| T2 | Avería Media | 4 horas | Sí |
| T3 | Avería Grave | 8 horas | Sí |

### 5. Mejoras Futuras

1. **Integración con Backend**: Obtener el tipo de avería real desde el backend para camiones averiados manualmente
2. **Indicadores Visuales**: Mostrar iconos diferentes según el tipo de avería
3. **Información Detallada**: Mostrar tiempo estimado de reparación y estado actual
4. **Historial de Averías**: Mostrar historial de averías del camión

## Archivos Modificados

- `src/components/mapa/utils/averias.ts` - Funciones utilitarias
- `src/components/mapa/utils/index.ts` - Exportaciones
- `src/components/Mapa.tsx` - Componente principal (modificaciones pendientes)
- `src/services/averiaApiService.ts` - Servicio para obtener averías del backend 