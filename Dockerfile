FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY target/gestion-guias-despacho-0.0.1-SNAPSHOT.jar app.jar

COPY wallet /app/wallet

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]