# API de Simulación Iterativa

Esta documentación describe las nuevas APIs para manejar la simulación iterativa, que permite configurar y ejecutar simulaciones paso a paso sin el enfoque de bucle continuo.

## Endpoints Disponibles

### 1. Configurar Simulación Iterativa

**Endpoint:** `POST /api/simulacion-iterativa/configurar`

**Descripción:** Configura la simulación iterativa con una fecha de inicio. Debe llamarse antes de obtener soluciones.

**Cuerpo de la Petición:**
```json
{
    "fechaInicio": "2024-01-15T08:00:00"
}
```

**Respuesta Exitosa:**
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

**Respuesta de Error:**
```json
{
    "success": false,
    "message": "La fecha de inicio es requerida"
}
```

### 2. Obtener Siguiente Solución

**Endpoint:** `POST /api/simulacion-iterativa/obtener-solucion`

**Descripción:** Obtiene la siguiente solución del algoritmo genético. Ejecuta una iteración y devuelve el mejor individuo.

**Cuerpo de la Petición:** No requiere cuerpo.

**Respuesta Exitosa:**
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

**Respuesta cuando la simulación termina:**
```json
{
    "success": false,
    "solucion": null,
    "message": "La simulación ha terminado",
    "estado": {
        "configurada": true,
        "finalizada": true,
        "iteraciones": 336,
        "fechaActual": "2024-01-22T08:00:00",
        "fechaLimite": "2024-01-22T08:00:00",
        "pedidosSemanales": 0,
        "pedidosPorAtender": 0,
        "pedidosPlanificados": 0,
        "pedidosEntregados": 150
    }
}
```

### 3. Obtener Solución para Fecha Específica

**Endpoint:** `POST /api/simulacion-iterativa/obtener-solucion-fecha`

**Descripción:** Obtiene la solución del algoritmo genético para una fecha específica. Permite especificar si se debe avanzar la simulación hasta esa fecha o solo calcular la solución.

**Cuerpo de la Petición:**
```json
{
    "fecha": "2024-01-15T14:30:00",
    "avanzarSimulacion": false
}
```

**Parámetros:**
- `fecha`: Fecha para la cual obtener la solución (requerido)
- `avanzarSimulacion`: 
  - `true`: Avanza la simulación hasta la fecha especificada y actualiza el estado
  - `false`: Solo calcula la solución para esa fecha sin cambiar el estado actual

**Nota importante:** 
- La primera iteración de la simulación NO usa `actualizarEstadoGlobal` para mantener el estado inicial
- Las siguientes iteraciones SÍ usan `actualizarEstadoGlobal` para reflejar el progreso real
- Al calcular para una fecha específica (sin avanzar), se usa una copia temporal del estado

**Respuesta Exitosa (solo calcular):**
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

**Respuesta Exitosa (avanzar simulación):**
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
    "avanzarSimulacion": true,
    "estado": {
        "configurada": true,
        "finalizada": false,
        "iteraciones": 13,
        "fechaActual": "2024-01-15T15:00:00",
        "fechaLimite": "2024-01-22T08:00:00",
        "pedidosSemanales": 142,
        "pedidosPorAtender": 1,
        "pedidosPlanificados": 2,
        "pedidosEntregados": 0
    }
}
```

### 4. Obtener Estado de la Simulación

**Endpoint:** `GET /api/simulacion-iterativa/estado`

**Descripción:** Obtiene el estado actual de la simulación iterativa sin ejecutar ninguna iteración.

**Respuesta:**
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

### 5. Reiniciar Simulación

**Endpoint:** `POST /api/simulacion-iterativa/reiniciar`

**Descripción:** Reinicia la simulación iterativa, limpiando todo el estado y permitiendo configurar una nueva simulación.

**Cuerpo de la Petición:** No requiere cuerpo.

**Respuesta:**
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

## Flujo de Uso

### 1. Configuración Inicial

1. **Configurar pedidos semanales** (usando endpoints existentes para cargar pedidos)
2. **Configurar simulación iterativa** con `POST /api/simulacion-iterativa/configurar`
3. **Verificar estado** con `GET /api/simulacion-iterativa/estado`

### 2. Ejecución Iterativa

**Opción A: Secuencial (paso a paso)**
1. **Obtener siguiente solución** con `POST /api/simulacion-iterativa/obtener-solucion`
2. **Procesar la solución** en el frontend
3. **Repetir** hasta que `success` sea `false` (simulación terminada)

**Opción B: Por fecha específica**
1. **Obtener solución para fecha** con `POST /api/simulacion-iterativa/obtener-solucion-fecha`
2. **Especificar fecha** y si se debe avanzar la simulación
3. **Procesar la solución** en el frontend
4. **Repetir** con diferentes fechas según necesidad

### 3. Monitoreo

- Use `GET /api/simulacion-iterativa/estado` para verificar el progreso
- Verifique el campo `finalizada` para saber si la simulación terminó
- Use `iteraciones` para mostrar el progreso

## Estados de la Simulación

### Campos del Estado

- **`configurada`**: `true` si la simulación ha sido configurada exitosamente
- **`finalizada`**: `true` si la simulación ha terminado (todos los pedidos procesados o tiempo límite alcanzado)
- **`iteraciones`**: Número de iteraciones ejecutadas
- **`fechaActual`**: Fecha y hora actual de la simulación
- **`fechaLimite`**: Fecha límite de la simulación (generalmente 7 días después del inicio)
- **`pedidosSemanales`**: Pedidos que aún no han sido procesados
- **`pedidosPorAtender`**: Pedidos que están listos para ser procesados
- **`pedidosPlanificados`**: Pedidos que están en proceso de planificación
- **`pedidosEntregados`**: Pedidos que han sido entregados exitosamente

### Flujo de Estados

```
[No configurada] → [Configurada] → [Ejecutando] → [Finalizada]
                         ↓
                    [Reiniciada]
```

## Ejemplos de Uso

### Configurar y Ejecutar una Simulación Completa (Secuencial)

```javascript
// 1. Configurar simulación
const configResponse = await fetch('/api/simulacion-iterativa/configurar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        fechaInicio: '2024-01-15T08:00:00'
    })
});

const configData = await configResponse.json();
console.log('Configuración:', configData);

// 2. Ejecutar iteraciones secuenciales
while (true) {
    const solucionResponse = await fetch('/api/simulacion-iterativa/obtener-solucion', {
        method: 'POST'
    });
    
    const solucionData = await solucionResponse.json();
    
    if (!solucionData.success) {
        console.log('Simulación terminada:', solucionData.message);
        break;
    }
    
    console.log('Iteración:', solucionData.estado.iteraciones);
    console.log('Fitness:', solucionData.solucion.individuo.fitness);
    
    // Procesar la solución...
}
```

### Obtener Soluciones para Fechas Específicas

```javascript
// 1. Configurar simulación
await configurarSimulacion();

// 2. Obtener solución para una fecha específica (sin avanzar)
const solucionFecha = await fetch('/api/simulacion-iterativa/obtener-solucion-fecha', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        fecha: '2024-01-15T14:30:00',
        avanzarSimulacion: false
    })
});

const solucionData = await solucionFecha.json();
console.log('Solución para 14:30:', solucionData.solucion);

// 3. Avanzar simulación hasta una fecha específica
const avanzarFecha = await fetch('/api/simulacion-iterativa/obtener-solucion-fecha', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        fecha: '2024-01-15T16:00:00',
        avanzarSimulacion: true
    })
});

const avanzarData = await avanzarFecha.json();
console.log('Simulación avanzada hasta 16:00:', avanzarData.estado.fechaActual);
```

### Navegación por Fechas

```javascript
// Navegar a diferentes momentos de la simulación
const fechas = [
    '2024-01-15T08:00:00',
    '2024-01-15T12:00:00', 
    '2024-01-15T16:00:00',
    '2024-01-16T08:00:00'
];

for (const fecha of fechas) {
    const response = await fetch('/api/simulacion-iterativa/obtener-solucion-fecha', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            fecha: fecha,
            avanzarSimulacion: false // Solo calcular, no avanzar
        })
    });
    
    const data = await response.json();
    if (data.success) {
        console.log(`Solución para ${fecha}:`, data.solucion.individuo.fitness);
    }
}
```

### Monitorear Estado

```javascript
const estadoResponse = await fetch('/api/simulacion-iterativa/estado');
const estadoData = await estadoResponse.json();

console.log('Estado actual:', estadoData.estado);
console.log('Progreso:', `${estadoData.estado.iteraciones} iteraciones`);
console.log('Pedidos restantes:', estadoData.estado.pedidosSemanales);
```

## Diferencias con el Enfoque Anterior

### Enfoque Anterior (Bucle Continuo)
- Ejecutaba toda la simulación de una vez con `while` continuo
- Difícil de controlar y monitorear
- No permitía intervención durante la ejecución
- Siempre usaba `actualizarEstadoGlobal`

### Enfoque Nuevo (Iterativo)
- **NO usa `while` para múltiples iteraciones** - cada petición devuelve UNA solución
- Control completo sobre cada paso
- Permite monitoreo en tiempo real
- Facilita la integración con interfaces de usuario
- Permite pausar, continuar o modificar la simulación
- **Manejo inteligente de `actualizarEstadoGlobal`**:
  - Primera iteración: NO usa `actualizarEstadoGlobal` (mantiene estado inicial)
  - Iteraciones siguientes: SÍ usa `actualizarEstadoGlobal` (refleja progreso real)
- **Cálculo por fecha específica**: Usa copias temporales del estado sin afectar el estado real

## Consideraciones

1. **Orden de Llamadas**: Debe configurarse antes de obtener soluciones
2. **Estado Persistente**: El estado se mantiene entre peticiones
3. **Concurrencia**: Las peticiones deben ser secuenciales para mantener consistencia
4. **Limpieza**: Use `/reiniciar` para limpiar el estado antes de una nueva simulación
5. **Monitoreo**: Verifique el estado `finalizada` para saber cuándo parar

## Códigos de Error Comunes

- **400**: Parámetros faltantes o inválidos
- **500**: Error interno del servidor
- **200 con success: false**: Simulación terminada o no configurada 