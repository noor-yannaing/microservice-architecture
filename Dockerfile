FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
ARG SERVICE
RUN mvn -B -pl ${SERVICE} -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system commerce && useradd --system --gid commerce commerce
ARG SERVICE
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-1.0.0-SNAPSHOT.jar app.jar
USER commerce
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "-jar", "app.jar"]
