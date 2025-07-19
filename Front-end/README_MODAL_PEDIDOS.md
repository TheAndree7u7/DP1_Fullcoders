# Modal para Agregar Pedidos

## Descripción

Se ha implementado un modal para agregar pedidos en la sección de pedidos del RightMenu. Este modal permite agregar pedidos de dos formas:

1. **Pedido Individual**: Permite crear un pedido ingresando todos los datos manualmente
2. **Archivo de Pedidos**: Permite cargar un archivo con múltiples pedidos

## Funcionalidades

### Pedido Individual

El formulario incluye los siguientes campos:

- **Fecha y Hora**: Año, mes, día, hora y minutos
- **Coordenadas**: Coordenada X y Coordenada Y
- **Cliente**: Nombre del cliente
- **Volumen GLP**: Cantidad de GLP a entregar (en m³)
- **Horas Límite**: Tiempo límite para la entrega (en horas)

**Nota**: Los pedidos individuales se procesan como archivos con una sola línea, generando automáticamente el nombre del archivo basado en el año y mes ingresados.

### Archivo de Pedidos

- **Validación**: El archivo se valida automáticamente usando el validador existente
- **Formato**: Debe seguir el formato `fechaHora:coordenadaX,coordenadaY,codigoCliente,volumenGLP,horasLimite`
- **Ejemplo**: Se puede descargar un archivo de ejemplo para ver el formato correcto

## Formato del Archivo

### Nombre del Archivo
El archivo debe tener el siguiente formato de nombre:
```
ventasYYYYMM.txt
```

Donde:
- `YYYY`: Año (ej: 2025)
- `MM`: Mes con dos dígitos (ej: 01 para enero, 12 para diciembre)

**Ejemplos:**
- `ventas202501.txt` (enero 2025)
- `ventas202512.txt` (diciembre 2025)

### Contenido del Archivo
Cada línea del archivo debe tener el siguiente formato:

```
DDdHHhMMm:X,Y,c-NUMERO,VOLUMENm3,HORASh
```

Donde:
- `DDdHHhMMm`: Día, hora y minutos (ej: 01d00h24m)
- `X,Y`: Coordenadas del pedido
- `c-NUMERO`: Código del cliente (ej: c-198)
- `VOLUMENm3`: Volumen de GLP (ej: 3m3)
- `HORASh`: Horas límite (ej: 4h)

## Formato JSON de Salida

Tanto para pedidos individuales como para archivos, el sistema genera un objeto JSON con la siguiente estructura:

```json
{
  "nombre": "ventas202507.txt",
  "contenido": "01d00h24m:16,13,c-198,3m3,4h",
  "datos": [
    {
      "fechaHora": "01d00h24m",
      "coordenadaX": 16,
      "coordenadaY": 13,
      "codigoCliente": "c-198",
      "volumenGLP": 3,
      "horasLimite": 4
    }
  ]
}
```

### Componentes del JSON:
- **`nombre`**: Nombre del archivo generado (formato: `ventasYYYYMM.txt`)
- **`contenido`**: Contenido del archivo en formato texto
- **`datos`**: Array de objetos `DatosVentas` parseados

### Ejemplo de archivo válido:

```
01d00h24m:16,13,c-198,3m3,4h
01d00h48m:5,18,c-12,9m3,17h
01d01h12m:63,13,c-83,2m3,9h
01d01h35m:4,6,c-37,2m3,16h
01d01h59m:54,16,c-115,9m3,5h
```

## Validaciones

### Para Pedidos Individuales:
- El nombre del cliente es obligatorio
- El volumen GLP debe ser mayor a 0
- Las horas límite deben ser mayor a 0
- Las coordenadas deben ser números válidos

### Para Archivos:
- **Nombre del archivo**: Debe seguir el formato `ventasYYYYMM.txt`
- Cada línea debe seguir el formato especificado
- Las fechas deben tener el formato correcto (DDdHHhMMm)
- Los códigos de cliente deben seguir el patrón c-NUMERO
- Los volúmenes deben incluir "m3" al final
- Las horas deben incluir "h" al final

## Uso

1. En el RightMenu, selecciona la pestaña "Pedidos"
2. Haz clic en el botón "Agregar Pedidos" (ícono +)
3. Selecciona el modo deseado:
   - **Pedido Individual**: Completa el formulario y haz clic en "Agregar Pedido"
   - **Archivo de Pedidos**: Descarga el ejemplo, completa tu archivo y súbelo

### Generación del Nombre del Archivo

- **Para pedidos individuales**: El nombre se genera automáticamente usando el año y mes ingresados en el formulario
- **Para archivos**: El nombre debe seguir el formato `ventasYYYYMM.txt` y se valida antes de procesar

## Archivos Modificados

- `Front-end/src/components/ModalAgregarPedidos.tsx` - Nuevo componente modal
- `Front-end/src/components/TablaPedidos.tsx` - Agregado botón y lógica del modal

## Integración

El modal está integrado con:
- Los validadores existentes (`validadores.ts`)
- Los ejemplos existentes (`ejemplos.ts`)
- Los tipos de datos existentes (`types.ts`)

## Próximos Pasos

Para completar la funcionalidad, se necesita:
1. Implementar la lógica real de agregar pedidos al backend
2. Conectar con el contexto de simulación para actualizar la lista de pedidos
3. Agregar notificaciones de éxito/error
4. Implementar persistencia de datos 