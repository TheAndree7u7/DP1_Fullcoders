# Orden de Ejecución y Almacenamiento - Front-end

## 1. Inicio de la Aplicación (npm run dev)

### 1.1. Carga de Configuración
1. **`package.json`**
   - Carga de dependencias npm
   - Scripts de ejecución
   - Configuración de Vite

2. **`vite.config.ts`**
   - Configuración del servidor de desarrollo
   - Configuración de plugins
   - Configuración de alias
   - Configuración de proxy

3. **`tsconfig.json`**
   - Configuración de TypeScript
   - Configuración de paths
   - Configuración de compilación

### 1.2. Inicialización de Componentes
1. **`main.tsx`**
   - Punto de entrada de la aplicación
   - Inicialización de React
   - Montaje del árbol de componentes

2. **`App.tsx`**
   - Componente raíz
   - Configuración de rutas
   - Inicialización de contextos

3. **Contextos (`context/`)**
   - `SimulacionContext.tsx`
     - Estado global de simulación
     - Configuración de providers
     - Inicialización de valores

4. **Servicios (`services/`)**
   - `simulacionApiService.ts`
     - Configuración de axios
     - Interceptores
     - Manejo de errores

## 2. Flujo de Ejecución en Runtime

### 2.1. Carga de Vistas
1. **`views/SimulacionSemanal.tsx`**
   - Carga de datos iniciales
   - Inicialización de componentes
   - Configuración de estado

2. **Componentes (`components/`)**
   - `Mapa.tsx`
     - Carga de datos geográficos
     - Inicialización de mapa
     - Configuración de marcadores

   - `TablaPedidos.tsx`
     - Carga de datos de pedidos
     - Configuración de paginación
     - Inicialización de filtros

   - `CardCamion.tsx`
     - Carga de datos de camiones
     - Configuración de estados
     - Inicialización de eventos

### 2.2. Ciclos de Vida de Componentes

#### Simulación
1. Inicialización en `SimulacionSemanal.tsx`
2. Carga de datos del contexto
3. Actualización de estado
4. Renderizado de componentes
5. Manejo de eventos

#### Mapa
1. Carga en `Mapa.tsx`
2. Inicialización de librería de mapas
3. Carga de marcadores
4. Configuración de eventos
5. Actualización de rutas

#### Pedidos
1. Carga en `TablaPedidos.tsx`
2. Inicialización de datos
3. Configuración de paginación
4. Manejo de filtros
5. Actualización de estado

## 3. Almacenamiento de Datos

### 3.1. Estado Global
- **Contexto de Simulación**
  1. Estado de simulación
  2. Datos de pedidos
  3. Datos de camiones
  4. Configuraciones

### 3.2. Estado Local
- **Componentes**
  1. Estado de UI
  2. Datos temporales
  3. Configuraciones locales

### 3.3. Almacenamiento Persistente
- **LocalStorage**
  1. Configuraciones de usuario
  2. Preferencias de UI
  3. Datos de sesión

- **SessionStorage**
  1. Datos temporales
  2. Estado de formularios
  3. Datos de navegación

## 4. Ciclos de Actualización

### 4.1. Actualización de UI
1. Cambios de estado
2. Re-renderizado de componentes
3. Actualización de DOM
4. Aplicación de estilos

### 4.2. Actualización de Datos
1. Peticiones al backend
2. Actualización de contexto
3. Propagación de cambios
4. Actualización de UI

## 5. Manejo de Eventos

### 5.1. Eventos de Usuario
1. Clicks
2. Inputs
3. Formularios
4. Navegación

### 5.2. Eventos del Sistema
1. Cambios de ruta
2. Actualizaciones de estado
3. Eventos de red
4. Eventos de temporizador

## 6. Cierre de Aplicación

### 6.1. Limpieza
1. Desmontaje de componentes
2. Limpieza de event listeners
3. Liberación de recursos
4. Cierre de conexiones

### 6.2. Persistencia
1. Guardado de estado
2. Limpieza de caché
3. Cierre de sesiones
4. Guardado de preferencias 