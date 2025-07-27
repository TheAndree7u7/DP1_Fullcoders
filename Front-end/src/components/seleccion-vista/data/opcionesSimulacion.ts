import semanalImg from "../../../assets/semanal.svg";
import diarioImg from "../../../assets/diario.svg";
import colapsoImg from "../../../assets/colapso.svg";
import type { OpcionSimulacion } from "../SimulacionCard";

export const opcionesSimulacion: OpcionSimulacion[] = [
  {
    id: "tiempo-real",
    titulo: "Ejecución en Tiempo Real",
    descripcion: "Simulación diaria que inicia automáticamente con la fecha y hora actual, mostrando la operación logística en tiempo real",
    ruta: "/carga-simulacion-diaria",
    imagen: diarioImg,
    color: "from-blue-500 to-blue-600",
    icono: "⚡",
    tipoSimulacion: "DIARIA"
  },
  {
    id: "semanal",
    titulo: "Simulación Semanal",
    descripcion: "Vista semanal completa del sistema logístico con análisis de rendimiento y métricas",
    ruta: "/simulacion-semanal",
    imagen: semanalImg,
    color: "from-green-500 to-green-600",
    icono: "📊",
    tipoSimulacion: "SEMANAL"
  },
  {
    id: "colapso",
    titulo: "Colapso Logístico",
    descripcion: "Simulación de escenarios de colapso para análisis de contingencia y planificación",
    ruta: "/colapso-logistico",
    imagen: colapsoImg,
    color: "from-red-500 to-red-600",
    icono: "🚨",
    tipoSimulacion: "COLAPSO"
  }
]; 