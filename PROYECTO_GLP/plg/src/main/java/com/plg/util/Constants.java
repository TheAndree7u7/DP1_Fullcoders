package com.plg.util;

import java.time.ZoneId;

public final class Constants {
    private Constants() { /* evita instanciación */ }

    /** Zona horaria de la operación de PLG */
    public static final ZoneId ZONE_LIMA = ZoneId.of("America/Lima");
}
