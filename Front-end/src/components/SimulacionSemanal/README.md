# SimulacionSemanal Component

## Overview
The `SimulacionSemanal` component is a complete weekly simulation view that provides an interactive interface for managing trucks, monitoring deliveries, and visualizing route planning on a map.

## Architecture

### Clean Code Principles Applied
- **Single Responsibility**: Each component has a clear, single purpose
- **DRY (Don't Repeat Yourself)**: Shared logic extracted into custom hooks
- **Separation of Concerns**: UI, business logic, and side effects are properly separated
- **Component Composition**: Large component broken into smaller, reusable pieces

### Component Structure
```
SimulacionSemanal/
├── SimulationHeader.tsx     - Displays simulation time and status
├── ControlPanel.tsx         - Collapsible control panel for simulation
├── ElementHighlightIndicator.tsx - Shows selected elements on map
├── index.ts                 - Barrel exports
└── README.md               - This documentation
```

### Custom Hooks
```
hooks/useSimulacionSemanal.ts
├── useUIState()             - Manages UI state (panels, menus)
├── useElementSelection()    - Handles element selection and highlighting  
├── useSimulationData()      - Manages simulation data state
├── usePanelNavigation()     - Panel switching logic
├── useCamionRouteModal()    - Route modal event handling
└── useBottomMenuSync()      - Bottom menu state synchronization
```

## Features

### UI State Management
- **Responsive panels**: Left menu, right menu, bottom menu with smooth transitions
- **Panel switching**: Toggle between 'camiones' and 'bloqueos' views
- **Collapsible controls**: Expandable control panel for simulation management

### Element Selection & Highlighting
- **Visual feedback**: Animated indicators for selected elements
- **Clear actions**: Easy-to-access clear buttons for selections
- **Contextual display**: Different layouts for different panel types

### Error Handling
- **Error boundaries**: Catches and displays errors gracefully
- **Development aids**: Enhanced error information in development mode
- **Recovery options**: Retry mechanisms for failed states

### Accessibility (a11y)
- **ARIA labels**: Proper labels for interactive elements
- **Keyboard navigation**: All interactive elements are keyboard accessible
- **Screen reader support**: Semantic HTML and proper ARIA attributes

## Props & State

### No External Props
The component is self-contained and manages its own state internally.

### Internal State
```typescript
// UI State
menuExpandido: boolean
bottomMenuExpandido: boolean  
controlPanelExpandido: boolean
panel: 'camiones' | 'bloqueos'

// Selection State
camionSeleccionadoExterno: string | null
elementoResaltado: ElementoResaltado | null

// Simulation State  
tiempoTranscurrido: number
nodoActual: number
tiempoSimulado: Date | null
```

## Usage

```tsx
import SimulacionSemanal from './views/SimulacionSemanal';

function App() {
  return <SimulacionSemanal />;
}
```

## Development Notes

### Performance Considerations
- **Memoized components**: Child components should use React.memo where appropriate
- **Optimized re-renders**: State is properly separated to minimize unnecessary renders
- **Efficient event handling**: Event listeners are properly cleaned up

### Future Enhancements
- **Loading states**: Add loading indicators for async operations
- **Offline support**: Cache data for offline functionality  
- **Real-time updates**: WebSocket integration for live data
- **Export functionality**: Allow exporting simulation data

### Testing Strategy
- **Unit tests**: Test individual hooks and components
- **Integration tests**: Test component interactions
- **E2E tests**: Test complete user workflows
- **Accessibility tests**: Ensure a11y compliance

## Dependencies

### Core Dependencies
- React 18+
- Lucide React (icons)
- Tailwind CSS (styling)

### Internal Dependencies
- `../components/Mapa`
- `../components/Navbar`
- `../components/BloqueosTable`
- `../components/RightMenu`
- `../components/BottomMenu`

## Troubleshooting

### Common Issues
1. **Missing props on Mapa**: Ensure all required props are passed with default empty arrays
2. **Event listener conflicts**: Check for multiple event listeners on same elements
3. **State sync issues**: Verify useEffect dependencies are correct

### Debug Tips
- Use React DevTools to inspect component state
- Check browser console for error messages
- Verify network requests in Network tab
- Test accessibility with screen reader tools
