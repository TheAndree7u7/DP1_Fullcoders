# Guía de Pruebas - Flujo de Averías con Recálculo

## Pruebas Básicas

### 1. Probar Avería Tipo 1 (TI1) - Sin Recálculo
**Objetivo**: Verificar que las averías menores no activen recálculo de rutas

**Pasos**:
1. Ejecutar backend y frontend
2. En el mapa, hacer clic en cualquier camión en movimiento
3. Seleccionar "Avería Tipo 1"
4. **Resultado esperado**:
   - Toast: "🚛💥 Camión [ID] averiado (Tipo 1)"
   - Camión se marca como averiado (color rojo)
   - **NO aparece** toast de "Recalculando rutas..."
   - **NO aparece** toast de "Rutas recalculadas"

### 2. Probar Avería Tipo 2 (TI2) - Con Recálculo
**Objetivo**: Verificar que las averías significativas activen recálculo de rutas

**Pasos**:
1. En el mapa, hacer clic en cualquier camión en movimiento
2. Seleccionar "Avería Tipo 2"
3. **Resultado esperado**:
   - Toast: "🚛💥 Camión [ID] averiado (Tipo 2)"
   - Toast: "🔄 Recalculando rutas debido a la avería..."
   - Después de ~2 segundos: "✅ Rutas recalculadas exitosamente"
   - Camión se marca como averiado

### 3. Probar Avería Tipo 3 (TI3) - Con Recálculo
**Objetivo**: Igual que TI2 pero con tipo 3

**Pasos**: Igual que prueba 2, pero seleccionar "Avería Tipo 3"
**Resultado esperado**: Igual que prueba 2

## Pruebas de Consola

### Backend - Verificar logs
**Abrir consola del backend y buscar**:
```
🔄 Nueva simulación solicitada debido a avería TI2 en camión [ID]
```
O:
```
🔄 Nueva simulación solicitada debido a avería TI3 en camión [ID]
```

**NO debe aparecer para TI1**.

### Frontend - Verificar logs
**Abrir DevTools > Console y buscar**:
```
🔄 Recargando simulación debido a avería...
✅ Simulación recargada exitosamente
```

## Pruebas de Red

### DevTools > Network
**Para TI2/TI3, verificar llamadas**:
1. `POST /api/averias/averiar-camion-con-nueva-simulacion`
   - Status: 201 Created
   - Body contiene la avería creada

2. `GET /api/simulacion/mejor` (después de 2 segundos)
   - Status: 200 OK  
   - Body contiene nueva simulación

**Para TI1, solo debe haber**:
1. `POST /api/averias/averiar-camion-con-nueva-simulacion`
   - No debe haber llamada posterior a `/simulacion/mejor`

## Pruebas de Estado

### Verificar estado del camión
**En cualquier tipo de avería**:
1. El camión debe dejar de moverse en el mapa
2. El color del camión debe cambiar a rojo (averiado)
3. En el panel derecho, el contador de "Averiados" debe incrementar

### Verificar recarga de datos (TI2/TI3)
**Después del recálculo**:
1. Los datos del contexto se actualizan
2. Si hay cambios en las rutas, el mapa se actualiza
3. Los contadores se recalculan

## Pruebas de Error

### 1. Simular error en backend
**Método**: Temporalmente cambiar URL del servicio a una inválida
```typescript
// En averiaApiService.ts, cambiar temporalmente:
const response = await fetch(`${API_CONFIG.BASE_URL}/averias/INVALID_ENDPOINT`, {
```

**Resultado esperado**:
- Toast: "❌ Error al averiar el camión"
- No se marca como averiado
- No se intenta recálculo

### 2. Simular error en recarga
**Método**: Temporalmente causar error en `cargarDatos()`

**Resultado esperado**:
- Avería se registra correctamente
- Toast: "⚠️ La avería fue registrada pero no se pudieron recalcular las rutas automáticamente"

## Validación Visual

### Antes de la avería
- Camión se mueve normalmente (color según estado)
- Progreso de ruta avanza

### Durante el proceso (TI2/TI3)
- Secuencia de toasts aparece correctamente
- Camión se detiene inmediatamente al reportar avería

### Después del recálculo (TI2/TI3)
- Datos actualizados en contexto
- Si hay nueva solución, rutas pueden cambiar
- Camión permanece averiado (correcto)

## Comandos de Testing

### Verificar que backend está ejecutándose
```bash
curl -X GET http://localhost:8080/api/averias
```

### Verificar que frontend está ejecutándose
Abrir: `http://localhost:5173` (o puerto configurado)

### Test manual de endpoint
```bash
# Probar endpoint directo
curl -X POST http://localhost:8080/api/averias/averiar-camion-con-nueva-simulacion \
  -H "Content-Type: application/json" \
  -d '{
    "codigoCamion": "TA01",
    "tipoIncidente": "TI2",
    "fechaHoraReporte": "2024-06-24T10:00:00"
  }'
```

## Checklist de Verificación

- [ ] TI1 no activa recálculo de rutas
- [ ] TI2 activa recálculo de rutas  
- [ ] TI3 activa recálculo de rutas
- [ ] Logs aparecen en backend para TI2/TI3
- [ ] Logs aparecen en frontend para TI2/TI3
- [ ] Llamadas de red correctas según tipo
- [ ] Camión se marca como averiado en todos los casos
- [ ] Manejo de errores funciona correctamente
- [ ] Toasts informativos aparecen correctamente
- [ ] Contexto se actualiza tras recálculo

## Notas de Debugging

### Si no funciona el recálculo:
1. Verificar que `gaTriggerQueue` está disponible en `com.plg.utils.Simulacion`
2. Verificar que no hay errores en consola del backend
3. Verificar que el algoritmo genético está ejecutándose
4. Verificar que no hay problemas de timing (aumentar timeout si necesario)

### Si no aparecen los toasts:
1. Verificar que react-toastify está importado correctamente
2. Verificar que no hay errores de JavaScript en consola
3. Verificar que los tipos de incidente se están comparando correctamente (`tipo === 2`)

### Si las rutas no se actualizan:
1. El recálculo sí puede estar funcionando
2. El algoritmo genético puede estar generando la misma solución
3. Esto es comportamiento normal y esperado en algunos casos
