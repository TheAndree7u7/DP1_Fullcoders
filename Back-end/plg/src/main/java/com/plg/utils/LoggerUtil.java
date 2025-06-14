package com.plg.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggerUtil {

    private static boolean logsOn;
    private static final String LOG_FILE = "algo.log";

    // Códigos ANSI para colores en consola
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_CYAN = "\u001B[36m";

    @Value("${ON_LOGS_ON:true}")
    public void setLogsOn(boolean value) {
        LoggerUtil.logsOn = value;
    }

    public static void log(String msg) {
        if (logsOn) {
            String logMsg = LocalDateTime.now() + " | INFO | " + msg;
            System.out.println(ANSI_GREEN + logMsg + ANSI_RESET);
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                fw.write(logMsg + System.lineSeparator());
            } catch (IOException e) {
                System.err.println("[LoggerUtil] Error escribiendo en algo.log: " + e.getMessage());
            }
        }
    }

    public static void logError(String msg) {
        if (logsOn) {
            String logMsg = LocalDateTime.now() + " | ERROR | " + msg;
            System.err.println(ANSI_RED + logMsg + ANSI_RESET);
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                fw.write(logMsg + System.lineSeparator());
            } catch (IOException e) {
                System.err.println("[LoggerUtil] Error escribiendo en algo.log: " + e.getMessage());
            }
        }
    }

    public static void logWarning(String msg) {
        if (logsOn) {
            String logMsg = LocalDateTime.now() + " | WARNING | " + msg;
            System.out.println(ANSI_YELLOW + logMsg + ANSI_RESET);
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                fw.write(logMsg + System.lineSeparator());
            } catch (IOException e) {
                System.err.println("[LoggerUtil] Error escribiendo en algo.log: " + e.getMessage());
            }
        }
    }

    public static void logAlways(String msg) {
        String logMsg = LocalDateTime.now() + " | ALWAYS | " + msg;
        System.out.println(ANSI_CYAN + logMsg + ANSI_RESET);
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(logMsg + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("[LoggerUtil] Error escribiendo en algo.log: " + e.getMessage());
        }
    }

    /**
     * Escribe un log en un archivo específico basado en el nombre del servicio
     *
     * @param servicio Nombre del servicio (se usará para el nombre del archivo:
     * [servicio].log)
     * @param mensaje Mensaje a registrar
     * @param nivel Nivel de log (INFO, ERROR, WARNING)
     */
    public static void logToService(String servicio, String mensaje, String nivel) {
        String nombreArchivo = servicio + ".log";
        String logMsg = LocalDateTime.now() + " | " + nivel + " | " + mensaje;
        try (FileWriter fw = new FileWriter(nombreArchivo, true)) {
            fw.write(logMsg + System.lineSeparator());
        } catch (IOException e) {
            // Error silencioso - no queremos logs en consola
        }
    }

    // Métodos de conveniencia para diferentes servicios
    public static void logAlgoritmoGenetico(String msg) {
        logToService("AlgoritmoGenetico", msg, "INFO");
    }

    public static void logAlgoritmoGeneticoError(String msg) {
        logToService("AlgoritmoGenetico", msg, "ERROR");
    }

    public static void logSimulacion(String msg) {
        logToService("Simulacion", msg, "INFO");
    }

    public static void logSimulacionError(String msg) {
        logToService("Simulacion", msg, "ERROR");
    }

    // Método genérico - permite crear logs para cualquier servicio sin modificar esta clase
    public static void logServicio(String nombreServicio, String msg, String nivel) {
        logToService(nombreServicio, msg, nivel);
    }
}
