interface ConfigSimulacion {
  periodoLlamado: number;
  pasoTiempo: number;
  nodosEquivalentes: number;
  intervaloActualizacion: number;
}

export const CONFIG_SIMULACION: ConfigSimulacion = {
  periodoLlamado: 10, // segundos 
  pasoTiempo: 30, // minutos
  nodosEquivalentes: 15, // nodos por segmento (menos = más lento)
  intervaloActualizacion: 100, // ms (más frecuente = más suave)
};
