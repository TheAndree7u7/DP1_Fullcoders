import React, { createContext, useContext, useEffect, useState } from "react";
import { getMejorIndividuo } from "../services/simulacionApiService";
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
    if (!fechaInicioSimulacion || !inicioEjecucion) {
      // Si no tenemos fecha de inicio, usar una fecha base
      const fechaBase = new Date('2024-07-08T06:00:00');
      return fechaBase.toISOString();
    }

    const ahora = new Date();
    const tiempoTranscurridoMs = ahora.getTime() - inicioEjecucion.getTime();
    
    // 1 segundo real = 3 minutos simulados
    // 1000ms real = 3 * 60 * 1000ms simulados = 180,000ms simulados
    const factorAceleracion = 180; // 1 segundo real = 180 segundos simulados
    const tiempoSimuladoMs = tiempoTranscurridoMs * factorAceleracion;
    
    const fechaSimulada = new Date(fechaInicioSimulacion.getTime() + tiempoSimuladoMs);
    return fechaSimulada.toISOString();
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

  // Función para cargar datos de simulación
  const cargarDatosSimulacion = async () => {
    try {
      setCargando(true);
      
      // Calcular la fecha simulada basada en el tiempo transcurrido
      const fechaSimulada = calcularFechaSimulada();
      
      console.log("🔄 TIEMPO REAL: Cargando datos de simulación...");
      console.log("📅 TIEMPO REAL: Fecha simulada para endpoint:", fechaSimulada);
      console.log("🌐 TIEMPO REAL: URL que se llamaría:", `http://localhost:8085/api/simulacion/obtenerMejorIndividuo?fecha=${fechaSimulada}`);

      // Llamar al nuevo endpoint con la fecha simulada
      let data: IndividuoConBloqueos;
      try {
        const response = await fetch(`http://localhost:8085/api/simulacion/obtenerMejorIndividuo?fecha=${fechaSimulada}`);
        
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        data = await response.json() as IndividuoConBloqueos;
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

          return {
            id: ruta.id,
            ubicacion: ruta.ruta[0] || "(0,0)",
            porcentaje: Math.floor(Math.random() * 100), // Progreso simulado
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
    
    // Establecer fecha de inicio de simulación (base para cálculos)
    if (!fechaInicioSimulacion) {
      const fechaBase = new Date('2024-07-08T06:00:00');
      setFechaInicioSimulacion(fechaBase);
      console.log("📅 TIEMPO REAL: Fecha base de simulación establecida:", fechaBase.toISOString());
    }

    // En tiempo real no cargamos almacenes automáticamente
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

