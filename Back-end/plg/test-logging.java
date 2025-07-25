// Archivo de prueba para verificar logging
// Ejecutar: java test-logging.java

public class TestLogging {
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE LOGGING ===");
        System.out.println("Este es un mensaje de prueba desde System.out.println");
        System.out.println("Fecha y hora: " + java.time.LocalDateTime.now());
        
        System.err.println("Este es un mensaje de error desde System.err.println");
        
        for (int i = 1; i <= 5; i++) {
            System.out.println("Mensaje de prueba #" + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("=== FIN DE PRUEBA ===");
    }
} 