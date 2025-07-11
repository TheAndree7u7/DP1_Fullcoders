import React, { createContext, useContext, useEffect, useState } from "react";
import { getMejorIndividuo, getMejorIndividuoPorFecha } from "../services/simulacionApiService";
import { getAlmacenes } from "../services/almacenApiService";
import type {
  Pedido,
  Individuo,
  Gen,
  Nodo,
  Almacen,
  Coordenada,
} from "../types";
import {
  calcularPesoCarga,
  calcularPesoCombinado,
  calcularConsumoGalones,
  calcularDistanciaMaxima,
} from "../types";

export interface CamionEstado {
  id: string;
  ubicacion: string;
  porcentaje: number;
  estado: string;
  capacidadActualGLP: number;
  capacidadMaximaGLP: number;
  combustibleActual: number;
  combustibleMaximo: number;
  distanciaMaxima: number;
  pesoCarga: number;
  pesoCombinado: number;
  tara: number;
  tipo: string;
  velocidadPromedio: number;
}

export interface RutaCamion {
  id: string;
  ruta: string[];
  puntoDestino: string;
  pedidos: Pedido[];
}

export interface Bloqueo {
  coordenadas: Coordenada[];
  fechaInicio: string;
  fechaFin: string;
}

type IndividuoConBloqueos = Individuo & {
  bloqueos?: Bloqueo[];
  almacenes?: Almacen[];
  fechaHoraSimulacion?: string;
};

interface TiempoRealContextType {
  // Estado de la simulación
  ejecutando: boolean;
  cargando: boolean;

  // Datos de simulación
  camiones: CamionEstado[];
  rutasCamiones: RutaCamion[];
  almacenes: Almacen[];
  bloqueos: Bloqueo[];

  // Tiempo
  tiempoReal: Date;
  fechaHoraSimulacion: string | null;
  horaSimulacion: string;
  tiempoTranscurrido: string;
  inicioEjecucion: Date | null;

  // Controles
  iniciar: () => void;
  pausar: () => void;
  reiniciar: () => void;

  // Métricas
  camionesCargados: number;
  pedidosPendientes: number;
  almacenesDisponibles: number;
}

const TiempoRealContext = createContext<TiempoRealContextType | null>(null);

export const TiempoRealProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  // Estados básicos
  const [ejecutando, setEjecutando] = useState<boolean>(false);
  const [cargando, setCargando] = useState<boolean>(false);
  const [tiempoReal, setTiempoReal] = useState<Date>(new Date());

  // Datos de simulación
  const [camiones, setCamiones] = useState<CamionEstado[]>([]);
  const [rutasCamiones, setRutasCamiones] = useState<RutaCamion[]>([]);
  const [almacenes, setAlmacenes] = useState<Almacen[]>([]);
  const [bloqueos, setBloqueos] = useState<Bloqueo[]>([]);

  // Tiempo de simulación
  const [fechaHoraSimulacion, setFechaHoraSimulacion] = useState<string | null>(
    null,
  );
  const [horaSimulacion, setHoraSimulacion] = useState<string>("00:00:00");
  const [tiempoTranscurrido, setTiempoTranscurrido] = useState<string>("00:00:00");
  const [inicioEjecucion, setInicioEjecucion] = useState<Date | null>(null);
  const [fechaInicioSimulacion, setFechaInicioSimulacion] = useState<Date | null>(null);


  // Actualización del tiempo real cada segundo
  useEffect(() => {
    const interval = setInterval(() => {
      setTiempoReal(new Date());
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  // Contador de tiempo transcurrido
  useEffect(() => {
    if (!inicioEjecucion || !ejecutando) return;

    const interval = setInterval(() => {
      const ahora = new Date();
      const diferencia = ahora.getTime() - inicioEjecucion.getTime();
      const segundosTotales = Math.floor(diferencia / 1000);
      
      const horas = Math.floor(segundosTotales / 3600);
      const minutos = Math.floor((segundosTotales % 3600) / 60);
      const segundos = segundosTotales % 60;
      
      const tiempoFormateado = `${horas.toString().padStart(2, "0")}:${minutos.toString().padStart(2, "0")}:${segundos.toString().padStart(2, "0")}`;
      setTiempoTranscurrido(tiempoFormateado);
    }, 1000);

    return () => clearInterval(interval);
  }, [inicioEjecucion, ejecutando]);

  // Función para calcular la fecha simulada basada en el tiempo transcurrido
  const calcularFechaSimulada = (): string => {
    // Crear fecha base forzando UTC: 01/01/2025 00:00:00
    const fechaBase = new Date(Date.UTC(2025, 0, 1, 0, 0, 0, 0)); // UTC
    
    // Si no hay inicio de ejecución, devolver la fecha base
    if (!inicioEjecucion) {
      return fechaBase.toISOString().slice(0, 19); // Formato: YYYY-MM-DDTHH:mm:ss
    }

    const ahora = new Date();
    const tiempoTranscurridoMs = ahora.getTime() - inicioEjecucion.getTime();
    
    // 1 segundo real = 3 minutos simulados
    // 1000ms real = 3 * 60 * 1000ms simulados = 180,000ms simulados
    const factorAceleracion = 180; // 1 segundo real = 180 segundos simulados
    const tiempoSimuladoMs = tiempoTranscurridoMs * factorAceleracion;
    
    const fechaSimulada = new Date(fechaBase.getTime() + tiempoSimuladoMs);
    return fechaSimulada.toISOString().slice(0, 19); // Formato: YYYY-MM-DDTHH:mm:ss
  };

  // Formatear hora de simulación basada en fechaHoraSimulacion
  useEffect(() => {
    if (fechaHoraSimulacion) {
      const fecha = new Date(fechaHoraSimulacion);
      const horaFormateada = fecha.toLocaleTimeString("es-ES", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
      });
      setHoraSimulacion(horaFormateada);
    }
  }, [fechaHoraSimulacion]);

  // Polling para nueva solución cada 10 segundos (equivale a 30 minutos simulados)
  useEffect(() => {
    if (!ejecutando || !inicioEjecucion) return;

    console.log("🔄 TIEMPO REAL: Iniciando polling para nuevas soluciones cada 10 segundos (30 min simulados)...");

    // Cargar datos inmediatamente al iniciar
    cargarDatosSimulacion();

    const interval = setInterval(async () => {
      try {
        console.log("🔍 TIEMPO REAL: Solicitando nueva solución (30 minutos simulados transcurridos)...");
        await cargarDatosSimulacion();
      } catch (error) {
        console.log("⚠️ TIEMPO REAL: Error al cargar nueva solución:", error);
      }
    }, 10 * 1000); // 10 segundos reales = 30 minutos simulados

    return () => {
      console.log("🛑 TIEMPO REAL: Deteniendo polling de nuevas soluciones");
      clearInterval(interval);
    };
  }, [ejecutando, inicioEjecucion]);

  // Actualización continua de posición de camiones para animación suave
  useEffect(() => {
    if (!ejecutando || !inicioEjecucion || rutasCamiones.length === 0) return;

    console.log("🎬 TIEMPO REAL: Iniciando animación continua de camiones...");

    const interval = setInterval(() => {
      // Actualizar solo la posición de los camiones existentes
      setCamiones(camionesActuales => {
        return camionesActuales.map(camion => {
          const ruta = rutasCamiones.find(r => r.id === camion.id);
          if (!ruta) return camion;

          // Calcular nuevo índice del nodo actual
          const ahora = new Date();
          const tiempoTranscurridoMs = ahora.getTime() - inicioEjecucion.getTime();
          const tiempoTranscurridoSegundos = tiempoTranscurridoMs / 1000;
          
          // Calcular en qué ciclo de 10 segundos estamos
          const tiempoEnCicloActual = tiempoTranscurridoSegundos % 10;
          
          // 25 nodos en 10 segundos = 2.5 nodos por segundo
          const nodosAvanzadosPorSegundo = 25 / 10;
          const nodosAvanzadosEnCiclo = tiempoEnCicloActual * nodosAvanzadosPorSegundo;
          
          const totalNodos = ruta.ruta.length;
          const nuevoIndice = Math.min(Math.floor(nodosAvanzadosEnCiclo), Math.min(24, totalNodos - 1)); // Máximo 24 nodos (índice 0-24)

          return {
            ...camion,
            porcentaje: nuevoIndice
          };
        });
      });
    }, 100); // Actualizar cada 100ms para animación más suave

    return () => {
      console.log("🛑 TIEMPO REAL: Deteniendo animación continua de camiones");
      clearInterval(interval);
    };
  }, [ejecutando, inicioEjecucion, rutasCamiones]);

  // Función para cargar datos de simulación
  const cargarDatosSimulacion = async () => {
    try {
      setCargando(true);
      
      // Calcular la fecha simulada basada en el tiempo transcurrido
      const fechaSimulada = calcularFechaSimulada();
      
      console.log("🔄 TIEMPO REAL: Cargando datos de simulación...");
      console.log("📅 TIEMPO REAL: Fecha simulada para endpoint:", fechaSimulada);
      console.log("🌐 TIEMPO REAL: URL que se llamaría:", `http://localhost:8085/api/simulacion/mejor?fecha=${fechaSimulada}`);

      // Llamar al nuevo endpoint con la fecha simulada
      let data: IndividuoConBloqueos;
      try {
        data = await getMejorIndividuoPorFecha(fechaSimulada) as IndividuoConBloqueos;
        console.log("✅ TIEMPO REAL: Respuesta del nuevo endpoint recibida");
      } catch (error) {
        console.log("⚠️ TIEMPO REAL: Error en nuevo endpoint, simulando respuesta:", error);
        // Simular respuesta vacía para que no falle la aplicación
        data = { cromosoma: [] } as IndividuoConBloqueos;
      }

      if (data && data.cromosoma && Array.isArray(data.cromosoma)) {
        console.log("✅ TIEMPO REAL: Datos recibidos:", data);

        // Actualizar fecha y hora
        if (data.fechaHoraSimulacion) {
          setFechaHoraSimulacion(data.fechaHoraSimulacion);
        }

        // Actualizar rutas
        const nuevasRutas: RutaCamion[] = data.cromosoma.map((gen: Gen) => ({
          id: gen.camion.codigo,
          ruta: gen.nodos.map(
            (n: Nodo) => `(${n.coordenada.x},${n.coordenada.y})`,
          ),
          puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
          pedidos: gen.pedidos,
        }));
        setRutasCamiones(nuevasRutas);

        // Actualizar estado de camiones
        const nuevosCamiones: CamionEstado[] = nuevasRutas.map((ruta) => {
          const gen = data.cromosoma.find(
            (g: Gen) => g.camion.codigo === ruta.id,
          );
          const camion = gen?.camion;

          // Calcular índice del nodo actual basado en tiempo transcurrido
          const calcularIndiceNodoActual = (rutaId: string): number => {
            if (!inicioEjecucion) return 0;
            
            const ahora = new Date();
            const tiempoTranscurridoMs = ahora.getTime() - inicioEjecucion.getTime();
            const tiempoTranscurridoSegundos = tiempoTranscurridoMs / 1000;
            
            // Calcular en qué ciclo de 10 segundos estamos
            const tiempoEnCicloActual = tiempoTranscurridoSegundos % 10;
            
            // 25 nodos en 10 segundos = 2.5 nodos por segundo
            const nodosAvanzadosPorSegundo = 25 / 10;
            const nodosAvanzadosEnCiclo = tiempoEnCicloActual * nodosAvanzadosPorSegundo;
            
            const totalNodos = ruta.ruta.length;
            if (totalNodos <= 1) return 0;
            
            // Retornar el índice del nodo actual (máximo: 24 nodos = índice 0-24)
            return Math.min(Math.floor(nodosAvanzadosEnCiclo), Math.min(24, totalNodos - 1));
          };

          return {
            id: ruta.id,
            ubicacion: ruta.ruta[0] || "(0,0)",
            porcentaje: calcularIndiceNodoActual(ruta.id),
            estado:
              camion?.estado === "DISPONIBLE" ? "Disponible" : "En Camino",
            capacidadActualGLP: camion?.capacidadActualGLP ?? 0,
            capacidadMaximaGLP: camion?.capacidadMaximaGLP ?? 0,
            combustibleActual: camion?.combustibleActual ?? 0,
            combustibleMaximo: camion?.combustibleMaximo ?? 0,
            distanciaMaxima: camion?.distanciaMaxima ?? 0,
            pesoCarga: camion?.pesoCarga ?? 0,
            pesoCombinado: camion?.pesoCombinado ?? 0,
            tara: camion?.tara ?? 0,
            tipo: camion?.tipo ?? "",
            velocidadPromedio: camion?.velocidadPromedio ?? 0,
          };
        });
        setCamiones(nuevosCamiones);

        // Actualizar bloqueos
        if (data.bloqueos) {
          setBloqueos(data.bloqueos);
        }

        // Actualizar almacenes si vienen en la respuesta
        if (data.almacenes && data.almacenes.length > 0) {
          setAlmacenes(data.almacenes);
        }

        console.log("✅ TIEMPO REAL: Datos actualizados exitosamente");
      } else {
        console.log("⏳ TIEMPO REAL: No hay datos disponibles aún");
      }
    } catch (error) {
      console.log("⚠️ TIEMPO REAL: Error al cargar datos:", error);
    } finally {
      setCargando(false);
    }
  };

  // Función para cargar almacenes
  const cargarAlmacenes = async () => {
    try {
      console.log("🏪 TIEMPO REAL: Cargando almacenes...");
      const data = await getAlmacenes();
      setAlmacenes(data);
      console.log("✅ TIEMPO REAL: Almacenes cargados:", data.length);
    } catch (error) {
      console.log("⚠️ TIEMPO REAL: Error al cargar almacenes:", error);
    }
  };

  // Controles de simulación
  const iniciar = () => {
    console.log("▶️ TIEMPO REAL: Iniciando simulación...");
    const ahora = new Date();
    setEjecutando(true);
    setInicioEjecucion(ahora);
    setTiempoTranscurrido("00:00:00");
    
    console.log("📅 TIEMPO REAL: Simulación iniciará desde las 00:00:00 del 01/01/2025");
    console.log("ℹ️ TIEMPO REAL: Simulación iniciada, esperando datos del nuevo endpoint...");
  };

  const pausar = () => {
    console.log("⏸️ TIEMPO REAL: Pausando simulación...");
    setEjecutando(false);
  };

  const reiniciar = () => {
    console.log("🔄 TIEMPO REAL: Reiniciando simulación...");
    setEjecutando(false);
    setInicioEjecucion(null);
    setFechaInicioSimulacion(null);
    setTiempoTranscurrido("00:00:00");
    setCamiones([]);
    setRutasCamiones([]);
    setBloqueos([]);
    setFechaHoraSimulacion(null);
    setHoraSimulacion("00:00:00");
    setTiempoReal(new Date());
  };

  // Métricas calculadas
  const camionesCargados = camiones.filter(
    (c) => c.estado === "En Camino",
  ).length;
  const pedidosPendientes = rutasCamiones.reduce(
    (total, ruta) => total + ruta.pedidos.length,
    0,
  );
  const almacenesDisponibles = almacenes.length;

  const value: TiempoRealContextType = {
    ejecutando,
    cargando,
    camiones,
    rutasCamiones,
    almacenes,
    bloqueos,
    tiempoReal,
    fechaHoraSimulacion,
    horaSimulacion,
    tiempoTranscurrido,
    inicioEjecucion,
    iniciar,
    pausar,
    reiniciar,
    camionesCargados,
    pedidosPendientes,
    almacenesDisponibles,
  };

  return (
    <TiempoRealContext.Provider value={value}>
      {children}
    </TiempoRealContext.Provider>
  );
};

export const useTiempoReal = (): TiempoRealContextType => {
  const context = useContext(TiempoRealContext);
  if (!context) {
    throw new Error("useTiempoReal debe usarse dentro de TiempoRealProvider");
  }
  return context;
};

