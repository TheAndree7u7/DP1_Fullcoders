import { createContext } from "react";
import type { PaqueteContextType } from "./PaqueteContext";

export const PaqueteContext = createContext<PaqueteContextType | undefined>(undefined);
