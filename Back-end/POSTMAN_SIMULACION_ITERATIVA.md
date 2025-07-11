# Guía de Postman para Simulación Iterativa

Esta guía te muestra cómo probar todos los endpoints de simulación iterativa usando Postman.

## Configuración Inicial

### Opción 1: Importar Archivos de Postman (Recomendado)

1. **Importar Colección**: 
   - Abre Postman
   - Haz clic en "Import"
   - Selecciona el archivo `Simulacion_Iterativa_Postman_Collection.json`
   - Se importará toda la colección con todos los endpoints configurados

2. **Importar Entorno**:
   - Haz clic en "Import" nuevamente
   - Selecciona el archivo `Simulacion_Iterativa_Environment.json`
   - Se importará el entorno con todas las variables necesarias

3. **Seleccionar Entorno**:
   - En la esquina superior derecha de Postman, selecciona "Simulación Iterativa Environment"
   - Ahora todas las variables estarán disponibles

### Opción 2: Configuración Manual

Si prefieres configurar manualmente:

#### URL Base
```
http://localhost:8080
```

#### Headers
```
Content-Type: application/json
```

#### Variables de Entorno
Crea las siguientes variables en tu entorno de Postman:
- `baseUrl`: `http://localhost:8080`
- `fechaInicio`: `2024-01-15T08:00:00`
- `fechaEspecifica`: `2024-01-15T14:30:00`

## 1. Configurar Simulación

### Endpoint: `POST /api/simulacion-iterativa/configurar`

**URL:** `http://localhost:8080/api/simulacion-iterativa/configurar`

**Method:** `POST`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
    "fechaInicio": "2024-01-15T08:00:00"
}
```

**Respuesta esperada:**
```json
{
    "success": true,
    "message": "Simulación configurada exitosamente",
    "estado": {
        "configurada": true,
        "finalizada": false,
        "iteraciones": 0,
        "fechaActual": "2024-01-15T08:00:00",
        "fechaLimite": "2024-01-22T08:00:00",
        "pedidosSemanales": 150,
        "pedidosPorAtender": 0,
        "pedidosPlanificados": 0,
        "pedidosEntregados": 0
    }
}
```

## 2. Obtener Siguiente Solución (Secuencial)

### Endpoint: `POST /api/simulacion-iterativa/obtener-solucion`

**URL:** `http://localhost:8080/api/simulacion-iterativa/obtener-solucion`

**Method:** `POST`

**Headers:**
```
Content-Type: application/json
```

**Body:** No requiere body

**Respuesta esperada:**
```json
{
    "success": true,
    "solucion": {
        "individuo": {
            "genes": [...],
            "fitness": 85.5,
            "kilometrosRecorridos": 120.3
        },
        "pedidos": [...],
        "bloqueos": [...],
        "fechaHoraInicioIntervalo": "2024-01-15T08:00:00",
        "fechaHoraFinIntervalo": "2024-01-15T08:30:00",
        "camiones": [...]
    },
    "estado": {
        "configurada": true,
        "finalizada": false,
        "iteraciones": 1,
        "fechaActual": "2024-01-15T08:30:00",
        "fechaLimite": "2024-01-22T08:00:00",
        "pedidosSemanales": 149,
        "pedidosPorAtender": 3,
        "pedidosPlanificados": 0,
        "pedidosEntregados": 0
    }
}
```

## 3. Obtener Solución para Fecha Específica

### Endpoint: `POST /api/simulacion-iterativa/obtener-solucion-fecha`

**URL:** `http://localhost:8080/api/simulacion-iterativa/obtener-solucion-fecha`

**Method:** `POST`

**Headers:**
```
Content-Type: application/json
```

### Opción A: Solo Calcular (sin avanzar)

**Body (raw JSON):**
```json
{
    "fecha": "2024-01-15T14:30:00",
    "avanzarSimulacion": false
}
```

**Respuesta esperada:**
```json
{
    "success": true,
    "solucion": {
        "individuo": {
            "genes": [...],
            "fitness": 87.2,
            "kilometrosRecorridos": 135.8
        },
        "pedidos": [...],
        "bloqueos": [...],
        "fechaHoraInicioIntervalo": "2024-01-15T14:30:00",
        "fechaHoraFinIntervalo": "2024-01-15T15:00:00",
        "camiones": [...]
    },
    "fechaSolicitada": "2024-01-15T14:30:00",
    "avanzarSimulacion": false,
    "estado": {
        "configurada": true,
        "finalizada": false,
        "iteraciones": 1,
        "fechaActual": "2024-01-15T08:30:00",
        "fechaLimite": "2024-01-22T08:00:00",
        "pedidosSemanales": 145,
        "pedidosPorAtender": 2,
        "pedidosPlanificados": 3,
        "pedidosEntregados": 0
    }
}
```

### Opción B: Avanzar hasta la Fecha

**Body (raw JSON):**
```json
{
    "fecha": "2024-01-15T16:00:00",
    "avanzarSimulacion": true
}
```

**Respuesta esperada:**
```json
{
    "success": true,
    "solucion": {
        "individuo": {
            "genes": [...],
            "fitness": 87.2,
            "kilometrosRecorridos": 135.8
        },
        "pedidos": [...],
        "bloqueos": [...],
        "fechaHoraInicioIntervalo": "2024-01-15T16:00:00",
        "fechaHoraFinIntervalo": "2024-01-15T16:30:00",
        "camiones": [...]
    },
    "fechaSolicitada": "2024-01-15T16:00:00",
    "avanzarSimulacion": true,
    "estado": {
        "configurada": true,
        "finalizada": false,
        "iteraciones": 16,
        "fechaActual": "2024-01-15T16:30:00",
        "fechaLimite": "2024-01-22T08:00:00",
        "pedidosSemanales": 142,
        "pedidosPorAtender": 1,
        "pedidosPlanificados": 2,
        "pedidosEntregados": 0
    }
}
```

## 4. Obtener Estado Actual

### Endpoint: `GET /api/simulacion-iterativa/estado`

**URL:** `http://localhost:8080/api/simulacion-iterativa/estado`

**Method:** `GET`

**Headers:** No requiere headers especiales

**Body:** No requiere body

**Respuesta esperada:**
```json
{
    "success": true,
    "estado": {
        "configurada": true,
        "finalizada": false,
        "iteraciones": 5,
        "fechaActual": "2024-01-15T10:30:00",
        "fechaLimite": "2024-01-22T08:00:00",
        "pedidosSemanales": 145,
        "pedidosPorAtender": 2,
        "pedidosPlanificados": 3,
        "pedidosEntregados": 0
    }
}
```

## 5. Reiniciar Simulación

### Endpoint: `POST /api/simulacion-iterativa/reiniciar`

**URL:** `http://localhost:8080/api/simulacion-iterativa/reiniciar`

**Method:** `POST`

**Headers:**
```
Content-Type: application/json
```

**Body:** No requiere body

**Respuesta esperada:**
```json
{
    "success": true,
    "message": "Simulación reiniciada exitosamente",
    "estado": {
        "configurada": false,
        "finalizada": false,
        "iteraciones": 0,
        "fechaActual": null,
        "fechaLimite": null,
        "pedidosSemanales": 0,
        "pedidosPorAtender": 0,
        "pedidosPlanificados": 0,
        "pedidosEntregados": 0
    }
}
```

## Flujos de Prueba Recomendados

### Flujo 1: Simulación Secuencial Completa

1. **Configurar simulación**
   ```
   POST /api/simulacion-iterativa/configurar
   ```

2. **Obtener estado inicial**
   ```
   GET /api/simulacion-iterativa/estado
   ```

3. **Ejecutar iteraciones secuenciales** (repetir hasta que termine)
   ```
   POST /api/simulacion-iterativa/obtener-solucion
   ```

4. **Verificar estado final**
   ```
   GET /api/simulacion-iterativa/estado
   ```

### Flujo 2: Navegación por Fechas Específicas

1. **Configurar simulación**
   ```
   POST /api/simulacion-iterativa/configurar
   ```

2. **Calcular solución para diferentes fechas** (sin avanzar)
   ```
   POST /api/simulacion-iterativa/obtener-solucion-fecha
   Body: {"fecha": "2024-01-15T08:00:00", "avanzarSimulacion": false}
   POST /api/simulacion-iterativa/obtener-solucion-fecha
   Body: {"fecha": "2024-01-15T12:00:00", "avanzarSimulacion": false}
   POST /api/simulacion-iterativa/obtener-solucion-fecha
   Body: {"fecha": "2024-01-15T16:00:00", "avanzarSimulacion": false}
   ```

3. **Avanzar hasta una fecha específica**
   ```
   POST /api/simulacion-iterativa/obtener-solucion-fecha
   Body: {"fecha": "2024-01-16T08:00:00", "avanzarSimulacion": true}
   ```

### Flujo 3: Manejo de Errores

1. **Intentar obtener solución sin configurar**
   ```
   POST /api/simulacion-iterativa/obtener-solucion
   ```

2. **Configurar con fecha inválida**
   ```
   POST /api/simulacion-iterativa/configurar
   Body: {"fechaInicio": null}
   ```

3. **Solicitar fecha fuera de rango**
   ```
   POST /api/simulacion-iterativa/obtener-solucion-fecha
   Body: {"fecha": "2024-01-30T08:00:00", "avanzarSimulacion": false}
   ```

## Variables de Entorno en Postman

### Configurar Variables

1. **Crear variable `baseUrl`**
   - Value: `http://localhost:8080`

2. **Crear variable `fechaInicio`**
   - Value: `2024-01-15T08:00:00`

3. **Crear variable `fechaEspecifica`**
   - Value: `2024-01-15T14:30:00`

### Usar Variables en URLs

```
{{baseUrl}}/api/simulacion-iterativa/configurar
{{baseUrl}}/api/simulacion-iterativa/obtener-solucion-fecha
```

### Usar Variables en Body

```json
{
    "fechaInicio": "{{fechaInicio}}"
}
```

```json
{
    "fecha": "{{fechaEspecifica}}",
    "avanzarSimulacion": false
}
```

## Colección de Postman

### Crear Colección

1. **Nombre:** `Simulación Iterativa API`
2. **Descripción:** Endpoints para simulación iterativa

### Organizar en Carpetas

```
📁 Simulación Iterativa API
├── 📁 Configuración
│   ├── Configurar Simulación
│   └── Obtener Estado
├── 📁 Ejecución Secuencial
│   └── Obtener Siguiente Solución
├── 📁 Navegación por Fechas
│   └── Obtener Solución para Fecha
└── 📁 Administración
    └── Reiniciar Simulación
```

## Scripts de Prueba Automatizada

### Pre-request Script (para configurar automáticamente)

```javascript
// Solo ejecutar si no está configurada
pm.sendRequest({
    url: pm.environment.get("baseUrl") + "/api/simulacion-iterativa/estado",
    method: 'GET'
}, function (err, response) {
    if (response.json().estado.configurada === false) {
        pm.sendRequest({
            url: pm.environment.get("baseUrl") + "/api/simulacion-iterativa/configurar",
            method: 'POST',
            header: {
                'Content-Type': 'application/json'
            },
            body: {
                mode: 'raw',
                raw: JSON.stringify({
                    fechaInicio: pm.environment.get("fechaInicio")
                })
            }
        });
    }
});
```

### Test Script (para validar respuestas)

```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has success field", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('success');
});

pm.test("Success is true", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.eql(true);
});

pm.test("Response has estado field", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('estado');
});
```

## Consejos de Uso

### 1. Orden de Ejecución
- Siempre configurar primero
- Verificar estado antes de ejecutar
- Reiniciar cuando sea necesario

### 2. Monitoreo
- Usar el endpoint `/estado` frecuentemente
- Verificar el campo `finalizada`
- Monitorear `iteraciones` para progreso

### 3. Manejo de Errores
- Verificar que el servidor esté corriendo
- Validar fechas en formato ISO 8601
- Revisar logs del servidor para errores

### 4. Rendimiento
- Las peticiones pueden tomar tiempo
- Usar timeouts apropiados
- No hacer demasiadas peticiones simultáneas

## Códigos de Estado HTTP

- **200**: Operación exitosa
- **400**: Parámetros inválidos
- **500**: Error interno del servidor

## Ejemplos de Fechas Válidas

```json
"2024-01-15T08:00:00"
"2024-01-15T14:30:00"
"2024-01-16T00:00:00"
"2024-01-22T08:00:00"
```

## Endpoint de Información de Postman

### Endpoint: `GET /api/simulacion-iterativa/postman-info`

**URL:** `http://localhost:8080/api/simulacion-iterativa/postman-info`

**Method:** `GET`

**Descripción:** Este endpoint devuelve información completa sobre todos los endpoints disponibles, incluyendo ejemplos de uso, variables recomendadas y flujos de prueba.

**Respuesta esperada:**
```json
{
    "success": true,
    "message": "Información de Postman obtenida exitosamente",
    "postmanInfo": {
        "baseUrl": "http://localhost:8080",
        "description": "API de Simulación Iterativa - Endpoints para configurar y ejecutar simulaciones paso a paso",
        "variables": {
            "baseUrl": "http://localhost:8080",
            "fechaInicio": "2024-01-15T08:00:00",
            "fechaEspecifica": "2024-01-15T14:30:00"
        },
        "endpoints": {
            "configurar": {
                "method": "POST",
                "url": "{{baseUrl}}/api/simulacion-iterativa/configurar",
                "description": "Configura la simulación iterativa con una fecha de inicio",
                "body": {
                    "fechaInicio": "{{fechaInicio}}"
                },
                "headers": {
                    "Content-Type": "application/json"
                }
            },
            "obtenerSiguienteSolucion": {
                "method": "POST",
                "url": "{{baseUrl}}/api/simulacion-iterativa/obtener-solucion",
                "description": "Obtiene la siguiente solución del algoritmo genético (secuencial)",
                "body": "No requiere body",
                "headers": {
                    "Content-Type": "application/json"
                }
            }
        },
        "flujos": {
            "secuencial": {
                "nombre": "Simulación Secuencial Completa",
                "pasos": [
                    "1. POST /configurar",
                    "2. GET /estado",
                    "3. POST /obtener-solucion (repetir hasta que termine)",
                    "4. GET /estado"
                ]
            },
            "fechas": {
                "nombre": "Navegación por Fechas Específicas",
                "pasos": [
                    "1. POST /configurar",
                    "2. POST /obtener-solucion-fecha (con diferentes fechas, avanzarSimulacion: false)",
                    "3. POST /obtener-solucion-fecha (con avanzarSimulacion: true)"
                ]
            }
        },
        "ejemplos": {
            "respuestaExito": {
                "success": true,
                "message": "Operación exitosa",
                "estado": {
                    "configurada": true,
                    "finalizada": false,
                    "iteraciones": 5,
                    "fechaActual": "2024-01-15T10:30:00",
                    "fechaLimite": "2024-01-22T08:00:00",
                    "pedidosSemanales": 145,
                    "pedidosPorAtender": 2,
                    "pedidosPlanificados": 3,
                    "pedidosEntregados": 0
                }
            }
        }
    }
}
```

**Uso:** Este endpoint es útil para:
- Obtener información programática sobre la API
- Generar documentación automática
- Crear herramientas de testing automatizado
- Verificar que todos los endpoints estén disponibles 