# Usar una imagen base de Eclipse Temurin (OpenJDK oficial)
FROM eclipse-temurin:17-jdk-alpine

# Crear un directorio para la aplicación
WORKDIR /app

# Copiar el archivo JAR de la aplicación
COPY target/tuprivada-app-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto que utiliza la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
