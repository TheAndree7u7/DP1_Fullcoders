# Utilidades de Simulación

Esta carpeta contiene las funciones auxiliares que fueron refactorizadas del `SimulacionContext.tsx` para mantener el código organizado y reutilizable.

## Estructura de archivos

### `coordenadas.ts`
- **`parseCoord(s: string): Coordenada`**: Convierte una coordenada en formato string "(x,y)" a un objeto Coordenada.

### `camiones.ts`
- **`adaptarCamionParaCalculos(camion: CamionEstado): Camion`**: Adapta un objeto CamionEstado a un objeto Camion compatible con las funciones de cálculo de types.ts.

### `tiempo.ts`
- **`formatearTiempoTranscurrido(tiempoHMS: string): string`**: Convierte tiempo en formato HH:MM:SS a un formato legible como "transcurrieron X días Y horas Z minutos".

### `index.ts`
- **Exportaciones centralizadas**: Permite importar todas las utilidades desde un solo punto de entrada.

## Uso

```typescript
import { parseCoord, adaptarCamionParaCalculos, formatearTiempoTranscurrido } from "./simulacion/utils";

// Uso individual
const coordenada = parseCoord("(12,8)");
const camionAdaptado = adaptarCamionParaCalculos(camionEstado);
const tiempoLegible = formatearTiempoTranscurrido("02:30:15");
```

## Beneficios de la refactorización

1. **Separación de responsabilidades**: Las funciones auxiliares están separadas del contexto principal
2. **Reutilización**: Estas funciones pueden ser reutilizadas en otros componentes
3. **Mantenibilidad**: Es más fácil mantener y testear funciones individuales
4. **Organización**: El código está mejor organizado por funcionalidad 