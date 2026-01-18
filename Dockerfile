FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# сначала зависимости (кэш)
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# потом исходники
COPY src ./src

# ограничим память (VPS 1GB)
ENV MAVEN_OPTS="-Xmx512m"
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
