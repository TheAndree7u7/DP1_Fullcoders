# TestSimulacionController - Controlador de Pruebas de Simulación

Este controlador permite ejecutar simulaciones en bucle entre un rango de fechas específico, ideal para pruebas automatizadas y análisis de rendimiento.

## Endpoints Disponibles

### 1. Ejecutar Simulación en Bucle
**POST** `/api/test-simulacion/ejecutar-bucle`

Ejecuta simulaciones automáticamente desde una fecha de inicio hasta una fecha de fin con un intervalo configurable.

#### Parámetros:
- `fechaInicio` (String, requerido): Fecha de inicio en formato ISO (yyyy-MM-ddTHH:mm:ss)
- `fechaFin` (String, requerido): Fecha de fin en formato ISO (yyyy-MM-ddTHH:mm:ss)
- `intervaloMinutos` (int, opcional): Intervalo entre simulaciones en minutos (por defecto: 30)

#### Ejemplo de uso:
```bash
curl -X POST "http://localhost:8080/api/test-simulacion/ejecutar-bucle" \
  -d "fechaInicio=2025-01-01T00:00:00" \
  -d "fechaFin=2025-01-02T00:00:00" \
  -d "intervaloMinutos=60"
```

#### Respuesta exitosa:
```json
{
  "message": "Simulación en bucle iniciada desde 2025-01-01T00:00:00 hasta 2025-01-02T00:00:00 con intervalo de 60 minutos"
}
```

### 2. Cancelar Simulación
**POST** `/api/test-simulacion/cancelar`

Cancela la simulación en bucle que esté en proceso.

#### Ejemplo de uso:
```bash
curl -X POST "http://localhost:8080/api/test-simulacion/cancelar"
```

#### Respuesta:
```json
{
  "message": "Simulación en bucle cancelada. Total ejecutadas: 5"
}
```

### 3. Obtener Estado
**GET** `/api/test-simulacion/estado`

Obtiene el estado actual de la simulación en bucle.

#### Ejemplo de uso:
```bash
curl -X GET "http://localhost:8080/api/test-simulacion/estado"
```

#### Respuesta:
```json
{
  "message": "En proceso: SÍ | Total simulaciones ejecutadas: 3"
}
```

### 4. Obtener Logs
**GET** `/api/test-simulacion/logs`

Obtiene el historial de logs de la simulación en bucle.

#### Ejemplo de uso:
```bash
curl -X GET "http://localhost:8080/api/test-simulacion/logs"
```

#### Respuesta:
```json
[
  "[2025-01-01T10:30:00] 🚀 Simulación en bucle iniciada",
  "[2025-01-01T10:30:01] 📅 Rango: 2025-01-01T00:00:00 hasta 2025-01-02T00:00:00",
  "[2025-01-01T10:30:02] ⏱️ Intervalo: 60 minutos",
  "[2025-01-01T10:30:03] 🔄 Ejecutando simulación para: 2025-01-01T00:00:00",
  "[2025-01-01T10:30:05] ✅ Simulación #1 completada para: 2025-01-01T00:00:00",
  "[2025-01-01T10:30:05] 📦 Pedidos procesados: 15",
  "[2025-01-01T10:30:05] 🧬 Fitness: 1250.5",
  "[2025-01-01T10:30:05] 📊 Estadísticas de Pedidos - 2025-01-01T00:00:00\n   • Total asignados: 15\n   • Entregados completamente: 12\n   • Entregados parcialmente: 2\n   • No entregados: 1\n   • Volumen asignado: 150.00 m³\n   • Volumen entregado: 142.50 m³\n   • Porcentaje entrega: 95.00%\n   • Por estado: {REGISTRADO=15}"
]
```

### 5. Obtener Estadísticas
**GET** `/api/test-simulacion/estadisticas`

Obtiene las estadísticas detalladas de pedidos de todas las simulaciones ejecutadas.

#### Ejemplo de uso:
```bash
curl -X GET "http://localhost:8080/api/test-simulacion/estadisticas"
```

#### Respuesta:
```json
[
  {
    "fechaSimulacion": "2025-01-01T00:00:00",
    "totalPedidosAsignados": 15,
    "pedidosEntregadosCompletamente": 12,
    "pedidosEntregadosParcialmente": 2,
    "pedidosNoEntregados": 1,
    "volumenTotalAsignado": 150.0,
    "volumenTotalEntregado": 142.5,
    "porcentajeEntrega": 95.0,
    "pedidosPorEstado": {
      "REGISTRADO": 15
    },
    "pedidosNoEntregadosCodigos": ["PED-001"],
    "pedidosEntregadosCodigos": ["PED-002", "PED-003", "PED-004", "PED-005", "PED-006", "PED-007", "PED-008", "PED-009", "PED-010", "PED-011", "PED-012", "PED-013", "PED-014"]
  }
]
```

### 6. Obtener Última Estadística
**GET** `/api/test-simulacion/estadisticas/ultima`

Obtiene las estadísticas de pedidos de la última simulación ejecutada.

#### Ejemplo de uso:
```bash
curl -X GET "http://localhost:8080/api/test-simulacion/estadisticas/ultima"
```

### 7. Obtener Resumen de Estadísticas
**GET** `/api/test-simulacion/estadisticas/resumen`

Obtiene un resumen completo de todas las estadísticas de pedidos en formato legible.

#### Ejemplo de uso:
```bash
curl -X GET "http://localhost:8080/api/test-simulacion/estadisticas/resumen"
```

#### Respuesta:
```
📊 RESUMEN DE ESTADÍSTICAS DE PEDIDOS
=====================================

📈 TOTALES ACUMULADOS:
   • Simulaciones ejecutadas: 48
   • Pedidos asignados: 720
   • Pedidos entregados: 684
   • Pedidos no entregados: 36
   • Volumen asignado: 7200.00 m³
   • Volumen entregado: 6840.00 m³
   • Porcentaje entrega global: 95.00%

📋 DETALLE POR SIMULACIÓN:
========================

1. 📊 Estadísticas de Pedidos - 2025-01-01T00:00:00
   • Total asignados: 15
   • Entregados completamente: 12
   • Entregados parcialmente: 2
   • No entregados: 1
   • Volumen asignado: 150.00 m³
   • Volumen entregado: 142.50 m³
   • Porcentaje entrega: 95.00%
   • Por estado: {REGISTRADO=15}

2. 📊 Estadísticas de Pedidos - 2025-01-01T00:30:00
   • Total asignados: 18
   • Entregados completamente: 16
   • Entregados parcialmente: 1
   • No entregados: 1
   • Volumen asignado: 180.00 m³
   • Volumen entregado: 172.00 m³
   • Porcentaje entrega: 95.56%
   • Por estado: {REGISTRADO=18}
```

## Características del Controlador

### ✅ Funcionalidades Implementadas

1. **Ejecución Asíncrona**: Las simulaciones se ejecutan en un hilo separado para no bloquear la aplicación
2. **Control de Estado**: Solo permite una simulación en bucle a la vez
3. **Cancelación**: Permite cancelar la simulación en cualquier momento
4. **Logging Detallado**: Registra cada paso de la simulación con timestamps
5. **Manejo de Errores**: Captura y registra errores sin detener todo el proceso
6. **Validaciones**: Valida fechas, intervalos y estados antes de ejecutar

### 🔧 Configuración

- **Intervalo Mínimo**: 1 minuto
- **Máximo de Logs**: 1000 entradas (se eliminan los más antiguos automáticamente)
- **Pausa entre Simulaciones**: 100ms para no saturar el sistema

### 📊 Información Registrada

Para cada simulación se registra:
- Fecha y hora de ejecución
- Número de pedidos procesados
- Valor de fitness del mejor individuo
- Estado de completado o error
- Timestamp de cada evento

### 📈 Estadísticas de Pedidos

Para cada simulación se calculan y registran las siguientes estadísticas:

#### **Métricas Cuantitativas**:
- **Total de pedidos asignados**: Número total de pedidos en el intervalo
- **Pedidos entregados completamente**: Pedidos con 100% del volumen entregado
- **Pedidos entregados parcialmente**: Pedidos con entrega parcial (1-99%)
- **Pedidos no entregados**: Pedidos sin entregas (0%)
- **Volumen total asignado**: Suma de todos los volúmenes asignados (m³)
- **Volumen total entregado**: Suma de todos los volúmenes entregados (m³)
- **Porcentaje de entrega**: (Volumen entregado / Volumen asignado) × 100

#### **Métricas Cualitativas**:
- **Pedidos por estado**: Distribución según `EstadoPedido` (REGISTRADO, ENTREGADO, PLANIFICADO)
- **Códigos de pedidos no entregados**: Lista de códigos de pedidos sin entregas
- **Códigos de pedidos entregados**: Lista de códigos de pedidos con entregas (completas o parciales)

#### **Ejemplo de Análisis**:
```
📊 Estadísticas de Pedidos - 2025-01-01T00:00:00
   • Total asignados: 15
   • Entregados completamente: 12 (80%)
   • Entregados parcialmente: 2 (13.3%)
   • No entregados: 1 (6.7%)
   • Volumen asignado: 150.00 m³
   • Volumen entregado: 142.50 m³
   • Porcentaje entrega: 95.00%
   • Por estado: {REGISTRADO=15}
   • Pedidos no entregados: [PED-001]
   • Pedidos entregados: [PED-002, PED-003, ..., PED-015]
```

## Ejemplos de Uso

### Ejemplo 1: Simulación Diaria
```bash
# Simular un día completo con intervalos de 30 minutos
curl -X POST "http://localhost:8080/api/test-simulacion/ejecutar-bucle" \
  -d "fechaInicio=2025-01-01T00:00:00" \
  -d "fechaFin=2025-01-02T00:00:00" \
  -d "intervaloMinutos=30"
```

### Ejemplo 2: Simulación Semanal
```bash
# Simular una semana con intervalos de 2 horas
curl -X POST "http://localhost:8080/api/test-simulacion/ejecutar-bucle" \
  -d "fechaInicio=2025-01-01T00:00:00" \
  -d "fechaFin=2025-01-08T00:00:00" \
  -d "intervaloMinutos=120"
```

### Ejemplo 3: Monitoreo en Tiempo Real
```bash
# Verificar estado cada 5 segundos
while true; do
  curl -X GET "http://localhost:8080/api/test-simulacion/estado"
  sleep 5
done
```

## Notas Importantes

1. **Una Simulación a la Vez**: Solo se puede ejecutar una simulación en bucle simultáneamente
2. **Persistencia de Datos**: Los resultados se almacenan en `GestorHistorialSimulacion` y pueden ser accedidos por otros endpoints
3. **Recursos del Sistema**: Las simulaciones intensivas pueden consumir CPU y memoria significativamente
4. **Logs Volátiles**: Los logs se mantienen en memoria y se pierden al reiniciar la aplicación

## Integración con el Sistema Existente

Este controlador utiliza las mismas clases y métodos que el sistema de simulación principal:
- `Simulacion.configurarSimulacionSemanal()`
- `AlgoritmoGenetico`
- `GestorHistorialSimulacion`
- `Parametros`

Los resultados generados son compatibles con el frontend existente y pueden ser visualizados normalmente. 