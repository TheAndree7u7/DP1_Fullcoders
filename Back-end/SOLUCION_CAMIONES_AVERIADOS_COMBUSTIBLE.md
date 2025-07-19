# Solución: Camiones Averiados Consumiendo Combustible

## Problema Identificado

Los camiones averiados estaban cambiando su nivel de combustible a pesar de que no se movían, lo cual es un comportamiento incorrecto desde el punto de vista lógico de la simulación.

## Causa Raíz

El problema se encontraba en **dos ubicaciones específicas** del código donde se llamaba al método `actualizarCombustible()` **sin verificar el estado del camión** y **sin considerar si realmente cambiaba de posición**:

### 1. Clase `Camion.java` - Método `actualizarEstado()`
**Ubicación:** Línea 136 (antes de la corrección)
```java
// Actualizar el nodo en el que se encuentra el camión
int cantNodos = (int) (Parametros.diferenciaTiempoMinRequest * velocidadPromedio / 60);
int antiguo = gen.getPosNodo();
gen.setPosNodo(antiguo + cantNodos);
int distanciaRecorrida = gen.getPosNodo() - antiguo;
actualizarCombustible(distanciaRecorrida); // ← PROBLEMA: Sin verificación de estado ni posición
```

### 2. Clase `Gen.java` - Método `procesarEntregaPedido()`
**Ubicación:** Línea 114 (antes de la corrección)
```java
if (dentroDeLimite) {
    fitness += rutaAstar.size();
    camion.actualizarCombustible(rutaAstar.size()); // ← PROBLEMA: Sin verificación de estado ni posición
    camion.entregarVolumenGLP(volumenAEntregar);
    // ...
}
```

## Estados de Camión Afectados

Los estados de camión que **NO deberían consumir combustible** son:
- `INMOVILIZADO_POR_AVERIA` - Camión detenido por avería menor (2h o 4h)
- `EN_MANTENIMIENTO_POR_AVERIA` - Camión en taller por avería

## Solución Implementada (MEJORADA)

### 1. Corrección en `Camion.java`
**Archivo:** `Back-end/plg/src/main/java/com/plg/entity/Camion.java`
**Líneas:** 155-185

```java
// SOLUCIÓN MEJORADA: Solo actualizar posición y combustible si el camión NO está averiado
// O si está averiado pero realmente cambia de posición
if (intermedio < gen.getRutaFinal().size()) {
    Coordenada nuevaCoordenada = gen.getRutaFinal().get(intermedio).getCoordenada();
    Coordenada coordenadaActual = getCoordenada();
    
    // Verificar si realmente cambió de posición
    boolean cambioPosicion = coordenadaActual == null || 
                           !coordenadaActual.equals(nuevaCoordenada);
    
    // Para camiones averiados: solo actualizar si realmente cambian de posición
    if (this.estado == EstadoCamion.INMOVILIZADO_POR_AVERIA || 
        this.estado == EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA) {
        
        if (cambioPosicion) {
            // Si el camión averiado cambia de posición (teletransporte), actualizar coordenada
            setCoordenada(nuevaCoordenada);
            System.out.println("🚛💥 TELETRANSPORTE BACKEND: Camión " + codigo + 
                             " averiado teletransportado de " + coordenadaActual + 
                             " a " + nuevaCoordenada);
            
            // Para camiones averiados que se teletransportan, NO consumir combustible
            // ya que el teletransporte no es movimiento real
        }
        // Si no cambió de posición, no hacer nada (camión averiado en su lugar)
    } else {
        // Para camiones no averiados: actualizar posición normalmente
        setCoordenada(nuevaCoordenada);
        
        // Solo consumir combustible si realmente se movió
        if (cambioPosicion && distanciaRecorrida > 0) {
            actualizarCombustible(distanciaRecorrida);
        }
    }
}
```

### 2. Corrección en `Gen.java`
**Archivo:** `Back-end/plg/src/main/java/com/plg/utils/Gen.java`
**Líneas:** 125-145

```java
// SOLUCIÓN MEJORADA: Solo consumir combustible si el camión NO está averiado
// O si está averiado pero realmente cambia de posición
if (camion.getEstado() != com.plg.entity.EstadoCamion.INMOVILIZADO_POR_AVERIA && 
    camion.getEstado() != com.plg.entity.EstadoCamion.EN_MANTENIMIENTO_POR_AVERIA) {
    // Para camiones no averiados, consumir combustible normalmente
    camion.actualizarCombustible(rutaAstar.size());
} else {
    // Para camiones averiados, verificar si realmente cambian de posición
    Coordenada posicionActualCoord = camion.getCoordenada();
    Coordenada nuevaPosicionCoord = pedido.getCoordenada();
    
    if (posicionActualCoord != null && nuevaPosicionCoord != null && 
        !posicionActualCoord.equals(nuevaPosicionCoord)) {
        // Si el camión averiado cambia de posición (teletransporte), NO consumir combustible
        System.out.println("🚛💥 GEN: Camión " + camion.getCodigo() + 
                         " averiado teletransportado de " + posicionActualCoord + 
                         " a " + nuevaPosicionCoord + " - NO consume combustible");
    }
    // Si no cambió de posición, no hacer nada
}
```

## Flujo de Ejecución Corregido

1. **Durante la simulación:** El método `actualizarEstadoGlobal()` en `Simulacion.java` llama a `actualizarCamiones()`
2. **Para cada camión:** Se ejecuta `camion.actualizarEstado()` 
3. **Verificación de estado y posición:** 
   - Si el camión está averiado: solo actualizar posición si realmente cambia (teletransporte)
   - Si el camión no está averiado: actualizar posición y consumir combustible si se mueve
4. **Consumo condicional:** Solo se consume combustible si el camión realmente cambia de posición

## Lógica de Teletransporte

### Camiones Averiados
- **Teletransporte permitido:** Los camiones averiados pueden cambiar de posición (teletransporte)
- **Sin consumo de combustible:** El teletransporte no consume combustible porque no es movimiento real
- **Logs informativos:** Se registra cuando ocurre un teletransporte

### Camiones Operativos
- **Movimiento normal:** Los camiones operativos se mueven normalmente por la ruta
- **Consumo de combustible:** Solo consumen combustible si realmente cambian de posición
- **Verificación de distancia:** Se verifica que la distancia recorrida sea mayor que 0

## Estados de Camión que SÍ Consumen Combustible

Los siguientes estados **SÍ deberían consumir combustible** porque representan camiones en movimiento:
- `DISPONIBLE` - Camión listo para operar
- `NO_DISPONIBLE` - Camión no listo para operar
- `EN_RUTA` - Camión en camino
- `ENTREGANDO_GLP_A_CLIENTE` - Camión descargando GLP
- `EN_MANTENIMIENTO` - Mantenimiento general
- `EN_MANTENIMIENTO_PREVENTIVO` - Mantenimiento preventivo
- `EN_MANTENIMIENTO_CORRECTIVO` - Mantenimiento correctivo
- `SIN_COMBUSTIBLE` - Sin combustible (aunque no debería moverse)
- `RECIBIENDO_COMBUSTIBLE` - Recargando combustible
- `ENTREGANDO_COMBUSTIBLE_A_CAMION` - Dando combustible a otro camión
- `RECIBIENDO_GLP` - Recargando GLP
- `ENTREGANDO_GLP_A_CAMION` - Transfiriendo GLP a otro camión
- `ALMACEN_TEMPORAL` - Unidad averiada como depósito temporal

## Verificación de la Solución

Para verificar que la solución funciona correctamente:

1. **Camiones averiados sin movimiento:** Su combustible debe permanecer constante
2. **Camiones averiados con teletransporte:** Su combustible debe permanecer constante (teletransporte no consume)
3. **Camiones operativos:** Su combustible debe disminuir solo si realmente cambian de posición
4. **Logs de simulación:** Deben mostrar teletransportes de camiones averiados sin consumo de combustible

## Casos de Uso

### Caso 1: Camión Averiado Sin Movimiento
- Camión en estado `INMOVILIZADO_POR_AVERIA` en posición (5,3)
- No cambia de posición durante la simulación
- **Resultado:** Combustible permanece constante

### Caso 2: Camión Averiado Con Teletransporte
- Camión en estado `INMOVILIZADO_POR_AVERIA` en posición (5,3)
- Algoritmo lo teletransporta a posición (8,2)
- **Resultado:** Posición cambia, combustible permanece constante

### Caso 3: Camión Operativo Con Movimiento
- Camión en estado `EN_RUTA` en posición (5,3)
- Se mueve a posición (6,3)
- **Resultado:** Posición cambia, combustible disminuye

### Caso 4: Camión Operativo Sin Movimiento
- Camión en estado `EN_RUTA` en posición (5,3)
- No cambia de posición durante la simulación
- **Resultado:** Posición no cambia, combustible no disminuye

## Impacto en el Sistema

- ✅ **Corrección lógica:** Los camiones averiados ya no consumen combustible
- ✅ **Teletransporte permitido:** Los camiones averiados pueden cambiar de posición sin consumir combustible
- ✅ **Verificación de posición:** Solo se consume combustible si realmente hay cambio de posición
- ✅ **Sin impacto en rendimiento:** Las verificaciones son muy rápidas
- ✅ **Mantenimiento de funcionalidad:** Los camiones operativos siguen funcionando normalmente
- ✅ **Consistencia:** El comportamiento ahora es lógicamente correcto
- ✅ **Logs informativos:** Se registran los teletransportes para debugging

## Archivos Modificados

1. `Back-end/plg/src/main/java/com/plg/entity/Camion.java`
2. `Back-end/plg/src/main/java/com/plg/utils/Gen.java`

## Fecha de Implementación

**Fecha:** 2025-01-XX
**Problema:** Camiones averiados consumiendo combustible
**Solución:** Verificación de estado y cambio real de posición antes de actualizar combustible 