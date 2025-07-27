# Componentes de Selección de Vista

Esta carpeta contiene los componentes modulares para la página de selección de vista del sistema de simulación logística.

## Estructura

```
seleccion-vista/
├── index.ts                    # Exportaciones principales
├── Header.tsx                  # Componente de encabezado
├── Footer.tsx                  # Componente de pie de página
├── StatusMessage.tsx           # Mensajes de estado
├── SimulacionCard.tsx          # Tarjeta individual de simulación
├── SimulacionGrid.tsx          # Grilla de tarjetas de simulación
├── InformacionSistema.tsx      # Información del sistema
├── GestionPedidos.tsx          # Gestión de pedidos
├── Bienvenida.tsx              # Mensaje de bienvenida
├── hooks/
│   └── useSeleccionVista.ts    # Hook personalizado para lógica
└── data/
    └── opcionesSimulacion.ts   # Datos de opciones de simulación
```

## Componentes

### Header
- **Propósito**: Encabezado reutilizable con logo y título
- **Props**: `titulo`, `version`
- **Uso**: Se muestra en la parte superior de la página

### Footer
- **Propósito**: Pie de página con información de copyright
- **Props**: `texto`
- **Uso**: Se muestra en la parte inferior de la página

### StatusMessage
- **Propósito**: Muestra mensajes de estado (éxito, error, info)
- **Props**: `mensaje` (objeto con texto y tipo)
- **Uso**: Se muestra cuando hay cambios de estado

### SimulacionCard
- **Propósito**: Tarjeta individual para cada opción de simulación
- **Props**: `opcion`, `cargando`, `onSeleccionar`
- **Uso**: Renderiza una opción de simulación con imagen, descripción y botón

### SimulacionGrid
- **Propósito**: Contenedor para la grilla de tarjetas de simulación
- **Props**: `opciones`, `cargando`, `onSeleccionar`
- **Uso**: Organiza las tarjetas en una grilla responsive

### InformacionSistema
- **Propósito**: Muestra características del sistema
- **Props**: `titulo`, `caracteristicas`
- **Uso**: Sección informativa sobre las capacidades del sistema

### GestionPedidos
- **Propósito**: Botón para navegar a la gestión de pedidos
- **Props**: `titulo`, `descripcion`, `textoBoton`, `onNavegar`
- **Uso**: Acceso rápido a la funcionalidad de pedidos

### Bienvenida
- **Propósito**: Mensaje de bienvenida al usuario
- **Props**: `titulo`, `descripcion`
- **Uso**: Se muestra en la parte superior del contenido

## Hooks

### useSeleccionVista
- **Propósito**: Maneja toda la lógica de selección de vista
- **Retorna**: `cargando`, `mensaje`, `handleSeleccionVista`, `handleNavegarAPedidos`
- **Uso**: Hook personalizado que encapsula la lógica de estado y navegación

## Datos

### opcionesSimulacion
- **Propósito**: Array con las opciones de simulación disponibles
- **Contenido**: Configuración de cada tipo de simulación (DIARIA, SEMANAL, COLAPSO)
- **Uso**: Datos centralizados para las opciones de simulación

## Beneficios de la Modularización

1. **Reutilización**: Los componentes pueden ser reutilizados en otras partes de la aplicación
2. **Mantenibilidad**: Cada componente tiene una responsabilidad específica
3. **Testabilidad**: Es más fácil escribir pruebas para componentes pequeños
4. **Legibilidad**: El código es más fácil de entender y navegar
5. **Escalabilidad**: Fácil agregar nuevas funcionalidades sin afectar otros componentes

## Uso

```tsx
import {
  Header,
  Footer,
  StatusMessage,
  SimulacionGrid,
  InformacionSistema,
  GestionPedidos,
  Bienvenida,
  useSeleccionVista,
  opcionesSimulacion
} from "../components/seleccion-vista";

const SeleccionVista: React.FC = () => {
  const {
    cargando,
    mensaje,
    handleSeleccionVista,
    handleNavegarAPedidos
  } = useSeleccionVista();

  return (
    <div className="bg-gradient-to-br from-gray-50 to-gray-100 w-screen h-screen flex flex-col">
      <Header />
      <div className="flex-1 flex flex-col justify-center items-center p-8">
        <Bienvenida />
        <StatusMessage mensaje={mensaje} />
        <SimulacionGrid
          opciones={opcionesSimulacion}
          cargando={cargando}
          onSeleccionar={handleSeleccionVista}
        />
        <InformacionSistema />
        <GestionPedidos onNavegar={handleNavegarAPedidos} />
      </div>
      <Footer />
    </div>
  );
};
``` 