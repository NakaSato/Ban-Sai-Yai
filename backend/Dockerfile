# Build Stage
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Resolve dependencies to improve cache
RUN ./mvnw dependency:go-offline
COPY src ./src
# Build skipping tests and test compilation
RUN ./mvnw clean package -Dmaven.test.skip=true

# Runtime Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Set timezone
ENV TZ=Asia/Bangkok
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Security: Run as non-root user
RUN useradd -m spring
USER spring

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
