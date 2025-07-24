# API de Archivos de Pedidos

## Descripción

El `ArchivosController` proporciona endpoints para procesar archivos de pedidos y agregarlos al sistema. Soporta tanto archivos completos como pedidos individuales.

## Endpoints

### 1. Procesar Archivo de Pedidos

**POST** `/api/archivos/pedidos`

Procesa un archivo completo de pedidos y los agrega al sistema.

#### Request Body

```json
{
  "nombre": "ventas202507.txt",
  "contenido": "01d00h24m:16,13,c-198,3m3,4h\n01d00h48m:5,18,c-12,9m3,17h\n01d01h12m:63,13,c-83,2m3,9h",
  "datos": [
    {
      "fechaHora": "01d00h24m",
      "coordenadaX": 16,
      "coordenadaY": 13,
      "codigoCliente": "c-198",
      "volumenGLP": 3,
      "horasLimite": 4
    },
    {
      "fechaHora": "01d00h48m",
      "coordenadaX": 5,
      "coordenadaY": 18,
      "codigoCliente": "c-12",
      "volumenGLP": 9,
      "horasLimite": 17
    }
  ]
}
```

#### Response

```json
{
  "nombreArchivo": "ventas202507.txt",
  "totalPedidosAgregados": 2,
  "pedidosAgregados": [
    {
      "codigo": "PEDIDO-13-16",
      "coordenada": {
        "fila": 13,
        "columna": 16
      },
      "horasLimite": 4.0,
      "volumenGLPAsignado": 3.0,
      "estado": "REGISTRADO",
      "fechaRegistro": "2025-07-01T00:24:00",
      "fechaLimite": "2025-07-01T04:24:00"
    }
  ],
  "mensaje": "Archivo 'ventas202507.txt' procesado exitosamente. 2 pedidos agregados."
}
```

### 2. Procesar Pedidos Individuales

**POST** `/api/archivos/pedidos/individuales`

Procesa pedidos individuales usando los datos parseados.

#### Request Body

Mismo formato que el endpoint anterior.

#### Response

Mismo formato que el endpoint anterior.

## Validaciones

### Nombre del Archivo
- Debe seguir el formato: `ventasYYYYMM.txt`
- Ejemplo: `ventas202507.txt`
- Se extrae automáticamente el año y mes para actualizar los parámetros globales

### Contenido del Archivo
- Cada línea debe seguir el formato: `DDdHHhMMm:X,Y,c-NUMERO,VOLUMENm3,HORASh`
- Ejemplo: `01d00h24m:16,13,c-198,3m3,4h`

### Datos de Ventas
- `fechaHora`: Formato DDdHHhMMm
- `coordenadaX`: Número entero (0-69)
- `coordenadaY`: Número entero (0-49)
- `codigoCliente`: Formato c-NUMERO
- `volumenGLP`: Número entero positivo
- `horasLimite`: Número entero ≥ 4

## Funcionalidades

### 1. Validación Automática
- Valida el formato del nombre del archivo
- Valida el contenido de cada línea
- Maneja errores de formato sin detener el proceso completo

### 2. Actualización de Parámetros
- Extrae año y mes del nombre del archivo
- Actualiza `Parametros.anho` y `Parametros.mes`
- Mantiene consistencia con el sistema existente

### 3. Integración con DataLoader
- Agrega pedidos al `Parametros.dataLoader.pedidos`
- Utiliza el `PedidoFactory` existente
- Mantiene compatibilidad con el sistema actual

### 4. Manejo de Errores
- Errores de validación: HTTP 400
- Errores internos: HTTP 500
- Errores de formato: Se registran pero no detienen el proceso

## Ejemplo de Uso

### cURL

```bash
curl -X POST http://localhost:8080/api/archivos/pedidos \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

### JavaScript/Fetch

```javascript
const response = await fetch('/api/archivos/pedidos', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    nombre: 'ventas202507.txt',
    contenido: '01d00h24m:16,13,c-198,3m3,4h',
    datos: [
      {
        fechaHora: '01d00h24m',
        coordenadaX: 16,
        coordenadaY: 13,
        codigoCliente: 'c-198',
        volumenGLP: 3,
        horasLimite: 4
      }
    ]
  })
});

const result = await response.json();
console.log(result);
```

## Códigos de Estado HTTP

- **201 Created**: Archivo procesado exitosamente
- **400 Bad Request**: Error de validación en la solicitud
- **500 Internal Server Error**: Error interno del servidor

## Archivos Creados

1. **`ArchivosController.java`**: Controlador REST para manejar archivos
2. **`ArchivosService.java`**: Servicio con la lógica de negocio
3. **`ArchivoPedidosRequest.java`**: DTO para la solicitud
4. **`ArchivoPedidosResponse.java`**: DTO para la respuesta
5. **`DatosVentas.java`**: DTO para los datos de ventas

## Integración con el Sistema Existente

- Utiliza `PedidoFactory.crearPedido()` para crear pedidos
- Actualiza `Parametros.dataLoader.pedidos` para mantener consistencia
- Utiliza `PedidoRepository` para persistencia
- Mantiene compatibilidad con el sistema de validaciones existente 