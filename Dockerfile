#build the app
FROM maven:3.9.7-sapmachine-22 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests
RUN echo "done"

#run the app
FROM openjdk:22-slim
WORKDIR /app
COPY --from=build /app/target/stayease-app.jar /app/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/stayease-app.jar"]