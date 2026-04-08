# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable and download dependencies
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the JAR
COPY src src
# Tạm thời xóa file application.properties (hoặc copy một file rỗng) để tránh lỗi encoding
RUN rm -f src/main/resources/application.properties
# Hoặc tạo file application.properties mới với nội dung đơn giản
RUN echo "spring.application.name=personal-finance-manager" > src/main/resources/application.properties

RUN ./mvnw package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]