# Simulation Context Architecture

## Overview
This directory implements a robust, production-ready context architecture following MVVM principles for managing simulation state across the application.

## Architecture Principles

### ✅ Clean Code & MVVM Pattern
- **Model (State)**: Pure state interfaces and reducers in `SimulacionContext.tsx`
- **ViewModel (Logic)**: Custom hooks in `/hooks` for business logic
- **View (UI)**: Components consume context through specialized hooks

### ✅ Separation of Concerns
- **Context Definition**: `SimulacionContextDefinition.ts` - Pure context creation
- **Context Logic**: `SimulacionContext.tsx` - State management and provider
- **Context Hooks**: `/hooks/useSimulacionContext.ts` - Business logic abstractions

## File Structure

```
context/
├── SimulacionContext.tsx           # Main context with state management
├── SimulacionContextDefinition.ts  # Context definition (fast refresh compatible)
└── simulacion/
    └── utils/
        └── tiempo.ts               # Time utilities

hooks/
└── useSimulacionContext.ts        # Custom hooks for consuming context
```

## State Management

### SimulacionState Interface
```typescript
interface SimulacionState {
  // Time & Simulation Status
  tiempoTranscurridoSimulado: number;    // Elapsed time in seconds
  tiempoRealSimulacion: Date | null;      // Real simulation time
  fechaHoraSimulacion: string | null;     // ISO timestamp
  horaActual: number;                     // Current hour/node
  
  // Execution State
  isRunning: boolean;                     // Is simulation running
  isPaused: boolean;                      // Is simulation paused
  speed: number;                          // Simulation speed multiplier
  
  // Loading & Error States
  isLoading: boolean;                     // Loading state
  error: string | null;                   // Error message
}
```

### Actions Interface
```typescript
interface SimulacionActions {
  startSimulation: () => void;
  pauseSimulation: () => void;
  stopSimulation: () => void;
  resetSimulation: () => void;
  setSpeed: (speed: number) => void;
  updateTime: (tiempo: number) => void;
  setError: (error: string | null) => void;
  setLoading: (loading: boolean) => void;
}
```

## Custom Hooks

### Primary Hook
```typescript
const useSimulacion = (): SimulacionContextType
```
Main hook providing complete context access.

### Specialized Hooks
```typescript
const useSimulationState = () => { /* state only */ }
const useSimulationControls = () => { /* actions only */ }
```
Focused hooks for specific functionality.

## Usage Examples

### In Components
```tsx
import { useSimulacion } from '../hooks/useSimulacionContext';

const MyComponent: React.FC = () => {
  const { 
    tiempoTranscurridoSimulado, 
    startSimulation, 
    isRunning 
  } = useSimulacion();
  
  return (
    <div>
      <p>Time: {tiempoTranscurridoSimulado}s</p>
      <button onClick={startSimulation}>
        {isRunning ? 'Stop' : 'Start'}
      </button>
    </div>
  );
};
```

### With Specialized Hooks
```tsx
import { useSimulationState, useSimulationControls } from '../hooks/useSimulacionContext';

const TimeDisplay: React.FC = () => {
  const { tiempoTranscurridoSimulado, isRunning } = useSimulationState();
  return <div>{tiempoTranscurridoSimulado}s</div>;
};

const ControlPanel: React.FC = () => {
  const { startSimulation, pauseSimulation } = useSimulationControls();
  return (
    <div>
      <button onClick={startSimulation}>Start</button>
      <button onClick={pauseSimulation}>Pause</button>
    </div>
  );
};
```

## Features

### ✅ State Management
- **Reducer pattern** for predictable state updates
- **Immutable updates** for performance optimization
- **Type safety** with TypeScript interfaces

### ✅ Side Effects Management
- **Automatic timer** management for real-time updates
- **Memory leak prevention** with proper cleanup
- **Performance optimized** interval handling

### ✅ Error Handling
- **Graceful error states** with user-friendly messages
- **Loading states** for async operations
- **Error boundaries compatibility**

### ✅ Developer Experience
- **Fast Refresh** compatible architecture
- **TypeScript** fully typed interfaces
- **Separation of concerns** for maintainability

## State Flow

```
User Action → Component → Hook → Context → Reducer → State Update → Re-render
```

### Timer Flow
```
useSimulationTimer → setInterval → dispatch(UPDATE_TIME) → State Update
```

## Best Practices

### ✅ Do's
- Use specialized hooks for focused functionality
- Keep components pure and logic-free
- Handle loading and error states
- Use TypeScript interfaces for type safety

### ❌ Don'ts
- Don't access context directly in components
- Don't put UI logic in context
- Don't forget error handling
- Don't create multiple contexts for same domain

## Testing Strategy

### Unit Tests
- Test individual hooks in isolation
- Test reducer functions with different actions
- Test timer logic and cleanup

### Integration Tests
- Test component-context interactions
- Test state persistence across navigation
- Test error scenarios and recovery

### Performance Tests
- Monitor re-render frequency
- Test with large state objects
- Verify memory leak prevention

## Future Enhancements

### Planned Features
- **Persistence**: Save/restore simulation state
- **Real-time**: WebSocket integration for live updates
- **Optimization**: Memoization for expensive calculations
- **Analytics**: State change tracking and analytics

### Scalability
- **Multiple contexts**: Split by domain (UI, Data, Settings)
- **State normalization**: For complex nested data
- **Middleware**: Add logging, persistence middleware
- **Lazy loading**: Context splitting for code splitting

## Troubleshooting

### Common Issues

**Context not available error**
```
Error: useSimulacion debe usarse dentro de SimulacionProvider
```
**Solution**: Ensure component is wrapped with `<SimulacionProvider>`

**State not updating**
- Check reducer implementation
- Verify action dispatching
- Test with React DevTools

**Performance issues**
- Use specialized hooks instead of full context
- Add React.memo for expensive components
- Check for unnecessary re-renders

## Migration Guide

### From Old Context
1. Replace `useSimulacion` imports
2. Update state property names if changed
3. Test all simulation functionality
4. Verify error handling works

### Integration Steps
1. Wrap app with `SimulacionProvider`
2. Replace context imports in components
3. Update state access patterns
4. Test thoroughly in development
