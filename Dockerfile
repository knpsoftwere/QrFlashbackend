FROM eclipse-temurin:21-jdk

LABEL authors="hikinokouji"

WORKDIR /app

COPY build/libs/startUp-plain.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]