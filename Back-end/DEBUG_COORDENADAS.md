# Debugging de Coordenadas en Averías

## Problema Identificado

El backend está recibiendo coordenadas como `NULL` a pesar de que el frontend las está enviando correctamente. Esto indica un problema en la deserialización del JSON.

## Solución Implementada

Se han agregado herramientas de debugging para identificar exactamente qué está pasando con las coordenadas.

### 1. Endpoint de Debugging

Se ha creado un endpoint temporal para capturar el JSON raw:

```
POST /api/averias/debug-averia-raw
```

Este endpoint:
- Captura el JSON raw que llega al backend
- Lo parsea para verificar si tiene la estructura correcta
- Muestra información detallada sobre la coordenada

### 2. Logs Mejorados

Se han agregado logs detallados en el endpoint principal:

```
🔍 AVERÍA BACKEND: Recibida solicitud de avería con estado completo
   - Código del camión: TA01
   - Tipo de incidente: TipoIncidente(...)
   - Fecha y hora del reporte: 2025-07-23T00:03:25
   - Coordenada recibida: NULL
   - ⚠️ ADVERTENCIA: La coordenada es NULL
```

### 3. Función de Debugging en Frontend

Se ha agregado una función temporal en el frontend:

```typescript
import { debugAveriaRaw } from '../services/averiaApiService';

// Usar para debugging
await debugAveriaRaw(camionId, tipo, fechaHoraReporte, estadoCompleto);
```

## Cómo Usar el Debugging

### Paso 1: Probar el Endpoint de Debugging

1. **En el frontend**, importar la función de debugging:
```typescript
import { debugAveriaRaw } from '../services/averiaApiService';
```

2. **Llamar la función** en lugar de la función normal:
```typescript
// En lugar de:
// await averiarCamionConEstado(camionId, tipo, fechaHoraReporte, estadoCompleto);

// Usar:
await debugAveriaRaw(camionId, tipo, fechaHoraReporte, estadoCompleto);
```

3. **Revisar los logs** en la consola del navegador y en el backend.

### Paso 2: Analizar los Logs

#### Logs del Frontend:
```
🔍 DEBUG FRONTEND: Enviando JSON raw para debugging...
🔍 DEBUG FRONTEND: JSON que se enviará:
{
  "codigoCamion": "TA01",
  "tipoIncidente": "TI1",
  "fechaHoraReporte": "2025-07-23T00:03:25",
  "coordenada": {
    "fila": 10,
    "columna": 10
  },
  "estadoSimulacion": {
    // ... estado completo
  }
}
```

#### Logs del Backend:
```
🔍 DEBUG BACKEND: JSON Raw recibido:
{"codigoCamion":"TA01","tipoIncidente":"TI1",...}

🔍 DEBUG BACKEND: JSON parseado correctamente
   - Tiene coordenada: true
   - Coordenada es null: false
   - Coordenada tiene fila: true
   - Coordenada tiene columna: true
   - Valor fila: 10
   - Valor columna: 10
```

## Posibles Causas del Problema

### 1. Problema de Deserialización
- El JSON llega correctamente pero Jackson no lo deserializa bien
- Posible problema con anotaciones o configuración

### 2. Problema de Estructura
- El JSON tiene una estructura diferente a la esperada
- Campos con nombres diferentes

### 3. Problema de Tipos
- Los tipos de datos no coinciden entre frontend y backend
- Problemas con conversión de tipos

## Configuración de Jackson

Se ha agregado configuración de Jackson para ser más tolerante:

```java
@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configurar para ser más tolerante con propiedades desconocidas
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Configurar para manejar valores nulos
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        // Configurar para ser más tolerante con tipos de datos
        mapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, true);
        
        return mapper;
    }
}
```

## Próximos Pasos

1. **Ejecutar el debugging** para ver exactamente qué JSON llega
2. **Analizar los logs** para identificar el problema específico
3. **Ajustar la configuración** según los resultados
4. **Probar nuevamente** con la configuración corregida
5. **Remover el código de debugging** una vez solucionado

## Comandos para Testing

### Frontend (en la consola del navegador):
```javascript
// Simular una avería automática para debugging
const estadoCompleto = capturarEstadoCompleto(estadoSimulacion);
await debugAveriaRaw("TA01", 1, "2025-07-23T00:03:25", estadoCompleto);
```

### Backend (en los logs):
Buscar los logs que empiecen con:
- `🔍 DEBUG BACKEND:`
- `🔍 AVERÍA BACKEND:`
- `⚠️ ADVERTENCIA:`

## Estructura Esperada del JSON

```json
{
  "codigoCamion": "TA01",
  "tipoIncidente": "TI1",
  "fechaHoraReporte": "2025-07-23T00:03:25",
  "coordenada": {
    "fila": 10,
    "columna": 10
  },
  "estadoSimulacion": {
    "timestamp": "2025-07-23T02:25:32.634Z",
    "horaActual": 0,
    "horaSimulacion": "00:03:25",
    "camiones": [...],
    "rutasCamiones": [...],
    "almacenes": [...],
    "bloqueos": [...]
  }
}
``` 