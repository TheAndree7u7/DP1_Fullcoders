# Usar una imagen base de OpenJDK 17
FROM openjdk:21-jdk-slim

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR de la aplicación
COPY target/*.jar app.jar

# Crear directorio para logs
RUN mkdir -p /app/logs

# Configurar opciones de JVM para mejor manejo de memoria
ENV JAVA_OPTS="-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"

# Exponer el puerto 8080
EXPOSE 8080

# Comando para ejecutar la aplicación con configuraciones de memoria optimizadas
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
