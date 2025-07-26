package com.plg.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.plg.entity.Averia;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
import com.plg.repository.AveriaRepository;
import com.plg.utils.ExcepcionesPerzonalizadas.ResourceNotFoundException;

public class Herramientas {
    // Método genérico para leer todas las líneas de un archivo de recursos

    

    public static List<String> readAllLines(String resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("La ruta del recurso no puede ser nula o vacía.");
        }
        try (InputStream inputStream = Herramientas.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("El archivo de recurso no se encontró en la ruta: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new IOException("Error al leer el archivo de recurso: " + resourcePath, e);
        }
    }

    // Método genérico para leer fechas del siguiente formato ##d##h##m
    public static LocalDateTime readFecha(String fecha) {
        String[] partes = fecha.split("[dhm]");
        DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String anho = Parametros.anho;
        String mes = Parametros.mes;
        String dia = partes[0];
        String hora = partes[1];
        String minutos = partes[2];

        LocalDateTime fechaInicial = LocalDateTime.parse(dia + "/" + mes + "/" + anho + " " + hora + ":" + minutos,
                formatoFechaHora);

        return fechaInicial;
    }

    public static LocalDateTime fechaNameArchivo(String file_name) {
        String mes = file_name.substring(10, 12);
        String anho = file_name.substring(6, 10);
        DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return LocalDateTime.parse("01/" + mes + "/" + anho + " 00:00", formatoFechaHora);
    }

    // !DETECTA EN QUE TURNO SE ENCUENTRA UNA FECHA (son 3 turnos) de 00:00 a 08:00,
    // 08:00 a 16:00, 16:00 a 00:00
    // ! RETORNA 1, 2 O 3
    public static int detectarTurno(LocalDateTime fecha) {
        LocalTime hora = fecha.toLocalTime();
        if (hora.isBefore(LocalTime.of(8, 0))) {
            return 1;
        } else if (hora.isBefore(LocalTime.of(16, 0))) {
            return 2;
        } else {
            return 3;
        }
    }

    public static void  agregarAveriasAutomaticas(List<Averia> averiasAutomaticas, List<Gen> cromosoma, 
            LocalDateTime fechaHoraInicioIntervalo) {

        int turno = Herramientas.detectarTurno(fechaHoraInicioIntervalo);
        if(Parametros.turnoSistema != turno) {
            Parametros.turnoSistema = turno;
        }else{
            Parametros.dataLoader.camionesAveriados.clear();
            return;
        }
        List<Averia> averiasAutomaticasTurno = averiasAutomaticas.stream()
                .filter(averia -> averia.getTurnoOcurrencia() == turno)
                .toList();
        List<CamionAveriaAuxiliar> camiones_para_averia = new ArrayList<>();
        for(Averia averia : averiasAutomaticasTurno) {
            Gen gen = cromosoma.stream()
                    .filter(g -> g.getCamion().getCodigo().equals(averia.getCamion().getCodigo()))
                    .findFirst()
                    .orElse(null);
            if (gen != null) {
                Camion camion = gen.getCamion();
                if (camion.getEstado() == EstadoCamion.DISPONIBLE) {
                    int posicionAveria = gen.colocar_nodo_de_averia_automatica(averia);
                    if (posicionAveria == -1) {
                        continue; // No se pudo colocar la avería
                    }
                    AveriaRepository.save(averia);
                    camiones_para_averia.add(new CamionAveriaAuxiliar(camion, averia, posicionAveria));
                }
            }
        }
        Parametros.dataLoader.camionesAveriados = camiones_para_averia;
    }

    public static class CamionAveriaAuxiliar{
        private Camion camion;
        private Averia averia;
        private int posicionAveria;
        public CamionAveriaAuxiliar(Camion camion, Averia averia, int posicionAveria) {
            this.camion = camion;
            this.averia = averia;
            this.posicionAveria = posicionAveria;
        }
        public Camion getCamion() {
            return camion;
        }
        public int getPosicionAveria() {
            return posicionAveria;
        }
        public Averia getAveria() {
            return averia;
        }
    }

      public static CamionAveriaAuxiliar obtenerCamionAveriaAuxiliar(Camion camion){
        return Parametros.dataLoader.camionesAveriados.stream()
                .filter(camionAveriado -> camionAveriado.getCamion().getCodigo().equals(camion.getCodigo()))
                .findFirst()
                .orElse(null);
    }

}

    
