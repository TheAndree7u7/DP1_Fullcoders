package com.plg.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.PrintStream;
import java.io.OutputStream;

@Configuration
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @PostConstruct
    public void redirectSystemOut() {
        // Redirigir System.out al logger
        System.setOut(new PrintStream(new OutputStream() {
            private StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                if (b == '\n') {
                    String line = buffer.toString().trim();
                    if (!line.isEmpty()) {
                        logger.info("[STDOUT] " + line);
                    }
                    buffer.setLength(0);
                } else {
                    buffer.append((char) b);
                }
            }

            @Override
            public void flush() {
                String line = buffer.toString().trim();
                if (!line.isEmpty()) {
                    logger.info("[STDOUT] " + line);
                    buffer.setLength(0);
                }
            }
        }, true));

        // Redirigir System.err al logger
        System.setErr(new PrintStream(new OutputStream() {
            private StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                if (b == '\n') {
                    String line = buffer.toString().trim();
                    if (!line.isEmpty()) {
                        logger.error("[STDERR] " + line);
                    }
                    buffer.setLength(0);
                } else {
                    buffer.append((char) b);
                }
            }

            @Override
            public void flush() {
                String line = buffer.toString().trim();
                if (!line.isEmpty()) {
                    logger.error("[STDERR] " + line);
                    buffer.setLength(0);
                }
            }
        }, true));

        logger.info("System.out y System.err redirigidos al sistema de logging");
    }
}