FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN bash mvnw -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/dreadlocks-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
