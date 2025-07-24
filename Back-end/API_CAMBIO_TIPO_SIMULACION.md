# API para Cambio de Tipo de Simulación

Este documento describe los endpoints disponibles para cambiar y consultar el tipo de simulación en el sistema.

## Endpoints Disponibles

### 1. Cambiar Tipo de Simulación

**Endpoint:** `POST /api/simulacion/cambiar-tipo-simulacion`

**Descripción:** Permite cambiar el tipo de simulación actual a uno de los tipos disponibles.

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
    "tipoSimulacion": "DIARIA"
}
```

**Tipos de Simulación Disponibles:**
- `DIARIA` - Simulación diaria
- `SEMANAL` - Simulación semanal (por defecto)
- `COLAPSO` - Simulación de colapso

**Respuesta Exitosa (200 OK):**
```json
{
    "tipoSimulacionAnterior": "SEMANAL",
    "tipoSimulacionNuevo": "DIARIA",
    "mensaje": "Tipo de simulación cambiado exitosamente de SEMANAL a DIARIA",
    "exito": true
}
```

**Respuesta de Error (400 Bad Request):**
```json
{
    "tipoSimulacionAnterior": "SEMANAL",
    "tipoSimulacionNuevo": null,
    "mensaje": "Error: Tipo de simulación no proporcionado",
    "exito": false
}
```

**Respuesta de Error (500 Internal Server Error):**
```json
{
    "tipoSimulacionAnterior": "SEMANAL",
    "tipoSimulacionNuevo": null,
    "mensaje": "Error al cambiar tipo de simulación: [descripción del error]",
    "exito": false
}
```

### 2. Consultar Tipo de Simulación Actual

**Endpoint:** `GET /api/simulacion/tipo-simulacion-actual`

**Descripción:** Obtiene información sobre el tipo de simulación actualmente configurado.

**Respuesta Exitosa (200 OK):**
```json
{
    "tipoSimulacion": "SEMANAL",
    "descripcion": "Simulación semanal - Simula una semana completa de operaciones",
    "timestamp": "2025-01-15T10:30:00"
}
```

## Ejemplos de Uso

### Ejemplo 1: Cambiar a Simulación Diaria

```bash
curl -X POST http://localhost:8080/api/simulacion/cambiar-tipo-simulacion \
  -H "Content-Type: application/json" \
  -d '{
    "tipoSimulacion": "DIARIA"
  }'
```

### Ejemplo 2: Cambiar a Simulación de Colapso

```bash
curl -X POST http://localhost:8080/api/simulacion/cambiar-tipo-simulacion \
  -H "Content-Type: application/json" \
  -d '{
    "tipoSimulacion": "COLAPSO"
  }'
```

### Ejemplo 3: Consultar Tipo Actual

```bash
curl -X GET http://localhost:8080/api/simulacion/tipo-simulacion-actual
```

## Consideraciones Importantes

1. **Persistencia:** El cambio de tipo de simulación se mantiene en memoria durante la ejecución de la aplicación.

2. **Valor por Defecto:** Si no se ha cambiado explícitamente, el tipo de simulación por defecto es `SEMANAL`.

3. **Validación:** El sistema valida que el tipo de simulación proporcionado sea uno de los valores válidos del enum.

4. **Logs:** Todos los cambios se registran en los logs del sistema para auditoría.

5. **Impacto:** El cambio de tipo de simulación puede afectar el comportamiento de:
   - El algoritmo genético
   - Las averías automáticas
   - Los intervalos de tiempo
   - La configuración de parámetros

## Códigos de Estado HTTP

- `200 OK`: Operación exitosa
- `400 Bad Request`: Datos de entrada inválidos
- `500 Internal Server Error`: Error interno del servidor

## Logs del Sistema

El sistema registra las siguientes operaciones:

```
🌐 ENDPOINT LLAMADO: /api/simulacion/cambiar-tipo-simulacion
🔄 Solicitando cambio de tipo de simulación a: DIARIA
✅ Tipo de simulación cambiado exitosamente:
   • Tipo anterior: SEMANAL
   • Tipo nuevo: DIARIA
```

```
🌐 ENDPOINT LLAMADO: /api/simulacion/tipo-simulacion-actual
✅ Tipo de simulación actual: SEMANAL
``` 