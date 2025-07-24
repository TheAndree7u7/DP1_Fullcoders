# Logs para Debugging de Averías Automáticas

## Descripción

Este documento explica los logs que se han agregado para monitorear y debuggear la funcionalidad de averías automáticas en el frontend.

## Logs Implementados

### 1. **Contexto de Simulación** (`SimulacionContext.tsx`)

#### Log de Inicio de Avance de Hora
```
⏰ CONTEXTO: Iniciando avance de hora...
```
- **Cuándo**: Al inicio de cada avance de hora
- **Información**: Hora actual, total de camiones, camiones averiados, rutas con tipos de nodos

#### Log de Estado de Simulación
```
📊 CONTEXTO: Estado de simulación creado para averías automáticas...
```
- **Cuándo**: Después de crear el estado de simulación
- **Información**: Hora de simulación, fecha, total de camiones y rutas

#### Log de Completado
```
✅ CONTEXTO: Avance de hora completado
```
- **Cuándo**: Al finalizar el avance de hora

### 2. **Avance de Hora** (`avanceHora.ts`)

#### Log de Inicio
```
🚀 AVANCE_HORA: Iniciando avance de hora...
```
- **Cuándo**: Al inicio de la función `avanzarHora`
- **Información**: Total de camiones, camiones averiados, rutas con tipos de nodos, disponibilidad del estado de simulación

#### Log de Avance de Camiones
```
🚛 AVANCE_HORA: Avanzando camiones con estado de simulación...
```
- **Cuándo**: Antes de llamar a `avanzarTodosLosCamiones`

#### Log de Nuevas Averías
```
🚛💥 AVANCE_HORA: Se detectaron nuevas averías automáticas...
```
- **Cuándo**: Si se detectan nuevas averías automáticas
- **Información**: Número de averiados antes y después, nuevas averías detectadas

### 3. **Lógica de Camiones** (`camionLogic.ts`)

#### Log de Detección de Avería
```
🚛💥 CAMION_LOGIC: DETECTADA AVERÍA AUTOMÁTICA EN avanzarCamion...
```
- **Cuándo**: Cuando se detecta una avería automática en `avanzarCamion`
- **Información**: ID del camión, tipo de avería, porcentaje, siguiente paso, ubicación actual, estado actual, disponibilidad del estado de simulación

#### Log de Marcado como Averiado
```
🚛🔴 CAMION_LOGIC: Marcando camión como averiado automáticamente...
```
- **Cuándo**: Cuando se marca un camión como averiado
- **Información**: ID del camión, tipo de avería, nueva ubicación

#### Log de Registro en Backend
```
📡 CAMION_LOGIC: Registrando avería automática en backend...
```
- **Cuándo**: Cuando se va a registrar la avería en el backend

#### Log de Error de Estado
```
⚠️ CAMION_LOGIC: No se pudo registrar avería automática - estadoSimulacion no disponible
```
- **Cuándo**: Si no se puede registrar la avería por falta del estado de simulación

### 4. **Detección de Averías** (`camionLogic.ts`)

#### Log de Verificación
```
🔍 DETECTAR_AVERIA: Verificando avería automática...
```
- **Cuándo**: Al inicio de cada verificación de avería automática
- **Información**: ID del camión, estado actual, siguiente paso, disponibilidad de tipos de nodos

#### Log de Camión Ya Averiado
```
🔍 DETECTAR_AVERIA: Camión ya está averiado, no necesita detección
```
- **Cuándo**: Si el camión ya está averiado

#### Log de Tipos de Nodos No Disponibles
```
🔍 DETECTAR_AVERIA: No hay tipos de nodos disponibles o índice fuera de rango...
```
- **Cuándo**: Si no hay tipos de nodos o el índice está fuera de rango

#### Log de Tipo de Nodo Actual
```
🔍 DETECTAR_AVERIA: Tipo de nodo actual...
```
- **Cuándo**: Para cada verificación de tipo de nodo
- **Información**: Tipo de nodo actual y siguiente paso

#### Log de Avería Detectada
```
🚛💥 DETECTAR_AVERIA: DETECTADA AVERÍA AUTOMÁTICA: Camión [ID] en nodo [TIPO]
```
- **Cuándo**: Cuando se detecta una avería automática

#### Log de No Es Avería
```
🔍 DETECTAR_AVERIA: No es nodo de avería automática
```
- **Cuándo**: Cuando el nodo no es de avería automática

### 5. **Componente Mapa** (`Mapa.tsx`)

#### Log de Procesamiento
```
🗺️ MAPA: Procesando camiones para visualización...
```
- **Cuándo**: Al procesar los camiones para visualización
- **Información**: Total de camiones, total de rutas, camiones averiados, rutas con tipos de nodos

#### Log de Camión en Nodo de Avería
```
🚛💥 MAPA: Camión en nodo de avería automática...
```
- **Cuándo**: Cuando un camión está en un nodo de avería automática
- **Información**: ID del camión, tipo de nodo, porcentaje, siguiente paso, estado actual, ubicación

#### Log de Camión Averiado
```
🚛🔴 MAPA: Camión averiado detectado...
```
- **Cuándo**: Cuando se detecta un camión averiado
- **Información**: ID del camión, ubicación, porcentaje

## Cómo Usar los Logs para Debugging

### 1. **Verificar que los Tipos de Nodos se Carguen**
Busca estos logs al inicio de la simulación:
```
🗺️ MAPA: Procesando camiones para visualización...
rutasConTiposNodos: [número]
```

### 2. **Verificar que la Detección Funcione**
Busca estos logs durante la simulación:
```
🔍 DETECTAR_AVERIA: Verificando avería automática...
🔍 DETECTAR_AVERIA: Tipo de nodo actual...
```

### 3. **Verificar que se Detecten Averías**
Busca estos logs cuando un camión recorra un nodo de avería automática:
```
🚛💥 DETECTAR_AVERIA: DETECTADA AVERÍA AUTOMÁTICA...
🚛💥 CAMION_LOGIC: DETECTADA AVERÍA AUTOMÁTICA EN avanzarCamion...
```

### 4. **Verificar que se Registren en el Backend**
Busca estos logs cuando se registre una avería:
```
📡 CAMION_LOGIC: Registrando avería automática en backend...
🚛💥 INICIO DE AVERÍA AUTOMÁTICA...
```

### 5. **Verificar Errores**
Busca estos logs si hay problemas:
```
⚠️ CAMION_LOGIC: No se pudo registrar avería automática...
❌ ERROR AL PROCESAR AVERÍA AUTOMÁTICA...
```

## Problemas Comunes y Soluciones

### 1. **No se detectan averías automáticas**
- Verificar que `rutasConTiposNodos` sea mayor que 0
- Verificar que los tipos de nodos contengan `AVERIA_AUTOMATICA_T1`, `AVERIA_AUTOMATICA_T2`, o `AVERIA_AUTOMATICA_T3`

### 2. **No se registran en el backend**
- Verificar que `estadoSimulacionDisponible` sea `true`
- Verificar que no haya errores en la consola

### 3. **Los camiones no se marcan como averiados**
- Verificar que `debeAveriarse` sea `true`
- Verificar que el estado del camión cambie a "Averiado"

## Filtros de Consola Útiles

Para filtrar solo los logs de averías automáticas, usa estos filtros en la consola del navegador:

- `🚛💥` - Para ver todas las detecciones de averías automáticas
- `🔍 DETECTAR_AVERIA` - Para ver solo los logs de detección
- `📡 CAMION_LOGIC` - Para ver solo los logs de registro en backend
- `🗺️ MAPA` - Para ver solo los logs del componente Mapa 