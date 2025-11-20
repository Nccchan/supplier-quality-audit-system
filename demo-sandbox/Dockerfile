FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/sqas-demo.jar .

# Create output directory
RUN mkdir -p /app/demo-output

# Run application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "sqas-demo.jar"]
