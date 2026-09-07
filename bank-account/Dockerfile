FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -pl bank-account -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/bank-account/target/*.jar app.jar
EXPOSE 8200
ENTRYPOINT ["java", "-jar", "app.jar"]
