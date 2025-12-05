# Simple runtime stage - using pre-built JAR
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the backend JAR (built locally with ./mvnw clean package -DskipTests)
COPY target/*.jar app.jar

# Set timezone to Bangkok
ENV TZ=Asia/Bangkok
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Expose port
EXPOSE 9090

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
