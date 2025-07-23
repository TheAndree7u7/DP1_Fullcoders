# Control de Averías Automáticas

## Descripción

Esta funcionalidad permite al usuario activar o desactivar las averías automáticas y sus notificaciones toast desde la interfaz de usuario. Cuando las averías automáticas están desactivadas, los camiones no se marcarán automáticamente como averiados al pasar por nodos con averías automáticas.

## Funcionalidades Implementadas

### 1. **Estado de Control**
- Nuevo estado `averiasAutomaticasActivas` en el contexto de simulación
- Por defecto está activado (`true`)
- Se puede alternar con la función `toggleAveriasAutomaticas()`

### 2. **Componente de Control**
- Nuevo componente `ControlAveriasAutomaticas` que muestra:
  - Estado actual (activado/desactivado)
  - Botón para alternar el estado
  - Iconos visuales (AlertTriangle para activado, CheckCircle para desactivado)
  - Descripción del comportamiento actual
  - Nota explicativa sobre el funcionamiento

### 3. **Integración en la Interfaz**
- Nuevo panel "Control de Averías" en el menú derecho
- Accesible desde el botón "Control de Averías" en la barra de navegación del panel

### 4. **Lógica de Detección Modificada**
- La función `detectarAveriaAutomatica` ahora respeta el estado de activación
- Si las averías automáticas están desactivadas, no detecta ninguna avería
- Los camiones continúan su ruta normalmente sin ser marcados como averiados

### 5. **Control de Notificaciones**
- La función `handleAveriaAutomatica` ahora acepta un parámetro para controlar los toasts
- Cuando las averías automáticas están desactivadas, no se muestran notificaciones
- Los errores también respetan esta configuración

## Archivos Modificados

### 1. **Tipos (`src/context/simulacion/types.ts`)**
```typescript
export interface SimulacionContextType {
  // ... propiedades existentes ...
  averiasAutomaticasActivas: boolean;
  toggleAveriasAutomaticas: () => void;
}
```

### 2. **Contexto (`src/context/SimulacionContext.tsx`)**
```typescript
// Nuevo estado
const [averiasAutomaticasActivas, setAveriasAutomaticasActivas] = useState<boolean>(true);

// Nueva función
const toggleAveriasAutomaticas = () => {
  setAveriasAutomaticasActivas(prev => !prev);
};
```

### 3. **Lógica de Camiones (`src/context/simulacion/camionLogic.ts`)**
```typescript
// Función modificada para aceptar el estado de activación
export const detectarAveriaAutomatica = (
  camion: CamionEstado,
  ruta: RutaCamion,
  siguientePaso: number,
  averiasAutomaticasActivas: boolean = true
): { debeAveriarse: boolean; tipoAveria?: string } => {
  // Si las averías automáticas están desactivadas, no detectar ninguna
  if (!averiasAutomaticasActivas) {
    return { debeAveriarse: false };
  }
  // ... resto de la lógica
};
```

### 4. **Averías Automáticas (`src/components/mapa/utils/averiasAutomaticas.ts`)**
```typescript
// Función modificada para controlar toasts
export const handleAveriaAutomatica = async (
  camionId: string,
  tipoNodo: string,
  estadoSimulacion: {...},
  mostrarToasts: boolean = true
): Promise<void> => {
  // ... lógica existente ...
  
  // Mostrar toast solo si está habilitado
  if (mostrarToasts) {
    toast.info(`🚛💥 Camión ${camionId} averiado automáticamente (${tipoAveriaString})`, {...});
  }
};
```

### 5. **Menú Derecho (`src/components/RightMenu.tsx`)**
```typescript
// Nuevo panel agregado
const [panel, setPanel] = React.useState<... | 'controlAverias'>('camiones');

// Nuevo botón
<button onClick={() => setPanel('controlAverias')}>
  Control de Averías
</button>

// Nuevo contenido del panel
{panel === 'controlAverias' && (
  <ControlAveriasAutomaticas />
)}
```

### 6. **Componente de Control (`src/components/ControlAveriasAutomaticas.tsx`)**
- Componente completamente nuevo para la interfaz de control

## Flujo de Funcionamiento

### Cuando las Averías Automáticas están **ACTIVADAS**:
1. Los camiones detectan nodos con averías automáticas (T1, T2, T3)
2. Se marcan automáticamente como "Averiado"
3. Se registra la avería en el backend
4. Se muestran notificaciones toast informativas
5. La simulación continúa con el camión averiado

### Cuando las Averías Automáticas están **DESACTIVADAS**:
1. Los camiones **NO** detectan nodos con averías automáticas
2. Continúan su ruta normalmente sin ser marcados como averiados
3. **NO** se registran averías en el backend
4. **NO** se muestran notificaciones toast
5. La simulación continúa sin interrupciones por averías automáticas

## Uso de la Interfaz

### Acceder al Control:
1. Abrir el menú derecho (panel lateral)
2. Hacer clic en el botón "Control de Averías"
3. Ver el estado actual y la descripción

### Alternar el Estado:
1. Hacer clic en el botón "Activar" o "Desactivar"
2. El estado cambia inmediatamente
3. La descripción y el icono se actualizan
4. El comportamiento de la simulación cambia en tiempo real

## Ventajas de la Implementación

### ✅ **Control Total del Usuario**
- El usuario decide si quiere averías automáticas o no
- Cambio inmediato sin necesidad de reiniciar la simulación

### ✅ **Interfaz Intuitiva**
- Estado visual claro con iconos y colores
- Descripción del comportamiento actual
- Botón de acción simple y directo

### ✅ **Integración Completa**
- Respeta todas las configuraciones existentes
- No interfiere con otras funcionalidades
- Mantiene la consistencia del sistema

### ✅ **Flexibilidad**
- Se puede cambiar en cualquier momento durante la simulación
- No afecta las averías manuales (desde el panel "Averiar Camiones")
- Solo controla las averías automáticas de nodos

## Casos de Uso

### **Escenario 1: Pruebas sin Interrupciones**
- Desactivar averías automáticas para probar rutas completas
- Verificar que los camiones llegan a sus destinos sin interrupciones
- Analizar el comportamiento del sistema sin factores externos

### **Escenario 2: Simulación Realista**
- Activar averías automáticas para simular condiciones reales
- Observar cómo el sistema maneja las averías automáticas
- Probar la robustez del algoritmo genético

### **Escenario 3: Comparación de Estrategias**
- Ejecutar la misma simulación con y sin averías automáticas
- Comparar tiempos de entrega y eficiencia
- Evaluar el impacto de las averías en el rendimiento

## Notas Técnicas

### **Persistencia del Estado**
- El estado se mantiene durante toda la sesión
- Se reinicia a `true` cuando se recarga la página
- No se persiste entre sesiones (se puede implementar con localStorage si es necesario)

### **Rendimiento**
- No hay impacto en el rendimiento
- La verificación es muy rápida (solo un booleano)
- No afecta la velocidad de la simulación

### **Compatibilidad**
- Compatible con todas las funcionalidades existentes
- No rompe ninguna funcionalidad anterior
- Mantiene la API del contexto intacta

## Próximas Mejoras Posibles

### **Persistencia**
- Guardar el estado en localStorage
- Restaurar el estado al recargar la página

### **Configuración Avanzada**
- Control granular por tipo de avería (T1, T2, T3)
- Configuración de probabilidades de avería
- Horarios específicos para activar/desactivar

### **Estadísticas**
- Mostrar estadísticas de averías automáticas
- Historial de cambios de estado
- Métricas de impacto en la simulación 