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

import com.plg.dto.CamionDto;
import com.plg.dto.GenDto;
import com.plg.entity.Averia;
import com.plg.entity.Camion;
import com.plg.entity.EstadoCamion;
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
    public static int detectarTurno(int hora) {
        if (hora >= 0 && hora < 8) {
            return 1;
        } else if (hora >= 8 && hora < 16) {
            return 2;
        } else if (hora >= 16 && hora < 24) {
            return 3;
        }
        return 0;
    }

    public static void agregarAveriasAutomaticas(List<Averia> averiasAutomaticas, List<Gen> cromosoma,
            LocalDateTime fechaHoraInicioIntervalo, LocalDateTime fechaHoraFinIntervalo) {
        //
        // sacar la fecha en medio del intervalo de simulacion
        System.out.println("----AVERIAS AUTOMATICAS--------------------------------");
        // !Obtener el turno de la fecha en medio del intervalo de simulacion
        int hora_inicio_intervalo = fechaHoraInicioIntervalo.getHour();
        int hora_fin_intervalo = fechaHoraFinIntervalo.getHour();
        int hora_medio_intervalo = (hora_inicio_intervalo + hora_fin_intervalo) / 2;

        int turno = Herramientas.detectarTurno(hora_medio_intervalo);
        System.out.println("Turno: " + turno);
        // ! Prevenir que se agreguen averias en el mismo turno
        switch (turno) {
            case 1:
                if (Parametros.averio_turno_1 == true) {
                    System.out.println("⚠️ Ya se aplicaron averías automáticas en el turno 1. Saltando...");
                    Parametros.averio_turno_2 = false;
                    return;
                }
                Parametros.averio_turno_1 = true;
                System.out.println("✅ Avería automática configurada para turno 1");
                break;
            case 2:
                if (Parametros.averio_turno_2 == true) {
                    System.out.println("⚠️ Ya se aplicaron averías automáticas en el turno 2. Saltando...");
                    Parametros.averio_turno_3 = false;
                    return;
                }
                Parametros.averio_turno_2 = true;
                System.out.println("✅ Avería automática configurada para turno 2");
                break;
            case 3:
                if (Parametros.averio_turno_3 == true) {
                    System.out.println("⚠️ Ya se aplicaron averías automáticas en el turno 3. Saltando...");
                    Parametros.averio_turno_1 = false;
                    return;
                }
                Parametros.averio_turno_3 = true;
                System.out.println("✅ Avería automática configurada para turno 3");
                break;
            default:
                System.out.println("⚠️ Turno no válido: " + turno + ". No se aplicarán averías automáticas.");
                return;
        }

        // !Sacar la lista de averias automaticas del turno
        if (averiasAutomaticas.isEmpty())
            return;
        List<Averia> averiasAutomaticasTurno = averiasAutomaticas.stream()
                .filter(averia -> averia.getTurnoOcurrencia() == turno)
                .toList();
        if (averiasAutomaticasTurno.isEmpty())
            return;

        System.out.println("🔍 Procesando averías automáticas para turno " + turno);
        System.out.println("📋 Averías automáticas del turno: " + averiasAutomaticasTurno.size());
        averiasAutomaticasTurno.forEach(averia -> System.out.println(
                "   - " + averia.getCamion().getCodigo() + " (" + averia.getTipoIncidente().getCodigo() + ")"));

        List<Camion> camiones_para_averiar_automaticamente = new ArrayList<>();
        for (Gen gen : cromosoma) {
            // !Verifica si el camion cumple con las condiciones para ser averiado
            // automaticamente  
            boolean camion_en_averias_automaticas = averiasAutomaticasTurno.stream()
                    .anyMatch(averia -> averia.getCamion().getCodigo().equals(gen.getCamion().getCodigo()));
            boolean camion_estado_disponible = gen.getCamion().getEstado().equals(EstadoCamion.DISPONIBLE);
            boolean camion_ya_averiado = Parametros.dataLoader.camionesAveriados.stream()
                    .anyMatch(c -> c.getCodigo().equals(gen.getCamion().getCodigo()));

            if (camion_en_averias_automaticas && camion_estado_disponible && !camion_ya_averiado) {
                camiones_para_averiar_automaticamente.add(gen.getCamion());
                System.out.println("✅ Camión " + gen.getCamion().getCodigo() + " seleccionado para avería automática");
            } else {
                if (camion_en_averias_automaticas) {
                    if (!camion_estado_disponible) {
                        System.out.println("⚠️ Camión " + gen.getCamion().getCodigo() + " no está disponible (estado: "
                                + gen.getCamion().getEstado() + ")");
                    }
                    if (camion_ya_averiado) {
                        System.out.println(
                                "⚠️ Camión " + gen.getCamion().getCodigo() + " ya está en la lista de averiados");
                    }
                }
            }
        }
        if (camiones_para_averiar_automaticamente.isEmpty())
            return;

        System.out.println(
                "🚛 Aplicando averías automáticas a " + camiones_para_averiar_automaticamente.size() + " camiones");

        // Verificación adicional: solo procesar camiones que están en la lista de
        // averías automáticas del turno
        List<String> codigosCamionesConfigurados = averiasAutomaticasTurno.stream()
                .map(averia -> averia.getCamion().getCodigo())
                .toList();

        System.out.println("📋 Códigos de camiones configurados para averías automáticas en turno " + turno + ": "
                + codigosCamionesConfigurados);

        for (Gen gen : cromosoma) {
            // Verificación doble: el camión debe estar en la lista de averías automáticas
            // del turno
            if (camiones_para_averiar_automaticamente.stream()
                    .anyMatch(camion -> camion.getCodigo().equals(gen.getCamion().getCodigo())) &&
                    codigosCamionesConfigurados.contains(gen.getCamion().getCodigo())) {

                System.out.println("🔧 Aplicando avería automática al camión " + gen.getCamion().getCodigo());
                gen.colocar_nodo_de_averia_automatica();
            } else if (codigosCamionesConfigurados.contains(gen.getCamion().getCodigo())) {
                System.out.println("⚠️ Camión " + gen.getCamion().getCodigo()
                        + " está configurado pero no cumple condiciones para avería automática");
            }
        }
        Parametros.dataLoader.camionesAveriados.addAll(camiones_para_averiar_automaticamente);
        if (camiones_para_averiar_automaticamente.isEmpty()) {
            System.out.println("No hay camiones para averiar automaticamente");
            return;
        } else {
            System.out.println("Averias automaticas: " + averiasAutomaticasTurno.size());
            System.out
                    .println("Camiones para averiar automaticamente: " + camiones_para_averiar_automaticamente.size());
            System.out.println("Lista de codigos de camiones para averiar automaticamente: ");
            for (Camion camion : camiones_para_averiar_automaticamente) {
                System.out.println(camion.getCodigo());
            }
            System.out.println("--------------------------------");
        }

    }

}
