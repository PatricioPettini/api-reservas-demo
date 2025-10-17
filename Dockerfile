# ===== Etapa 1: build (Maven + JDK 17) =====
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos el POM primero para cachear dependencias
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Copiamos el código y construimos
COPY src ./src
RUN mvn -B -DskipTests=false clean package

# ===== Etapa 2: runtime (JRE 17) =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# Render asigna el puerto en la variable de entorno PORT
ENV PORT=8080
EXPOSE 8080

# Copiamos el jar build
COPY --from=build /app/target/*.jar app.jar

# Iniciar la app usando el puerto de Render
CMD ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]

