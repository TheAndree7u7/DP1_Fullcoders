import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { cambiarTipoSimulacion, type TipoSimulacion } from "../../../services/simulacionApiService";
import { OpcionSimulacion } from "../SimulacionCard";

export const useSeleccionVista = () => {
  const navigate = useNavigate();
  const [cargando, setCargando] = useState<string | null>(null);
  const [mensaje, setMensaje] = useState<{ texto: string; tipo: 'success' | 'error' | 'info' } | null>(null);

  const handleSeleccionVista = async (opcion: OpcionSimulacion) => {
    try {
      setCargando(opcion.id);
      setMensaje({ texto: `Configurando simulación ${opcion.titulo.toLowerCase()}...`, tipo: 'info' });

      // Cambiar el tipo de simulación en el backend
      const respuesta = await cambiarTipoSimulacion(opcion.tipoSimulacion as TipoSimulacion);
      
      if (respuesta.exito) {
        setMensaje({ 
          texto: `✅ ${respuesta.mensaje}`, 
          tipo: 'success' 
        });
        
        // Para simulación diaria, navegar inmediatamente sin esperar
        if (opcion.tipoSimulacion === 'DIARIA') {
          navigate(opcion.ruta);
        } else {
          // Para otras simulaciones, esperar un momento para mostrar el mensaje de éxito
          setTimeout(() => {
            navigate(opcion.ruta);
          }, 1000);
        }
      } else {
        setMensaje({ 
          texto: `❌ Error: ${respuesta.mensaje}`, 
          tipo: 'error' 
        });
      }
    } catch (error) {
      console.error("Error al cambiar tipo de simulación:", error);
      setMensaje({ 
        texto: `❌ Error al configurar la simulación: ${error instanceof Error ? error.message : 'Error desconocido'}`, 
        tipo: 'error' 
      });
    } finally {
      setCargando(null);
    }
  };

  const handleNavegarAPedidos = () => {
    navigate('/agregar-pedidos');
  };

  return {
    cargando,
    mensaje,
    handleSeleccionVista,
    handleNavegarAPedidos
  };
}; 