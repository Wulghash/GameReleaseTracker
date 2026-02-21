# ---- Stage 1: Build frontend ----
FROM node:20-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: Build backend ----
FROM maven:3.9-eclipse-temurin-17 AS backend
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -q
COPY src/ ./src/
# Embed the built frontend as Spring Boot static resources
COPY --from=frontend /app/dist ./src/main/resources/static/
RUN mvn package -DskipTests -q

# ---- Stage 3: Runtime ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar
COPY entrypoint.sh ./
RUN chmod +x entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
