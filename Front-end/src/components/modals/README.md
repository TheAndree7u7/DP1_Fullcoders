# Componentes Modales

Esta carpeta contiene los componentes modales reutilizables de la aplicación.

## ModalCamion.tsx

Componente modal para mostrar información detallada de un camión seleccionado.

### Características:

- **Información del camión**: Muestra todos los datos relevantes del camión
- **Colores según leyenda**: 
  - **GLP**: Colores según el porcentaje de capacidad (verde >75%, amarillo 40-75%, naranja <40%)
  - **Combustible**: Mismos colores que GLP según el porcentaje
- **Botones de avería**: Permite simular averías de diferentes tipos
- **Botón de ruta**: Muestra la ruta del camión en el mapa

### Props:

```typescript
interface ModalCamionProps {
  clickedCamion: string | null;
  camiones: any[];
  rutasCamiones: any[];
  averiando: string | null;
  onClose: () => void;
  onAveriar: (camionId: string, tipoAveria: number, ...) => void;
  // ... otras props para funcionalidad de averías
}
```

### Uso:

```tsx
<ModalCamion
  clickedCamion={clickedCamion}
  camiones={camiones}
  rutasCamiones={rutasCamiones}
  averiando={averiando}
  onClose={() => setClickedCamion(null)}
  onAveriar={handleAveriar}
  // ... otras props
/>
```

### Colores de la leyenda:

- **Verde (#22c55e)**: >75% de capacidad
- **Amarillo (#eab308)**: 40-75% de capacidad  
- **Naranja (#f97316)**: <40% de capacidad
- **Azul (#3b82f6)**: 100% (inicio, lleno) 