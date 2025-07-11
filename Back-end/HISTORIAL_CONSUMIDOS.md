# 📋 Historial de Paquetes Consumidos

## 🎯 **Descripción**

El sistema ahora mantiene un historial completo de todos los paquetes (individuos) que han sido consumidos por el frontend. Esto permite:

- **Rastrear el progreso** de la simulación
- **Consultar paquetes anteriores** que ya fueron procesados
- **Analizar el historial** de decisiones tomadas
- **Exportar información** para análisis posterior

## 🔧 **Implementación**

### **Clase Principal: `GestorHistorialSimulacion`**

Se agregó una nueva lista `historialConsumidos` que almacena todos los paquetes que han sido consumidos:

```java
private static final List<IndividuoDto> historialConsumidos = new ArrayList<>();
```

### **Flujo de Consumo**

1. **Frontend solicita** paquete via `/api/simulacion/mejor`
2. **Se ejecuta** `obtenerSiguientePaquete()`
3. **Se agrega** el paquete al `historialConsumidos` ANTES de devolverlo
4. **Se incrementa** el índice del frontend
5. **Se devuelve** el paquete al frontend

## 🌐 **Endpoints Disponibles**

### **1. Obtener Historial Completo**
```http
GET /api/simulacion/historial-consumidos
```
**Respuesta:** Lista de todos los paquetes consumidos
```json
[
  {
    "fechaHoraSimulacion": "2025-01-01T08:00:00",
    "pedidos": [...],
    "cromosoma": [...],
    "bloqueos": [...],
    "almacenes": [...]
  }
]
```

### **2. Información de Consumo**
```http
GET /api/simulacion/info-consumo
```
**Respuesta:** Estado detallado del consumo
```
📊 ESTADO DE CONSUMO: Disponibles=5, Consumidos=10, Próximo=10, Total=15
```

### **3. Exportar Historial**
```http
GET /api/simulacion/exportar-consumidos
```
**Respuesta:** Historial en formato texto legible
```
📋 HISTORIAL DE PAQUETES CONSUMIDOS
=====================================
#1 | Tiempo: 2025-01-01T08:00 | Pedidos: 5 | Cromosoma: 8 genes
#2 | Tiempo: 2025-01-01T08:30 | Pedidos: 3 | Cromosoma: 6 genes
=====================================
Total consumidos: 2
```

### **4. Último Paquete Consumido**
```http
GET /api/simulacion/ultimo-consumido
```
**Respuesta:** El último paquete que fue consumido

### **5. Paquete por Índice**
```http
GET /api/simulacion/consumido/{indice}
```
**Ejemplo:** `/api/simulacion/consumido/0` - Primer paquete consumido

### **6. Total de Consumidos**
```http
GET /api/simulacion/total-consumidos
```
**Respuesta:** Número total de paquetes consumidos

### **7. Limpiar Historial de Consumidos**
```http
DELETE /api/simulacion/limpiar-consumidos
```
**Respuesta:** Confirmación de limpieza con número de paquetes eliminados

### **8. Estadísticas del Historial**
```http
GET /api/simulacion/estadisticas-consumidos
```
**Respuesta:** Estadísticas detalladas del historial de consumidos
```
📊 ESTADÍSTICAS DEL HISTORIAL DE CONSUMIDOS
==========================================
Total paquetes consumidos: 15
Total pedidos procesados: 127
Promedio pedidos por paquete: 8.5
Mínimo pedidos en paquete: 2
Máximo pedidos en paquete: 12
Primera fecha: 2025-01-01T08:00
Última fecha: 2025-01-01T14:30
==========================================
```

## 📊 **Métodos Disponibles**

### **En `GestorHistorialSimulacion`:**

```java
// Obtener historial completo
List<IndividuoDto> getHistorialConsumidos()

// Obtener total de consumidos
int getTotalConsumidos()

// Obtener paquete específico
IndividuoDto obtenerPaqueteConsumidoPorIndice(int indice)

// Obtener último paquete
IndividuoDto obtenerUltimoPaqueteConsumido()

// Información de estado
String obtenerInfoConsumo()

// Buscar por rango de fechas
List<IndividuoDto> buscarPaquetesConsumidosPorFecha(LocalDateTime inicio, LocalDateTime fin)

// Exportar a texto
String exportarHistorialConsumidos()

// Limpiar solo historial de consumidos
void limpiarHistorialConsumidos()

// Obtener estadísticas
String obtenerEstadisticasConsumidos()
```

## 🔄 **Gestión del Historial**

### **Gestión del Historial**

#### **Limpieza Automática**

El historial de consumidos se limpia automáticamente SOLO cuando:

1. **Se limpia el historial completo** (`limpiarHistorialCompleto()`) - Nueva simulación

#### **Limpieza Manual**

El historial de consumidos se PRESERVA cuando:

1. **Se reinicia la reproducción** (`reiniciarReproduccion()`) - Mantiene el registro histórico
2. **Se pausa/reanuda la simulación** - No afecta el historial

#### **Limpieza Opcional**

Puedes limpiar manualmente el historial de consumidos usando:

```http
DELETE /api/simulacion/limpiar-consumidos
```

### **Logs de Consumo**

Cada vez que se consume un paquete, se registra en los logs:

```
🔥 PAQUETE CONSUMIDO #0 | Tiempo: 2025-01-01T08:00 | Total disponibles: 10 | Total consumidos: 1
🔥 PAQUETE CONSUMIDO #1 | Tiempo: 2025-01-01T08:30 | Total disponibles: 10 | Total consumidos: 2
```

## 💡 **Casos de Uso**

### **1. Análisis de Progreso**
```javascript
// Frontend puede consultar el progreso
fetch('/api/simulacion/info-consumo')
  .then(response => response.text())
  .then(info => console.log(info));
```

### **2. Revisar Paquetes Anteriores**
```javascript
// Obtener el último paquete consumido
fetch('/api/simulacion/ultimo-consumido')
  .then(response => response.json())
  .then(paquete => console.log('Último:', paquete));
```

### **3. Exportar para Análisis**
```javascript
// Exportar historial completo
fetch('/api/simulacion/exportar-consumidos')
  .then(response => response.text())
  .then(exportacion => {
    // Guardar en archivo o mostrar
    console.log(exportacion);
  });
```

### **4. Consultar Paquete Específico**
```javascript
// Obtener el primer paquete consumido
fetch('/api/simulacion/consumido/0')
  .then(response => response.json())
  .then(paquete => console.log('Primer paquete:', paquete));
```

## 🚀 **Ventajas del Sistema**

1. **Trazabilidad completa** de la simulación
2. **Análisis retrospectivo** de decisiones
3. **Debugging mejorado** con historial de estados
4. **Exportación de datos** para análisis externo
5. **Monitoreo en tiempo real** del progreso

## ⚠️ **Consideraciones**

- **Memoria:** El historial se mantiene en memoria, considerar límites para simulaciones muy largas
- **Concurrencia:** Todos los métodos son `synchronized` para thread-safety
- **Persistencia:** El historial se preserva al reiniciar la reproducción (solo se limpia con nueva simulación)
- **Control Manual:** Puedes limpiar el historial de consumidos independientemente cuando sea necesario

## 🔍 **Ejemplo de Uso Completo**

```java
// En el backend
public void ejemploUso() {
    // Obtener información de consumo
    String info = GestorHistorialSimulacion.obtenerInfoConsumo();
    System.out.println(info);
    
    // Obtener último paquete
    IndividuoDto ultimo = GestorHistorialSimulacion.obtenerUltimoPaqueteConsumido();
    if (ultimo != null) {
        System.out.println("Último tiempo: " + ultimo.getFechaHoraSimulacion());
    }
    
    // Buscar paquetes en un rango de fechas
    LocalDateTime inicio = LocalDateTime.of(2025, 1, 1, 8, 0);
    LocalDateTime fin = LocalDateTime.of(2025, 1, 1, 12, 0);
    List<IndividuoDto> enRango = GestorHistorialSimulacion.buscarPaquetesConsumidosPorFecha(inicio, fin);
    System.out.println("Paquetes en rango: " + enRango.size());
    
    // Obtener estadísticas del historial
    String estadisticas = GestorHistorialSimulacion.obtenerEstadisticasConsumidos();
    System.out.println(estadisticas);
    
    // Limpiar historial de consumidos si es necesario
    // GestorHistorialSimulacion.limpiarHistorialConsumidos();
}
```

---

**🎉 ¡El sistema de historial de consumidos está listo para usar!** 