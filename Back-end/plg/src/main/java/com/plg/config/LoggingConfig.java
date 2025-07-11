package com.plg.config;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;

/**
 * Configuración para redirigir System.out y System.err al sistema de logging
 * para que todos los logs se guarden en el archivo configurado.
 */
@Configuration
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);
    private static final Logger systemOutLogger = LoggerFactory.getLogger("SYSTEM_OUT");
    private static final Logger systemErrLogger = LoggerFactory.getLogger("SYSTEM_ERR");

    @PostConstruct
    public void redirectSystemStreams() {
        logger.info("🔧 Configurando redirección de System.out y System.err al sistema de logging");

        // Guardar las referencias originales
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        // Crear PrintStream personalizado para System.out
        PrintStream logOut = new PrintStream(new OutputStream() {
            private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                if (b == '\n') {
                    String line = buffer.toString().trim();
                    if (!line.isEmpty()) {
                        // Enviar también a la consola original
                        originalOut.println(line);
                        // Enviar al sistema de logging
                        systemOutLogger.info(line);
                    }
                    buffer.reset();
                } else {
                    buffer.write(b);
                }
            }

            @Override
            public void flush() {
                String line = buffer.toString().trim();
                if (!line.isEmpty()) {
                    originalOut.println(line);
                    systemOutLogger.info(line);
                    buffer.reset();
                }
            }
        });

        // Crear PrintStream personalizado para System.err
        PrintStream logErr = new PrintStream(new OutputStream() {
            private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                if (b == '\n') {
                    String line = buffer.toString().trim();
                    if (!line.isEmpty()) {
                        // Enviar también a la consola original
                        originalErr.println(line);
                        // Enviar al sistema de logging
                        systemErrLogger.error(line);
                    }
                    buffer.reset();
                } else {
                    buffer.write(b);
                }
            }

            @Override
            public void flush() {
                String line = buffer.toString().trim();
                if (!line.isEmpty()) {
                    originalErr.println(line);
                    systemErrLogger.error(line);
                    buffer.reset();
                }
            }
        });

        // Redirigir System.out y System.err
        System.setOut(logOut);
        System.setErr(logErr);

        logger.info("✅ Redirección de System.out y System.err configurada exitosamente");

        // Mostrar la ruta completa del archivo de logs
        String logPath = System.getProperty("user.dir") + "/Back-end/logs/application.log";
        logger.info("📄 Todos los logs se guardarán en: " + logPath);

        // Verificar que el directorio existe
        java.io.File logDir = new java.io.File(System.getProperty("user.dir") + "/Back-end/logs");
        if (!logDir.exists()) {
            if (logDir.mkdirs()) {
                logger.info("📁 Directorio de logs creado: " + logDir.getAbsolutePath());
            } else {
                logger.error("❌ No se pudo crear el directorio de logs: " + logDir.getAbsolutePath());
            }
        } else {
            logger.info("📁 Directorio de logs existe: " + logDir.getAbsolutePath());
        }
    }
}