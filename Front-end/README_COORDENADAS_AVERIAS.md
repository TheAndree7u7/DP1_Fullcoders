# Manejo de Coordenadas en Averías Automáticas

## Descripción

Este documento explica cómo se manejan las coordenadas cuando ocurre una avería automática en un camión durante la simulación.

## Flujo de Coordenadas en Averías Automáticas

### 1. Detección de Avería Automática

Cuando un camión recorre un nodo con avería automática (`AVERIA_AUTOMATICA_T1`, `AVERIA_AUTOMATICA_T2`, `AVERIA_AUTOMATICA_T3`), el sistema:

1. **Detecta** el nodo de avería automática en la función `detectarAveriaAutomatica`
2. **Marca** el camión como "Averiado" en el estado local
3. **Registra** la avería en el backend con las coordenadas exactas donde ocurrió

### 2. Extracción de Coordenadas

El sistema utiliza múltiples estrategias para obtener las coordenadas exactas donde ocurrió la avería:

#### Estrategia 1: Coordenada del Camión
```typescript
// Extraer coordenada del estado actual del camión
const camionAveriado = estadoCompleto.camiones.find(c => c.id === codigoCamion);
if (camionAveriado && camionAveriado.ubicacion) {
  const match = camionAveriado.ubicacion.match(/\((\d+),(\d+)\)/);
  if (match) {
    coordenadaAveria = {
      fila: parseInt(match[2]), // y
      columna: parseInt(match[1]) // x
    };
  }
}
```

#### Estrategia 2: Coordenada de la Ruta
```typescript
// Si no se pudo extraer del camión, obtener de la ruta
if (!coordenadaAveria) {
  const rutaCamion = estadoCompleto.rutasCamiones?.find(r => r.id === codigoCamion);
  if (rutaCamion && rutaCamion.ruta && rutaCamion.ruta.length > 0) {
    const porcentaje = camionAveriado?.porcentaje || 0;
    const posicionEnRuta = Math.floor(porcentaje);
    const coordenadaRuta = rutaCamion.ruta[posicionEnRuta];
    
    if (coordenadaRuta) {
      const matchRuta = coordenadaRuta.match(/\((\d+),(\d+)\)/);
      if (matchRuta) {
        coordenadaAveria = {
          fila: parseInt(matchRuta[2]), // y
          columna: parseInt(matchRuta[1]) // x
        };
      }
    }
  }
}
```

#### Estrategia 3: Coordenada por Defecto
```typescript
// Si no se pudo obtener coordenada, usar almacén central
if (!coordenadaAveria) {
  coordenadaAveria = { fila: 8, columna: 12 }; // Almacén central
}
```

### 3. Validación de Coordenadas

Antes de enviar al backend, se valida que las coordenadas sean válidas:

```typescript
// Validar que la coordenada tenga valores válidos
if (coordenadaAveria.fila < 0 || coordenadaAveria.columna < 0) {
  console.warn("⚠️ AVERÍA: Coordenada inválida detectada, usando coordenada por defecto");
  coordenadaAveria = { fila: 8, columna: 12 };
}
```

### 4. Envío al Backend

Las coordenadas se envían al backend en el formato correcto:

```typescript
const datosEnvio = {
  codigoCamion,
  tipoIncidente: `TI${tipo}`,
  fechaHoraReporte,
  coordenada: coordenadaAveria, // Objeto con fila y columna
  estadoSimulacion: estadoConvertido
};
```

### 5. Procesamiento en el Backend

El backend recibe la coordenada y la procesa correctamente:

```java
// En AveriaService.java
if (request.getCoordenada() != null) {
    camionService.cambiarCoordenada(request.getCodigoCamion(), request.getCoordenada());
    System.out.println("Coordenada actualizada: " + request.getCoordenada());
} else {
    System.out.println("No se actualizo la coordenada");
}
```

## Estructura de Coordenadas

### Frontend (TypeScript)
```typescript
interface Coordenada {
  fila: number;    // Coordenada Y (fila)
  columna: number; // Coordenada X (columna)
}
```

### Backend (Java)
```java
public class Coordenada {
    private int fila;     // Coordenada Y (fila)
    private int columna;  // Coordenada X (columna)
}
```

## Logs de Debugging

El sistema incluye logs detallados para debugging de coordenadas:

```typescript
console.log("🔍 AVERÍA: Buscando coordenada del camión averiado:", {
  camionId: codigoCamion,
  camionEncontrado: !!camionAveriado,
  ubicacionCamion: camionAveriado?.ubicacion,
  estadoCamion: camionAveriado?.estado
});

console.log("📍 AVERÍA: Coordenada extraída del camión averiado:", coordenadaAveria);
console.log("   - Coordenada (fila):", datosEnvio.coordenada.fila);
console.log("   - Coordenada (columna):", datosEnvio.coordenada.columna);
```

## Casos de Uso

### 1. Avería Automática Normal
- **Entrada**: Camión en nodo `AVERIA_AUTOMATICA_T1`
- **Proceso**: Se extrae coordenada del estado del camión
- **Salida**: Avería registrada con coordenadas exactas

### 2. Avería con Coordenada de Ruta
- **Entrada**: Camión sin ubicación válida en estado
- **Proceso**: Se extrae coordenada de la posición actual en la ruta
- **Salida**: Avería registrada con coordenadas de la ruta

### 3. Avería con Coordenada por Defecto
- **Entrada**: No se puede obtener coordenada válida
- **Proceso**: Se usa coordenada del almacén central (8,12)
- **Salida**: Avería registrada con coordenada por defecto

## Ejemplo de Uso

### Objeto de Coordenadas en Avería Automática

```typescript
// Ejemplo de avería automática con coordenadas
const averiaRequest = {
  codigoCamion: "CAM001",
  tipoIncidente: "TI1", // Avería automática tipo 1
  fechaHoraReporte: "2025-01-15T10:30:00",
  coordenada: {
    fila: 25,      // Coordenada Y (fila)
    columna: 30    // Coordenada X (columna)
  },
  estadoSimulacion: {
    // ... estado completo de la simulación
    timestamp: "2025-01-15T10:30:00",
    horaActual: 10,
    horaSimulacion: "10:30",
    fechaHoraSimulacion: "2025-01-15T10:30:00",
    // ... resto del estado
  }
};
```

### Logs de Ejemplo

```
🔍 AVERÍA: Buscando coordenada del camión averiado: {
  camionId: "CAM001",
  camionEncontrado: true,
  ubicacionCamion: "(30,25)",
  estadoCamion: "Averiado"
}

📍 AVERÍA: Coordenada extraída del camión averiado: {
  fila: 25,
  columna: 30
}

📡 ===== DATOS COMPLETOS QUE SE ENVÍAN AL BACKEND =====
🏷️  DATOS BÁSICOS DE LA AVERÍA:
   - Código del camión: CAM001
   - Tipo de incidente: TI1
   - Fecha y hora del reporte: 2025-01-15T10:30:00
   - Coordenada de la avería: { fila: 25, columna: 30 }
   - Coordenada (fila): 25
   - Coordenada (columna): 30
```

### Respuesta del Backend

```json
{
  "averia": {
    "id": 123,
    "camion": {
      "codigo": "CAM001",
      "coordenada": {
        "fila": 25,
        "columna": 30
      }
    },
    "tipoIncidente": "TI1",
    "fechaHoraReporte": "2025-01-15T10:30:00",
    "coordenada": {
      "fila": 25,
      "columna": 30
    }
  },
  "mensaje": "Avería creada exitosamente con estado completo de la simulación",
  "timestampEstado": "2025-01-15T10:30:00",
  "horaSimulacion": "10:30"
}
```

## Mejoras Implementadas

1. **Múltiples estrategias** de extracción de coordenadas
2. **Validación** de coordenadas antes del envío
3. **Logs detallados** para debugging
4. **Coordenada por defecto** como fallback
5. **Consistencia** entre averías automáticas y manuales

## Archivos Modificados

- `Front-end/src/services/averiaApiService.ts` - Mejora en extracción de coordenadas
- `Front-end/src/components/mapa/utils/averiasAutomaticas.ts` - Manejo de coordenadas en averías automáticas
- `Back-end/plg/src/main/java/com/plg/service/AveriaService.java` - Procesamiento de coordenadas en backend 