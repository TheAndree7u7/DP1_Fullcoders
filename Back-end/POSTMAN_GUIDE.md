# Guía de Endpoints para Postman

Este archivo resume las URL principales para probar el backend con Postman.
Todos los endpoints permiten **CORS** para facilitar las pruebas.

### Pedidos
- **GET /api/pedidos**  
  Lista todos los pedidos cargados en memoria.
- **GET /api/pedidos/resumen**
  Devuelve un resumen con el total y el número de pedidos por estado.
- **GET /api/pedidos/rango?inicio=YYYY-MM-DDTHH:MM&fin=YYYY-MM-DDTHH:MM**
  Lista los pedidos cuyo `fechaRegistro` se encuentra en el rango indicado.
- **POST /api/pedidos**  
  Crea un nuevo pedido. Ejemplo de cuerpo JSON:
  ```json
  {
    "x": 10,
    "y": 5,
    "volumenGLP": 50,
    "horasLimite": 8
  }
  ```

### Camiones
- **GET /api/camiones**  
  Lista los camiones existentes.
- **GET /api/camiones/resumen**  
  Muestra un resumen por tipo de camión.
- **POST /api/camiones**  
  Crea un camión. Cuerpo de ejemplo:
  ```json
  {
    "tipo": "TB",
    "operativo": true,
    "x": 12,
    "y": 8
  }
  ```

### Almacenes
- **GET /api/almacenes**  
  Devuelve la lista de almacenes.
- **GET /api/almacenes/resumen**  
  Resumen por tipo de almacén.
- **POST /api/almacenes**  
  Crea un almacén. Ejemplo:
  ```json
  {
    "tipo": "SECUNDARIO",
    "x": 20,
    "y": 20,
    "capacidadMaxGLP": 200,
    "capacidadMaxCombustible": 100
  }
  ```
