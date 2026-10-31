# syntax=docker/dockerfile:1

# ============================================================
# Etapa 1: compilación (JDK 21 + Maven Wrapper)
# ============================================================
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /workspace

# 1) Descargar dependencias en una capa propia: solo se repite si cambia el pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# 2) Compilar el código y generar el JAR ejecutable
COPY src src
RUN ./mvnw -B -q package -DskipTests \
    && cp target/champions-*.jar app.jar

# ============================================================
# Etapa 2: ejecución (solo JRE 21 + el JAR)
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios para ejecutar la aplicación
RUN addgroup -S app && adduser -S app -G app

COPY --from=build --chown=app:app /workspace/app.jar app.jar
USER app

# La JVM usa como máximo el 75 % de la memoria asignada al contenedor
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
