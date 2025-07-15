import { createContext } from 'react';
import type { SimulacionContextType } from './SimulacionContext';

// Context creation - separated for fast refresh compatibility
export const SimulacionContext = createContext<SimulacionContextType | undefined>(undefined);
