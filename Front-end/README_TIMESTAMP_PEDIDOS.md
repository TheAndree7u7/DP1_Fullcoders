# Mejoras en Timestamp de Simulación - Modal de Pedidos

## 📋 Descripción

Se han implementado mejoras en el modal de agregar pedidos para detectar automáticamente el timestamp de simulación actual y usarlo como fecha de registro del pedido, facilitando el proceso de registro.

## 🎯 Funcionalidades Implementadas

### 1. **Detección Automática del Timestamp**
- ✅ El modal detecta automáticamente el timestamp de simulación actual
- ✅ Los campos de fecha y hora se inicializan con la fecha de simulación
- ✅ Se actualiza automáticamente cuando cambia el timestamp de simulación

### 2. **Visualización Clara del Timestamp**
- ✅ Muestra el timestamp de simulación actual en formato legible
- ✅ Indica claramente que esa es la fecha de registro del pedido
- ✅ Explica que se puede modificar manualmente si es necesario

### 3. **Sincronización Manual**
- ✅ Botón para sincronizar manualmente con el timestamp actual
- ✅ Útil cuando el usuario ha modificado la fecha manualmente

### 4. **Consistencia en Ambos Modos**
- ✅ Información del timestamp en modo "Pedido Individual"
- ✅ Información del timestamp en modo "Archivo de Pedidos"

## 🔧 Componentes Modificados

### **ModalAgregarPedidos.tsx**

#### **1. Cálculo de Timestamp**
```typescript
// Calcular timestamp de simulación actual
const timestampSimulacion = calcularTimestampSimulacion(
  fechaHoraSimulacion,
  horaSimulacion
);
```

#### **2. Extracción de Fecha y Hora**
```typescript
const extraerFechaHoraSimulacion = () => {
  if (!timestampSimulacion) {
    return {
      año: new Date().getFullYear(),
      mes: new Date().getMonth() + 1,
      dia: new Date().getDate(),
      hora: 0,
      minuto: 0
    };
  }

  // El timestamp tiene formato: "YYYY-MM-DD HH:mm:ss"
  const fecha = new Date(timestampSimulacion);
  return {
    año: fecha.getFullYear(),
    mes: fecha.getMonth() + 1,
    dia: fecha.getDate(),
    hora: fecha.getHours(),
    minuto: fecha.getMinutes()
  };
};
```

#### **3. Inicialización con Fecha de Simulación**
```typescript
const [pedidoIndividual, setPedidoIndividual] = useState(() => {
  const fechaSimulacion = extraerFechaHoraSimulacion();
  return {
    año: fechaSimulacion.año,
    mes: fechaSimulacion.mes,
    dia: fechaSimulacion.dia,
    hora: fechaSimulacion.hora,
    minuto: fechaSimulacion.minuto,
    // ... otros campos
  };
});
```

#### **4. Actualización Automática**
```typescript
React.useEffect(() => {
  const fechaSimulacion = extraerFechaHoraSimulacion();
  setPedidoIndividual(prev => ({
    ...prev,
    año: fechaSimulacion.año,
    mes: fechaSimulacion.mes,
    dia: fechaSimulacion.dia,
    hora: fechaSimulacion.hora,
    minuto: fechaSimulacion.minuto
  }));
}, [timestampSimulacion]);
```

## 🎨 Interfaz de Usuario

### **Sección de Fecha y Hora (Modo Individual)**
```
📅 Fecha y Hora de Registro del Pedido

🕐 Timestamp de Simulación Actual: 2025-01-15 14:30:00

Esta fecha se actualiza automáticamente según el tiempo de simulación actual. 
Puedes modificarla manualmente si necesitas registrar el pedido en un momento específico.

[Año] [Mes] [Día] [Hora] [Minutos]

🔄 Sincronizar con Timestamp de Simulación Actual
```

### **Sección de Timestamp (Modo Archivo)**
```
🕐 Timestamp de Simulación Actual

Fecha y Hora: 2025-01-15 14:30:00

Los pedidos del archivo se registrarán en el momento actual de la simulación. 
Asegúrate de que el archivo contenga la fecha y hora correctas para el registro.
```

## 🔄 Flujo de Funcionamiento

### **1. Apertura del Modal**
- Se calcula el timestamp de simulación actual
- Se extraen año, mes, día, hora y minutos
- Se inicializan los campos del formulario

### **2. Durante la Simulación**
- El timestamp se actualiza automáticamente
- Los campos de fecha se sincronizan automáticamente
- El usuario puede modificar manualmente si es necesario

### **3. Sincronización Manual**
- Botón "🔄 Sincronizar con Timestamp de Simulación Actual"
- Restaura la fecha actual de simulación
- Útil después de modificaciones manuales

### **4. Limpieza del Formulario**
- Después de agregar un pedido
- Se restablece con la fecha de simulación actual
- No vuelve a la fecha del sistema

## 📊 Beneficios

### **1. Facilidad de Uso**
- ✅ No es necesario calcular manualmente la fecha
- ✅ Evita errores de fecha incorrecta
- ✅ Sincronización automática con la simulación

### **2. Precisión**
- ✅ Los pedidos se registran en el momento exacto de la simulación
- ✅ Consistencia con el flujo de tiempo de la simulación
- ✅ Evita anacronismos en los datos

### **3. Flexibilidad**
- ✅ Permite modificación manual si es necesario
- ✅ Botón de sincronización para restaurar fecha actual
- ✅ Información clara sobre el timestamp actual

### **4. Experiencia de Usuario**
- ✅ Interfaz intuitiva y clara
- ✅ Información contextual sobre el timestamp
- ✅ Feedback visual del estado actual

## 🔍 Casos de Uso

### **1. Registro Normal**
- Usuario abre modal
- Fecha se establece automáticamente
- Usuario llena resto de información
- Pedido se registra en el momento actual de simulación

### **2. Registro con Fecha Específica**
- Usuario abre modal
- Modifica manualmente la fecha
- Registra pedido en momento específico
- Puede sincronizar después si es necesario

### **3. Registro de Archivo**
- Usuario selecciona archivo
- Ve el timestamp actual de simulación
- Confirma que el archivo tiene fechas correctas
- Procesa el archivo

## ⚠️ Consideraciones

### **1. Formato de Timestamp**
- El timestamp debe tener formato: "YYYY-MM-DD HH:mm:ss"
- Se maneja el caso cuando no hay timestamp disponible
- Fallback a fecha del sistema si es necesario

### **2. Sincronización**
- La actualización automática solo ocurre si cambia el timestamp
- El usuario puede desactivar la sincronización modificando manualmente
- El botón de sincronización restaura la fecha actual

### **3. Validaciones**
- Las validaciones de fecha siguen siendo las mismas
- Se mantiene la compatibilidad con el formato existente
- No afecta la lógica de procesamiento del backend

## 🚀 Uso

### **Para Usuarios:**
1. Abrir modal de agregar pedidos
2. Ver el timestamp de simulación actual
3. Completar el resto de información del pedido
4. Hacer clic en "Agregar Pedido"
5. El pedido se registra en el momento actual de simulación

### **Para Desarrolladores:**
- El timestamp se calcula usando `calcularTimestampSimulacion()`
- La fecha se extrae usando `extraerFechaHoraSimulacion()`
- La sincronización automática usa `useEffect`
- La sincronización manual usa el botón dedicado 