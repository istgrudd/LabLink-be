# --- TAHAP 1: BUILD (Ganti ke Eclipse Temurin) ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# --- TAHAP 2: RUN (Ganti ke Eclipse Temurin JRE Alpine) ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]