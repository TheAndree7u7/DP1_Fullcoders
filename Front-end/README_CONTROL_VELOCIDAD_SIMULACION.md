# Control de Velocidad de Simulación en Tiempo Real

## Descripción

Esta funcionalidad permite configurar el tiempo que dura cada nodo en la simulación en tiempo real, con la opción de ajuste dinámico basado en la velocidad de los camiones.

## Características Principales

### ⏱️ Configuración de Tiempo por Nodo
- **Tiempo configurable**: Cada nodo puede durar entre 0.1 y 100 segundos en tiempo real
- **Valor por defecto**: 62.9 segundos por nodo
- **Ajuste en tiempo real**: Los cambios se aplican inmediatamente sin reiniciar la simulación

### 🚀 Modo Dinámico
- **Ajuste automático**: El intervalo se ajusta automáticamente según la velocidad promedio de los camiones activos
- **Velocidad de referencia**: 60 km/h como velocidad base
- **Cálculo inteligente**: Los camiones más rápidos avanzan más rápido en la simulación

### 📊 Información de Velocidad
- **Velocidad promedio**: Muestra la velocidad promedio de todos los camiones activos
- **Rango de velocidades**: Muestra la velocidad mínima, máxima y promedio
- **Actualización en tiempo real**: La información se actualiza automáticamente

## Cómo Usar

### 1. Acceso al Control
- En el componente `Mapa`, haz clic en el botón "⚡ Control Velocidad"
- Se abrirá un panel con todas las opciones de configuración

### 2. Configuración Manual
1. **Segundos por nodo**: Ajusta el tiempo que debe durar cada nodo
2. **Presets**: Usa los botones predefinidos (Lento, Normal, Rápido, Muy Rápido)
3. **Intervalo**: El sistema calcula automáticamente el intervalo en milisegundos

### 3. Modo Dinámico
1. **Activar**: Marca la casilla "Modo dinámico"
2. **Ajuste automático**: El intervalo se ajusta según la velocidad de los camiones
3. **Desactivar**: Desmarca la casilla para volver al modo manual

## Archivos Modificados

### Nuevos Archivos
- `src/context/simulacion/utils/velocidad.ts` - Utilidades para manejo de velocidad
- `src/components/ControlVelocidad.tsx` - Componente de control de velocidad
- `README_CONTROL_VELOCIDAD_SIMULACION.md` - Esta documentación

### Archivos Modificados
- `src/context/simulacion/types.ts` - Agregadas funciones de cálculo de intervalo
- `src/context/simulacion/utils/index.ts` - Exportación de nuevas utilidades
- `src/components/Mapa.tsx` - Integración del control de velocidad

## Funciones Principales

### `calcularIntervaloTiempoReal(segundosPorNodo, velocidadCamion?)`
```typescript
// Ejemplo de uso
const intervalo = calcularIntervaloTiempoReal(62.9, 80); // 80 km/h
// Resultado: 629ms (ajustado por velocidad)
```

### `calcularVelocidadPromedioCamiones(camiones)`
```typescript
// Ejemplo de uso
const velocidadPromedio = calcularVelocidadPromedioCamiones(camiones);
// Resultado: 65.5 km/h
```

### `calcularIntervaloDinamico(segundosPorNodo, camiones)`
```typescript
// Ejemplo de uso
const intervalo = calcularIntervaloDinamico(62.9, camiones);
// Resultado: Intervalo ajustado según velocidad promedio de camiones
```

## Configuración por Defecto

- **Segundos por nodo**: 62.9 segundos
- **Intervalo calculado**: 629ms
- **Velocidad de referencia**: 60 km/h
- **Límites**: 100ms - 10000ms (0.1s - 10s)

## Presets Disponibles

| Preset | Segundos por Nodo | Descripción |
|--------|------------------|-------------|
| Lento | 120s | Simulación muy lenta para análisis detallado |
| Normal | 62.9s | Velocidad estándar recomendada |
| Rápido | 30s | Simulación acelerada |
| Muy Rápido | 10s | Simulación muy acelerada |

## Consideraciones Técnicas

### Rendimiento
- Los cálculos de velocidad se realizan solo cuando cambian los camiones
- El modo dinámico se actualiza automáticamente sin impacto en el rendimiento
- Los límites de intervalo previenen problemas de rendimiento

### Compatibilidad
- Compatible con todas las funcionalidades existentes de la simulación
- No afecta el comportamiento del backend
- Mantiene la sincronización con el tiempo de simulación

### Extensibilidad
- Fácil agregar nuevos presets de velocidad
- Posible agregar más factores de ajuste (tráfico, clima, etc.)
- Estructura modular para futuras mejoras

## Ejemplos de Uso

### Configuración Básica
```typescript
// En el componente Mapa
const [segundosPorNodo, setSegundosPorNodo] = useState(62.9);
const [intervalo, setIntervalo] = useState(obtenerIntervaloPorDefecto());

// Recalcular cuando cambie la configuración
useEffect(() => {
  const nuevoIntervalo = calcularIntervaloTiempoReal(segundosPorNodo);
  setIntervalo(nuevoIntervalo);
}, [segundosPorNodo]);
```

### Modo Dinámico
```typescript
// Calcular intervalo basado en velocidad de camiones
const intervaloDinamico = calcularIntervaloDinamico(62.9, camiones);
setIntervalo(intervaloDinamico);
```

## Troubleshooting

### Problema: La simulación va muy lenta
**Solución**: Reduce los segundos por nodo o usa un preset más rápido

### Problema: La simulación va muy rápida
**Solución**: Aumenta los segundos por nodo o usa un preset más lento

### Problema: El modo dinámico no funciona
**Solución**: Verifica que haya camiones activos con velocidad configurada

### Problema: El intervalo no se actualiza
**Solución**: Verifica que el efecto esté correctamente configurado y las dependencias sean correctas

## Futuras Mejoras

1. **Factores adicionales**: Considerar tráfico, clima, tipo de carretera
2. **Perfiles de velocidad**: Guardar configuraciones personalizadas
3. **Análisis de rendimiento**: Métricas de velocidad y eficiencia
4. **Integración con backend**: Sincronización de velocidades con el servidor 