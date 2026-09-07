FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -pl crypto-exchange -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/crypto-exchange/target/*.jar app.jar
EXPOSE 8400
ENTRYPOINT ["java", "-jar", "app.jar"]
