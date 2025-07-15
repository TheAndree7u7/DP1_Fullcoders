import { useCallback } from 'react';
import { useSimulacion } from './useSimulacionContext';
import type { Individuo } from '../types';

// ============================
// HOOK PARA ACTUALIZAR CONTEXTO DE SIMULACIÓN
// ============================

export const useSimulacionUpdater = () => {
  const { setCamiones, setRutasCamiones } = useSimulacion();

  const actualizarDesdeIndividuo = useCallback((paquete: Individuo) => {
    console.log("🔄 Actualizando contexto desde individuo:", {
      genes: paquete.cromosoma?.length || 0,
      cromosoma: paquete.cromosoma
    });

    if (paquete.cromosoma && paquete.cromosoma.length > 0) {
      console.log("📋 Detalle de genes:", paquete.cromosoma.map((gen, idx) => ({
        idx,
        camion: gen.camion?.codigo || 'sin codigo',
        pedidos: gen.pedidos?.length || 0,
        rutaFinal: gen.rutaFinal?.length || 0
      })));

      const camiones = paquete.cromosoma.map(gen => gen.camion);
      const rutasCamiones = paquete.cromosoma.map(gen => ({
        id: gen.camion.codigo,
        puntoDestino: `(${gen.destino.x},${gen.destino.y})`,
        ruta: gen.rutaFinal.map(nodo => `(${nodo.coordenada.x},${nodo.coordenada.y})`),
        pedidos: gen.pedidos
      }));

      console.log("✅ Datos extraídos:", {
        camiones: camiones.length,
        rutasCamiones: rutasCamiones.length,
        camionesSample: camiones.slice(0, 2).map(c => ({ codigo: c.codigo, coordenada: c.coordenada })),
        rutasSample: rutasCamiones.slice(0, 2).map(r => ({ id: r.id, rutaLength: r.ruta.length }))
      });

      console.log("🔄 Llamando setCamiones y setRutasCamiones...");
      setCamiones(camiones);
      setRutasCamiones(rutasCamiones);

      console.log("✅ Contexto actualizado correctamente");
    } else {
      console.log("❌ No hay genes en el cromosoma");
    }
  }, [setCamiones, setRutasCamiones]);

  return { actualizarDesdeIndividuo };
};
