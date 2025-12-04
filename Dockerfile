# ШАГ 1: Берем готовый образ с Java 17 (как если бы установили Java на компьютер)
FROM eclipse-temurin:17-jdk-alpine as builder

# ШАГ 2: Устанавливаем рабочую папку внутри контейнера
WORKDIR /app

# ШАГ 3: Копируем наши файлы проекта в контейнер
COPY pom.xml .
COPY src ./src

# ШАГ 4: Устанавливаем Maven и собираем проект (как команда mvn package)
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# ШАГ 5: Создаем финальный образ (делаем его легче)
FROM eclipse-temurin:17-jre-alpine

# ШАГ 6: Рабочая папка для запуска
WORKDIR /app

# ШАГ 7: Копируем собранный JAR из предыдущего этапа
COPY --from=builder /app/target/*.jar app.jar

# ШАГ 8: Открываем порт 8080 (как открыть порт в фаерволе)
EXPOSE 8080

# ШАГ 9: Команда которая запустится при старте контейнера
ENTRYPOINT ["java", "-jar", "app.jar"]