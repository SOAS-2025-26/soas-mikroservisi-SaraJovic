FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -pl users-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/users-service/target/*.jar app.jar
EXPOSE 8770
ENTRYPOINT ["java", "-jar", "app.jar"]
