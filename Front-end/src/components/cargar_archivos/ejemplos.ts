import type { EjemploArchivo } from "../../types";

// Ejemplos de archivos
export const ejemplos: EjemploArchivo[] = [
  {
    nombre: "ventas202501.txt",
    descripcion: "Archivo de ventas/pedidos",
    tipo: "ventas",
    formato: "Formato: fechaHora:coordenadaX,coordenadaY,codigoCliente,volumenGLP,horasLimite",
    contenido: `01d00h24m:16,13,c-198,3m3,4h
01d00h48m:5,18,c-12,9m3,17h
01d01h12m:63,13,c-83,2m3,9h
01d01h35m:4,6,c-37,2m3,16h
01d01h59m:54,16,c-115,9m3,5h`
  },
  {
    nombre: "202501.bloqueos.txt",
    descripcion: "Archivo de bloqueos",
    tipo: "bloqueos",
    formato: "Formato: fechaInicio-fechaFin:coordenadas",
    contenido: `01d00h31m-01d21h35m:15,10,30,10,30,18
01d01h13m-01d20h38m:08,03,08,23,20,23
01d02h40m-01d22h32m:57,30,57,45
01d03h54m-01d21h25m:25,25,30,25,30,30,35,30
01d05h05m-01d21h37m:42,08,42,15,47,15,47,27,55,27`
  },
  {
    nombre: "camiones.txt",
    descripcion: "Archivo de camiones",
    tipo: "camiones",
    formato: "Formato: TIPO,cantidad (TIPO: TA, TB, TC, TD)",
    contenido: `TA,2
TB,4
TC,3
TD,2`
  },
  {
    nombre: "mantpreventivo.txt",
    descripcion: "Archivo de mantenimiento preventivo",
    tipo: "mantenimiento",
    formato: "Formato: aaaammdd:TTNN (ej: 20250401:TA01)",
    contenido: `20250401:TA01
20250403:TD01
20250405:TC01
20250407:TB01
20250410:TD02
20250413:TD03
20250416:TB02
20250419:TD04
20250422:TC02
20250425:TD05`
  }
];

// Función para descargar ejemplo
export const descargarEjemplo = (ejemplo: EjemploArchivo) => {
  const blob = new Blob([ejemplo.contenido], { type: 'text/plain' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = ejemplo.nombre;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}; 