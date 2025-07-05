# Utilidades del Componente Mapa

Esta carpeta contiene las funciones auxiliares que fueron refactorizadas del componente `Mapa.tsx` para mantener el código organizado y reutilizable.

## Estructura de archivos

### `coordenadas.ts`
- **`parseCoord(s: string): Coordenada`**: Convierte una coordenada en formato string "(x,y)" a un objeto Coordenada con validación de errores.

### `camiones.ts`
- **`calcularRotacion(from: Coordenada, to: Coordenada): number`**: Calcula la rotación de un camión basada en su movimiento entre dos coordenadas.

### `pedidos.ts`
- **`getPedidosPendientes(rutasCamiones: RutaCamion[], camiones: CamionEstado[]): Pedido[]`**: Obtiene los pedidos pendientes (no entregados) de todas las rutas, considerando el estado actual de los camiones.

### `index.ts`
- **Exportaciones centralizadas**: Permite importar todas las utilidades desde un solo punto de entrada.

## Uso

```typescript
import { parseCoord, calcularRotacion, getPedidosPendientes } from './mapa/utils';

// Parsear coordenadas
const coordenada = parseCoord("(12,8)");

// Calcular rotación del camión
const rotacion = calcularRotacion(coordOrigen, coordDestino);

// Obtener pedidos pendientes
const pedidosPendientes = getPedidosPendientes(rutasCamiones, camiones);
```

## Beneficios de la refactorización

1. **Separación de responsabilidades**: Las funciones auxiliares están separadas del componente principal
2. **Reutilización**: Estas funciones pueden ser reutilizadas en otros componentes del mapa
3. **Mantenibilidad**: Es más fácil mantener y testear funciones individuales
4. **Legibilidad**: El componente principal se enfoca en la lógica de renderizado
5. **Modularidad**: Cada archivo tiene una responsabilidad específica 