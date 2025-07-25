# Solución al Problema de Averías Automáticas

## Problema Identificado

Se reportó que en el backend se generaban averías automáticas a camiones que no estaban en la lista de averías automáticas configurada, específicamente para nodos de tipo `AVERIA_AUTOMATICA_T1`, `AVERIA_AUTOMATICA_T2`, etc.

## Análisis del Problema

### Configuración de Averías Automáticas

**Archivo:** `Back-end/plg/src/main/resources/data/averias/averias.v1.txt`
```
T1_TA01_TI1
T1_TA02_TI1
T1_TB01_TI1
T2_TB02_TI2
T2_TB03_TI2
T2_TB04_TI2
T3_TC01_TI3
T3_TC02_TI3
T3_TC03_TI3
```

### Camiones Generados por el Sistema

**Método:** `CamionFactory.crearCamionesPorTipo()`

- **TA:** TA01, TA02 (2 camiones)
- **TB:** TB01, TB02, TB03, TB04 (4 camiones)
- **TC:** TC01, TC02, TC03, TC04 (4 camiones)
- **TD:** TD01-TD10 (10 camiones)

### Verificación de Coincidencia

✅ **Los códigos de camiones SÍ coinciden** con la configuración de averías automáticas:
- TA01, TA02 ✓ (configurados en turno 1)
- TB01 ✓ (configurado en turno 1)
- TB02, TB03, TB04 ✓ (configurados en turno 2)
- TC01, TC02, TC03 ✓ (configurados en turno 3)

## Problema Real Identificado

El problema **NO** estaba en la configuración, sino en **dos errores críticos en la lógica de aplicación de averías automáticas**:

### 1. ❌ ERROR CRÍTICO: Lógica de Control de Turnos Incorrecta

**Archivo:** `Back-end/plg/src/main/java/com/plg/utils/Herramientas.java` (líneas 88-108)

**Problema:** Las variables de control de turnos se estaban modificando incorrectamente:

```java
// ❌ CÓDIGO INCORRECTO (ANTES)
switch (turno) {
    case 1:
        if (Parametros.averio_turno_1 == true) {
            Parametros.averio_turno_2 = false;  // ❌ ERROR: Debería ser averio_turno_1 = false
            return;
        }
    case 2:
        if (Parametros.averio_turno_2 == true) {
            Parametros.averio_turno_1 = false;  // ❌ ERROR: Debería ser averio_turno_2 = false
            return;
        }
    case 3:
        if (Parametros.averio_turno_3 == true) {
            Parametros.averio_turno_2 = false;  // ❌ ERROR: Debería ser averio_turno_3 = false
            return;
        }
}
```

**Consecuencias:**
- Se aplicaban averías automáticas múltiples veces en el mismo turno
- Se podían aplicar averías a camiones no configurados
- La lógica de control no funcionaba correctamente

### 2. ❌ ERROR CRÍTICO: Asignación por Defecto de Averías Automáticas

**Archivo:** `Back-end/plg/src/main/java/com/plg/utils/Gen.java` (líneas 309-316)

**Problema:** Cuando no se encontraba una avería configurada para un camión, el código **por defecto** asignaba `AVERIA_AUTOMATICA_T1`:

```java
// ❌ CÓDIGO INCORRECTO (ANTES)
if (averia != null) {
    // ... lógica para asignar tipo correcto
} else {
    tipo_nodo_averia = TipoNodo.AVERIA_AUTOMATICA_T1;  // ❌ ERROR: Asignación por defecto
}
```

**Consecuencias:**
- Camiones como TD01 recibían nodos de avería automática a pesar de no estar configurados
- Se generaban averías automáticas para camiones no autorizados
- El problema persistía incluso después de corregir la lógica de control de turnos

### 3. Aplicación Múltiple de Averías
- El método `colocar_nodo_de_averia_automatica()` se podía llamar múltiples veces para el mismo camión
- Cada llamada modificaba la ruta final del camión
- No había verificación de si ya se había aplicado una avería automática

### 4. Falta de Validación de Índices
- No se verificaba si el índice estaba dentro del rango de la lista `rutaFinal`
- Podía causar `IndexOutOfBoundsException`

### 5. Falta de Logs de Debugging
- No había información suficiente para diagnosticar problemas
- Difícil rastrear qué camiones se estaban procesando

## Solución Implementada

### 1. ✅ CORRECCIÓN: Lógica de Control de Turnos

**Archivo:** `Back-end/plg/src/main/java/com/plg/utils/Herramientas.java`

```java
// ✅ CÓDIGO CORREGIDO (DESPUÉS)
switch (turno) {
    case 1:
        if (Parametros.averio_turno_1 == true) {
            System.out.println("⚠️ Ya se aplicaron averías automáticas en el turno 1. Saltando...");
            return;
        }
        Parametros.averio_turno_1 = true;
        System.out.println("✅ Avería automática configurada para turno 1");
        break;
    case 2:
        if (Parametros.averio_turno_2 == true) {
            System.out.println("⚠️ Ya se aplicaron averías automáticas en el turno 2. Saltando...");
            return;
        }
        Parametros.averio_turno_2 = true;
        System.out.println("✅ Avería automática configurada para turno 2");
        break;
    case 3:
        if (Parametros.averio_turno_3 == true) {
            System.out.println("⚠️ Ya se aplicaron averías automáticas en el turno 3. Saltando...");
            return;
        }
        Parametros.averio_turno_3 = true;
        System.out.println("✅ Avería automática configurada para turno 3");
        break;
    default:
        System.out.println("⚠️ Turno no válido: " + turno + ". No se aplicarán averías automáticas.");
        return;
}
```

### 2. ✅ CORRECCIÓN: Eliminación de Asignación por Defecto

**Archivo:** `Back-end/plg/src/main/java/com/plg/utils/Gen.java`

```java
// ✅ CÓDIGO CORREGIDO (DESPUÉS)
Averia averia = Parametros.dataLoader.averiasAutomaticas.stream()
        .filter(a -> a.getCamion().getCodigo().equals(camion.getCodigo()))
        .findFirst()
        .orElse(null);

// Verificación crítica: solo proceder si se encuentra una avería configurada para este camión
if (averia == null) {
    System.out.println("❌ ERROR: Camión " + camion.getCodigo() + " no tiene avería automática configurada. No se aplicará avería automática.");
    return;
}

TipoNodo tipo_nodo_averia;
String tipo_nodo_averia_string = averia.getTipoIncidente().getCodigo();
if (tipo_nodo_averia_string.equals("TI1")) {
    tipo_nodo_averia = TipoNodo.AVERIA_AUTOMATICA_T1;
} else if (tipo_nodo_averia_string.equals("TI2")) {
    tipo_nodo_averia = TipoNodo.AVERIA_AUTOMATICA_T2;
} else if (tipo_nodo_averia_string.equals("TI3")) {
    tipo_nodo_averia = TipoNodo.AVERIA_AUTOMATICA_T3;
} else {
    System.out.println("❌ ERROR: Tipo de incidente no válido para camión " + camion.getCodigo() + ": " + tipo_nodo_averia_string);
    return;
}
```

### 3. Verificación Doble de Camiones Configurados

```java
// Verificación adicional: solo procesar camiones que están en la lista de averías automáticas del turno
List<String> codigosCamionesConfigurados = averiasAutomaticasTurno.stream()
        .map(averia -> averia.getCamion().getCodigo())
        .toList();

System.out.println("📋 Códigos de camiones configurados para averías automáticas en turno " + turno + ": " + codigosCamionesConfigurados);

for (Gen gen : cromosoma) {
    // Verificación doble: el camión debe estar en la lista de averías automáticas del turno
    if (camiones_para_averiar_automaticamente.stream()
            .anyMatch(camion -> camion.getCodigo().equals(gen.getCamion().getCodigo())) &&
            codigosCamionesConfigurados.contains(gen.getCamion().getCodigo())) {
        
        System.out.println("🔧 Aplicando avería automática al camión " + gen.getCamion().getCodigo());
        gen.colocar_nodo_de_averia_automatica();
    } else if (codigosCamionesConfigurados.contains(gen.getCamion().getCodigo())) {
        System.out.println("⚠️ Camión " + gen.getCamion().getCodigo() + " está configurado pero no cumple condiciones para avería automática");
    }
}
```

### 4. Verificación de Avería Automática Existente

**Archivo:** `Back-end/plg/src/main/java/com/plg/utils/Gen.java`

```java
public void colocar_nodo_de_averia_automatica() {
    // Verificar si ya se ha colocado un nodo de avería automática en esta ruta
    boolean yaTieneAveriaAutomatica = rutaFinal.stream()
            .anyMatch(nodo -> nodo.getTipoNodo() == TipoNodo.AVERIA_AUTOMATICA_T1 ||
                            nodo.getTipoNodo() == TipoNodo.AVERIA_AUTOMATICA_T2 ||
                            nodo.getTipoNodo() == TipoNodo.AVERIA_AUTOMATICA_T3);
    
    if (yaTieneAveriaAutomatica) {
        System.out.println("⚠️ Camión " + camion.getCodigo() + " ya tiene un nodo de avería automática. No se aplicará otra.");
        return;
    }
    // ... resto del código
}
```

### 5. Validación de Índices

```java
List<Integer> posiciones_normales = new ArrayList<>();
for (int i = posicion_inicial; i <= posicion_final; i++) {
    if (i < rutaFinal.size() && rutaFinal.get(i).getTipoNodo().equals(TipoNodo.NORMAL)) {
        posiciones_normales.add(i);
    }
}
```

### 6. Verificación de Camión Ya Averiado

**Archivo:** `Back-end/plg/src/main/java/com/plg/utils/Herramientas.java`

```java
boolean camion_ya_averiado = Parametros.dataLoader.camionesAveriados.stream()
        .anyMatch(c -> c.getCodigo().equals(gen.getCamion().getCodigo()));

if (camion_en_averias_automaticas && camion_estado_disponible && !camion_ya_averiado) {
    // Solo procesar si no está ya averiado
}
```

### 7. Logs Detallados de Debugging

```java
System.out.println("🔍 Procesando averías automáticas para turno " + turno);
System.out.println("📋 Averías automáticas del turno: " + averiasAutomaticasTurno.size());
averiasAutomaticasTurno.forEach(averia -> 
    System.out.println("   - " + averia.getCamion().getCodigo() + " (" + averia.getTipoIncidente().getCodigo() + ")"));
```

## Resultado

Con estas mejoras:

1. ✅ **Se corrigió el error crítico** en la lógica de control de turnos
2. ✅ **Se eliminó la asignación por defecto** de averías automáticas
3. ✅ **Se evita la aplicación múltiple** de averías automáticas al mismo camión
4. ✅ **Se valida correctamente** que solo se apliquen a camiones configurados
5. ✅ **Se previenen errores** de índices fuera de rango
6. ✅ **Se proporciona información detallada** para debugging
7. ✅ **Se mantiene la funcionalidad** original para camiones correctamente configurados

## Verificación

Para verificar que la solución funciona correctamente:

1. Ejecutar la simulación
2. Revisar los logs del backend
3. Verificar que solo los camiones configurados en `averias.v1.txt` reciban averías automáticas
4. Confirmar que no se generen errores de índices

Los logs mostrarán claramente:
- Qué camiones están configurados para averías automáticas
- Qué camiones se seleccionan para aplicar averías
- Qué camiones se excluyen y por qué razón
- Confirmación de cuando se aplica una avería automática
- Advertencias cuando ya se aplicaron averías en un turno
- **ERRORES cuando se intenta aplicar averías a camiones no configurados**

## Causa Raíz del Problema

El problema principal era la **combinación de dos errores críticos**:

1. **Error en la lógica de control de turnos** que causaba que se aplicaran averías automáticas múltiples veces
2. **Asignación por defecto de averías automáticas** cuando no se encontraba una configuración para el camión

Estos errores se han corregido completamente, asegurando que solo los camiones configurados en el archivo `averias.v1.txt` reciban averías automáticas. 