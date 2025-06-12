# Usa la imagen base de OpenJDK
FROM openjdk:21-jdk-slim

# Copia el .jar al contenedor
COPY target/plg-0.0.1-SNAPSHOT.jar app.jar

# Expón el puerto que usa tu app
EXPOSE 8085

# Comando para ejecutar tu app
ENTRYPOINT ["java", "-jar", "app.jar"]
