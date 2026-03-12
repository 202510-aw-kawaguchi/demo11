FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace/demo

COPY demo/mvnw ./
COPY demo/.mvn .mvn
COPY demo/pom.xml ./

RUN chmod +x ./mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY demo/src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

COPY --from=build /workspace/demo/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-Dserver.port=10000", "-jar", "/app/app.jar"]
