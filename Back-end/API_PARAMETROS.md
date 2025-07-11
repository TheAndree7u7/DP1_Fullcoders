# API de Parámetros de Simulación

Este documento describe los endpoints disponibles para manejar los parámetros de simulación desde el frontend.

## Base URL
```
http://localhost:8080/api/parametros
```

## Endpoints Disponibles

### 1. Obtener Todos los Parámetros
**GET** `/api/parametros`

Obtiene todos los parámetros actuales de la simulación.

**Respuesta:**
```json
{
  "dia": "01",
  "mes": "02", 
  "anho": "2025",
  "fechaInicial": null,
  "fechaFinal": null,
  "intervaloTiempo": 200,
  "contadorPrueba": 0,
  "kilometrosRecorridos": 0.0,
  "fitnessGlobal": 0.0,
  "semillaAleatoria": 12345,
  "primeraLlamada": true,
  "tipoSimulacion": "SEMANAL"
}
```

### 2. Obtener Parámetro Específico
**GET** `/api/parametros/{nombreParametro}`

Obtiene el valor de un parámetro específico.

**Parámetros disponibles:**
- `dia` - Día de la simulación
- `mes` - Mes de la simulación  
- `anho` - Año de la simulación
- `intervalotiempo` - Intervalo de tiempo en minutos
- `contadorprueba` - Contador de pruebas
- `kilometrosrecorridos` - Kilómetros totales recorridos
- `fitnessglobal` - Fitness global de la simulación
- `semillaaleatoria` - Semilla para números aleatorios
- `primerallamada` - Indica si es la primera llamada
- `tiposimulacion` - Tipo de simulación (SEMANAL, DIARIA, COLAPSO)

**Ejemplo:**
```
GET /api/parametros/intervalotiempo
```

**Respuesta:**
```
200
```

### 3. Actualizar Todos los Parámetros
**PUT** `/api/parametros`

Actualiza múltiples parámetros a la vez. Solo se actualizan los campos que no son null.

**Request Body:**
```json
{
  "intervaloTiempo": 300,
  "tipoSimulacion": "DIARIA",
  "semillaAleatoria": 54321
}
```

**Respuesta:**
```json
{
  "dia": "01",
  "mes": "02",
  "anho": "2025", 
  "fechaInicial": null,
  "fechaFinal": null,
  "intervaloTiempo": 300,
  "contadorPrueba": 0,
  "kilometrosRecorridos": 0.0,
  "fitnessGlobal": 0.0,
  "semillaAleatoria": 54321,
  "primeraLlamada": true,
  "tipoSimulacion": "DIARIA"
}
```

### 4. Actualizar Parámetro Específico
**PUT** `/api/parametros/{nombreParametro}`

Actualiza un parámetro específico.

**Ejemplo:**
```
PUT /api/parametros/intervalotiempo
Content-Type: text/plain

300
```

**Respuesta:**
```json
{
  "dia": "01",
  "mes": "02",
  "anho": "2025",
  "fechaInicial": null,
  "fechaFinal": null,
  "intervaloTiempo": 300,
  "contadorPrueba": 0,
  "kilometrosRecorridos": 0.0,
  "fitnessGlobal": 0.0,
  "semillaAleatoria": 12345,
  "primeraLlamada": true,
  "tipoSimulacion": "SEMANAL"
}
```

### 5. Reiniciar Parámetros
**DELETE** `/api/parametros`

Reinicia todos los parámetros a sus valores por defecto.

**Respuesta:**
```json
{
  "dia": "01",
  "mes": "02",
  "anho": "2025",
  "fechaInicial": null,
  "fechaFinal": null,
  "intervaloTiempo": 200,
  "contadorPrueba": 0,
  "kilometrosRecorridos": 0.0,
  "fitnessGlobal": 0.0,
  "semillaAleatoria": 12345,
  "primeraLlamada": true,
  "tipoSimulacion": "SEMANAL"
}
```

## Códigos de Estado HTTP

- **200 OK** - Operación exitosa
- **400 Bad Request** - Datos inválidos en el request
- **404 Not Found** - Parámetro no encontrado
- **500 Internal Server Error** - Error interno del servidor

## Ejemplos de Uso

### JavaScript/Fetch
```javascript
// Obtener todos los parámetros
const response = await fetch('http://localhost:8080/api/parametros');
const parametros = await response.json();

// Actualizar intervalo de tiempo
const updateResponse = await fetch('http://localhost:8080/api/parametros/intervalotiempo', {
  method: 'PUT',
  headers: {
    'Content-Type': 'text/plain'
  },
  body: '300'
});

// Actualizar múltiples parámetros
const multiUpdateResponse = await fetch('http://localhost:8080/api/parametros', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    intervaloTiempo: 300,
    tipoSimulacion: 'DIARIA'
  })
});
```

### cURL
```bash
# Obtener todos los parámetros
curl -X GET http://localhost:8080/api/parametros

# Actualizar intervalo de tiempo
curl -X PUT http://localhost:8080/api/parametros/intervalotiempo \
  -H "Content-Type: text/plain" \
  -d "300"

# Reiniciar parámetros
curl -X DELETE http://localhost:8080/api/parametros
```

## Notas Importantes

1. **Campos Estáticos**: Los parámetros son campos estáticos en la clase `Parametros`, por lo que los cambios se aplican globalmente.

2. **Validación**: El endpoint valida los tipos de datos antes de aplicar los cambios.

3. **Logging**: Todas las operaciones se registran en la consola del servidor para debugging.

4. **CORS**: El endpoint permite peticiones desde cualquier origen (`*`). Para producción, considera restringir esto.

5. **Thread Safety**: Los parámetros son accedidos de forma thread-safe para evitar problemas en entornos concurrentes. 