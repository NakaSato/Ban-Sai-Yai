# Frontend build stage
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
COPY frontend/ ./
RUN npm install --legacy-peer-deps
RUN npm run build

# Backend build stage
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Final stage - combine frontend and backend
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the backend JAR
COPY --from=backend-build /app/target/*.jar app.jar

# Copy the frontend build to serve static files
COPY --from=frontend-build /app/frontend/dist /app/static

# Set timezone to Bangkok
ENV TZ=Asia/Bangkok
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Expose port
EXPOSE 6060

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
