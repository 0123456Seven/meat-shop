spring:
  application:
    name: meat

  datasource:
    # Берем настройки из переменных окружения или используем значения по умолчанию
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/meat_market_db}
    username: ${SPRING_DATASOURCE_USERNAME:meat_admin}
    password: ${SPRING_DATASOURCE_PASSWORD:Xaero891500}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      # Обновляем схему БД при изменениях (create - пересоздает каждый раз)
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        default_schema: meat_shop
        format_sql: true    # Красиво форматирует SQL в логах
        show_sql: true      # Показывает SQL запросы в консоли
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
    try-it-out-enabled: true
    filter: true
  show-actuator: false

server:
  port: 8080

logging:
  level:
    org.hibernate: INFO
    org.springframework: INFO
    ru.xaero.meat: DEBUG
  file:
    name: /app/logs/application.log  # Логи будут сохраняться в файл