FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd -r spring \
    && useradd -r -g spring -s /usr/sbin/nologin spring

COPY target/*.jar app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]