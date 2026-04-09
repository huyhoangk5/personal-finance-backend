# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests -Dproject.build.sourceEncoding=UTF-8

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy file jar từ stage build sang
COPY --from=build /app/target/*.jar app.jar

# Render sẽ dùng biến PORT để lắng nghe, dòng này chỉ mang tính chất tài liệu
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]