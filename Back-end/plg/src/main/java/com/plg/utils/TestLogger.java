package com.plg.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(TestLogger.class);
    
    public static void testLogging() {
        logger.info("=== PRUEBA DE LOGGING ===");
        logger.info("Este es un mensaje de INFO");
        logger.warn("Este es un mensaje de WARN");
        logger.error("Este es un mensaje de ERROR");
        logger.info("=== FIN PRUEBA ===");
    }

    public static void main(String[] args) {
        testLogging();
    }
} 