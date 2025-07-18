import type { ValidacionArchivo, DatosVentas, DatosBloqueo, DatosCamion, TipoCamion } from "../../types";

// Función para validar archivo de ventas
export const validarArchivoVentas = (contenido: string): ValidacionArchivo => {
  const lineas = contenido.trim().split('\n');
  const errores: string[] = [];
  const advertencias: string[] = [];
  const datosParseados: DatosVentas[] = [];

  lineas.forEach((linea, index) => {
    if (!linea.trim()) return;

    const partes = linea.split(':');
    if (partes.length !== 2) {
      errores.push(`Línea ${index + 1}: Formato incorrecto. Debe ser 'fechaHora:datos'`);
      return;
    }

    const fechaHora = partes[0].trim();
    const datos = partes[1].trim();

    // Validar formato de fecha
    if (!/^\d{2}d\d{2}h\d{2}m$/.test(fechaHora)) {
      errores.push(`Línea ${index + 1}: Formato de fecha incorrecto. Debe ser 'DDdHHhMMm'`);
      return;
    }

    // Validar datos
    const valores = datos.split(',');
    if (valores.length !== 5) {
      errores.push(`Línea ${index + 1}: Debe tener 5 valores separados por coma`);
      return;
    }

    const [x, y, cliente, volumen, horas] = valores;
    
    if (!/^\d+$/.test(x) || !/^\d+$/.test(y)) {
      errores.push(`Línea ${index + 1}: Coordenadas deben ser números`);
      return;
    }

    if (!/^c-\d+$/.test(cliente)) {
      errores.push(`Línea ${index + 1}: Código de cliente debe ser 'c-NUMERO'`);
      return;
    }

    if (!/^\d+m3$/.test(volumen)) {
      errores.push(`Línea ${index + 1}: Volumen debe ser 'NUMEROm3'`);
      return;
    }

    if (!/^\d+h$/.test(horas)) {
      errores.push(`Línea ${index + 1}: Horas límite debe ser 'NUMEROh'`);
      return;
    }

    datosParseados.push({
      fechaHora,
      coordenadaX: parseInt(x),
      coordenadaY: parseInt(y),
      codigoCliente: cliente,
      volumenGLP: parseInt(volumen.replace('m3', '')),
      horasLimite: parseInt(horas.replace('h', ''))
    });
  });

  return {
    esValido: errores.length === 0,
    errores,
    advertencias,
    datosParseados
  };
};

// Función para validar archivo de bloqueos
export const validarArchivoBloqueos = (contenido: string): ValidacionArchivo => {
  const lineas = contenido.trim().split('\n');
  const errores: string[] = [];
  const advertencias: string[] = [];
  const datosParseados: DatosBloqueo[] = [];

  lineas.forEach((linea, index) => {
    if (!linea.trim()) return;

    const partes = linea.split(':');
    if (partes.length !== 2) {
      errores.push(`Línea ${index + 1}: Formato incorrecto. Debe ser 'fechaInicio-fechaFin:coordenadas'`);
      return;
    }

    const fechas = partes[0].trim();
    const coordenadas = partes[1].trim();

    // Validar formato de fechas
    const fechasPartes = fechas.split('-');
    if (fechasPartes.length !== 2) {
      errores.push(`Línea ${index + 1}: Formato de fechas incorrecto. Debe ser 'fechaInicio-fechaFin'`);
      return;
    }

    const [fechaInicio, fechaFin] = fechasPartes;
    if (!/^\d{2}d\d{2}h\d{2}m$/.test(fechaInicio) || !/^\d{2}d\d{2}h\d{2}m$/.test(fechaFin)) {
      errores.push(`Línea ${index + 1}: Formato de fecha incorrecto. Debe ser 'DDdHHhMMm'`);
      return;
    }

    // Validar coordenadas
    const coordValores = coordenadas.split(',');
    if (coordValores.length < 2 || coordValores.length % 2 !== 0) {
      errores.push(`Línea ${index + 1}: Debe tener un número par de coordenadas (x,y)`);
      return;
    }

    const coordenadasArray: Array<{x: number, y: number}> = [];
    for (let i = 0; i < coordValores.length; i += 2) {
      const x = parseInt(coordValores[i]);
      const y = parseInt(coordValores[i + 1]);
      
      if (isNaN(x) || isNaN(y)) {
        errores.push(`Línea ${index + 1}: Coordenadas deben ser números`);
        return;
      }
      
      coordenadasArray.push({ x, y });
    }

    datosParseados.push({
      fechaInicio,
      fechaFin,
      coordenadas: coordenadasArray
    });
  });

  return {
    esValido: errores.length === 0,
    errores,
    advertencias,
    datosParseados
  };
}; 

// Función para validar archivo de camiones
export const validarArchivoCamiones = (contenido: string): ValidacionArchivo => {
  const lineas = contenido.trim().split('\n');
  const errores: string[] = [];
  const advertencias: string[] = [];
  const datosParseados: DatosCamion[] = [];
  const tiposVistos: string[] = [];

  lineas.forEach((linea, index) => {
    if (!linea.trim()) return;

    const partes = linea.split(',');
    if (partes.length !== 2) {
      errores.push(`Línea ${index + 1}: Formato incorrecto. Debe ser 'TIPO,cantidad'`);
      return;
    }

    const tipo = partes[0].trim();
    const cantidad = partes[1].trim();

    // Validar tipo de camión
    const tiposValidos = ['TA', 'TB', 'TC', 'TD'];
    if (tiposValidos.indexOf(tipo) === -1) {
      errores.push(`Línea ${index + 1}: Tipo de camión inválido. Debe ser TA, TB, TC o TD`);
      return;
    }

    // Validar que no se repita el tipo
    if (tiposVistos.indexOf(tipo) !== -1) {
      errores.push(`Línea ${index + 1}: El tipo ${tipo} ya fue definido anteriormente`);
      return;
    }

    // Validar cantidad
    const cantidadNum = parseInt(cantidad);
    if (isNaN(cantidadNum) || cantidadNum <= 0) {
      errores.push(`Línea ${index + 1}: La cantidad debe ser un número positivo`);
      return;
    }

    tiposVistos.push(tipo);

    // Crear un camión de ejemplo para cada tipo (los datos específicos se generarán en el backend)
    datosParseados.push({
      codigo: `${tipo}_EJEMPLO`,
      tipo: tipo as TipoCamion,
      coordenadaX: 0,
      coordenadaY: 0,
      capacidadMaximaGLP: 0,
      combustibleMaximo: 0,
      velocidadPromedio: 0
    });
  });

  // Verificar que se hayan definido todos los tipos requeridos
  const tiposRequeridos = ['TA', 'TB', 'TC', 'TD'];
  const tiposFaltantes = tiposRequeridos.filter(tipo => tiposVistos.indexOf(tipo) === -1);
  
  if (tiposFaltantes.length > 0) {
    advertencias.push(`Tipos de camión faltantes: ${tiposFaltantes.join(', ')}`);
  }

  return {
    esValido: errores.length === 0,
    errores,
    advertencias,
    datosParseados
  };
}; 