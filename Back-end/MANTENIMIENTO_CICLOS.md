# Sistema de Mantenimiento Basado en Ciclos

## Descripción

El sistema de mantenimiento ahora incluye una lógica mejorada que calcula automáticamente las fechas de mantenimiento basándose en el primer mantenimiento registrado de cada camión y aplicando un ciclo de **cada 2 meses**.

## Cómo Funciona

### Formato del Archivo de Mantenimiento
```
20230401:TA01  // Fecha inicial (YYYYMMDD) : Código del camión
20230403:TD01
20230405:TC01
...
```

### Lógica de Cálculo

1. **Mantenimiento Base**: Se toma el primer mantenimiento registrado del camión (el que tiene el mes más bajo)
2. **Ciclo**: A partir de esa fecha, el mantenimiento se repite **cada 2 meses**
3. **Día Fijo**: El día del mes siempre es el mismo que el del mantenimiento inicial

### Ejemplo
Si un camión `TA01` tiene su primer mantenimiento el **1 de abril** (mes 4, día 1):
- Mantenimientos programados: 1/abril, 1/junio, 1/agosto, 1/octubre, 1/diciembre
- Meses: 4, 6, 8, 10, 12

## Nuevos Endpoints

### 1. Verificar Mantenimiento por Ciclo
```
GET /api/mantenimientos/verificar-ciclo?codigoCamion=TA01&dia=1&mes=6
```

**Respuesta:**
```json
{
  "codigoCamion": "TA01",
  "dia": 1,
  "mes": 6,
  "tieneMantenimientoCiclo": true,
  "metodo": "calculo_basado_en_ciclo"
}
```

### 2. Obtener Todas las Fechas de Mantenimiento de un Camión
```
GET /api/mantenimientos/fechas-camion?codigoCamion=TA01&año=2025
```

**Respuesta:**
```json
{
  "codigoCamion": "TA01",
  "año": 2025,
  "fechasMantenimiento": [
    {"dia": 1, "mes": 4},
    {"dia": 1, "mes": 6},
    {"dia": 1, "mes": 8},
    {"dia": 1, "mes": 10},
    {"dia": 1, "mes": 12}
  ],
  "totalMantenimientos": 5
}
```

### 3. Verificar Mantenimiento (Método Directo - para comparación)
```
GET /api/mantenimientos/verificar-directo?codigoCamion=TA01&dia=1&mes=6
```

**Respuesta:**
```json
{
  "codigoCamion": "TA01",
  "dia": 1,
  "mes": 6,
  "tieneMantenimientoDirecto": true,
  "metodo": "busqueda_directa"
}
```

## Métodos del Servicio

### `tieneMantenimientoProgramado(Camion camion, int dia, int mes)`
- **Nuevo método**: Calcula si hay mantenimiento basándose en el ciclo
- Busca el primer mantenimiento del camión
- Verifica si el día coincide
- Calcula si el mes está en el ciclo (cada 2 meses)

### `tieneMantenimientoProgramadoDirecto(Camion camion, int dia, int mes)`
- **Método original**: Busca directamente en la fecha específica
- Compatible con el comportamiento anterior

### `obtenerFechasMantenimientoCamion(Camion camion, int año)`
- **Nuevo método**: Genera todas las fechas de mantenimiento para un año
- Útil para planificación y visualización

## Ventajas del Nuevo Sistema

1. **Precisión**: Calcula exactamente cuándo debe tener mantenimiento cada camión
2. **Flexibilidad**: Permite consultar cualquier fecha sin necesidad de tener todos los registros
3. **Eficiencia**: No requiere almacenar todas las fechas, las calcula dinámicamente
4. **Compatibilidad**: Mantiene el método original para no romper funcionalidad existente

## Casos de Uso

### Verificar si un camión puede ser asignado en una fecha
```java
boolean puedeSerAsignado = !mantenimientoService.tieneMantenimientoProgramado(camion, 15, 6);
```

### Obtener todas las fechas de mantenimiento del año
```java
List<Map<String, Integer>> fechas = mantenimientoService.obtenerFechasMantenimientoCamion(camion, 2025);
```

### Planificar rutas evitando días de mantenimiento
```java
// Verificar cada día del mes para planificación
for (int dia = 1; dia <= 30; dia++) {
    if (!mantenimientoService.tieneMantenimientoProgramado(camion, dia, mes)) {
        // El camión está disponible este día
    }
}
```

## Ejemplo Completo

Dado el archivo `mantpreventivo.txt`:
```
20230401:TA01  // TA01 - Mantenimiento cada 2 meses desde abril, día 1
20230503:TD06  // TD06 - Mantenimiento cada 2 meses desde mayo, día 3
```

### Consultas:
- `TA01` en `1/junio` (día 1, mes 6) → **SÍ** (4→6, diferencia = 2)
- `TA01` en `1/julio` (día 1, mes 7) → **NO** (4→7, diferencia = 3, no múltiplo de 2)
- `TD06` en `3/julio` (día 3, mes 7) → **SÍ** (5→7, diferencia = 2)
- `TD06` en `3/agosto` (día 3, mes 8) → **NO** (5→8, diferencia = 3, no múltiplo de 2)
