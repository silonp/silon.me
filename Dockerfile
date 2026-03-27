FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mkdir -p personal-app
COPY personal-app/pom.xml ./personal-app/
COPY personal-app/src ./personal-app/src

# Don't use parent pom.xml as it builds both services.
RUN mvn clean package -f personal-app/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/personal-app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xms256m -Xmx256m"

ENTRYPOINT ["java", "-jar", "app.jar"]